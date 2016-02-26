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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.node.ConnectorNode;

/**
 * 
 * {@link RestConnectorNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public abstract class RestConnectorNode extends RestLogicNode {

	@XmlElement
	List<Short> childIndices;

	protected RestConnectorNode() {
		// For JAXB
	}
	/**
	 * @param logicNode
	 */
	public RestConnectorNode(ConnectorNode connectorNode) {
		super(connectorNode);
		childIndices = new ArrayList<Short>(connectorNode.getChildren().length);
		for (LogicNode child : connectorNode.getChildren()) {
			childIndices.add(child.getNodeIndex());
		}
	}    @Override
    public String toString() {
        return toString("");
    }
    
    @Override
    public String toString(String nodeIdSuffix) {
        if (childIndices != null && !childIndices.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            builder.append("➞[");
            for (short index : childIndices) {
                builder.append(index);
                builder.append(nodeIdSuffix);
                builder.append(", ");
            }
            builder.deleteCharAt(builder.length() -1);
            builder.deleteCharAt(builder.length() -1);
            builder.append("]");
            return builder.toString();
        }
        return "";
    }
}
