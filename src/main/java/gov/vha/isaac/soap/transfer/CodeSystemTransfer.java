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

@XmlRootElement(name = "CodeSystem")
@XmlType(propOrder = { "name", "vuid", "versionNames" })
public class CodeSystemTransfer {
	private String name;
	private Long vuid;
	private Collection<String> versionNames;

	@XmlElement(name = "Name", required = true, nillable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "VUID", required = true, nillable = false)
	public Long getVuid() {
		return vuid;
	}

	public void setVuid(Long vuid) {
		this.vuid = vuid;
	}

	@XmlElementWrapper(name = "VersionNames")
	@XmlElement(name = "VersionName")
	public Collection<String> getVersionNames() {
		return versionNames;
	}

	public void setVersionNames(Collection<String> versionNames) {
		this.versionNames = versionNames;
	}
}
