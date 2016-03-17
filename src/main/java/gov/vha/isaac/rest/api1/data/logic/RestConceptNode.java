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

package gov.vha.isaac.rest.api1.data.logic;

import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;

/**
 * 
 * {@link RestConceptNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * A REST logic graph node containing (referencing) a concept by sequence and its text description.
 * RestConceptNode has RestNodeSemantic. == NodeSemantic.CONCEPT and should never have any child nodes.
 */
public class RestConceptNode extends RestLogicNode {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;

	/**
	 * The int sequence of the concept referred to by this REST logic graph node
	 */
	@XmlElement
	int conceptSequence;
	
	/**
	 * Optionally-expandable RestConceptVersion corresponding to 
	 */
	@XmlElement
	RestConceptVersion conceptVersion;

	/**
	 * A boolean indicating whether the concept referred to by this RestConceptVersion is defined rather than primitive
	 */
	@XmlElement
	boolean isConceptDefined;
	
	/**
	 * The String text description of the concept referred to by this REST logic graph node. It is included as a convenience, as it may be retrieved based on the concept sequence.
	 */
	@XmlElement
	String conceptDescription;
	
	protected RestConceptNode() {
		// For JAXB
	}

	/**
	 * @param conceptNodeWithSequences
	 */
	public RestConceptNode(ConceptNodeWithSequences conceptNodeWithSequences) {
		super(conceptNodeWithSequences);
		conceptSequence = conceptNodeWithSequences.getConceptSequence();
		//TODO - Joel, can't use this method, we aren't setting the stamp details of the ISAAC default stamp stuff
		conceptDescription = Get.conceptDescriptionText(conceptSequence);
		
		try {
			// TODO Fine tune this when data problems resolved
			Optional<SememeChronology<? extends LogicGraphSememe<?>>> lgcOptional = Frills.getLogicGraphChronology(conceptSequence, RequestInfo.get().getStated());
			Optional<LatestVersion<LogicGraphSememe<?>>> lgs = Frills.getLogicGraphVersion(lgcOptional.get(), RequestInfo.get().getStampCoordinate());
			isConceptDefined = Frills.isConceptFullyDefined(lgs.get().value());
		} catch (Exception e) {
			LOG.warn("Problem getting isConceptDefined value (defaulting to false) for ConceptNode with " + Frills.getIdInfo(conceptSequence));
			isConceptDefined = false;
		}
		
		if (RequestInfo.get().shouldExpand(ExpandUtil.versionExpandable)) {
			@SuppressWarnings("rawtypes")
			ConceptChronology cc = Get.conceptService().getConcept(conceptSequence);
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> olcv = cc.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			conceptVersion = new RestConceptVersion(olcv.get().value(), true);
		} else {
			conceptVersion = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				// TODO make expandables work for versionExpandable
				// expandables.add(new Expandable(ExpandUtil.versionExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
	}
	/**
	 * @param conceptNodeWithUuids
	 */
	public RestConceptNode(ConceptNodeWithUuids conceptNodeWithUuids) {
		super(conceptNodeWithUuids);
		conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptNodeWithUuids.getConceptUuid());
		//TODO - Joel, can't use this method, we aren't setting the stamp details of the ISAAC default stamp stuff
		conceptDescription = Get.conceptDescriptionText(conceptSequence);
		try {
			// TODO Fine tune this when data problems resolved
			Optional<SememeChronology<? extends LogicGraphSememe<?>>> lgcOptional = Frills.getLogicGraphChronology(conceptSequence, RequestInfo.get().getStated());
			Optional<LatestVersion<LogicGraphSememe<?>>> lgs = Frills.getLogicGraphVersion(lgcOptional.get(), RequestInfo.get().getStampCoordinate());
			isConceptDefined = Frills.isConceptFullyDefined(lgs.get().value());
		} catch (Exception e) {
			LOG.warn("Problem getting isConceptDefined value (defaulting to false) for ConceptNode with " + Frills.getIdInfo(conceptSequence));
			isConceptDefined = false;
		}

		if (RequestInfo.get().shouldExpand(ExpandUtil.versionExpandable)) {
			@SuppressWarnings("rawtypes")
			ConceptChronology cc = Get.conceptService().getConcept(conceptSequence);
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> olcv = cc.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			conceptVersion = new RestConceptVersion(olcv.get().value(), true);
		} else {
			conceptVersion = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				// TODO make expandables work for versionExpandable
				// expandables.add(new Expandable(ExpandUtil.versionExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
	}
}