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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;

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
	 * Sequence number of the language concept associated with the language coordinate.
	 * The language will be something like
	 * english, spanish, french, danish, polish, dutch,
	 * lithuanian, chinese, japanese, or swedish.
	 */
	@XmlElement
	int language;
	
	/**
	 * Ordered list of dialect assemblage sequence numbers. Order determines preference.
	 * A dialect assemblage will be something like US (US Dialect) or GB (Great Britain Dialect).
	 */
	@XmlElement
	List<Integer> dialectAssemblagePreferences = new ArrayList<>();

	/**
	 * Ordered list of description type sequence numbers. Order determines preference.
	 * A description type will be something like FSN (Fully Specified Name), Synonym or Definition.
	 */
	@XmlElement
	List<Integer> descriptionTypePreferences = new ArrayList<>();
	
	/**
	 * @param ochreLanguageCoordinate OCHRE LanguageCoordinate
	 * 
	 * Constructs RestLanguageCoordinate from OCHRE LanguageCoordinate
	 */
	public RestLanguageCoordinate(LanguageCoordinate ochreLanguageCoordinate) {
		language = ochreLanguageCoordinate.getLanguageConceptSequence();
		for (int seq : ochreLanguageCoordinate.getDialectAssemblagePreferenceList()) {
			dialectAssemblagePreferences.add(seq);
		}
		for (int seq : ochreLanguageCoordinate.getDescriptionTypePreferenceList()) {
			descriptionTypePreferences.add(seq);
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
		result = prime * result + ((descriptionTypePreferences == null) ? 0 : descriptionTypePreferences.hashCode());
		result = prime * result
				+ ((dialectAssemblagePreferences == null) ? 0 : dialectAssemblagePreferences.hashCode());
		result = prime * result + language;
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
		if (descriptionTypePreferences == null) {
			if (other.descriptionTypePreferences != null)
				return false;
		} else if (!descriptionTypePreferences.equals(other.descriptionTypePreferences))
			return false;
		if (dialectAssemblagePreferences == null) {
			if (other.dialectAssemblagePreferences != null)
				return false;
		} else if (!dialectAssemblagePreferences.equals(other.dialectAssemblagePreferences))
			return false;
		if (language != other.language)
			return false;
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
