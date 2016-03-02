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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import gov.vha.isaac.ochre.model.logic.node.ConnectorNode;

/**
 * 
 * {@link RestConnectorNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 *Abstract base class of all connectors nodes in the logic graph grammar
 */
@XmlSeeAlso({
	RestUntypedConnectorNode.class,
	RestTypedConnectorNode.class,
	RestFeatureNode.class,
	RestRoleNode.class
})
@XmlRootElement
public abstract class RestConnectorNode extends RestLogicNode {
	
	protected RestConnectorNode() {
		// For JAXB
	}

	/**
	 * @param ConnectorNode
	 */
	protected RestConnectorNode(ConnectorNode connectorNode) {
		super(connectorNode);
	}
}