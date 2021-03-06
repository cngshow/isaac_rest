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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Designation")
@XmlType(propOrder = { "name", "code", "type", "status", "properties", "subsets" })
public class DesignationDetailTransfer {
	private String name;
	private String code;
	private String type;
	private String status;
	private List<PropertyTransfer> properties;
	private List<ValueSetTransfer> subsets;

	public DesignationDetailTransfer() {
	}

	public DesignationDetailTransfer(String name, String code, String type, String status,
			List<PropertyTransfer> properties, List<ValueSetTransfer> subsets) {
		super();
		this.name = name;
		this.code = code;
		this.type = type;
		this.status = status;
		this.properties = properties;
		this.subsets = subsets;
	}

	@XmlElement(name = "Name", required = true, nillable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "Code", required = true, nillable = false)
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlElement(name = "Type", required = true, nillable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "Status", required = true, nillable = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlElementWrapper(name = "Properties")
	@XmlElement(name = "Property", required = true, nillable = false)
	public List<PropertyTransfer> getProperties() {
		return properties;
	}

	public void setProperties(List<PropertyTransfer> properties) {
		this.properties = properties;
	}

	@XmlElementWrapper(name = "ValueSets")
	@XmlElement(name = "ValueSet", required = true, nillable = false)
	public List<ValueSetTransfer> getSubsets() {
		return subsets;
	}

	public void setSubsets(List<ValueSetTransfer> subsets) {
		this.subsets = subsets;
	}
}
