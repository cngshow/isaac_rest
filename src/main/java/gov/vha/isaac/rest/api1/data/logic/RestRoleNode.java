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

import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeAllWithUuids;
import gov.vha.isaac.ochre.model.logic.node.external.RoleNodeSomeWithUuids;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeAllWithSequences;
import gov.vha.isaac.ochre.model.logic.node.internal.RoleNodeSomeWithSequences;

/**
 * 
 * {@link RestRoleNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * RestRoleNode is a RestTypedConnectorNode corresponding to either RoleNodeSome or RoleNodeAll.
 * 
 * RestFeatureNode must have exactly 1 child node.
 * 
 * RestRoleNode for RoleNodeSome has RestNodeSemantic == NodeSemantic.ROLE_SOME
 * RestRoleNode for RoleNodeAll has RestNodeSemantic == NodeSemantic.ROLE_ALL
 */
public class RestRoleNode extends RestTypedConnectorNode {
	/**
	 * 
	 */
	protected RestRoleNode() {
		// FOR JAXB
	}

	/**
	 * @param roleNodeSomeWithSequences
	 */
	public RestRoleNode(RoleNodeSomeWithSequences roleNodeSomeWithSequences) {
		super(roleNodeSomeWithSequences);
	}
	/**
	 * @param roleNodeSomeWithUuids
	 */
	public RestRoleNode(RoleNodeSomeWithUuids roleNodeSomeWithUuids) {
		super(roleNodeSomeWithUuids);
	}

	/**
	 * @param roleNodeAllWithSequences
	 */
	public RestRoleNode(RoleNodeAllWithSequences roleNodeAllWithSequences) {
		super(roleNodeAllWithSequences);
	}
	/**
	 * @param roleNodeAllWithUuids
	 */
	public RestRoleNode(RoleNodeAllWithUuids roleNodeAllWithUuids) {
		super(roleNodeAllWithUuids);
	}
}
