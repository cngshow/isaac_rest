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
package gov.vha.isaac.rest.api1.data.search;

import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.enumerations.IdType;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestSearchResult}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSearchResult
{
	private static Logger log = LogManager.getLogger();
	
	/**
	 * The internal identifier of the sememe that matched the query
	 */
	@XmlElement 
	Integer matchNid;
	
	/**
	 * The text of the description that matched the query (may be blank, if the description is not available/active on the path used to populate this)
	 */
	
	@XmlElement 
	String matchText;
	
	/**
	 * The Lucene Score for this result.  This value is only useful for ranking search results relative to other search results within the SAME QUERY 
	 * execution.  It may not be used to rank one query against another.
	 */
	@XmlElement 
	float score;
	
	/**
	 * Returns true if the sememe that matched the query is active (with the specified of default stamp, 
	 * false if inactive.
	 */
	@XmlElement 
	boolean active;
	
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The (optionally) populated identifiers (UUIDs) of the sememe that matched the query.  Must pass expand='uuid' to have
	 * this populated.
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	/**
	 * The (optionally) populated concept that is referenced (indirectly) by the sememe that matched the query.  This is calculated by
	 * looking up the sememe of the matchNid, and then getting the referenced component of that sememe.  If the referenced component 
	 * is a concept, that is the concept that is returned.  If the referenced component is a sememe, then the process is repeated (following
	 * the referenced component reference of the sememe) - continuing until a concept is found.  If an (unusual) case occurs where the 
	 * sememe chain doesn't lead to a concept, this will not be populated.  This is populated by passing the expand parameter 'referencedConcept'.
	 * If this is passed, you may also (optionally) pass the parameters 'versionsLatestOnly' or 'versionsAll'
	 */
	@XmlElement
	RestConceptChronology referencedConcept;

	protected RestSearchResult()
	{
		//for Jaxb
	}

	public RestSearchResult(int matchNid, String matchText, float score, State state)
	{
		this.matchNid = matchNid;
		this.matchText = matchText;
		this.score = score;
		this.active = (state == State.ACTIVE);
		
		expandables = new Expandables();
		if (RequestInfo.get().shouldExpand(ExpandUtil.uuid))
		{
			Optional<? extends ObjectChronology<? extends StampedVersion>> object = Get.identifiedObjectService()
					.getIdentifiedObjectChronology(matchNid);
			if (object.isPresent())
			{
				identifiers = new RestIdentifiedObject(object.get().getUuidList());
			}
			else
			{
				log.warn("Couldn't identify UUID for matchNid " + matchNid);
			}
		}
		else
		{
			identifiers = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables.add(new Expandable(ExpandUtil.uuid, RestPaths.idAppPathComponent + RestPaths.idTranslateComponent + matchNid + "?inputType=" 
						+ IdType.NID.getDisplayName() + "&outputType=" + IdType.UUID.getDisplayName()));
			}
		}
		
		if (RequestInfo.get().shouldExpand(ExpandUtil.referencedConcept))
		{
			int conceptSequence = findConcept(matchNid);
			if (conceptSequence >= 0)
			{
				referencedConcept = new RestConceptChronology(Get.conceptService().getConcept(conceptSequence), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));
				if (RequestInfo.get().returnExpandableLinks())
				{
					if (!RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable))
					{
						expandables.add(new Expandable(ExpandUtil.versionsAllExpandable, RestPaths.conceptVersionAppPathComponent + conceptSequence + "?expand=" 
								+ ExpandUtil.versionsAllExpandable));
					}
					if (!RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable))
					{
						expandables.add(
								new Expandable(
										ExpandUtil.versionsLatestOnlyExpandable,
										RestPaths.conceptVersionAppPathComponent + conceptSequence + "?expand=" 
												+ ExpandUtil.versionsLatestOnlyExpandable
												+ "&" + RequestParameters.coordToken + "=" + RequestInfo.get().getCoordinatesToken().getSerialized()));
					}
				}
			}
		}
		else 
		{
			referencedConcept = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				//This is expensive to calculate, not going to support it as a convenience at this time.
				expandables.add(new Expandable(ExpandUtil.referencedConcept, ""));
				//two other variations (that depend on this)
				expandables.add(new Expandable(ExpandUtil.versionsLatestOnlyExpandable, ""));
				expandables.add(new Expandable(ExpandUtil.versionsAllExpandable, ""));
			}
		}
		
		if (expandables.size() == 0)
		{
			expandables = null;
		}
	}
	
	/**
	 * Returns a concept sequence, or -1 if no concept found
	 */
	private int findConcept(int nid)
	{
		Optional<? extends ObjectChronology<? extends StampedVersion>> c = Get.identifiedObjectService().getIdentifiedObjectChronology(nid);
		
		if (c.isPresent())
		{
			if (c.get().getOchreObjectType() == OchreExternalizableObjectType.SEMEME)
			{
				return findConcept(((SememeChronology<?>)c.get()).getReferencedComponentNid());
			}
			else if (c.get().getOchreObjectType() == OchreExternalizableObjectType.CONCEPT)
			{
				return ((ConceptChronology<?>)c.get()).getConceptSequence();
			}
			else
			{
				log.warn("Unexpected object type: " + c.get().getOchreObjectType());
			}
		}
		return -1;
	}
}
