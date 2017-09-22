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

@XmlRootElement(name = "ValueSetList")
@XmlType(propOrder = { "totalNumberOfRecords", "valueSetTransfersDetails" })
public class ValueSetListTransfer {
	private Long totalNumberOfRecords;
	private List<ValueSetTransfer> valueSetTransfersDetails = new ArrayList<ValueSetTransfer>();

	@XmlElement(name = "TotalNumberOfRecords")
	public Long getTotalNumberOfRecords() {
		return totalNumberOfRecords;
	}

	public void setTotalNumberOfRecords(Long totalNumberOfRecords) {
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	@XmlElement(name = "ValueSet", required = false, nillable = true)
	public List<ValueSetTransfer> getValueSetTransfersDetails() {
		return valueSetTransfersDetails;
	}

	public void setValueSetTransfersDetails(List<ValueSetTransfer> valueSetTransfersDetails) {
		this.valueSetTransfersDetails = valueSetTransfersDetails;
	}
}
