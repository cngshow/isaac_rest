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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.associations.AssociationInstance;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * {@link RestAssociationItemVersion}
 * Represents an association between two components - essentially, a triplet of Source -> Type -> Target
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */

@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestAssociationItemVersion extends RestAssociationItemVersionBaseCreate
{
	private static Logger log = LogManager.getLogger();
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The concept sequence of the association type
	 */
	@XmlElement
	int associationTypeSequence;
	
	/**
	 * The sememe UUID(s) of the sememe that represents this association
	 */
	@XmlElement
	RestIdentifiedObject identifiers; 
	
	/**
	 * The StampedVersion details for this association entry
	 */
	@XmlElement
	RestStampedVersion associationItemStamp;
	
	/**
	 * The Concept Chronology of the concept represented by sourceNid - if the the sourceNid represents a concept.  Blank, unless requested via the expand parameter
	 * 'source' and the nid represents a concept.  If 'source' is passed, you can also pass 'versionsAll' or 'versionsLatestOnly'
	 */
	@XmlElement
	public RestConceptChronology sourceConcept;
	
	/**
	 * The Sememe Chronology of the sememe represented by sourceNid - if the sourceNid represents a sememe.  Blank, unless requested via the expand parameter
	 * 'source' and the nid represents a sememe.  If 'source' is passed, you can also pass 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 */
	@XmlElement
	public RestSememeChronology sourceSememe;
	
	/**
	 * The Concept Chronology of the concept represented by sourceNid.  Typically blank, unless requested via the expand parameter
	 * 'target'  If 'target' is passed, you can also pass 'versionsAll' or 'versionsLatestOnly'
	 */
	@XmlElement
	public RestConceptChronology targetConcept;
	
	/**
	 * The Sememe Chronology of the sememe represented by targetNid - if the targetNid represents a sememe.  Blank, unless requested via the expand parameter
	 * 'target' and the nid represents a sememe.  If 'target' is passed, you can also pass 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 */
	@XmlElement
	public RestSememeChronology targetSememe;
	
	/**
	 * The nested sememes (if any) attached to this association.  Not populated by default, include expand=nestedSememes to expand these.  When 'nestedSememes' is passed,
	 * you can also pass 'referencedDetails' and 'chronology'
	 */
	@XmlElement
	RestSememeVersion[] nestedSememes;

	protected RestAssociationItemVersion()
	{
		//for jaxb
	}
	
	/**
	 * @param read
	 * @throws RestException 
	 */
	public RestAssociationItemVersion(AssociationInstance read) throws RestException
	{
		associationTypeSequence = read.getAssociationTypeSequenece();
		identifiers = new RestIdentifiedObject(read.getData().getUuidList());
		associationItemStamp = new RestStampedVersion(read.getData());
		sourceNid = read.getSourceComponent().getNid();
		targetNid = read.getTargetComponent().isPresent() ? read.getTargetComponent().get().getNid() : null;
		
		sourceConcept = null;
		sourceSememe = null;
		targetConcept = null;
		targetSememe = null;
		nestedSememes = null;

		if (RequestInfo.get().shouldExpand(ExpandUtil.source) || RequestInfo.get().shouldExpand(ExpandUtil.target) 
				|| RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable))
		{
			expandables = new Expandables();
			if (RequestInfo.get().shouldExpand(ExpandUtil.source))
			{
				if (Get.identifierService().getChronologyTypeForNid(sourceNid) == ObjectChronologyType.CONCEPT)
				{
					sourceConcept = new RestConceptChronology(Get.conceptService().getConcept(sourceNid), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));
				}
				else if (Get.identifierService().getChronologyTypeForNid(sourceNid) == ObjectChronologyType.SEMEME)
				{
					sourceSememe = new RestSememeChronology(Get.sememeService().getSememe(sourceNid), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
				}
				else
				{
					log.error("Unexpected object type for source nid: " + sourceNid);
				}
			}
			
			if (RequestInfo.get().shouldExpand(ExpandUtil.target) && targetNid != null)
			{
				if (Get.identifierService().getChronologyTypeForNid(targetNid) == ObjectChronologyType.CONCEPT)
				{
					targetConcept = new RestConceptChronology(Get.conceptService().getConcept(targetNid), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));
				}
				else if (Get.identifierService().getChronologyTypeForNid(targetNid) == ObjectChronologyType.SEMEME)
				{
					targetSememe = new RestSememeChronology(Get.sememeService().getSememe(targetNid), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
				}
				else
				{
					log.error("Unexpected object type for source nid: " + targetNid);
				}
			}
			
			if (RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable))
			{
				nestedSememes = SememeAPIs.get(identifiers.getFirst().toString(), null, RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), true, 
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails), true);
			}
			if (expandables.size() == 0)
			{
				expandables = null;
			}
		}
		buildMissingExpandables();
	}
	
	private void buildMissingExpandables()
	{
		if (RequestInfo.get().returnExpandableLinks())
		{
			if (expandables == null)
			{
				expandables = new Expandables();
			}
			if (!RequestInfo.get().shouldExpand(ExpandUtil.source))
			{
				if (Get.identifierService().getChronologyTypeForNid(sourceNid) == ObjectChronologyType.CONCEPT)
				{
					expandables.add(new Expandable(ExpandUtil.source, RestPaths.conceptChronologyAppPathComponent   + sourceNid));
				}
				else if (Get.identifierService().getChronologyTypeForNid(sourceNid) == ObjectChronologyType.SEMEME)
				{
					expandables.add(new Expandable(ExpandUtil.source, RestPaths.sememeChronologyAppPathComponent + sourceNid));
				}
			}
			if (!RequestInfo.get().shouldExpand(ExpandUtil.target))
			{
				if (Get.identifierService().getChronologyTypeForNid(targetNid) == ObjectChronologyType.CONCEPT)
				{
					expandables.add(new Expandable(ExpandUtil.target, RestPaths.conceptChronologyAppPathComponent   + targetNid));
				}
				else if (Get.identifierService().getChronologyTypeForNid(targetNid) == ObjectChronologyType.SEMEME)
				{
					expandables.add(new Expandable(ExpandUtil.target, RestPaths.sememeChronologyAppPathComponent + targetNid));
				}
			}
			if (!RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable))
			{
				expandables.add(new Expandable(ExpandUtil.nestedSememesExpandable, RestPaths.sememeAPIsPathComponent + RestPaths.byReferencedComponentComponent 
						+ identifiers.getFirst().toString()));
			}
		}
	}
}
