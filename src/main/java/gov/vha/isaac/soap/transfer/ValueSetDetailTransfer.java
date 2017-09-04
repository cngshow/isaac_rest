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

@XmlRootElement(name = "ValueSetDetail")
@XmlType(propOrder = { "codeSystemVUID", "versionName", "count" })
public class ValueSetDetailTransfer extends ValueSetTransfer {
	private Long codeSystemVUID;
	private String versionName;
	private Long count;

	public ValueSetDetailTransfer() {
		super();
	}

	public ValueSetDetailTransfer(String name, Long vuid, String subsetStatus, Long codeSystemVUID, String versionName,
			long count) {
		super(name, vuid, subsetStatus);
		this.codeSystemVUID = codeSystemVUID;
		this.versionName = versionName;
		this.count = count;
	}

	@XmlElement(name = "codeSystemVUID", required = true, nillable = false)
	public Long getCodeSystemVUID() {
		return codeSystemVUID;
	}

	public void setCodeSystemVUID(Long codeSystemVUID) {
		this.codeSystemVUID = codeSystemVUID;
	}

	@XmlElement(name = "VersionName", required = true, nillable = false)
	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	@XmlElement(name = "DesignationCount", required = true, nillable = false)
	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

}
