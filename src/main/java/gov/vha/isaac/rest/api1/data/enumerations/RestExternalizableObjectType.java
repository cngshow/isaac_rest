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

import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.util.NumericUtils;

/**
 * {@link RestExternalizableObjectType}
 * A class that maps ISAAC {@link OchreExternalizableObjectType} values to REST.
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
public class RestExternalizableObjectType extends Enumeration
{
	protected RestExternalizableObjectType()
	{
		//for jaxb
	}
	
	public RestExternalizableObjectType(OchreExternalizableObjectType dt)
	{
		super(dt.name(), null, dt.ordinal());
	}
	
	public static RestExternalizableObjectType[] getAll()
	{
		RestExternalizableObjectType[] result = new RestExternalizableObjectType[OchreExternalizableObjectType.values().length];
		for (int i = 0; i < OchreExternalizableObjectType.values().length; i++)
		{
			result[i] = new RestExternalizableObjectType(OchreExternalizableObjectType.values()[i]);
		}
		return result;
	}

	public static RestExternalizableObjectType valueOf(String str) {
		for (OchreExternalizableObjectType enumValue : OchreExternalizableObjectType.values()) {
			if (enumValue.name().equals(str.trim())) {
				return new RestExternalizableObjectType(enumValue);
			} else {
				Optional<Integer> intOptional = NumericUtils.getInt(str.trim());
				if (intOptional.isPresent() && intOptional.get() == enumValue.ordinal()) {
					return new RestExternalizableObjectType(enumValue);
				}
			}
		}
		throw new IllegalArgumentException("invalid RestExternalizableObjectType value \"" + str + "\"");
	}
}
