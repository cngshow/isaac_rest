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
package gov.vha.isaac.rest.api1.data.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * A wrapper for a list of {@link RestWorkflowProcess}
 * This class carries back result sets
 * 
 * {@link RestWorkflowProcesses}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcesses
{
	/**
	 * The contained results
	 */
	@XmlElement
	List<RestWorkflowProcess> results = new ArrayList<>();

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcesses()
	{
		//For jaxb
	}

	/**
	 * @param results
	 */
	public RestWorkflowProcesses(Collection<RestWorkflowProcess> results) {
		if (results != null) {
			this.results.addAll(results);
		}
	}

	/**
	 * @return the results
	 */
	@XmlTransient
	public List<RestWorkflowProcess> getResults() {
		return Collections.unmodifiableList(results);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcesses [results=" + results + "]";
	}
}