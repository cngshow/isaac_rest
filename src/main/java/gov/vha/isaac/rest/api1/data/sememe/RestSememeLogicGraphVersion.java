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
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.logic.RestLogicNode;
import gov.vha.isaac.rest.api1.data.logic.RestLogicNodeFactory;

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
	 * The String text of the description of the associated concept
	 */
	@XmlElement
	String referencedConceptDescription;

	/**
	 * A boolean indicating whether the concept referred to by this RestSememeLogicGraphVersion is defined rather than primitive
	 */
	@XmlElement
	boolean isReferencedConceptDefined;
	
	/**
	 * The root node of the logical expression tree associated with the concept
	 */
	@XmlElement
	RestLogicNode rootLogicNode;

	protected RestSememeLogicGraphVersion()
	{
		//for Jaxb
	}
	
	/**
	 * @param lgs - A LogicGraphSememe
	 * @param includeChronology - A boolean value indicating whether or not the RestSememeLogicGraphVersion should include a populated chronology
	 * @param expandNested - A boolean value indicating whether or not nested values shoudl be expanded
	 * @param stated - A boolean value indicating whether a stated or inferred logic graph should be retrieved 
	 * @throws RestException
	 * 
	 * Constructor for RestSememeLogicGraphVersion taking a LogicGraphSememe
	 */
	public RestSememeLogicGraphVersion(
			LogicGraphSememe<?> lgs,
			boolean includeChronology) throws RestException
	{
		super();
		setup(lgs, includeChronology, false, null);
		//TODO - Joel, can't use this method, we aren't setting the stamp details of the ISAAC default stamp stuff
		referencedConceptDescription = Get.conceptDescriptionText(lgs.getReferencedComponentNid());
		rootLogicNode = constructRootRestLogicNodeFromLogicGraphSememe(lgs);
		try {
			// TODO Fine tune this when data problems resolved
			isReferencedConceptDefined = Frills.isConceptFullyDefined(lgs);
		} catch (Exception e) {
			LOG.warn("Problem getting isConceptDefined value (defaulting to false) for LogicGraphSememe referencing " + Frills.getIdInfo(lgs.getReferencedComponentNid()));
			isReferencedConceptDefined = false;
		}
	}
	
	/**
	 * @param lgs - A LogicGraphSememe
	 * @return - A RestUntypedConnectorNode with NodeSemantic of DEFINITION_ROOT
	 * 
	 * Constructs a RestUntypedConnectorNode with NodeSemantic of DEFINITION_ROOT which is the root of the logic graph tree
	 */
	private static RestLogicNode constructRootRestLogicNodeFromLogicGraphSememe(LogicGraphSememe<?> lgs) {
		LogicalExpression le = lgs.getLogicalExpression();
		
		LOG.debug("Processing LogicalExpression for concept " + Get.conceptDescriptionText(le.getConceptSequence()));
		LOG.debug(le.toString());
		LOG.debug("Root is a " + le.getRoot().getNodeSemantic().name());

		if (le.getNodeCount() > 0) {
			LOG.debug("Passed LogicalExpression with {} > 0 nodes", le.getNodeCount());			
			for (int i = 0; i < le.getNodeCount(); ++i) {
				LOG.debug(le.getNode(i).getNodeSemantic() + " node #" + ((int)i + 1) + " of " + le.getNodeCount() + ": class=" + le.getNode(i).getClass().getName() + ", " + le.getNode(i));
			}

			return RestLogicNodeFactory.create(le.getRoot());
		} else { // (le.getNodeCount() <= 0) {
			LOG.warn("Passed LogicalExpression with no children");
			throw new RuntimeException("No children found in LogicalExpression for " + Get.conceptDescriptionText(le.getConceptSequence()) + ": " + lgs);
		}
	}
}
