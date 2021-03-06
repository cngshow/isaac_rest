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
 * {@link RestAssociationItemVersionCreate}
 * This stub class is used by callers to create {@link RestAssociationItemVersion} objects.  This class, in combination with {@link RestAssociationItemVersionUpdate}
 * contains the fields that can be populated for creation.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestAssociationItemVersionCreate.class)
public class RestAssociationItemVersionCreate extends RestAssociationItemVersionUpdate
{
	/**
	 * The concept uuid, nid or sequence of the association type
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String associationType;
	
	/**
	 * The nid or UUID of the source item in the association.  Typically this is a concept, but it may also be a sememe.
	 * Sequences are not allowed here.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String sourceId;
	
	protected RestAssociationItemVersionCreate()
	{
		//for jaxb
	}
	
	public RestAssociationItemVersionCreate(int associationTypeSequence, int sourceNid)
	{
		this.associationType = associationTypeSequence + "";
		this.sourceId = sourceNid + "";
	}
}
