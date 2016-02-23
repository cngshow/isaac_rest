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
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;

/**
 * 
 * {@link RestObjectChronologyType}
 * 
 * A class that maps ISAAC {@link ObjectChronologyType} objects to REST
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@XmlRootElement
public class RestObjectChronologyType extends Enumeration
{
	protected RestObjectChronologyType()
	{
		//for jaxb
	}
	
	public RestObjectChronologyType(ObjectChronologyType oct)
	{
		super(oct.toString(), oct.ordinal());
	}
	
	public static RestObjectChronologyType[] getAll()
	{
		RestObjectChronologyType[] result = new RestObjectChronologyType[ObjectChronologyType.values().length];
		for (int i = 0; i < ObjectChronologyType.values().length; i++)
		{
			result[i] = new RestObjectChronologyType(ObjectChronologyType.values()[i]);
		}
		return result;
	}
}
