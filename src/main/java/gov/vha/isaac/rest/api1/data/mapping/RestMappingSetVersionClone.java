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

package gov.vha.isaac.rest.api1.data.mapping;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestMappingSetVersionClone}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetVersionBase.class)
public class RestMappingSetVersionClone extends RestMappingSetVersionBase {

	/**
	 * The concept id (sequence, NID or UUID) of the target mapping set to clone  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String cloneTargetConcept;

	/**
	 * 
	 */
	RestMappingSetVersionClone() {
		// For JAXB
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 * @param active
	 */
	public RestMappingSetVersionClone(
			String cloneTargetConcept,
			String name,
			String inverseName,
			String description,
			String purpose,
			Boolean active) {
		super(name, inverseName, description, purpose, active);
		this.cloneTargetConcept = cloneTargetConcept;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersionClone [cloneTargetConcept=" + cloneTargetConcept + ", name=" + name
				+ ", inverseName=" + inverseName + ", description=" + description + ", purpose=" + purpose + ", active="
				+ active + "]";
	}
}
