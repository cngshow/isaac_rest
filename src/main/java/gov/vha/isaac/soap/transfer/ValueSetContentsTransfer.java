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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ValueSetContent")
@XmlType(propOrder = { "designationVuid", "designationName", "designationStatus", "designationType", "membershipStatus",
		"totalNumberOfRecords" })
public class ValueSetContentsTransfer {
	private Long designationVuid;
	private String designationName;
	private String designationStatus;
	private String designationType;
	private String membershipStatus;
	private Long totalNumberOfRecords;

	public ValueSetContentsTransfer() {
	}

	public ValueSetContentsTransfer(Long designationVuid, String designationName, String designationStatus,
			String designationType, String membershipStatus) {
		this.designationVuid = designationVuid;
		this.designationName = designationName;
		this.designationStatus = designationStatus;
		this.designationType = designationType;
		this.membershipStatus = membershipStatus;
	}

	@XmlElement(name = "VUID", required = true, nillable = false)
	public Long getDesignationVuid() {
		return designationVuid;
	}

	public void setDesignationVuid(Long designationVuid) {
		this.designationVuid = designationVuid;
	}

	@XmlElement(name = "Name", required = true, nillable = false)
	public String getDesignationName() {
		return designationName;
	}

	public void setDesignationName(String designationName) {
		this.designationName = designationName;
	}

	@XmlElement(name = "Status", required = true, nillable = false)
	public String getDesignationStatus() {
		return designationStatus;
	}

	public void setDesignationStatus(String designationStatus) {
		this.designationStatus = designationStatus;
	}

	@XmlElement(name = "Type", required = true, nillable = false)
	public String getDesignationType() {
		return designationType;
	}

	public void setDesignationType(String designationType) {
		this.designationType = designationType;
	}

	@XmlElement(name = "MembershipStatus", required = true, nillable = false)
	public String getMembershipStatus() {
		return membershipStatus;
	}

	public void setMembershipStatus(String membershipStatus) {
		this.membershipStatus = membershipStatus;
	}

	@XmlElement(name = "TotalNumberOfRecords", required = true, nillable = false)
	public Long getTotalNumberOfRecords() {
		return totalNumberOfRecords;
	}

	public void setTotalNumberOfRecords(Long totalNumberOfRecords) {
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
}
