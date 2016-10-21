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

package gov.vha.isaac.rest.api1.data.enumerations;

import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.util.NumericUtils;

/**
 * 
 * {@link RestStampPrecedenceType}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public class RestStampPrecedenceType extends Enumeration
{
	protected RestStampPrecedenceType()
	{
		//for jaxb
	}
	
	public RestStampPrecedenceType(StampPrecedence st)
	{
		super(st.name(), st.toString(), st.ordinal());
	}

	public static RestStampPrecedenceType valueOf(String str) {
		for (StampPrecedence spValue : StampPrecedence.values()) {
			if (spValue.name().equals(str.trim())
					|| spValue.toString().equals(str.trim())) {
				return new RestStampPrecedenceType(spValue);
			} else {
				Optional<Integer> intOptional = NumericUtils.getInt(str.trim());
				if (intOptional.isPresent() && intOptional.get() == spValue.ordinal()) {
					return new RestStampPrecedenceType(spValue);
				}
			}
		}
		throw new IllegalArgumentException("invalid RestStampPrecedenceType value \"" + str + "\"");
	}

	public static RestStampPrecedenceType[] getAll()
	{
		RestStampPrecedenceType[] result = new RestStampPrecedenceType[StampPrecedence.values().length];
		for (int i = 0; i < StampPrecedence.values().length; i++)
		{
			result[i] = new RestStampPrecedenceType(StampPrecedence.values()[i]);
		}
		return result;
	}
}