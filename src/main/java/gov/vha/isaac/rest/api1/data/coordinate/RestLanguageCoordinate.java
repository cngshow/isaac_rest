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

package gov.vha.isaac.rest.api1.data.coordinate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.rest.HashCodeUtils;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;

/**
 * 
 * {@link RestLanguageCoordinate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestLanguageCoordinate {
	/**
	 * Identifier of the language concept associated with the language coordinate.
	 * The language will be something like
	 * english, spanish, french, danish, polish, dutch,
	 * lithuanian, chinese, japanese, or swedish.
	 */
	@XmlElement
	public RestIdentifiedObject language;
	
	/**
	 * Ordered list of dialect assemblage concept identifiers. Order determines preference.
	 * A dialect assemblage will be something like US (US Dialect) or GB (Great Britain Dialect).
	 */
	@XmlElement
	public RestIdentifiedObject[] dialectAssemblagePreferences;

	/**
	 * Ordered list of description type concept identifiers. Order determines preference.
	 * A description type will be something like FSN (Fully Specified Name), Synonym or Definition.
	 */
	@XmlElement
	public RestIdentifiedObject[] descriptionTypePreferences;
	
	/**
	 * @param ochreLanguageCoordinate OCHRE LanguageCoordinate
	 * 
	 * Constructs RestLanguageCoordinate from OCHRE LanguageCoordinate
	 */
	public RestLanguageCoordinate(LanguageCoordinate ochreLanguageCoordinate) {
		language = new RestIdentifiedObject(ochreLanguageCoordinate.getLanguageConceptSequence(), ObjectChronologyType.CONCEPT);
		dialectAssemblagePreferences = new RestIdentifiedObject[ochreLanguageCoordinate.getDialectAssemblagePreferenceList() != null ? ochreLanguageCoordinate.getDialectAssemblagePreferenceList().length : 0];
		int index = 0;
		for (int seq : ochreLanguageCoordinate.getDialectAssemblagePreferenceList()) {
			dialectAssemblagePreferences[index++] = new RestIdentifiedObject(seq, ObjectChronologyType.CONCEPT);
		}
		descriptionTypePreferences = new RestIdentifiedObject[ochreLanguageCoordinate.getDescriptionTypePreferenceList() != null ? ochreLanguageCoordinate.getDescriptionTypePreferenceList().length : 0];
		index = 0;
		for (int seq : ochreLanguageCoordinate.getDescriptionTypePreferenceList()) {
			descriptionTypePreferences[index++] = new RestIdentifiedObject(seq, ObjectChronologyType.CONCEPT);
		}
	}

	protected RestLanguageCoordinate() {
		// For JAXB
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + HashCodeUtils.getOrderedUniqueValues(descriptionTypePreferences).hashCode();
		result = prime * result
				+ HashCodeUtils.getOrderedUniqueValues(dialectAssemblagePreferences).hashCode();
		result = prime * result + (language == null ? 0 : language.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestLanguageCoordinate other = (RestLanguageCoordinate) obj;
		if (!HashCodeUtils.getOrderedUniqueValues(descriptionTypePreferences).equals(HashCodeUtils.getOrderedUniqueValues(other.descriptionTypePreferences)))
			return false;
		if (!HashCodeUtils.getOrderedUniqueValues(dialectAssemblagePreferences).equals(HashCodeUtils.getOrderedUniqueValues(other.dialectAssemblagePreferences)))
			return false;
		if (!HashCodeUtils.equals(language, other.language)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestLanguageCoordinate [language=" + language + ", dialectAssemblagePreferences="
				+ dialectAssemblagePreferences + ", descriptionTypePreferences=" + descriptionTypePreferences + "]";
	}
}
