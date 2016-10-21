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

import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;

/**
 * 
 * {@link RestDynamicSememeValidatorType}
 *
 * A class that maps ISAAC {@link DynamicSememeValidatorType} values to REST
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestDynamicSememeValidatorType extends Enumeration
{
	protected RestDynamicSememeValidatorType()
	{
		//for jaxb
	}
	
	public RestDynamicSememeValidatorType(DynamicSememeValidatorType vt)
	{
		super(vt.name(), vt.getDisplayName(), vt.ordinal());
	}
	
	public static RestDynamicSememeValidatorType[] getAll()
	{
		RestDynamicSememeValidatorType[] result = new RestDynamicSememeValidatorType[DynamicSememeValidatorType.values().length];
		for (int i = 0; i < DynamicSememeValidatorType.values().length; i++)
		{
			result[i] = new RestDynamicSememeValidatorType(DynamicSememeValidatorType.values()[i]);
		}
		return result;
	}
	
	public DynamicSememeValidatorType translate()
	{
		return DynamicSememeValidatorType.values()[this.enumId];
	}
	
	public static DynamicSememeValidatorType[] translate(RestDynamicSememeValidatorType[] values)
	{
		if (values == null)
		{
			return null;
		}
		DynamicSememeValidatorType[] result = new DynamicSememeValidatorType[values.length];
		for (int i = 0; i < values.length; i++)
		{
			result[i] = values[i].translate();
		}
		return result;
	}
}
