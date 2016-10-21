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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeBoolean;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeByteArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeDouble;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeFloat;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;

/**
 * {@link RestDynamicSememeDataType}
 * A class that maps ISAAC {@link DynamicSememeDataType} values to REST.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public class RestDynamicSememeDataType extends Enumeration
{
	
	/**
	 * The full value of the "@class" annotation that needs to be passed back in when constructing a RestDynamicSememeData like
	 * RestDynamicSememeDouble.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String classType;
	
	protected RestDynamicSememeDataType()
	{
		//for jaxb
	}
	
	public RestDynamicSememeDataType(DynamicSememeDataType dt)
	{
		super(dt.name(), dt.getDisplayName(), dt.ordinal());
		switch (dt)
		{
			case ARRAY:
				classType = RestDynamicSememeArray.class.getName();
				break;
			case BOOLEAN:
				classType = RestDynamicSememeBoolean.class.getName();
				break;
			case BYTEARRAY:
				classType = RestDynamicSememeByteArray.class.getName();
				break;
			case DOUBLE:
				classType = RestDynamicSememeDouble.class.getName();
				break;
			case FLOAT:
				classType = RestDynamicSememeFloat.class.getName();
				break;
			case INTEGER:
				classType = RestDynamicSememeInteger.class.getName();
				break;
			case LONG:
				classType = RestDynamicSememeLong.class.getName();
				break;
			case NID:
				classType = RestDynamicSememeNid.class.getName();
				break;
			case SEQUENCE:
				classType = RestDynamicSememeSequence.class.getName();
				break;
			case STRING:
				classType = RestDynamicSememeString.class.getName();
				break;
			case UUID:
				classType = RestDynamicSememeUUID.class.getName();
				break;
			case UNKNOWN:
			case POLYMORPHIC:
			default :
				break;
		}
	}
	
	public static RestDynamicSememeDataType[] getAll()
	{
		RestDynamicSememeDataType[] result = new RestDynamicSememeDataType[DynamicSememeDataType.values().length];
		for (int i = 0; i < DynamicSememeDataType.values().length; i++)
		{
			result[i] = new RestDynamicSememeDataType(DynamicSememeDataType.values()[i]);
		}
		return result;
	}
	
	public DynamicSememeDataType translate()
	{
		return DynamicSememeDataType.values()[this.enumId];
	}
}
