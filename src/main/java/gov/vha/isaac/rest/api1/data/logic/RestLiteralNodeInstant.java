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

import java.time.Instant;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInstant;

/**
 * 
 * {@link RestLiteralNodeInstant}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * RestLiteralNodeInstant is a logic node containing only an Instant literal value
 * 
 * Each RestLiteralNodeInstant instance has a RestNodeSemanticType/NodeSemantic == NodeSemantic.LITERAL_INSTANT
 * 
 * A RestLiteralNodeInstant may not have any child logic nodes.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestLiteralNodeInstant extends RestLogicNode {

	/**
	 * RestLiteralNodeInstant contains a literal Instant value, literalValue
	 */
	@XmlElement
	Instant literalValue;

	protected RestLiteralNodeInstant() {
		// For JAXB
	}
	/**
	 * @param literalNodeInstant
	 */
	public RestLiteralNodeInstant(LiteralNodeInstant literalNodeInstant) {
		super(literalNodeInstant);
		literalValue = literalNodeInstant.getLiteralValue();
	}
}
