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
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * 
 * {@link RestSememeComponentUpdateData}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeComponentUpdateData
{
	/**
	 * The sememe sequence of the new component sememe
	 */
	@XmlElement
	int componentSememeSequence;
	
	/**
	 * The concept sequence of the assemblage of which the new component must be a member
	 */
	@XmlElement
	int assemblageConceptSequence;

	/**
	 * The nid of the component to which this sememe refers
	 */
	@XmlElement
	int referencedComponentNid;

	/**
	 * The boolean indicating whether the sememe state is ACTIVE (vs INACTIVE)
	 */
	@XmlElement
	boolean active;

	protected RestSememeComponentUpdateData()
	{
		//for Jaxb
	}

	/**
	 * @param componentSememeSequence
	 * @param assemblageConceptSequence
	 * @param referencedComponentNid
	 * @param active
	 */
	public RestSememeComponentUpdateData(
			int componentSememeSequence,
			int assemblageConceptSequence,
			int referencedComponentNid,
			boolean active) {
		super();
		this.componentSememeSequence = componentSememeSequence;
		this.assemblageConceptSequence = assemblageConceptSequence;
		this.referencedComponentNid = referencedComponentNid;
		this.active = active;
	}

	/**
	 * @return the componentSememeSequence
	 */
	@XmlTransient
	public int getComponentSememeSequence() {
		return componentSememeSequence;
	}

	/**
	 * @return the assemblageConceptSequence
	 */
	@XmlTransient
	public int getAssemblageConceptSequence() {
		return assemblageConceptSequence;
	}

	/**
	 * @return the referencedComponentNid
	 */
	@XmlTransient
	public int getReferencedComponentNid() {
		return referencedComponentNid;
	}

	/**
	 * @return the active
	 */
	@XmlTransient
	public boolean isActive() {
		return active;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestSememeComponentCreateData ["
				+ "componentSememeSequence=" + componentSememeSequence
				+ ", assemblageConceptSequence=" + assemblageConceptSequence
				+ ", referencedComponentNid=" + referencedComponentNid
				+ ", active=" + active
				+ "]";
	}
}
