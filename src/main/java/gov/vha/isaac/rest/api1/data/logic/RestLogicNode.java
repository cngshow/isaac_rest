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
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.data.enumerations.RestNodeSemantic;
import gov.vha.isaac.rest.api1.session.RequestInfo;

/**
 * 
 * {@link RestLogicNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 * 
 * The abstract base class of all REST logic graph tree structure nodes.
 * Each node represents a part of the logic graph grammar and has, at least,
 * its own UUID, a RestNodeSemantic enumerated type and a list of child RestNodeSemantic nodes.
 * The allowed number of child RestNodeSemantic nodes and any additional data 
 * depend on the RestNodeSemantic enumerated type.
 */
@XmlSeeAlso({
	RestConceptNode.class,
	RestConnectorNode.class,
	RestUntypedConnectorNode.class,
	RestTypedConnectorNode.class,
	RestFeatureNode.class,
	RestRoleNode.class,
	RestLiteralNode.class,
	RestLiteralNodeBoolean.class,
	RestLiteralNodeInteger.class,
	RestLiteralNodeFloat.class,
	RestLiteralNodeString.class,
	RestLiteralNodeInstant.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestLogicNode {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The UUID of the logic node itself (not of any referenced or associated component or concept)
	 */
	@XmlElement
	UUID nodeUuid;

	/**
	 * The list of child RestLogicNode instances contained within this RestLogicNode.
	 * A RestTypedConnctorNode may have exactly one child node
	 * A literal node may not have any child nodes at all
	 * Others may have one or more child nodes
	 */
	@XmlElement
	List<RestLogicNode> children;

	/**
	 * The RestNodeSemantic type of this node corresponding to the NodeSemantic enum
	 */
	@XmlElement
	RestNodeSemantic nodeSemantic;

	protected RestLogicNode()
	{
		//For jaxb
	}

	private static String getClassBaseName(Object obj) {
		return obj.getClass().getSimpleName().replaceAll(".*\\.", "");
	}
	
	/**
	 * @param passedLogicNode constructor takes an AbstractLogicNode representing the
	 * root of a logic graph tree or tree fragment and recursively creates and populates an equivalent RestLogicNode
	 */
	public RestLogicNode(AbstractLogicNode passedLogicNode) {
		expandables = new Expandables();

		if (RequestInfo.get().shouldExpand(ExpandUtil.logicNodeUuidsExpandable)) {
			nodeUuid = passedLogicNode.getNodeUuidSetForDepth(1).first();
		} else {
			nodeUuid = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				// TODO make expandables work for logicNodeUuidsExpandable
				// expandables.add(new Expandable(ExpandUtil.logicNodeUuidsExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
		this.nodeSemantic = new RestNodeSemantic(passedLogicNode.getNodeSemantic());

		AbstractLogicNode[] childrenOfPassedLogicNode = passedLogicNode.getChildren();
		this.children = new ArrayList<>(childrenOfPassedLogicNode.length);

		LOG.debug("Constructing " + getClassBaseName(this) + " " + this.nodeSemantic + " from " + passedLogicNode.toString() + " with {} child nodes", childrenOfPassedLogicNode.length);			
		for (int i = 0; i < childrenOfPassedLogicNode.length; ++i) {
			LOG.debug(childrenOfPassedLogicNode[i].getNodeSemantic() + " node #" + ((int)i + 1) + " of " + childrenOfPassedLogicNode.length + " (node index=" + childrenOfPassedLogicNode[i].getNodeIndex() + "): class=" + getClassBaseName(childrenOfPassedLogicNode[i]) + ", " + childrenOfPassedLogicNode[i]);
		}
		for (int i = 0; i < childrenOfPassedLogicNode.length; ++i) {
			LogicNode childOfPassedLogicNode = childrenOfPassedLogicNode[i];
			LOG.debug(getClassBaseName(this) + " " + this.nodeSemantic + " constructing child node from " + childOfPassedLogicNode + " with {} child nodes", childOfPassedLogicNode.getChildren().length);			
			RestLogicNode newRestNode = RestLogicNodeFactory.create(childOfPassedLogicNode);
			LOG.debug(getClassBaseName(this) + " " + this.nodeSemantic + " ctor inserting new " + getClassBaseName(newRestNode) + " " + newRestNode.nodeSemantic + " (index=" + childOfPassedLogicNode.getNodeIndex() + ") into child list at index " + i);
			children.add(newRestNode);
		}
	}
}
