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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;

/**
 * 
 * {@link RestMappingItemDisplayField}
 * 
 * This class is used to convey available mapping set display fields.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemDisplayField.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingItemDisplayField extends RestMappingSetDisplayField
{
	RestMappingItemDisplayField()
	{
		//for Jaxb
		super();
	}

	public RestMappingItemDisplayField(String name, MapSetItemComponent component) throws RestException
	{
		//for Jaxb
		this(name, (IdentifiedObject)null, component, (String)null);
	}
	public RestMappingItemDisplayField(String name, IdentifiedObject concept, MapSetItemComponent component, String description) throws RestException
	{
		//for Jaxb
		super(name, concept, component, description);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayField [id=" + id + ", componentType=" + componentType + ", description="
				+ description + ", fieldNameConceptIdentifiers=" + fieldNameConceptIdentifiers + "]";
	}
}
