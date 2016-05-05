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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api1.data.systeminfo.RestDependencyInfo;
import gov.vha.isaac.rest.api1.data.systeminfo.RestLicenseInfo;

/**
 * {@link SystemInfo}
 * 
 * This class carries back various system information about this deployment.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class SystemInfo
{
	/**
	 * The full version number of this API.  Note, this is an array, because in the future
	 * the API may simultaneously support versions such as [1.3, 2.0] for reverse compatibility.
	 */
	@XmlElement
	String[] supportedAPIVersions = new String[] {"1.0"};
	
	/**
	 * DB Maven dependency
	 */
	@XmlElement
	public RestDependencyInfo dbDependency;

	/**
	 * SCM URL
	 */
	@XmlElement
	public String scmUrl;
	
	@XmlElement
	public String isaacVersion;
	@XmlElement
	public String isaacGuiVersion;
	@XmlElement
	public String assemblyVersion;
	@XmlElement
	public String metadataVersion = "?";

	//TODO add more system config info, such as the following
	@XmlElement
	List<RestLicenseInfo> appLicenses = new ArrayList<>();
	@XmlElement
	List<RestLicenseInfo> dbLicenses = new ArrayList<>();
	@XmlElement
	List<RestDependencyInfo> dbDependencies = new ArrayList<>();

	public SystemInfo()
	{
		//For jaxb
	}

	/**
	 * @param supportedAPIVersions the supportedAPIVersions to set
	 */
	public void setSupportedAPIVersions(String[] supportedAPIVersions) {
		this.supportedAPIVersions = supportedAPIVersions;
	}

	/**
	 * @param RestDependencyInfo to set
	 */
	public void setDbDependency(RestDependencyInfo dbDependency) {
		this.dbDependency = dbDependency;
	}

	/**
	 * @param scmUrl the scmUrl to set
	 */
	public void setScmUrl(String scmUrl) {
		this.scmUrl = scmUrl;
	}

	/**
	 * @param isaacVersion the isaacVersion to set
	 */
	public void setIsaacVersion(String isaacVersion) {
		this.isaacVersion = isaacVersion;
	}

	/**
	 * @param isaacGuiVersion the isaacGuiVersion to set
	 */
	public void setIsaacGuiVersion(String isaacGuiVersion) {
		this.isaacGuiVersion = isaacGuiVersion;
	}

	/**
	 * @param assemblyVersion the assemblyVersion to set
	 */
	public void setAssemblyVersion(String assemblyVersion) {
		this.assemblyVersion = assemblyVersion;
	}

	/**
	 * @param metadataVersion the metadataVersion to set
	 */
	public String setMetadataVersion(String metadataVersion) {
		return this.metadataVersion = metadataVersion;
	}

	/**
	 * @return the dbDependencies
	 */
	public List<RestDependencyInfo> getDbDependencies() {
		return dbDependencies;
	}

	/**
	 * @param dbDependencies the dbDependencies to set
	 */
	public void addDbDependency(RestDependencyInfo dbDependency) {
		this.dbDependencies.add(dbDependency);
	}

	/**
	 * @return the dbLicenses
	 */
	public List<RestLicenseInfo> getDbLicenses() {
		return dbLicenses;
	}

	/**
	 * @param dbLicenses the dbLicenses to set
	 */
	public void addDbLicense(RestLicenseInfo dbLicense) {
		this.dbLicenses.add(dbLicense);
	}

	/**
	 * @return the appLicenses
	 */
	public List<RestLicenseInfo> getAppLicenses() {
		return appLicenses;
	}

	/**
	 * @param appLicenses the appLicenses to set
	 */
	public void addAppLicense(RestLicenseInfo appLicense) {
		this.appLicenses.add(appLicense);
	}
}
