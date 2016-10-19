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
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;

/**
 * {@link RestDynamicSememeTypeCreate}
 *
 * This stub class is used for callers to create an instance of a concept which defines a Sememe Assemblage.
 * 
 * The API never returns this class.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestDynamicSememeTypeCreate.class)
public class RestDynamicSememeTypeCreate 
{
	protected RestDynamicSememeTypeCreate()
	{
		//for jaxb
	}
	
	
	/**
	 * The primary name of this sememe.  
	 */
	@XmlElement
	@JsonInclude
	public String name;
	
	/**
	 * The description of this sememe
	 */
	@XmlElement
	@JsonInclude
	public String description;
	
	/**
	 * The (optional) extended fields that are declared for each sememe instance that is created using this sememe definition.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeColumnInfoCreate[] dataColumnsDefinition;
	
	/**
	 * the optional uuid, nid or sequence of the parent concept for the created sememe definition.  If not provided, this defaults to
	 * {@link DynamicSememeConstants#DYNAMIC_SEMEME_ASSEMBLAGES}
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String parentConcept;
	
	/**
	 * optional - may be null - if provided - this restricts the type of object referenced by the nid or  UUID that is set for the referenced component 
	 * in an instance of this sememe.  If {@link ObjectChronologyType#UNKNOWN_NID} is passed, it is ignored, as if it were null.
	 * 
	 * The value passed here can be the value provided by {@link RestObjectChronologyType#name} or {@link RestObjectChronologyType#enumId}.  
	 * To retrieve the valid RestObjectChronologyType types, call 1/system/enumeration/restObjectChronologyType/
	 * 
	 * The typical values for this parameter would be "CONCEPT" or "SEMEME"
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String referencedComponentRestriction;
	
	/**
	 * optional - may be null - subtype restriction which is applicable when {@link #referencedComponentRestriction} is set to SEMEME.
	 * 
	 * The value passed here can be the value provided by {@link RestSememeType#name} or {@link RestSememeType#enumId}.  
	 * To retrieve the valid RestSememeType types, call 1/system/enumeration/restSememeType/
	 * 
	 * The typical values for this parameter would be "LONG" or "STRING" or "DYNAMIC"
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String referencedComponentSubRestriction;
}
