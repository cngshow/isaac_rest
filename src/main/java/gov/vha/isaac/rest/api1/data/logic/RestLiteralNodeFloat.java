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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.model.logic.node.LiteralNodeFloat;

/**
 * 
 * {@link RestLiteralNodeFloat}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * RestLiteralNodeFloat is a logic node containing only a float literal value
 * 
 * Each RestLiteralNodeFloat instance has a RestNodeSemanticType/NodeSemantic == NodeSemantic.LITERAL_FLOAT
 * 
 * A RestLiteralNodeFloat may not have any child logic nodes.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestLiteralNodeFloat extends RestLogicNode {
	/**
	 * RestLiteralNodeFloat contains a literal float value, literalValue
	 */
	@XmlElement
	float literalValue;

	protected RestLiteralNodeFloat() {
		// For JAXB
	}
	/**
	 * @param literalNodeFloat
	 */
	public RestLiteralNodeFloat(LiteralNodeFloat literalNodeFloat) {
		super(literalNodeFloat);
		literalValue = literalNodeFloat.getLiteralValue();
	}
}
