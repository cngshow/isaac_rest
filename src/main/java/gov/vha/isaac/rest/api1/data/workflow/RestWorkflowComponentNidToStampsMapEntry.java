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
 * A tuple containing the key/value pair constituting a map entry
 * in a map of component nids to stamps
 * A set of these constitutes a map contained in {@link RestWorkflowProcess}
 * 
 * {@link RestWorkflowComponentNidToStampsMapEntry}
 * 
 * This class carries back result map
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowComponentNidToStampsMapEntry
{
	/**
	 * The key
	 */
	@XmlElement
	int key;

	/**
	 * The value
	 */
	@XmlElement
	List<Integer> value = new ArrayList<>();

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowComponentNidToStampsMapEntry()
	{
		//For jaxb
	}

	/**
	 * @param map
	 */
	public RestWorkflowComponentNidToStampsMapEntry(int key, Collection<Integer> value) {
		this.key = key;
		if (value != null) {
			for (int stamp : value) {
				this.value.add(stamp);
			}
		}
	}

	/**
	 * @return the key
	 */
	@XmlTransient
	public int getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	@XmlTransient
	public List<Integer> getValue() {
		return Collections.unmodifiableList(value);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + key;
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
		RestWorkflowComponentNidToStampsMapEntry other = (RestWorkflowComponentNidToStampsMapEntry) obj;
		if (key != other.key)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowComponentNidToStampsMapEntry [key=" + key + ", value=" + value + "]";
	}
}