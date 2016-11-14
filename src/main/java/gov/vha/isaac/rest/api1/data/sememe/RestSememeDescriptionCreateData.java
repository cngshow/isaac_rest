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

import java.util.Collection;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * 
 * {@link RestSememeDescriptionCreateData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestSememeDescriptionCreateData.class)
public class RestSememeDescriptionCreateData
{
	/**
	 * The concept identifier (uuid, nid or sequence) of the concept that represents the case significance flag on the description .
	 * This should be description case sensitive, description not case sensitive or description initial character sensitive
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String caseSignificanceConcept;
	
	/**
	 * The optional concept identifier (uuid, nid or sequence) of the concept that represents the language of the description (note, this is NOT 
	 * the dialect).  If not specified, defaults to ENGLISH
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String languageConcept;
	
	/**
	 * The text of the description
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String text;
	
	/**
	 * The concept identifier (uuid, nid or sequence) of the concept that represents the type of the description.  
	 * This should be FSN, Synonym, or Definition.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String descriptionTypeConcept;

	/**
	 * An optional concept identifier (nid, sequence or UUID) of a concept that represents an extended type of the description.  
	 * This may be a concept like Abbreviation or Vista Name
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String extendedDescriptionTypeConcept;
	
	/**
	 * The optional concepts (UUID, nid or sequence) that represent preferred dialects attached to this sememe.  If not specified, 
	 * defaults to US English
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Collection<String> preferredInDialectAssemblagesIds = new HashSet<>();

	/**
	 * The optional concepts (UUID, nid or sequence) that represent acceptable dialects attached to this sememe.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Collection<String> acceptableInDialectAssemblagesIds = new HashSet<>();

	/**
	 * The identifier (UUID or nid) of the component to which this sememe refers.  May NOT be a sequence identifier.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String referencedComponentId;
	
	/**
	 * True to indicate the mapping set should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
	
	protected RestSememeDescriptionCreateData()
	{
		//for Jaxb
	}

	/**
	 * @param caseSignificanceConceptSequence
	 * @param languageConceptSequence
	 * @param text
	 * @param descriptionTypeConceptSequence
	 * @param dialectIds
	 * @param referencedComponentNid
	 */
	public RestSememeDescriptionCreateData(
			int caseSignificanceConceptSequence,
			int languageConceptSequence,
			String text,
			int descriptionTypeConceptSequence,
			Collection<String> preferredInDialectAssemblagesIds,
			Collection<String> acceptableInDialectAssemblagesIds,
			int referencedComponentNid) {
		super();
		this.caseSignificanceConcept = caseSignificanceConceptSequence + "";
		this.languageConcept = languageConceptSequence + "";
		this.text = text;
		this.descriptionTypeConcept = descriptionTypeConceptSequence +"";
		if (preferredInDialectAssemblagesIds != null) {
			this.preferredInDialectAssemblagesIds.addAll(preferredInDialectAssemblagesIds);
		}
		if (acceptableInDialectAssemblagesIds != null) {
			this.acceptableInDialectAssemblagesIds.addAll(acceptableInDialectAssemblagesIds);
		}
		this.referencedComponentId = referencedComponentNid + "";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestSememeDescriptionCreateData ["
				+ "caseSignificanceConceptSequence=" + caseSignificanceConcept
				+ ", languageConceptSequence=" + languageConcept
				+ ", text=" + text
				+ ", descriptionTypeConceptSequence=" + descriptionTypeConcept
				+ ", extendedDescriptionTypeConcept=" + extendedDescriptionTypeConcept
				+ ", preferredInDialectAssemblagesIds=" + preferredInDialectAssemblagesIds
				+ ", acceptableInDialectAssemblagesIds=" + acceptableInDialectAssemblagesIds
				+ ", referencedComponentId=" + referencedComponentId
				+ "]";
	}
}
