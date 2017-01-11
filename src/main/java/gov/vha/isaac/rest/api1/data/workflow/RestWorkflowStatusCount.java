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

import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;

/**
 *
 *
 * {@link RestWorkflowStatusCount}
 *
 * @author <a href="mailto:nmarques@westcoastinformatics.com">Nuno Marques</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestWorkflowStatusCount {

	/**
	 * Status of the workflow.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	RestWorkflowProcessStatusType processStatus;

	/**
	 * Count of workflow processes instances.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	long count = 0L;

	/**
	 * Constructor for JAXB only
	 */
	protected RestWorkflowStatusCount() {
		// for Jaxb
		super();
	}

	/**
	 * @param processDetail-
	 *            ISAAC workflow DefinitionDetail
	 */
	public RestWorkflowStatusCount(RestWorkflowProcessStatusType workflowProcessStatusType, long count) {
		this.processStatus = workflowProcessStatusType;
		this.count = count;
	}

	/**
	 * @return the workflow status
	 */
	@XmlTransient
	public RestWorkflowProcessStatusType getProcessStatus() {
		return processStatus;
	}

	/**
	 * @return the count
	 */
	@XmlTransient
	public long getCount() {
		return count;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((processStatus == null) ? 0 : processStatus.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowStatusCount [processStatus=" + processStatus + ", count=" + count + "]";
	}
}
