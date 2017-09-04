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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import gov.vha.isaac.soap.model.Property;

@XmlRootElement(name = "Property")
@XmlType(propOrder = { "value", "type", "status" })
public class PropertyTransfer {
	protected String value;
	protected String type;
	protected String status;

	@XmlElement(name = "Name", required = true, nillable = false)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlElement(name = "Type", required = true, nillable = false)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "status", required = true, nillable = false)
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public static List<PropertyTransfer> convertFromPropertyList(List<Property> propertyList) {
		List<PropertyTransfer> propertyTransferList = new ArrayList<PropertyTransfer>();
		if (propertyList != null) {
			for (Property property : propertyList) {
				PropertyTransfer propertyTransfer = new PropertyTransfer();
				propertyTransfer.setType(property.getPropertyType().getName());
				propertyTransfer.setValue(property.getValue());
				String status = (property.getActive()) ? "Active" : "Inactive";
				propertyTransfer.setStatus(status);
				propertyTransferList.add(propertyTransfer);
			}
		}

		return propertyTransferList;
	}
}
