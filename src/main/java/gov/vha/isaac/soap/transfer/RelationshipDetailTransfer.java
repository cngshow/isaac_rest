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

@XmlRootElement(name = "AssociationDetailTransfer")
@XmlType(propOrder = { "entityId", "sourceConceptCode", "targetConceptCode", "sourceDesignationName",
		"targetDesignationName", "relationshipTypeName", "relationshipStatus" })
public class RelationshipDetailTransfer {
	private Long entityId;
	private String sourceConceptCode;
	private String targetConceptCode;
	private String sourceDesignationName;
	private String targetDesignationName;
	private String relationshipTypeName;
	private String relationshipStatus;

	@XmlElement(name = "AssociationId", required = true, nillable = false)
	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	@XmlElement(name = "SourceConceptCode", required = true, nillable = false)
	public String getSourceConceptCode() {
		return sourceConceptCode;
	}

	public void setSourceConceptCode(String sourceConceptCode) {
		this.sourceConceptCode = sourceConceptCode;
	}

	@XmlElement(name = "TargetConceptCode", required = true, nillable = false)
	public String getTargetConceptCode() {
		return targetConceptCode;
	}

	public void setTargetConceptCode(String targetConceptCode) {
		this.targetConceptCode = targetConceptCode;
	}

	@XmlElement(name = "SourceDesignationName", required = true, nillable = false)
	public String getSourceDesignationName() {
		return sourceDesignationName;
	}

	public void setSourceDesignationName(String sourceDesignationName) {
		this.sourceDesignationName = sourceDesignationName;
	}

	@XmlElement(name = "TargetDesignationName", required = true, nillable = false)
	public String getTargetDesignationName() {
		return targetDesignationName;
	}

	public void setTargetDesignationName(String targetDesignationName) {
		this.targetDesignationName = targetDesignationName;
	}

	@XmlElement(name = "AssociationTypeName", required = true, nillable = false)
	public String getRelationshipTypeName() {
		return relationshipTypeName;
	}

	public void setRelationshipTypeName(String relationshipTypeName) {
		this.relationshipTypeName = relationshipTypeName;
	}

	@XmlElement(name = "AssociationStatus", required = true, nillable = false)
	public String getRelationshipStatus() {
		return relationshipStatus;
	}

	public void setRelationshipStatus(String relationshipStatus) {
		this.relationshipStatus = relationshipStatus;
	}
}
