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

@XmlRootElement(name = "CodeSystem")
@XmlType(propOrder = { "name", "vuid", "description", "copyright", "copyrightURL", "preferredDesignationTypeName",
		"version" })
public class CodeSystemVersionTransfer {
	private String name;
	private Long vuid;
	private String description;
	private String copyright;
	private String copyrightURL;
	private String preferredDesignationTypeName;
	private VersionTransfer version;

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

	@XmlElement(name = "Description", required = true, nillable = false)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlElement(name = "Copyright", required = true, nillable = false)
	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	@XmlElement(name = "CopyrightURL", required = false)
	public String getCopyrightURL() {
		return copyrightURL;
	}

	public void setCopyrightURL(String copyrightURL) {
		this.copyrightURL = copyrightURL;
	}

	@XmlElement(name = "PreferredDesignationTypeName", required = false)
	public String getPreferredDesignationTypeName() {
		return preferredDesignationTypeName;
	}

	public void setPreferredDesignationTypeName(String preferredDesignationTypeName) {
		this.preferredDesignationTypeName = preferredDesignationTypeName;
	}

	@XmlElement(name = "Version")
	public VersionTransfer getVersion() {
		return version;
	}

	public void setVersion(VersionTransfer version) {
		this.version = version;
	}
}
