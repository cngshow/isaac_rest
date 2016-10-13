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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.util.PasswordHasher;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;

/**
 * 
 * {@link EditToken}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class EditToken
{
	private static final byte tokenVersion = 1;
	private static final int hashRounds = 2048;
	private static final int hashLength = 64;
	private static final int encodedHashLength = (int)Math.ceil(hashLength / 8f / 3f) * 4;  //http://stackoverflow.com/a/4715480
	
	private static final Logger log = LoggerFactory.getLogger(EditToken.class);
	
	private static volatile transient byte[] secret_;
	private static transient AtomicInteger increment = new AtomicInteger();  //Used for CSRF protection
	private transient boolean validForSubmit = false;
	
	private static HashMap<Integer, Long> validTokens = new HashMap<>();

	// Generate UUID
	// Using the text 'NO_ACTIVE_WORKFLOW'
	// and the domain '5a2e7786-3e41-11dc-8314-0800200c9a66' (path ID from FSN description)
	private static final UUID NULL_UUID = UUID.fromString("a051e620-4fe1-5174-97d9-53dbce2ead0d");

	private long creationTime;

	private final int authorSequence;
	private final int moduleSequence;
	private final int pathSequence;
	private final UUID workflowProcessId;
	private final Set<UserRole> roles = new TreeSet<>();
	
	private transient EditCoordinate editCoordinate = null;
	
	private final transient String serialization;
	
	/**
	 * Create a new edit token, valid for a short period of time.
	 * 
	 * @param authorSequence
	 * @param moduleSequence
	 * @param pathSequence
	 * @param workflowProcessId
	 */
	public EditToken(
			int authorSequence,
			int moduleSequence,
			int pathSequence,
			UUID workflowProcessId,
			UserRole...roles)
	{
		this.creationTime = System.currentTimeMillis();

		this.authorSequence = authorSequence;
		this.moduleSequence = moduleSequence;
		this.pathSequence = pathSequence;
		this.workflowProcessId = workflowProcessId;
		
		if (roles != null && roles.length > 0) {
			for (UserRole role : roles) {
				this.roles.add(role);
			}
		}

		serialization = serialize();
	}
	public EditToken(
			int authorSequence,
			int moduleSequence,
			int pathSequence,
			UUID workflowProcessId,
			Collection<UserRole> roles)
	{
		this(authorSequence, moduleSequence, pathSequence, workflowProcessId, roles != null ? roles.toArray(new UserRole[roles.size()]) : null);
	}

	/**
	 * Parse an edit token back
	 * @param encodedData
	 * @throws Exception
	 */
	public EditToken(String encodedData) throws Exception
	{
		long time = System.currentTimeMillis();
		String readHash = encodedData.substring(0, encodedHashLength);
		String calculatedHash = PasswordHasher.hash(encodedData.substring(encodedHashLength, encodedData.length()), getSecret(), hashRounds, hashLength);
		
		if (!readHash.equals(calculatedHash))
		{
			throw new RuntimeException("Invalid token!");
		}
		
		byte[] readBytes = Base64.getUrlDecoder().decode(encodedData.substring(encodedHashLength, encodedData.length()));
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer(readBytes);
		byte version = buffer.getByte();
		if (version != tokenVersion)
		{
			throw new Exception("Expected token version " + tokenVersion + " but read " + version);
		}
		
		int increment = buffer.getInt();
		creationTime = buffer.getLong();

		authorSequence = buffer.getInt();
		moduleSequence = buffer.getInt();
		pathSequence = buffer.getInt();
		
		UUID tmpUuid = buffer.getUuid();
		if (tmpUuid.equals(NULL_UUID)) {
			workflowProcessId = null;
		} else {
			workflowProcessId = tmpUuid;
		}

		byte numRoles = buffer.getByte();
		for (byte i = 0; i < numRoles; ++i) {
			roles.add(UserRole.safeValueOf(buffer.getInt()).get());
		}
		
		Long temp = validTokens.remove(increment);
		if (temp == null || (System.currentTimeMillis() - temp) > 20000)
		{
			validForSubmit = false;
		}
		else
		{
			validForSubmit = true;
		}
		
		expireUnusedTokens();

		log.debug("token decode time " + (System.currentTimeMillis() - time) + "ms");

		serialization = serialize();
	}
	
	private void expireUnusedTokens()
	{
		Iterator<Entry<Integer, Long>> x = validTokens.entrySet().iterator();
		while (x.hasNext())
		{
			if ((System.currentTimeMillis() -  x.next().getValue()) > 20000)
			{
				x.remove();
			}
		}
	}
	
	public String serialize()
	{
		if ((System.currentTimeMillis() - creationTime) > (1000 * 60 * 15))
		{
			throw new RuntimeException("Token Expired");
		}
		creationTime = System.currentTimeMillis();
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
	 * @return the workflowProcessId
	 */
	public UUID getWorkflowProcessId() {
		return workflowProcessId;
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

	public boolean isValidForSubmit()
	{
		return validForSubmit;
	}
	
	public void setInvalidForSubmit() {
		validForSubmit = false;
	}
	
	private byte[] getBytesToWrite()
	{
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer();
		int thisIncrement = increment.getAndIncrement();
		validTokens.put(thisIncrement, System.currentTimeMillis());
		buffer.putByte(tokenVersion);
		buffer.putInt(thisIncrement);
		buffer.putLong(creationTime);
		buffer.putInt(authorSequence);
		buffer.putInt(moduleSequence);
		buffer.putInt(pathSequence);
		
		if (workflowProcessId == null) {
			buffer.putUuid(NULL_UUID);
		} else {
			buffer.putUuid(workflowProcessId);
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
		return "EditToken [validForSubmit=" + validForSubmit + ", creationTime=" + creationTime + ", authorSequence="
				+ authorSequence + ", moduleSequence=" + moduleSequence + ", pathSequence=" + pathSequence
				+ ", workflowProcessId=" + workflowProcessId + ", roles=" + roles + ", serialization=" + serialization
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
				UserRole.ADMINISTRATOR, UserRole.EDITOR, UserRole.READ_ONLY);
		String token = t.serialize();
		System.out.println(token);
		EditToken t1 = new EditToken(token);
		System.out.println(t1.validForSubmit);
		
		String token1 = t1.serialize();
		System.out.println(token1);
		Thread.sleep(25000);
		System.out.println(new EditToken(token1).validForSubmit);
	}
}
