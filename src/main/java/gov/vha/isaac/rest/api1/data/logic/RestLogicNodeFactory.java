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

import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.node.AndNode;
import gov.vha.isaac.ochre.model.logic.node.DisjointWithNode;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeBoolean;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeFloat;
import gov.vha.isaac.ochre.model.logic.node.external.*;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInstant;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInteger;
import gov.vha.isaac.ochre.model.logic.node.LiteralNodeString;
import gov.vha.isaac.ochre.model.logic.node.NecessarySetNode;
import gov.vha.isaac.ochre.model.logic.node.OrNode;
import gov.vha.isaac.ochre.model.logic.node.RootNode;
import gov.vha.isaac.ochre.model.logic.node.SufficientSetNode;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.FeatureNodeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeAllWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.ConceptNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.FeatureNodeWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;

/**
 * 
 * {@link RestLogicNodeFactory}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public final class RestLogicNodeFactory {
	private RestLogicNodeFactory() {}

	public static RestLogicNode create(LogicNode logicNode) {

		if (logicNode instanceof AndNode) 
			return new RestUntypedConnectorNode((AndNode)logicNode);
		else if (logicNode instanceof ConceptNodeWithSequences)
			return new RestConceptNode((ConceptNodeWithSequences)logicNode);
		else if (logicNode instanceof ConceptNodeWithUuids)
			return new RestConceptNode((ConceptNodeWithUuids)logicNode);
		else if (logicNode instanceof DisjointWithNode)
			return new RestUntypedConnectorNode((DisjointWithNode)logicNode);
		else if (logicNode instanceof FeatureNodeWithSequences)
			return new RestFeatureNode((FeatureNodeWithSequences)logicNode);
		else if (logicNode instanceof FeatureNodeWithUuids)
			return new RestFeatureNode((FeatureNodeWithUuids)logicNode);
		else if (logicNode instanceof LiteralNodeBoolean)
			return new RestLiteralNodeBoolean((LiteralNodeBoolean)logicNode);
		else if (logicNode instanceof LiteralNodeFloat)
			return new RestLiteralNodeFloat((LiteralNodeFloat)logicNode);
		else if (logicNode instanceof LiteralNodeInstant)
			return new RestLiteralNodeInstant((LiteralNodeInstant)logicNode);
		else if (logicNode instanceof LiteralNodeInteger)
			return new RestLiteralNodeInteger((LiteralNodeInteger)logicNode);
		else if (logicNode instanceof LiteralNodeString)
			return new RestLiteralNodeString((LiteralNodeString)logicNode);
		else if (logicNode instanceof NecessarySetNode)
			return new RestUntypedConnectorNode((NecessarySetNode)logicNode);
		else if (logicNode instanceof OrNode)
			return new RestUntypedConnectorNode((OrNode)logicNode);
		else if (logicNode instanceof RoleNodeAllWithSequences)
			return new RestRoleNode((RoleNodeAllWithSequences)logicNode);
		else if (logicNode instanceof RoleNodeAllWithUuids)
			return new RestRoleNode((RoleNodeAllWithUuids)logicNode);
		else if (logicNode instanceof RoleNodeSomeWithSequences)
			return new RestRoleNode((RoleNodeSomeWithSequences)logicNode);
		else if (logicNode instanceof RoleNodeSomeWithUuids)
			return new RestRoleNode((RoleNodeSomeWithUuids)logicNode);
		else if (logicNode instanceof RootNode)
			return new RestUntypedConnectorNode((RootNode)logicNode);
		else if (logicNode instanceof SufficientSetNode)
			return new RestUntypedConnectorNode((SufficientSetNode)logicNode);
		else
			throw new IllegalArgumentException("create() Failed: Unsupported LogicNode " + logicNode.getClass().getName() + " " + logicNode);
	}
}
