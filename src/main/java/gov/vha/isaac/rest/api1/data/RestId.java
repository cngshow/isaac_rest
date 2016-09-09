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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api1.data.enumerations.IdType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSupportedIdType;

/**
 * {@link RestId}
 * 
 * This class carries back id information from the /id/translate/ APIs.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestId
{
	/**
	 * The id value
	 */
	@XmlElement
	public String value;
	
	/**
	 * The id type
	 */
	@XmlElement
	public RestSupportedIdType idType;
	
	public RestId(IdType type, String value)
	{
		this.value = value;
		this.idType = new RestSupportedIdType(type);
	}
	
	protected RestId()
	{
		//For jaxb
	}
	
}
