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
import javax.xml.bind.annotation.XmlSeeAlso;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.node.external.TypedNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.TypedNodeWithSequences;

/**
 * 
 * {@link RestTypedConnectorNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * RestTypedConnectorNode derived classes must have exactly 1 child node.
 * 
 * RestTypedConnectorNode is the abstract base class for logic graph nodes
 * containing a connector type specified by connectorTypeConceptSequence
 * and described by connectorTypeConceptDescription
 */
@XmlSeeAlso({RestFeatureNode.class,RestRoleNode.class})
@XmlRootElement
public abstract class RestTypedConnectorNode extends RestConnectorNode {
	/**
	 * RestTypedConnectorNode contains an int connectorTypeConceptSequence identifying a connector type concept 
	 */
	@XmlElement
	int connectorTypeConceptSequence;
	
	/**
	 * RestTypedConnectorNode contains a String connectorTypeConceptDescription describing a connector type concept 
	 */
	@XmlElement
	String connectorTypeConceptDescription;

	protected RestTypedConnectorNode() {
		// For JAXB
	}
	/**
	 * @param typedNodeWithSequences
	 */
	public RestTypedConnectorNode(TypedNodeWithSequences typedNodeWithSequences) {
		super(typedNodeWithSequences);
		connectorTypeConceptSequence = typedNodeWithSequences.getTypeConceptSequence();
		connectorTypeConceptDescription = Get.conceptDescriptionText(connectorTypeConceptSequence);
	}
	/**
	 * @param typedNodeWithUuids
	 */
	public RestTypedConnectorNode(TypedNodeWithUuids typedNodeWithUuids) {
		super(typedNodeWithUuids);
		connectorTypeConceptSequence = Get.identifierService().getConceptSequenceForUuids(typedNodeWithUuids.getTypeConceptUuid());
		connectorTypeConceptDescription = Get.conceptDescriptionText(connectorTypeConceptSequence);
	}
}
