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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;


/**
 * 
 * {@link RestSememeDescriptionUpdateData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeDescriptionUpdateData
{
	/**
	 * The concept sequence of the concept that represents the case significance flag on the description .
	 * This should be description case sensitive, description not case sensitive or description initial character sensitive
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	int caseSignificanceConceptSequence;
	
	/**
	 * The concept sequence of the concept that represents the language of the description (note, this is NOT 
	 * the dialect)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	int languageConceptSequence;
	
	/**
	 * The text of the description
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	String text;
	
	/**
	 * The concept sequence of the concept that represents the type of the description.  
	 * This should be FSN, Synonym, or Definition.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	int descriptionTypeConceptSequence;

	// TODO determine if extendedDescriptionTypeConceptSequence should be updatable
//	/**
//	 * A concept sequence of an optional concept that represents an extended type of the description.  
//	 * This may be something like Abbreviation or Vista Name
//	 */
//	@XmlElement
//	Integer extendedDescriptionTypeConceptSequence;

	/**
	 * The boolean indicating whether specified sememe should be saved as ACTIVE
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	boolean active;
	
	protected RestSememeDescriptionUpdateData()
	{
		//for Jaxb
	}

	/**
	 * @param caseSignificanceConceptSequence
	 * @param languageConceptSequence
	 * @param text
	 * @param descriptionTypeConceptSequence
	 */
	public RestSememeDescriptionUpdateData(
			int caseSignificanceConceptSequence,
			int languageConceptSequence,
			String text,
			int descriptionTypeConceptSequence,
//			Integer extendedDescriptionTypeConceptSequence,
			boolean active) {
		super();
		this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
		this.languageConceptSequence = languageConceptSequence;
		this.text = text;
		this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
		this.active = active;
	}

	/**
	 * @param caseSignificanceConceptSequence
	 * @param languageConceptSequence
	 * @param text
	 * @param descriptionTypeConceptSequence
	 */
	public RestSememeDescriptionUpdateData(
			RestSememeDescriptionVersion version) {
		this(
				version.getCaseSignificanceConceptSequence(),
				version.getLanguageConceptSequence(),
				version.getText(),
				version.getDescriptionTypeConceptSequence(),
				version.getSememeVersion().getState().equals(new RestStateType(State.ACTIVE)));
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
	 * @return whether the description should be active
	 */
	@XmlTransient
	public boolean isActive() {
		return active;
	}

	/**
	 * @param caseSignificanceConceptSequence the caseSignificanceConceptSequence to set
	 */
	public void setCaseSignificanceConceptSequence(int caseSignificanceConceptSequence) {
		this.caseSignificanceConceptSequence = caseSignificanceConceptSequence;
	}

	/**
	 * @param languageConceptSequence the languageConceptSequence to set
	 */
	public void setLanguageConceptSequence(int languageConceptSequence) {
		this.languageConceptSequence = languageConceptSequence;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * @param descriptionTypeConceptSequence the descriptionTypeConceptSequence to set
	 */
	public void setDescriptionTypeConceptSequence(int descriptionTypeConceptSequence) {
		this.descriptionTypeConceptSequence = descriptionTypeConceptSequence;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
}
