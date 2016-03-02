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
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
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
@XmlRootElement
public class RestConceptNode extends RestLogicNode {

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
		conceptDescription = Get.conceptDescriptionText(conceptSequence);
		
		if (RequestInfo.get().shouldExpand(ExpandUtil.logicNodeConceptVersionsExpandable)) {
			@SuppressWarnings("rawtypes")
			ConceptChronology cc = Get.conceptService().getConcept(conceptSequence);
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> olcv = cc.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			conceptVersion = new RestConceptVersion(olcv.get().value(), true, false, false, RequestInfo.get().useStated());
		} else {
			conceptVersion = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				// TODO make expandables work for logicNodeConceptVersionsExpandable
				// expandables.add(new Expandable(ExpandUtil.logicNodeConceptVersionsExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
	}
	/**
	 * @param conceptNodeWithUuids
	 */
	public RestConceptNode(ConceptNodeWithUuids conceptNodeWithUuids) {
		super(conceptNodeWithUuids);
		conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptNodeWithUuids.getConceptUuid());
		conceptDescription = Get.conceptDescriptionText(conceptSequence);
		
		if (RequestInfo.get().shouldExpand(ExpandUtil.logicNodeConceptVersionsExpandable)) {
			@SuppressWarnings("rawtypes")
			ConceptChronology cc = Get.conceptService().getConcept(conceptSequence);
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<ConceptVersionImpl>> olcv = cc.getLatestVersion(ConceptVersionImpl.class, RequestInfo.get().getStampCoordinate());
			conceptVersion = new RestConceptVersion(olcv.get().value(), true, false, false, RequestInfo.get().useStated());
		} else {
			conceptVersion = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				// TODO make expandables work for logicNodeConceptVersionsExpandable
				// expandables.add(new Expandable(ExpandUtil.logicNodeConceptVersionsExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
	}
}