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

@XmlRootElement(name = "Domain")
@XmlType(propOrder = { "domainVAPreferredName", "domainVuid", "domainStatus" })
public class DomainDetailTransfer {
	private String domainVAPreferredName;
	private Long domainVuid;
	private String domainStatus;

	public DomainDetailTransfer() {
	}

	@XmlElement(name = "DomainVAPreferredName", required = true, nillable = false)
	public String getDomainVAPreferredName() {
		return domainVAPreferredName;
	}

	public void setDomainVAPreferredName(String domainVAPreferredName) {
		this.domainVAPreferredName = domainVAPreferredName;
	}

	@XmlElement(name = "DomainVuid", required = true, nillable = false)
	public Long getDomainVuid() {
		return domainVuid;
	}

	public void setDomainVuid(Long domainVuid) {
		this.domainVuid = domainVuid;
	}

	@XmlElement(name = "DomainStatus", required = true, nillable = false)
	public String getDomainStatus() {
		return domainStatus;
	}

	public void setDomainStatus(String domainStatus) {
		this.domainStatus = domainStatus;
	}
}
