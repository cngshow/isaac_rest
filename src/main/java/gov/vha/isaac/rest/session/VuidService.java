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

package gov.vha.isaac.rest.session;

import java.util.Optional;

import org.jvnet.hk2.annotations.Contract;

import gov.vha.isaac.rest.api.data.vuid.RestVuidBlockData;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link VuidService}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Contract
public interface VuidService {
	/**
	 * Validates a VUID. If a VUID is less than the next vuid
	 * stored in the database, then it is considered 'valid'
	 * (as a number, no check if it's been used).
	 * 
	 * So we don't forget why this is:
	 * 
	 	15:47 Neill Robins
		Are the VUIDs going to come in negative values in test cases?
		15:49 Dan Armbrust
		bit of an open question how much we should validate when in test mode with negative vuids.
		it may be, that we don't do this step when in test mode.
		because the test would be pointless, if we aren't the authority server.
		but when we are, we would do the test.
		15:51 Neill Robins
		I'm curious how testers will test this part
		Or will they?
		15:51 Dan Armbrust
		they can put it in 'real'mode
		though they know it isn't in real mode
	 *
	 * @param vuidToValidate The VUID that should be validated
	 * @return true, if the VUID is valid, false if it's certain conditions aren't met, an excpetion is thrown for a bad VUID
	 */
	public boolean isVuidValid(long vuidToValidate) throws RestException;

	/**
	 * Allocate and return a block of VUIDs
	 *
	 * @param blockSize
	 *            the size of the block of VUIDs as a positive, non zero integer
	 * @param reason
	 *            the reason for the allocation request as text
	 * @param ssoToken
	 *            the user's SSO token string
	 * @return the RestVuidBlockData
	 * 
	 * This implementation gets a RestVuidBlockData object from VUID-rest IFF vuid.properties is in classpath
	 * and contains a value for property "vuid_allocate_url"
	 */
	Optional<RestVuidBlockData> allocate(int blockSize, String reason, String ssoToken) throws RestException;
	
	Optional<String> getVuidValidateServiceUrl();

	Optional<String> getVuidAllocateServiceUrl();
}