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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;

/**
 * 
 * {@link RestConceptNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * A REST logic graph node containing a concept
 */
@XmlRootElement
public class RestConceptNode extends RestLogicNode {

	/**
	 * The int sequence of the concept referred to by this REST logic graph node
	 */
	@XmlElement
	int conceptSequence;

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
	}
	/**
	 * @param conceptNodeWithUuids
	 */
	public RestConceptNode(ConceptNodeWithUuids conceptNodeWithUuids) {
		super(conceptNodeWithUuids);
		conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptNodeWithUuids.getConceptUuid());
		conceptDescription = Get.conceptDescriptionText(conceptSequence);
	}
}