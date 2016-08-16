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

import java.util.List;
import java.util.Set;
import java.util.UUID;

import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessDetailSubjectMatterType;

/**
 * 
 * {@link RestWorkflowProcessBaseCreate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class RestWorkflowProcessBaseCreate {

	/**
	 * The process id
	 */
	public UUID definitionId;

	/** The stamp sequences. */
	public List<Integer> stampSequences;
	
	/** The concept Sequences. */
	public Set<Integer> conceptSequences;

	/** The creator. */
	public int creator;

	/** The subject matter. */
	public RestWorkflowProcessDetailSubjectMatterType subjectMatter;

	/**
	 * COnstructor for JAXB
	 */
	protected RestWorkflowProcessBaseCreate() {
		super();
	}

	/**
	 * @param definitionId
	 * @param stampSequences
	 * @param conceptSequences
	 * @param creator
	 * @param subjectMatter
	 */
	public RestWorkflowProcessBaseCreate(
			UUID definitionId,
			List<Integer> stampSequences,
			Set<Integer> conceptSequences,
			int creator,
			RestWorkflowProcessDetailSubjectMatterType subjectMatter) {
		super();
		this.definitionId = definitionId;
		this.stampSequences = stampSequences;
		this.conceptSequences = conceptSequences;
		this.creator = creator;
		this.subjectMatter = subjectMatter;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestWorkflowProcessCreate [definitionId=" + definitionId + ", stampSequences=" + stampSequences
				+ ", conceptSequences=" + conceptSequences + ", creator=" + creator + ", subjectMatter=" + subjectMatter
				+ "]";
	}
}