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

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.util.NumericUtils;

/**
 * 
 * {@link RestStateType}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public class RestStateType extends Enumeration
{
	protected RestStateType()
	{
		//for jaxb
	}
	
	public RestStateType(State st)
	{
		super(st.toString(), st.ordinal());
	}
	
	public static RestStateType valueOf(String str) {
		for (State spValue : State.values()) {
			if (spValue.name().equals(str.trim())
					|| spValue.toString().equals(str.trim())
					|| spValue.getAbbreviation().equals(str.trim())) {
				return new RestStateType(spValue);
			} else {
				Optional<Integer> intOptional = NumericUtils.getInt(str.trim());
				if (intOptional.isPresent() && intOptional.get() == spValue.ordinal()) {
					return new RestStateType(spValue);
				}
			}
		}
		throw new IllegalArgumentException("invalid RestStateType value \"" + str + "\"");
	}
	
	public static RestStateType[] getAll()
	{
		RestStateType[] result = new RestStateType[State.values().length];
		for (int i = 0; i < State.values().length; i++)
		{
			result[i] = new RestStateType(State.values()[i]);
		}
		return result;
	}
}