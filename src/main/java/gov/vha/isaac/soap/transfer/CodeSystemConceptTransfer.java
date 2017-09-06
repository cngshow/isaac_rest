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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.soap.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Concept")
public class CodeSystemConceptTransfer {
	private String conceptCode;
	private boolean conceptStatus;
	private Collection<DesignationTransfer> designations;

	public CodeSystemConceptTransfer() {

	}

	public CodeSystemConceptTransfer(String conceptCode, boolean conceptStatus,
			Collection<DesignationTransfer> designations) {
		super();
		this.conceptCode = conceptCode;
		this.conceptStatus = conceptStatus;
		this.designations = designations;
	}

	@XmlElement(name = "Code")
	public String getConceptCode() {
		return conceptCode;
	}

	public void setConceptCode(String conceptCode) {
		this.conceptCode = conceptCode;
	}

	@XmlElement(name = "Status")
	public boolean isConceptStatus() {
		return conceptStatus;
	}

	public void setConceptStatus(boolean conceptStatus) {
		this.conceptStatus = conceptStatus;
	}

	@XmlElementWrapper(name = "Designations")
	@XmlElement(name = "Designation")
	public Collection<DesignationTransfer> getDesignations() {
		return designations;
	}

	public void setDesignations(Collection<DesignationTransfer> designations) {
		this.designations = designations;
	}
}
