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

import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;

/**
 * 
 * {@link RestTaxonomyCoordinate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestTaxonomyCoordinate {
	@XmlElement
	public boolean stated;

	@XmlElement
	public RestStampCoordinate stampCoordinate;
	
	@XmlElement
	public RestLanguageCoordinate languageCoordinate;
	
	@XmlElement
	public RestLogicCoordinate logicCoordinate;
	
	public RestTaxonomyCoordinate(TaxonomyCoordinate tc) {
		stated = tc.getTaxonomyType() == PremiseType.STATED;
		stampCoordinate = new RestStampCoordinate(tc.getStampCoordinate());
		languageCoordinate = new RestLanguageCoordinate(tc.getLanguageCoordinate());
		logicCoordinate = new RestLogicCoordinate(tc.getLogicCoordinate());
	}

	protected RestTaxonomyCoordinate() {
		// For JAXB
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((languageCoordinate == null) ? 0 : languageCoordinate.hashCode());
		result = prime * result + ((logicCoordinate == null) ? 0 : logicCoordinate.hashCode());
		result = prime * result + ((stampCoordinate == null) ? 0 : stampCoordinate.hashCode());
		result = prime * result + (stated ? 1231 : 1237);
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
		RestTaxonomyCoordinate other = (RestTaxonomyCoordinate) obj;
		if (languageCoordinate == null) {
			if (other.languageCoordinate != null)
				return false;
		} else if (!languageCoordinate.equals(other.languageCoordinate))
			return false;
		if (logicCoordinate == null) {
			if (other.logicCoordinate != null)
				return false;
		} else if (!logicCoordinate.equals(other.logicCoordinate))
			return false;
		if (stampCoordinate == null) {
			if (other.stampCoordinate != null)
				return false;
		} else if (!stampCoordinate.equals(other.stampCoordinate))
			return false;
		if (stated != other.stated)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestTaxonomyCoordinate [stated=" + stated + ", stampCoordinate=" + stampCoordinate
				+ ", languageCoordinate=" + languageCoordinate + ", logicCoordinate=" + logicCoordinate + "]";
	}
}
