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

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.model.logic.node.external.ConceptNodeWithUuids;

/**
 * 
 * {@link RestConceptNodeWithUuids}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public class RestConceptNodeWithUuids extends RestLogicNode {

	@XmlElement
	UUID conceptUuid;
	
	protected RestConceptNodeWithUuids() {
		// For JAXB
	}
	/**
	 * @param conceptNodeWithUuids
	 */
	public RestConceptNodeWithUuids(ConceptNodeWithUuids conceptNodeWithUuids) {
		super(conceptNodeWithUuids);
		conceptUuid = conceptNodeWithUuids.getConceptUuid();
	}
	
	public UUID getConceptUuid() {
        return conceptUuid;
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "ConceptNode[" + nodeIndex + nodeIdSuffix + "] \"" + Get.conceptService().getConcept(conceptUuid).toUserString() + "\"" + super.toString(nodeIdSuffix);
    }
}
