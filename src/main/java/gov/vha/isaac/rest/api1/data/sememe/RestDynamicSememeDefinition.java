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
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;

/**
 * 
 * {@link RestDynamicSememeDefinition}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestDynamicSememeDefinition 
{
	protected RestDynamicSememeDefinition()
	{
		//for jaxb
	}
	
	public RestDynamicSememeDefinition(DynamicSememeUsageDescription dsud)
	{
		this.assemblageConceptId = dsud.getDynamicSememeUsageDescriptorSequence();
		this.sememeUsageDescription = dsud.getDynamicSememeUsageDescription();
		this.referencedComponentTypeRestriction = dsud.getReferencedComponentTypeRestriction() == null ? null : 
			new RestObjectChronologyType(dsud.getReferencedComponentTypeRestriction());
		this.referencedComponentTypeSubRestriction = dsud.getReferencedComponentTypeSubRestriction() == null ? null :
			new RestSememeType(dsud.getReferencedComponentTypeSubRestriction());
		this.columnInfo = new RestDynamicSememeColumnInfo[dsud.getColumnInfo().length];
		int i = 0;
		for (DynamicSememeColumnInfo dsci : dsud.getColumnInfo())
		{
			this.columnInfo[i++] = new RestDynamicSememeColumnInfo(dsci);
		}
	}

	/**
	 * The concept sequence of the concept that is used as an assemblage.  The rest of the descriptive details of the 
	 * sememe assemblage (returned in this object) are read from this concept.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public int assemblageConceptId;
	
	/**
	 * the user-friendly description of the overall purpose of this sememe
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String sememeUsageDescription;
	
	/**
	 * The ordered column information which will correspond with the data returned by the dataColumns field of a  {@link RestDynamicSememeVersion}.
	 * These arrays will be the same size, and in the same order.  
	 * @return the column information that describes the data that may be returned as part of a sememe instance.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeColumnInfo[] columnInfo;
	
	/**
	 * Return the {@link RestObjectChronologyType} of the restriction on referenced components for this sememe (if any - may return null)
	 * 
	 * If there is a restriction, the nid set for the referenced component in an instance of this sememe must be of the type listed here.
	 * 
	 * See rest/1/enumeration/restObjectChronologyType for a list of potential object types returned.
	 * 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestObjectChronologyType referencedComponentTypeRestriction;
	
	/**
	 * Return the {@link RestSememeType} of the sub restriction on referenced components for this DynamicSememe (if any - may return null)
	 * 
	 * If there is a restriction, the nid set for the referenced component in an instance of this sememe must be of the type listed here.
	 * 
	 * This is only applicable when {@link #referencedComponentTypeRestriction} returns {@link RestObjectChronologyType#SEMEME}
	 * 
	 * See rest/1/enumeration/restSememeType for a list of potential object types returned.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestSememeType referencedComponentTypeSubRestriction;
}
