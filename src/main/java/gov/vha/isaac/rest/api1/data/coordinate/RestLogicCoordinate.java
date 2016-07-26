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

import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;

/**
 * 
 * {@link RestLogicCoordinate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestLogicCoordinate {
	/**
	 * Sequence number of the STATED assemblage.
	 */
	@XmlElement
	public int statedAssemblage;
	
	/**
	 * Sequence number of the INFERRED assemblage.
	 */
	@XmlElement
	public int inferredAssemblage;
	
	/**
	 * Sequence number of the description profile assemblage.
	 */
	@XmlElement
	public int descriptionLogicProfile;
	
	/**
	 * Sequence number of the classifier assemblage.
	 */
	@XmlElement
	public int classifier;

	/**
	 * @param ochreLogicCoordinate OCHRE LogicCoordinate
	 * 
	 * Constructs RestLogicCoordinate from OCHRE LogicCoordinate
	 */
	public RestLogicCoordinate(LogicCoordinate ochreLogicCoordinate) {
		statedAssemblage = ochreLogicCoordinate.getStatedAssemblageSequence();
		inferredAssemblage = ochreLogicCoordinate.getInferredAssemblageSequence();
		descriptionLogicProfile = ochreLogicCoordinate.getDescriptionLogicProfileSequence();
		classifier = ochreLogicCoordinate.getClassifierSequence();
	}

	protected RestLogicCoordinate() {
		// For JAXB
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + classifier;
		result = prime * result + descriptionLogicProfile;
		result = prime * result + inferredAssemblage;
		result = prime * result + statedAssemblage;
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
		RestLogicCoordinate other = (RestLogicCoordinate) obj;
		if (classifier != other.classifier)
			return false;
		if (descriptionLogicProfile != other.descriptionLogicProfile)
			return false;
		if (inferredAssemblage != other.inferredAssemblage)
			return false;
		if (statedAssemblage != other.statedAssemblage)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestLogicCoordinate [statedAssemblage=" + statedAssemblage + ", inferredAssemblage="
				+ inferredAssemblage + ", descriptionLogicProfile=" + descriptionLogicProfile + ", classifier="
				+ classifier + "]";
	}
}
