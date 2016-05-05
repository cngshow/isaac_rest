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
	@XmlElement
	public String groupId;
	
	@XmlElement
	public String artifactId;

	@XmlElement
	public String version;
	
	@XmlElement
	public String classifier;
	
	@XmlElement
	public String type;
	
	public RestDependencyInfo()
	{
		//For jaxb
	}

	/**
	 * @param groupId
	 * @param artifactId
	 * @param version
	 * @param classifier
	 * @param type
	 */
	public RestDependencyInfo(String groupId, String artifactId, String version, String classifier, String type) {
		super();
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.classifier = classifier;
		this.type = type;
	}
}
