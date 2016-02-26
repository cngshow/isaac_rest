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

import java.util.Arrays;
import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gov.vha.isaac.ochre.model.logic.node.external.TypedNodeWithUuids;

/**
 * 
 * {@link RestTypedNodeWithUuids}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public abstract class RestTypedNodeWithUuids extends RestConnectorNode {
	@XmlElement
	UUID typeConceptUuid;
	
	protected RestTypedNodeWithUuids() {
		// For JAXB
	}
	/**
	 * @param connectorNode
	 */
	public RestTypedNodeWithUuids(TypedNodeWithUuids typedNodeWithUuids) {
		super(typedNodeWithUuids);
		typeConceptUuid = typedNodeWithUuids.getTypeConceptUuid();
	}
	
	public UUID getTypeConceptUuid() {
        return typeConceptUuid;
    }
	
	public RestLogicNode getOnlyChild() {
        if (children.size() == 1) {
            return children.get(0);
        }
        throw new IllegalStateException("Typed nodes can have only one child. Found: " + Arrays.toString(children.toArray()));
    }
}
