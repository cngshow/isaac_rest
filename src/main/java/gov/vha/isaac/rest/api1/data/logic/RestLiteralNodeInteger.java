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
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.model.logic.node.LiteralNodeInteger;

/**
 * 
 * {@link RestLiteralNodeInteger}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
public class RestLiteralNodeInteger extends RestLiteralNode {
	
	@XmlElement
    int literalValue;

	protected RestLiteralNodeInteger() {
		// For JAXB
	}
	/**
	 * @param literalNode
	 */
	public RestLiteralNodeInteger(LiteralNodeInteger literalNode) {
		super(literalNode);
	}

    @Override
    public String toString() {
        return toString("");
    }

    @Override
    public String toString(String nodeIdSuffix) {
        return "String literal[" + nodeIndex + nodeIdSuffix + "]" + literalValue + super.toString(nodeIdSuffix);
    }
}
