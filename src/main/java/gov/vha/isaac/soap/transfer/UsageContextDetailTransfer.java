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
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "UsageContextDetail")
@XmlType(propOrder = { "name", "vuid", "usageContextDomains" })
public class UsageContextDetailTransfer {
	protected String name;
	protected String vuid;
	Collection<UsageContextTransfer> usageContextDomains;

	public UsageContextDetailTransfer() {
	}

	public UsageContextDetailTransfer(String name, String vuid, Collection<UsageContextTransfer> usageContextDomains) {
		super();
		this.name = name;
		this.vuid = vuid;
		this.usageContextDomains = usageContextDomains;
	}

	public UsageContextDetailTransfer(String name, String vuid) {
		this.name = name;
		this.vuid = vuid;
	}

	@XmlElement(name = "Name", required = true, nillable = false)
	public String getName() {
		return name;
	}

	@XmlElement(name = "VUID", required = false, nillable = false)
	public String getVuid() {
		return vuid;
	}

	public void setVuid(String vuid) {
		this.vuid = vuid;
	}

	@XmlElementWrapper(name = "UsageContextDomains")
	@XmlElement(name = "UsageContextDomain")
	public Collection<UsageContextTransfer> getUsageContextDomains() {
		return usageContextDomains;
	}

	public void setUsageContextDomains(Collection<UsageContextTransfer> usageContextDomains) {
		this.usageContextDomains = usageContextDomains;
	}

	public void setName(String name) {
		this.name = name;
	}
}
