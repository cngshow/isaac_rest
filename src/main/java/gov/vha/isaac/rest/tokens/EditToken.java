/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.tokens;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.util.PasswordHasher;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link EditToken}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class EditToken
{
	private static final Logger log = LoggerFactory.getLogger(EditToken.class);
	
	private static final byte tokenVersion = 1;
	private static final int hashRounds = 2048;
	private static final int hashLength = 64;
	private static final int encodedHashLength = (int)Math.ceil(hashLength / 8f / 3f) * 4;  //http://stackoverflow.com/a/4715480
	
	
	private static volatile transient byte[] secret_;
	private static final AtomicInteger globalIncrement = new AtomicInteger();  //Used for CSRF protection
	
	private static final ConcurrentHashMap<Integer, Long> validTokens = new ConcurrentHashMap<>();
	private static final long tokenMaxAge = 1000l * 60l * 60l;  //one hour
	 

	// Generate UUID
	// Using the text 'NO_ACTIVE_WORKFLOW'
	// and the domain '5a2e7786-3e41-11dc-8314-0800200c9a66' (path ID from FSN description)
	private static final UUID NULL_UUID = UUID.fromString("a051e620-4fe1-5174-97d9-53dbce2ead0d");

	private long creationTime;
	private int increment;
	private final int authorSequence;
	private final int moduleSequence;
	private final int pathSequence;
	private final UUID activeWorkflowProcessId;
	private final Set<UserRole> roles = new TreeSet<>();
	
	private transient EditCoordinate editCoordinate = null;
	private transient String serialization;
	
	/**
	 * Create a new edit token, valid for a short period of time.
	 * 
	 * @param authorSequence
	 * @param moduleSequence
	 * @param pathSequence
	 * @param activeWorkflowProcessId
	 */
	EditToken(
			int authorSequence,
			int moduleSequence,
			int pathSequence,
			UUID activeWorkflowProcessId,
			UserRole...roles)
	{
		this.authorSequence = authorSequence;
		this.moduleSequence = moduleSequence;
		this.pathSequence = pathSequence;
		this.activeWorkflowProcessId = activeWorkflowProcessId;
		
		if (roles != null && roles.length > 0) {
			for (UserRole role : roles) {
				this.roles.add(role);
			}
		}

		creationTime = System.currentTimeMillis();
		increment = globalIncrement.getAndIncrement();
		validTokens.put(increment, System.currentTimeMillis());
		serialization = serialize();
	}
	EditToken(
			int authorSequence,
			int moduleSequence,
			int pathSequence,
			UUID activeWorkflowProcessId,
			Collection<UserRole> roles)
	{
		this(authorSequence, moduleSequence, pathSequence, activeWorkflowProcessId, roles != null ? roles.toArray(new UserRole[roles.size()]) : null);
	}

	/**
	 * Parse an edit token back
	 * @param encodedData
	 * @throws Exception
	 */
	EditToken(String encodedData) throws Exception
	{
		try {
			long time = System.currentTimeMillis();
			String readHash = encodedData.substring(0, encodedHashLength);
			String calculatedHash = PasswordHasher.hash(encodedData.substring(encodedHashLength, encodedData.length()), getSecret(), hashRounds, hashLength);

			if (!readHash.equals(calculatedHash))
			{
				throw new SecurityException("Invalid token!");
			}

			byte[] readBytes = Base64.getUrlDecoder().decode(encodedData.substring(encodedHashLength, encodedData.length()));
			ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(readBytes);
			byte version = buffer.getByte();
			if (version != tokenVersion)
			{
				throw new Exception("Expected token version " + tokenVersion + " but read " + version);
			}

			increment = buffer.getInt();
			creationTime = buffer.getLong();

			if ((System.currentTimeMillis() - creationTime) > tokenMaxAge)
			{
				throw new RestException("Edit Token Expired");
			}

			authorSequence = buffer.getInt();
			moduleSequence = buffer.getInt();
			pathSequence = buffer.getInt();

			UUID tmpUuid = buffer.getUuid();
			if (tmpUuid.equals(NULL_UUID)) {
				activeWorkflowProcessId = null;
			} else {
				activeWorkflowProcessId = tmpUuid;
			}

			byte numRoles = buffer.getByte();
			for (byte i = 0; i < numRoles; ++i) {
				roles.add(UserRole.safeValueOf(buffer.getInt()).get());
			}

			log.debug("token decode time " + (System.currentTimeMillis() - time) + "ms");

			serialization = encodedData;
		} catch (Exception e) {
			log.warn("Failed creating EditToken from \"" + encodedData + "\"", e);
			throw e;
		}
	}
	
	private void expireUnusedTokens()
	{
		try
		{
			log.info("Expiring unused tokens - size before: " + validTokens.size());
			Iterator<Entry<Integer, Long>> x = validTokens.entrySet().iterator();
			while (x.hasNext())
			{
				if ((System.currentTimeMillis() -  x.next().getValue()) > tokenMaxAge)
				{
					x.remove();
				}
			}
			log.info("Finished expiring unused tokens - size after: " + validTokens.size());
		}
		catch (Exception e)
		{
			log.error("Unexpected error expiring unused tokens", e);
		}
	}
	
	private String serialize()
	{
		try
		{
			String data = Base64.getUrlEncoder().encodeToString(getBytesToWrite());
			return PasswordHasher.hash(data, getSecret(), hashRounds, hashLength) + data;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * @return the authorSequence
	 */
	public int getAuthorSequence() {
		return authorSequence;
	}

	/**
	 * @return the moduleSequence
	 */
	public int getModuleSequence() {
		return moduleSequence;
	}

	/**
	 * @return the pathSequence
	 */
	public int getPathSequence() {
		return pathSequence;
	}

	/**
	 * @return the active workflowProcess UUID id
	 */
	public UUID getActiveWorkflowProcessId() {
		return activeWorkflowProcessId;
	}

	/**
	 * @return the cached EditCoordinate
	 */
	public EditCoordinate getEditCoordinate() {
		if (editCoordinate == null) {
			editCoordinate = new EditCoordinateImpl(
					authorSequence,
					moduleSequence,
					pathSequence);
		}
		
		return editCoordinate;
	}

	/**
	 * @return the sorted set of roles
	 */
	public Set<UserRole> getRoles() {
		return Collections.unmodifiableSet(roles);
	}

	public String getSerialized()
	{
		return serialization;
	}

	/**
	 * Note that one can only ask if a token is valid for write once.  This should only be called by the filter code, when the token is first read.
	 * all subsequent calls will return false, until the token is renewed
	 * @return
	 */
	public boolean isValidForWrite()
	{
		return validTokens.remove(increment) == null ? false : true;
	}
	
	private byte[] getBytesToWrite()
	{
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer();
		buffer.putByte(tokenVersion);
		buffer.putInt(increment);
		buffer.putLong(creationTime);
		buffer.putInt(authorSequence);
		buffer.putInt(moduleSequence);
		buffer.putInt(pathSequence);
		
		if (activeWorkflowProcessId == null) {
			buffer.putUuid(NULL_UUID);
		} else {
			buffer.putUuid(activeWorkflowProcessId);
		}
		
		buffer.putByte((byte)roles.size());
		for (UserRole role : roles) {
			buffer.putInt(role.ordinal());
		}

		buffer.trimToSize();
		return buffer.getData();
	}
	
	private byte[] getSecret()
	{
		if (secret_ == null)
		{
			synchronized (EditToken.class)
			{
				if (secret_ == null)
				{
					byte[] temp = new byte[20];
					
					//Don't use secureRandom here, it hangs on linux, and we don't need that level of security.
					//new Random().nextBytes(temp);
					//SecureRandom.getInstanceStrong().nextBytes(temp);  //TODO determine if we need a better fix for this one.
					new SecureRandom().nextBytes(temp);
					secret_ = temp;
					
					Get.workExecutors().getScheduledThreadPoolExecutor().scheduleAtFixedRate(() -> {expireUnusedTokens();}, 15, 15, TimeUnit.MINUTES);
				}
			}
		}
		return secret_;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EditToken [increment=" + increment + ", creationTime=" + creationTime + ", authorSequence="
				+ authorSequence + ", moduleSequence=" + moduleSequence + ", pathSequence=" + pathSequence
				+ ", activeWorkflowProcessId=" + activeWorkflowProcessId + ", roles=" + roles + ", serialization=" + serialization
				+ "]";
	}

	public static void main(String[] args) throws Exception
	{
		UUID randomUuid = UUID.randomUUID();
		
		EditToken t = new EditToken(
				//5678,
				1,
				2,
				3,
				randomUuid,
				UserRole.SUPER_USER, UserRole.EDITOR, UserRole.READ_ONLY);
		String token = t.serialize();
		System.out.println(token);
		EditToken t1 = new EditToken(token);
		System.out.println(t1.increment);
		
		String token1 = t1.serialize();
		System.out.println(token1);
	}
}
