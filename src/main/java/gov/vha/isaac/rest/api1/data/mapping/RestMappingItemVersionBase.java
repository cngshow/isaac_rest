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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;

/**
 * 
 * {@link RestMappingItemVersionBase}
 * This stub class carries shared type information for reuse.
 * 
 * The API never returns this class.

 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemVersionBase.class)
public class RestMappingItemVersionBase
{
	/**
	 * The (optional) extended fields which carry additional information about this map item.  For details on these fields, read 
	 * the info returned as part of the {@link RestMappingSetVersion#mapItemFieldsDefinition} field
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestDynamicSememeData> mapItemExtendedFields;
	
	protected RestMappingItemVersionBase()
	{
		//for Jaxb
	}

	/**
	 * @param mapItemExtendedFields
	 */
	public RestMappingItemVersionBase(List<RestDynamicSememeData> mapItemExtendedFields) 
	{
		super();
		this.mapItemExtendedFields = mapItemExtendedFields;
	}
}
