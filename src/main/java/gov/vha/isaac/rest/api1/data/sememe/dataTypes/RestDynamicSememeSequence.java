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
package gov.vha.isaac.rest.api1.data.sememe.dataTypes;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeTypedData;

/**
 * 
 * {@link RestDynamicSememeSequence}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestDynamicSememeSequence extends RestDynamicSememeTypedData
{
	public RestDynamicSememeSequence(int columnNumber, int value)
	{
		super(columnNumber, value, ObjectChronologyType.UNKNOWN_NID);
		if (Get.conceptService().hasConcept(value))
		{
			if (Get.sememeService().hasSememe(value))
			{
				//leave unknown
			}
			else
			{
				setTypedData(ObjectChronologyType.CONCEPT);
			}
		}
		else if (Get.sememeService().hasSememe(value))
		{
			setTypedData(ObjectChronologyType.SEMEME);
		}
	}
	
	protected RestDynamicSememeSequence()
	{
		//for jaxb
	}

	public int getSequence()
	{
		//The rest parser sometimes deserializes to broader types
		if (!(data instanceof Integer))
		{
			if (data instanceof String)
			{
				return Integer.parseInt((String)data);
			}
			else if (data instanceof Number)
			{
				return ((Number)data).intValue();
			}
			else
			{
				throw new RuntimeException("Unexpected data type: " + data.getClass());
			}
		}
		return (int)data;
	}
}
