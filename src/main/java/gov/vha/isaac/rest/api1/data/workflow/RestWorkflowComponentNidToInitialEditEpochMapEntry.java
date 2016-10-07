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

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.commit.Stamp;


/** 
 * A tuple containing the key/value pair constituting a map entry
 * in a map of component nids to stamps
 * A set of these constitutes a map contained in {@link RestWorkflowProcess}
 * 
 * {@link RestWorkflowComponentNidToInitialEditEpochMapEntry}
 * 
 * This class carries back result map
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowComponentNidToInitialEditEpochMapEntry
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
	Stamp value;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowComponentNidToInitialEditEpochMapEntry()
	{
		//For jaxb
	}

	/**
	 * @param map
	 */
	public RestWorkflowComponentNidToInitialEditEpochMapEntry(int key, Stamp value) {
		this.key = key;
		this.value = value;
	}
	public RestWorkflowComponentNidToInitialEditEpochMapEntry(Map.Entry<Integer, Stamp> entry) {
		this(entry.getKey(), entry.getValue());
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
	public Stamp getValue() {
		return value;
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
		RestWorkflowComponentNidToInitialEditEpochMapEntry other = (RestWorkflowComponentNidToInitialEditEpochMapEntry) obj;
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