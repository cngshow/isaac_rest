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
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * {@link RestSememeBaseCreate}
 *
 * This stub class is used for callers to create {@link RestSememeVersion} objects.  This class, in combination with {@link RestSememeBase} 
 * contains the fields that can be populated for creation.  
 * 
 * The API never returns this class.

 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestSememeBaseCreate.class)
public class RestSememeBaseCreate extends RestSememeBase
{
	protected RestSememeBaseCreate()
	{
		//for jaxb
	}
	
	/**
	 * The concept sequence, nid or UUID that identifies the concept that defined the assemblage of this sememe.
	 * This is effectively the type of the sememe being created.
	 */
	@XmlElement
	@JsonInclude
	public String assemblageConcept;
	
	/**
	 * The nid or UUID (may NOT be a sequence) of desired referenced component of the sememe instance. 
	 */
	@XmlElement
	@JsonInclude
	public String referencedComponent;
}
