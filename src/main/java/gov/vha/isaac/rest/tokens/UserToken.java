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

import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.vha.isaac.ochre.api.externalizable.ByteArrayDataBuffer;
import gov.vha.isaac.ochre.api.util.PasswordHasher;

/**
 * 
 * {@link UserToken}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UserToken
{
	private static final byte tokenVersion = 1;
	private static final int hashRounds = 2048;
	private static final int hashLength = 64;
	private static final int encodedHashLength = (int)Math.ceil(hashLength / 8f / 3f) * 4;  //http://stackoverflow.com/a/4715480
	
	private static final Logger log = LoggerFactory.getLogger(UserToken.class);
	
	private static transient byte[] secret_;
	private static transient AtomicInteger increment = new AtomicInteger();  //Used for CSRF protection
	private transient boolean validForSubmit = false;
	
	private static HashMap<Integer, Long> validTokens = new HashMap<>();
	
	private int userIdentity;
	private long creationTime;
	
	/**
	 * Create a new user token, valid for a short period of time.
	 * @param authenticatedUserIdentity
	 */
	public UserToken(int authenticatedUserIdentity)
	{
		userIdentity = authenticatedUserIdentity;
		creationTime = System.currentTimeMillis();
	}
	
	/**
	 * Parse a user token back
	 * @param encodedData
	 * @throws Exception
	 */
	public UserToken(String encodedData) throws Exception
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
		userIdentity = buffer.getInt();
		creationTime = buffer.getLong();

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
	
	public boolean isValidForSubmit()
	{
		return validForSubmit;
	}
	
	private byte[] getBytesToWrite()
	{
		ByteArrayDataBuffer buffer = new ByteArrayDataBuffer();
		int thisIncrement = increment.getAndIncrement();
		validTokens.put(thisIncrement, System.currentTimeMillis());
		buffer.putByte(tokenVersion);
		buffer.putInt(thisIncrement);
		buffer.putInt(userIdentity);
		buffer.putLong(creationTime);
		buffer.trimToSize();
		return buffer.getData();
	}
	
	private byte[] getSecret()
	{
		if (secret_ == null)
		{
			synchronized (UserToken.class)
			{
				if (secret_ == null)
				{
					byte[] temp = new byte[20];
					
					//Don't use secureRandom here, it hangs on linux, and we don't need that level of security.
					new Random().nextBytes(temp);
					//SecureRandom.getInstanceStrong().nextBytes(temp);  //TODO determine if we need a better fix for this one.
					secret_ = temp;
				}
			}
		}
		return secret_;
	}
	
	
	
	public int getUserIdentity()
	{
		return userIdentity;
	}

	public static void main(String[] args) throws Exception
	{
		UserToken t = new UserToken(5678);
		String token = t.serialize();
		System.out.println(token);
		UserToken t1 = new UserToken(token);
		System.out.println(t1.validForSubmit);
		
		String token1 = t1.serialize();
		System.out.println(token1);
		Thread.sleep(25000);
		System.out.println(new UserToken(token1).validForSubmit);
	}
}
