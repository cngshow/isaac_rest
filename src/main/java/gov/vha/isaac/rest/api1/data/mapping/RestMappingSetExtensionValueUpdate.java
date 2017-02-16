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
package gov.vha.isaac.rest.api1.data.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingSetExtensionValueUpdate}
 * 
 * This stub class is used for callers as part of updating {@link RestMappingSetExtensionValue} objects.  This, combined with {@link RestMappingSetExtensionValueBase}
 * contains the fields that may be set during an update. 
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetExtensionValueUpdate.class)
public class RestMappingSetExtensionValueUpdate extends RestMappingSetExtensionValueBase
{
	/**
	 * The concept (uuid, nid or sequence) that describes the purpose of this extended field on a map set definition.  The descriptions from this concept
	 * will be used as the label of the extension.
	 */
	@XmlElement
	public String extensionNameConcept;

	public RestMappingSetExtensionValueUpdate()
	{
		//for Jaxb
		super();
	}
	
	public RestMappingSetExtensionValueUpdate(String extensionNameConcept, RestDynamicSememeData extensionValue, Boolean active)
	{
		super(extensionValue, (active != null) ? active : true);
		this.extensionNameConcept = extensionNameConcept;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetExtensionValueUpdate "
				+ "["
				+ "extensionNameConcept=" + extensionNameConcept
				+ ", extensionValue=" + extensionValue
				+ ", active=" + active
				+ "]";
	}
}
