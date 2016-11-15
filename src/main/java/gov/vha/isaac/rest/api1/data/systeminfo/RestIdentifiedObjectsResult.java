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
package gov.vha.isaac.rest.api1.data.systeminfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;

/**
 * {@link RestIdentifiedObjectsResult}
 * 
 * This class carries license information
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestIdentifiedObjectsResult
{
	/**
	 * Zero or one concept chronology
	 */
	@XmlElement
	RestConceptChronology concept;
	
	/**
	 * Zero or one sememe chronology
	 */
	@XmlElement
	RestSememeChronology sememe;

	public RestIdentifiedObjectsResult()
	{
		//For jaxb
	}

	/**
	 * @param concept RestConceptChronology
	 * @param sememe RestSememeChronology
	 */
	public RestIdentifiedObjectsResult(RestConceptChronology concept, RestSememeChronology sememe) {
		super();
		this.concept = concept;
		this.sememe = sememe;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestIdentifiedObjectsResult [concept=" + (concept != null ? concept.getDescription() : null) + ", sememe=" 
				+ (sememe != null ? sememe.identifiers.uuids : null) + "]";
	}

	/**
	 * @return the concept
	 */
	@XmlTransient
	public RestConceptChronology getConcept() {
		return concept;
	}

	/**
	 * @return the sememe
	 */
	@XmlTransient
	public RestSememeChronology getSememe() {
		return sememe;
	}
}
