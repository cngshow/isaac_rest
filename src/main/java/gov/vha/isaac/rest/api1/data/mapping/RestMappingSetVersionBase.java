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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestMappingSetVersionBase}
 * This stub class is used for callers to edit {@link RestMappingSetVersion} objects.  It only contains the fields that may be edited after creation.
 * 
 * The API never returns this class.
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetVersionBase.class)
public class RestMappingSetVersionBase
{
	
	/**
	 * The primary name of this map set.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String name;
	
	/**
	 * The (optional) inverse name of this map set.  Used when a map set is of the pattern:
	 * ingredient-of <--> has-ingredient 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String inverseName;
	
	/**
	 * The description of this map set
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String description;
	
	/**
	 * The (optional) purpose of this map set - or extended description of this map set.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String purpose;
	
	/**
	 * True to indicate the mapping set should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
	

	protected RestMappingSetVersionBase()
	{
		//for Jaxb
	}

	/**
	 * @param name
	 * @param inverseName
	 * @param description
	 * @param purpose
	 */
	public RestMappingSetVersionBase(String name, String inverseName, String description, String purpose) {
		super();
		this.name = name;
		this.inverseName = inverseName;
		this.description = description;
		this.purpose = purpose;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingSetVersionBase [name=" + name + ", inverseName=" + inverseName + ", description="
				+ description + ", purpose=" + purpose + "]";
	}
}
