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
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;

/**
 * 
 * {@link RestSememeChronology}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestSememeChronology
{
	
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The sememe sequence identifier of this sememe instance
	 */
	@XmlElement
	int sememeSequence;
	
	/**
	 * The concept sequence identifier of the concept that represents the type of this sememe
	 */
	@XmlElement
	int assemblageSequence;
	
	/**
	 * The NID identifier of the object that is referenced by this sememe instance.  This could represent a concept or a sememe.
	 */
	@XmlElement
	int referencedComponentNid;
	
	/**
	 * The permanent identifier object(s) attached to this sememe instance
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	protected RestSememeChronology()
	{
		//For Jaxb
	}

	@SuppressWarnings("rawtypes") 
	public RestSememeChronology(SememeVersion sv)
	{
		identifiers = new RestIdentifiedObject(sv.getChronology().getUuidList());
		sememeSequence = sv.getSememeSequence();
		assemblageSequence = sv.getAssemblageSequence();
		referencedComponentNid = sv.getReferencedComponentNid();
	}
}
