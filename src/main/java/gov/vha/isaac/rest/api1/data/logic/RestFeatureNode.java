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
import gov.vha.isaac.ochre.model.logic.node.external.FeatureNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.rest.api1.data.enumerations.RestConcreteDomainOperatorsType;

/**
 * 
 * {@link RestFeatureNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * The RestFeatureNode contains a RestConcreteDomainOperators operator type,
 * must have exactly 1 child node,
 * and has a RestNodeSemantic == NodeSemantic.FEATURE.
 * 
 */
public class RestFeatureNode extends RestTypedConnectorNode {

	/**
	 * RestFeatureNode contains a RestConcreteDomainOperators/ConcreteDomainOperators instance,
	 * which is an enumeration specifying a type of comparison
	 * 
	 * RestFeatureNode must have exactly 1 child node.
	 * 
	 * Available RestConcreteDomainOperators/ConcreteDomainOperator values include
	 *   EQUALS,
	 *   LESS_THAN,
	 *   LESS_THAN_EQUALS,
	 *   GREATER_THAN,
	 *   GREATER_THAN_EQUALS
	 */
	@XmlElement
	RestConcreteDomainOperatorsType operator;

	protected RestFeatureNode() {
		// For JAXB
	}
	/**
	 * @param featureNodeWithSequences
	 */
	public RestFeatureNode(FeatureNodeWithSequences featureNodeWithSequences) {
		super(featureNodeWithSequences);
		operator = new RestConcreteDomainOperatorsType(featureNodeWithSequences.getOperator());
	}
	/**
	 * @param featureNodeWithUuids
	 */
	public RestFeatureNode(FeatureNodeWithUuids featureNodeWithUuids) {
		super(featureNodeWithUuids);
		operator = new RestConcreteDomainOperatorsType(featureNodeWithUuids.getOperator());
	}
}
