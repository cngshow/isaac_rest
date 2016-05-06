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
package gov.vha.isaac.rest.api1.data.systeminfo;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * {@link RestDependencyInfo}
 * 
 * This class carries Maven dependency information
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestDependencyInfo
{
	/**
	 * Maven Dependency Group ID
	 */
	@XmlElement
	public String groupId;
	
	/**
	 * Maven Dependency Artifact ID
	 */
	@XmlElement
	public String artifactId;

	/**
	 * Maven Dependency Version
	 */
	@XmlElement
	public String version;
	
	/**
	 * Maven Dependency Classifier
	 */
	@XmlElement
	public String classifier;
	
	/**
	 * Maven Dependency Type
	 */
	@XmlElement
	public String type;
	
	public RestDependencyInfo()
	{
		//For jaxb
	}

	/**
	 * @param groupId Maven Dependency Group ID
	 * @param artifactId Maven Dependency Artifact ID
	 * @param version Maven Dependency Version
	 * @param classifier Maven Dependency Classifier
	 * @param type Maven Dependency Type
	 */
	public RestDependencyInfo(String groupId, String artifactId, String version, String classifier, String type) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestDependencyInfo [groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version
				+ ", classifier=" + classifier + ", type=" + type + "]";
	}
}
