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
package gov.vha.isaac.rest.api1.data.enumerations;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.webcohesion.enunciate.metadata.json.JsonSeeAlso;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;

/**
 * 
 * {@link Enumeration}
 *
 * Note that this is an abstract base class.  The actual returned type will be one of the
 * concrete subtype classes, such as {@link RestDynamicSememeValidatorType} or {@link RestDynamicSememeDataType}
 *
 * @see RestSememeDescriptionVersion
 * @see RestDynamicSememeVersion
 * @see RestObjectChronologyType
 * @see RestSememeType
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestDynamicSememeValidatorType.class, RestDynamicSememeDataType.class, RestObjectChronologyType.class, RestSememeType.class})
@JsonSeeAlso ({RestDynamicSememeValidatorType.class, RestDynamicSememeDataType.class, RestObjectChronologyType.class, RestSememeType.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class Enumeration implements Comparable<Enumeration>
{
	/**
	 * The name of this enumeration type
	 */
	@XmlElement
	String name;
	
	/**
	 * The identifier of this enumeration.  This would be passed back to a call that requested an enum type.
	 */
	@XmlElement
	int enumId;
	
	protected Enumeration(String name, int id)
	{
		this.name = name;
		this.enumId = id;
	}
	
	protected Enumeration()
	{
		//for jaxb
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Enumeration o) {
		return enumId - o.enumId;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getEnumId()
	{
		return enumId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + enumId;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Enumeration other = (Enumeration) obj;
		if (enumId != other.enumId)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
