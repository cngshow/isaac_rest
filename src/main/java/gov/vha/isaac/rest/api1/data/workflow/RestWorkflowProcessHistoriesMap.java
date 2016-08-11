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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * {@link RestWorkflowProcessHistoriesMap}
 * 
 * This class carries back result map
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessHistoriesMap
{
	/**
	 * The contained results
	 */
	@XmlElement
	public Map<Object, List<RestWorkflowProcessHistory>> map = null;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcessHistoriesMap()
	{
		//For jaxb
	}

	/**
	 * @param map
	 */
	public RestWorkflowProcessHistoriesMap(Map<Object, List<RestWorkflowProcessHistory>> map) {
		if (map != null) {
			this.map = new HashMap<>();
			this.map.putAll(map);
		}
	}
}