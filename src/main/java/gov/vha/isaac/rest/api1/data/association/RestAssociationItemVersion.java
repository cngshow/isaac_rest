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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestAssociationItemVersion.class)
public class RestAssociationItemVersion 
{
	private static Logger log = LogManager.getLogger();
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Expandables expandables;
	
	/**
	 * The target item in the association.  Typically this is a concept, but it may also be a sememe.  Note that 
	 * this may be null, in the case where the association intends to represent that no target is available for a particular 
	 * association type and source component.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject targetId;
	
	
	/**
	 * The concept identifiers of the association type
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject associationType;
	
	/**
	 * The identifiers of the source item in the association.  Typically this is a concept, but it may also be a sememe.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject sourceId;
	
	/**
	 * The sememe identifiers of the sememe that represents this association
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject identifiers; 
	
	/**
	 * The StampedVersion details for this association entry
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestStampedVersion associationItemStamp;
	
	/**
	 * The Concept Chronology of the concept represented by sourceNid - if the the sourceNid represents a concept.  Blank, unless requested via the expand parameter
	 * 'source' and the nid represents a concept.  If 'source' is passed, you can also pass 'versionsAll' or 'versionsLatestOnly'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestConceptChronology sourceConcept;
	
	/**
	 * The Sememe Chronology of the sememe represented by sourceNid - if the sourceNid represents a sememe.  Blank, unless requested via the expand parameter
	 * 'source' and the nid represents a sememe.  If 'source' is passed, you can also pass 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestSememeChronology sourceSememe;
	
	/**
	 * The Concept Chronology of the concept represented by sourceNid.  Typically blank, unless requested via the expand parameter
	 * 'target'  If 'target' is passed, you can also pass 'versionsAll' or 'versionsLatestOnly'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestConceptChronology targetConcept;
	
	/**
	 * The Sememe Chronology of the sememe represented by targetNid - if the targetNid represents a sememe.  Blank, unless requested via the expand parameter
	 * 'target' and the nid represents a sememe.  If 'target' is passed, you can also pass 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestSememeChronology targetSememe;
	
	/**
	 * The nested sememes (if any) attached to this association.  Not populated by default, include expand=nestedSememes to expand these.  When 'nestedSememes' is passed,
	 * you can also pass 'referencedDetails' and 'chronology'
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestSememeVersion[] nestedSememes;

	protected RestAssociationItemVersion()
	{
		//for jaxb
	}
	
	/**
	 * @param read
	 * @throws RestException 
	 */
	public RestAssociationItemVersion(AssociationInstance read, UUID processId) throws RestException
	{
		associationType = new RestIdentifiedObject(read.getAssociationTypeSequenece(), ObjectChronologyType.CONCEPT);
		identifiers = new RestIdentifiedObject(read.getData().getChronology());
		associationItemStamp = new RestStampedVersion(read.getData());
		sourceId = new RestIdentifiedObject(read.getSourceComponent());
		targetId = read.getTargetComponent().isPresent() ? new RestIdentifiedObject(read.getTargetComponent().get()) : null;
		
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
				if (sourceId.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
				{
					sourceConcept = new RestConceptChronology(Get.conceptService().getConcept(sourceId.sequence), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
							true,
							processId);
				}
				else if (sourceId.type.enumId == ObjectChronologyType.SEMEME.ordinal())
				{
					sourceSememe = new RestSememeChronology(Get.sememeService().getSememe(sourceId.sequence), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
							processId);
				}
				else
				{
					log.error("Unexpected object type for source nid: " + sourceId.nid);
				}
			}
			
			if (RequestInfo.get().shouldExpand(ExpandUtil.target) && targetId != null)
			{
				if (targetId.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
				{
					targetConcept = new RestConceptChronology(Get.conceptService().getConcept(targetId.sequence), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
							true,
							processId);
				}
				else if (targetId.type.enumId == ObjectChronologyType.SEMEME.ordinal())
				{
					targetSememe = new RestSememeChronology(Get.sememeService().getSememe(targetId.sequence), 
							RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), 
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
							processId);
				}
				else
				{
					log.error("Unexpected object type for target nid: " + targetId.nid);
				}
			}
			
			if (RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable))
			{
				nestedSememes = SememeAPIs.get(identifiers.getFirst().toString(), null, null, RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), true, 
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails), true, true, true, processId);
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
				if (sourceId.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
				{
					expandables.add(new Expandable(ExpandUtil.source, RestPaths.conceptChronologyAppPathComponent   + sourceId.nid));
				}
				else if (sourceId.type.enumId == ObjectChronologyType.SEMEME.ordinal())
				{
					expandables.add(new Expandable(ExpandUtil.source, RestPaths.sememeChronologyAppPathComponent + sourceId.nid));
				}
			}
			if (!RequestInfo.get().shouldExpand(ExpandUtil.target) && targetId != null)
			{
				if (targetId.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
				{
					expandables.add(new Expandable(ExpandUtil.target, RestPaths.conceptChronologyAppPathComponent   + targetId.nid));
				}
				else if (targetId.type.enumId == ObjectChronologyType.SEMEME.ordinal())
				{
					expandables.add(new Expandable(ExpandUtil.target, RestPaths.sememeChronologyAppPathComponent + targetId.nid));
				}
			}
			if (!RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable))
			{
				expandables.add(new Expandable(ExpandUtil.nestedSememesExpandable, RestPaths.sememeAPIsPathComponent + RestPaths.forReferencedComponentComponent 
						+ identifiers.getFirst().toString()));
			}
		}
	}
}
