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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.data.association;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * {@link RestAssociationTypeVersionBaseCreate}
 * This stub class is used by callers to create {@link RestAssociationTypeVersion} objects.  Typically, this class would be combined with a RestAssociationTypeVersionBase
 * class - but that doesn't yet exist at this time, as there are currently no editable fields in the association type.  Callers can edit the underlying concept that 
 * represents the association directly, if they choose.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestAssociationTypeVersionBaseCreate
{
	/**
	 * The best (primary) name of the association, per the user specified stamp coordinates.
	 * This would typically be a value like "broader than"
	 */
	@XmlElement
	@JsonInclude
	public String associationName;
	
	/**
	 * The inverse name (if any) of the association, per the user specified stamp coordinates.  This optional
	 * field may not be present.  This would typically be a value like "narrower than"
	 */
	@XmlElement
	@JsonInclude
	public String associationInverseName;

	/**
	 * The description of the purpose of this association.
	 */
	@XmlElement
	@JsonInclude
	public String description;
}
