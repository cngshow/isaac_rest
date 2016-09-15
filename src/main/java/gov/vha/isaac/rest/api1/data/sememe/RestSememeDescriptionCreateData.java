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
import java.util.Collections;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * 
 * {@link RestSememeDescriptionCreateData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeDescriptionCreateData
{
	/**
	 * The concept sequence of the concept that represents the case significance flag on the description .
	 * This should be description case sensitive, description not case sensitive or description initial character sensitive
	 */
	@XmlElement
	int caseSignificanceConceptSequence;
	
	/**
	 * The concept sequence of the concept that represents the language of the description (note, this is NOT 
	 * the dialect)
	 */
	@XmlElement
	int languageConceptSequence;
	
	/**
	 * The text of the description
	 */
	@XmlElement
	String text;
	
	/**
	 * The concept sequence of the concept that represents the type of the description.  
	 * This should be FSN, Synonym, or Definition.
	 */
	@XmlElement
	int descriptionTypeConceptSequence;

//	/**
//	 * A concept sequence of an optional concept that represents an extended type of the description.  
//	 * This may be something like Abbreviation or Vista Name
//	 */
//	@XmlElement
//	Integer extendedDescriptionTypeConceptSequence;
	
	/**
	 * The preferred dialects attached to this sememe
	 */
	@XmlElement
	Collection<Integer> preferredInDialectAssemblagesIds = new HashSet<>();

	/**
	 * The acceptable dialects attached to this sememe
	 */
	@XmlElement
	Collection<Integer> acceptableInDialectAssemblagesIds = new HashSet<>();

	/**
	 * The nid of the component to which this sememe refers
	 */
	@XmlElement
	int referencedComponentNid;
	
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
//			Integer extendedDescriptionTypeConceptSequence,
			Collection<Integer> preferredInDialectAssemblagesIds,
			Collection<Integer> acceptableInDialectAssemblagesIds,
			int referencedComponentNid) {
		super();
		this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
		this.languageConceptSequence = languageConceptSequence;
		this.text = text;
		this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
//		this.extendedDescriptionTypeConceptSequence = extendedDescriptionTypeConceptSequence;
		if (preferredInDialectAssemblagesIds != null) {
			this.preferredInDialectAssemblagesIds.addAll(preferredInDialectAssemblagesIds);
		}
		if (acceptableInDialectAssemblagesIds != null) {
			this.acceptableInDialectAssemblagesIds.addAll(acceptableInDialectAssemblagesIds);
		}
		this.referencedComponentNid = referencedComponentNid;
	}

	/**
	 * @return the caseSignificanceConceptSequence
	 */
	@XmlTransient
	public int getCaseSignificanceConceptSequence() {
		return caseSignificanceConceptSequence;
	}

	/**
	 * @return the languageConceptSequence
	 */
	@XmlTransient
	public int getLanguageConceptSequence() {
		return languageConceptSequence;
	}

	/**
	 * @return the text
	 */
	@XmlTransient
	public String getText() {
		return text;
	}

	/**
	 * @return the descriptionTypeConceptSequence
	 */
	@XmlTransient
	public int getDescriptionTypeConceptSequence() {
		return descriptionTypeConceptSequence;
	}
	
//	/**
//	 * @return the extendedDescriptionTypeConceptSequence
//	 */
//	@XmlTransient
//	public Integer getExtendedDescriptionTypeConceptSequence() {
//		return extendedDescriptionTypeConceptSequence;
//	}

	/**
	 * @return the preferred dialectIds
	 */
	@XmlTransient
	public Collection<Integer> getPreferredInDialectAssemblagesIds() {
		return Collections.unmodifiableCollection(preferredInDialectAssemblagesIds);
	}

	/**
	 * @return the acceptable dialectIds
	 */
	@XmlTransient
	public Collection<Integer> getAcceptableInDialectAssemblagesIds() {
		return Collections.unmodifiableCollection(acceptableInDialectAssemblagesIds);
	}

	/**
	 * @return the referencedComponentNid
	 */
	@XmlTransient
	public int getReferencedComponentNid() {
		return referencedComponentNid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestSememeDescriptionCreateData ["
				+ "caseSignificanceConceptSequence=" + caseSignificanceConceptSequence
				+ ", languageConceptSequence=" + languageConceptSequence
				+ ", text=" + text
				+ ", descriptionTypeConceptSequence=" + descriptionTypeConceptSequence
				+ ", preferredInDialectAssemblagesIds=" + preferredInDialectAssemblagesIds
				+ ", acceptableInDialectAssemblagesIds=" + acceptableInDialectAssemblagesIds
				+ ", referencedComponentNid=" + referencedComponentNid
				+ "]";
	}
}
