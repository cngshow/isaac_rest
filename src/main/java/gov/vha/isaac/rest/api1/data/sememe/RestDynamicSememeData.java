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
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
 * 
 * {@link RestDynamicSememeData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestDynamicSememeArray.class, RestDynamicSememeBoolean.class, RestDynamicSememeByteArray.class, RestDynamicSememeDouble.class, RestDynamicSememeFloat.class,
	RestDynamicSememeInteger.class, RestDynamicSememeLong.class, RestDynamicSememeNid.class, RestDynamicSememeSequence.class, RestDynamicSememeString.class, 
	RestDynamicSememeUUID.class,})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
public abstract class RestDynamicSememeData
{
	/**
	 * The 0 indexed column number for this data.  Will not be populated for nested RestDynamicSememeData objects where the 'data' field
	 * is of type RestDynamicSememeArray
	 */
	@XmlElement
	Integer columnNumber;
	
	/**
	 * The data for a column within a RestDynamicSememeVersion instance
	 */
	@XmlElement
	Object data;
	
	protected RestDynamicSememeData(Integer columnNumber, Object data)
	{
		this.columnNumber = columnNumber;
		this.data = data;
	}
	
	protected RestDynamicSememeData()
	{
		//for jaxb
	}
}
