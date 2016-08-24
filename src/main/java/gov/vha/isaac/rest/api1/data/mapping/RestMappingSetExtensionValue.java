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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingSetExtensionValue}
 * 
 * This stub class is used for callers as part of creating {@link RestMappingSetVersion} objects.  This, combined with {@link RestMappingSetExtensionValueBase}
 * contains the fields that may be set during the initial create. 
 * after creation.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingSetExtensionValue extends RestMappingSetExtensionValueBaseCreate
{
	/**
	 * The selected description of the extensionNameConcept that describes the purpose of this extended field on a map set definition.  
	 * This is provided as a convenience on read.  On create, this field should not be populated, and is ignored.
	 */
	@XmlElement
	public String extensionNameConceptDescription;
	
	public RestMappingSetExtensionValue()
	{
		//for Jaxb
		super();
	}
	
	public RestMappingSetExtensionValue(int extensionNameConcept, RestDynamicSememeData extensionValue)
	{
		this.extensionNameConcept = extensionNameConcept;
		this.extensionValue = extensionValue;
		this.extensionNameConceptDescription = Util.readBestDescription(extensionNameConcept);
	}
}
