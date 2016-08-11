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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * {@link RestWorkflowProcessDetails}
 * 
 * This class carries back result sets
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessDetails
{
	/**
	 * The contained results
	 */
	@XmlElement
	public Collection<RestWorkflowProcessDetail> results = null;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcessDetails()
	{
		//For jaxb
	}

	/**
	 * @param results
	 */
	public RestWorkflowProcessDetails(Collection<RestWorkflowProcessDetail> results) {
		if (results != null) {
			this.results = new ArrayList<>();
			this.results.addAll(results);
		}
	}
}