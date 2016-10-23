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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;

/**
 * {@link RestDynamicSememeBase}
 *
 * This stub class is used for callers to edit {@link RestSememeVersion} objects.  It only contains the fields that may be edited after creation.
 * 
 * The API never returns this class.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestDynamicSememeBase.class)
public class RestDynamicSememeBase
{
	protected RestDynamicSememeBase()
	{
		//for jaxb
	}
	/**
	 * The data to attach with this sememe instance (if any).  This may be null, empty, or a length up to the defined length of the sememe definition.
	 * 
	 * The supplied data must match the definition of the sememe - which can be read via {@link SememeAPIs#getSememeDefinition(String, String)} 
	 * (1/sememe/sememeDefinition/{assemblageId})
	 * 
	 * RestDynamicSememeData is an abstract type.  The data passed here, must be of a concrete type.  For the server to deserialize the type properly, 
	 * a field must be included of the form "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.CLASSTYPE"
	 * 
	 * where CLASSTYPE is one of:
	 * - RestDynamicSememeArray
	 * - RestDynamicSememeBoolean
	 * - RestDynamicSememeByteArray
	 * - RestDynamicSememeDouble
	 * - RestDynamicSememeFloat
	 * - RestDynamicSememeInteger,
	 * - RestDynamicSememeLong,
	 * - RestDynamicSememeString,
	 * - RestDynamicSememeNid
	 * - RestDynamicSememeSequence
	 * - RestDynamicSememeUUID
	 * 
	 * The class type strings are also available in the /rest/1/system/enumeration/restDynamicSememeDataType call, which returns all of the available data 
	 * types, names, ids, and class type information.
	 * 
	 * Example JSON that provides two columns of differing types:
	 * 
	 * {
	 *   "assemblageConcept": "-2147483449",
	 *   "referencedComponent": "-2147483557",
	 *   "columnData": [{
	 *     "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString",
		    "data": "test"
		    "columnNumber": "0"
	 *   }, {
	 *     "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong",
	 *     "data": 5
	 *     "columnNumber": "1"
	 *   }]
	 * }
	 * 
	 * 
	 */
	@XmlElement
	@JsonInclude
	public RestDynamicSememeData[] columnData;
	
	/**
	 * True to indicate the sememe should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
}
