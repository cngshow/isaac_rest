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
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.logic.LogicNode;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.model.logic.node.RootNode;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.logic.RestLogicNodeFactory;
import gov.vha.isaac.rest.api1.data.logic.RestRootNode;

/**
 * 
 * {@link RestSememeLogicGraphVersion}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
public class RestSememeLogicGraphVersion extends RestSememeVersion
{
	private static Logger LOG = LogManager.getLogger();

	/**
	 * The text of the description of the associated concept
	 */
	@XmlElement
	String conceptDescription;
	
	@XmlElement
	RestRootNode rootLogicNode;

	protected RestSememeLogicGraphVersion()
	{
		//for Jaxb
	}
	
	public RestSememeLogicGraphVersion(LogicGraphSememe<?> lgs, boolean includeChronology, boolean expandNested, boolean stated) throws RestException
	{
		// TODO Do something with expandNested and stated
		super();
		if (expandNested)
		{
		}
		setup(lgs, includeChronology, expandNested, null);

		conceptDescription = Get.conceptDescriptionText(lgs.getReferencedComponentNid());
		rootLogicNode = constructRootRestLogicNodeFromLogicGraphSememe(lgs);
	}
	
	private static RestRootNode constructRootRestLogicNodeFromLogicGraphSememe(LogicGraphSememe<?> lgs) {
		LogicalExpression le = lgs.getLogicalExpression();
		
		LOG.debug("Processing LogicalExpression for concept " + Get.conceptDescriptionText(le.getConceptSequence()));
		LOG.debug("Root is a " + le.getRoot().getNodeSemantic().name());

		if (le.getNodeCount() > 0) {
			LOG.debug("Passed LogicalExpression with {} > 0 nodes", le.getNodeCount());			
			for (int i = 0; i < le.getNodeCount(); ++i) {
				LOG.debug(le.getNode(i).getNodeSemantic() + " node #" + i + 1 + " of " + le.getNodeCount() + ": class=" + le.getNode(i).getClass().getName() + ", " + le.getNode(i));
			}

			return (RestRootNode)RestLogicNodeFactory.create(le.getRoot());
		} else { // (le.getNodeCount() <= 0) {
			LOG.warn("Passed LogicalExpression with no children");
			throw new RuntimeException("No children found in LogicalExpression for " + Get.conceptDescriptionText(le.getConceptSequence()) + ": " + lgs);
		}
	}
}
