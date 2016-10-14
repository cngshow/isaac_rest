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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * A tuple containing the key/value pair constituting a map entry
 * in a map of List<{@link RestWorkflowProcessHistory}> by {@link RestWorkflowProcess}

 * {@link RestWorkflowProcessHistoriesMapEntry}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowProcessHistoriesMapEntry
{
	/**
	 * The key {@link RestWorkflowProcess}
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestWorkflowProcess key;

	/**
	 * The array of {@link RestWorkflowProcessHistory} objects
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestWorkflowProcessHistory[] value;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowProcessHistoriesMapEntry()
	{
		//For jaxb
	}

	/**
	 * @param key
	 * @param value
	 */
	public RestWorkflowProcessHistoriesMapEntry(RestWorkflowProcess key, RestWorkflowProcessHistory[] value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	@XmlTransient
	public RestWorkflowProcess getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	@XmlTransient
	public RestWorkflowProcessHistory[] getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
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
		RestWorkflowProcessHistoriesMapEntry other = (RestWorkflowProcessHistoriesMapEntry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessHistoriesMapEntry [key=" + key + ", value=" + value + "]";
	}
}