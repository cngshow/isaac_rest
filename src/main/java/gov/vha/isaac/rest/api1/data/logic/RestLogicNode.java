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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.model.logic.node.AbstractLogicNode;
import gov.vha.isaac.rest.api1.data.enumerations.RestNodeSemantic;

/**
 * 
 * {@link RestLogicNode}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestLogicNode {
	private static Logger LOG = LogManager.getLogger();

	@XmlElement
	short nodeIndex;

	@XmlElement
	UUID nodeUuid;
	
	@XmlElement
	List<RestLogicNode> children;
	
	@XmlElement
	RestNodeSemantic nodeSemantic;
	
	protected RestLogicNode()
	{
		//For jaxb
	}
	
	private static String getClassBaseName(Object obj) {
		return obj.getClass().getSimpleName().replaceAll(".*\\.", "");
	}
	public RestLogicNode(AbstractLogicNode logicNode) {
		nodeUuid = logicNode.getNodeUuidSetForDepth(1).first();
		this.nodeSemantic = new RestNodeSemantic(logicNode.getNodeSemantic());
		this.nodeIndex = logicNode.getNodeIndex();
		this.children = new ArrayList<RestLogicNode>(logicNode.getChildren().length);
		
		AbstractLogicNode[] childNodes = logicNode.getChildren();
		LOG.debug("Constructing " + this.nodeSemantic + " " + getClassBaseName(this) + " from " + logicNode + " with {} child nodes", childNodes.length);			
		for (int i = 0; i < childNodes.length; ++i) {
			LOG.debug(childNodes[i].getNodeSemantic() + " node #" + i + 1 + " of " + childNodes.length + "(node index=" + childNodes[i].getNodeIndex() + "): class=" + getClassBaseName(childNodes[i]) + ", " + childNodes[i]);
		}
		for (LogicNode child : childNodes) {
			RestLogicNode newRestNode = RestLogicNodeFactory.create(child);
			LOG.debug(this.nodeSemantic + " " + getClassBaseName(this) + " ctor inserting new " + newRestNode.nodeSemantic + " (index=" + child.getNodeIndex() + ") into child list at index " + children.size());
			children.add(newRestNode);
		}
	}

//	public UUID getNodeUuid() {
//		return nodeUuid;
//	}
//	public RestNodeSemantic getNodeSemantic() {
//		return nodeSemantic;
//	}
//	public List<RestLogicNode> getChildren() {
//		return Collections.unmodifiableList(children);
//	}
//	public short getNodeIndex() {
//		return nodeIndex;
//	}

    
    /**
     * Use to when printing out multiple expressions, and you want to differentiate the 
     * identifiers so that they are unique across all the expressions. 
     * @param nodeIdSuffix the identifier suffix for this expression. 
     * @return a text representation of this expression. 
     */
    public String toString() {
        return toString("");
    }
    public String toString(String nodeIdSuffix) {
         return "";
    }
}
