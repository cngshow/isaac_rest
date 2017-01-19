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

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingSetExtensionValueCreate}
 * 
 * This stub class is used for callers as part of creating {@link RestMappingSetExtensionValue} objects.  This, combined with {@link RestMappingSetExtensionValueUpdate}
 * contains the fields that may be set during the initial create. 
 * 
 * In practice, for this API, Create and Update are identical - there are no fields that may not be updated, as this extension is being stored 
 * in a way that doesn't fit our normal patterns.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetExtensionValueCreate.class)
public class RestMappingSetExtensionValueCreate extends RestMappingSetExtensionValueUpdate
{
	public RestMappingSetExtensionValueCreate()
	{
		//for Jaxb
		super();
	}
	
	public RestMappingSetExtensionValueCreate(String extensionNameConcept, RestDynamicSememeData extensionValue)
	{
		super(extensionNameConcept, extensionValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetExtensionValueCreate [extensionNameConcept=" + extensionNameConcept + ", extensionValue="
				+ extensionValue + "]";
	}
}
