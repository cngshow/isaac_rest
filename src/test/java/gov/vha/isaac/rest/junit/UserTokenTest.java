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
package gov.vha.isaac.rest.junit;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import gov.vha.isaac.rest.tokens.UserToken;

/**
 * {@link UserTokenTest}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class UserTokenTest
{
	@Test
	public void testTokenRoundTrip() throws Exception
	{
		UUID randomUuid = UUID.randomUUID();
		
		UserToken ut = new UserToken(5555, 1, 2, 3, randomUuid);
		String token = ut.serialize();
		
		UserToken read = new UserToken(token);
		Assert.assertTrue(ut.getUserIdentity() + " does not equal " + read.getUserIdentity() , ut.getUserIdentity() == read.getUserIdentity());
		Assert.assertTrue("is not valid?", read.isValidForSubmit());
		
		//Can only use a token once for submit
		Assert.assertFalse("is valid when it shouldn't be", new UserToken(token).isValidForSubmit());
		
	}
}
