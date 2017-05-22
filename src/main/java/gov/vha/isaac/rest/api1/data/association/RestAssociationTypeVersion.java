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

import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.associations.AssociationType;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * {@link RestAssociationTypeVersion}
 * Carries the definition of an Association in the system.  
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestAssociationTypeVersion.class)
public class RestAssociationTypeVersion extends RestAssociationTypeVersionCreate
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Expandables expandables;
	
	/**
	 * The identifiers of the concept that represents the association definition
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this association type
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestStampedVersion associationItemStamp;
	
	/**
	 * The Concept Chronology of the concept represented by associationConceptSequence.  Typically blank, unless requested via the expand parameter
	 * 'referencedConcept'  If 'referencedConcept' is passed, you can also pass 'versionsAll' or 'versionsLatestOnly'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestConceptChronology associationConcept;

	protected RestAssociationTypeVersion()
	{
		//for jaxb
	}
	/**
	 * @param read
	 */
	public RestAssociationTypeVersion(AssociationType read, UUID processId)
	{
		//TODO the way that the AssociationType is constructed, it isn't paying attention to language or FSN vs Synonym prefs.  This should be fixed...
		associationName = read.getAssociationName();
		associationInverseName = read.getAssociationInverseName().orElse(null);
		description = read.getDescription();
		identifiers = new RestIdentifiedObject(read.getAssociationTypeConcept());
		
		if (RequestInfo.get().shouldExpand(ExpandUtil.referencedConcept))
		{
			associationConcept = new RestConceptChronology(read.getAssociationTypeConcept(), 
					RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
					true,
					processId);
		}
		else 
		{
			associationConcept = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables = new Expandables();
				expandables.add(new Expandable(ExpandUtil.referencedConcept, RestPaths.conceptChronologyAppPathComponent 
						+ read.getAssociationTypeConcept().getConceptSequence()));
			}
		}
	}
}
