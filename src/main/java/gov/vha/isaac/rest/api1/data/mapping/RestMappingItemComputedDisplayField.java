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
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;

/**
 * 
 * {@link RestMappingItemComputedDisplayField}
 * 
 * This, combined with {@link RestMappingSetDisplayFieldBase} returns a subset of information about fields, on an map set item level.
 * This class only carries enough information to link this computed field back to the full display field order specification provided
 * in the MapSet itself - the linkage is by the id field from this class, to the id field in {@link RestMappingSetDisplayField}
 * 
 * This class is only returned within item level object, and is only returned for COMPUTED fields.  The value calulated by the computation
 * is returned in the value attribute.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingItemComputedDisplayField.class)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
public class RestMappingItemComputedDisplayField extends RestMappingSetDisplayFieldBase
{
	/**
	 * In cases where this field represents a text description value for a calculated item such as source, target, or qualifier, 
	 * this will contain the value to display.   This field should always be populated.  This entire object will only be returned 
	 * for computed fields that have a value.
	 */
	@XmlElement
	public String value;
	
	//for Jaxb
	protected RestMappingItemComputedDisplayField()
	{
		super();
	}
	
	public RestMappingItemComputedDisplayField(String id, MapSetItemComponent component, String value) throws RestException
	{
		super(id, component);
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetDisplayField [id=" + id + ", componentType=" + componentType + ", value=" + value + "]";
	}
}
