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
import gov.vha.isaac.ochre.api.util.metainf.MavenLicenseInfo;

/**
 * {@link RestLicenseInfo}
 * 
 * This class carries license information
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestLicenseInfo
{
	/**
	 * Name of the license
	 */
	@XmlElement
	public String name;
	
	/**
	 * URL to the license text
	 */
	@XmlElement
	public String url;
	
	/**
	 * Comments related to the license
	 */
	@XmlElement
	public String comments;
	
	public RestLicenseInfo()
	{
		//For jaxb
	}

	/**
	 * @param name of the license
	 * @param url of the license text
	 * @param comments related to the license
	 */
	public RestLicenseInfo(String name, String url, String comments) {
		super();
		this.name = name;
		this.url = url;
		this.comments = comments;
	}

	/**
	 * @param mli
	 */
	public RestLicenseInfo(MavenLicenseInfo mli)
	{
		super();
		this.name = mli.name;
		this.url = mli.url;
		this.comments = mli.comments;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestLicenseInfo [name=" + name + ", url=" + url + ", comments=" + comments + "]";
	}
}
