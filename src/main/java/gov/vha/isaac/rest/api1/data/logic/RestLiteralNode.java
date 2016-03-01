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

import gov.vha.isaac.ochre.model.logic.node.LiteralNode;

/**
 * 
 * {@link RestLiteralNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 * 
 * RestLiteralNode is the abstract base class for logic nodes containing only literal values,
 * such as boolean, int, float, Instant and String.
 * 
 * Each RestLiteralNode derived class instance has a RestNodeSemantic/NodeSemantic according to its type
 * 
 * A RestLiteralNode derived class may not have any child logic nodes.
 *
 */
@XmlSeeAlso({RestLiteralNodeBoolean.class,RestLiteralNodeInteger.class, RestLiteralNodeFloat.class, RestLiteralNodeString.class, RestLiteralNodeInstant.class})
@XmlRootElement
public abstract class RestLiteralNode extends RestLogicNode {
	protected RestLiteralNode() {
		// For JAXB
	}

	/**
	 * @param literalNode
	 */
	protected RestLiteralNode(LiteralNode literalNode) {
		super(literalNode);
	}
}
