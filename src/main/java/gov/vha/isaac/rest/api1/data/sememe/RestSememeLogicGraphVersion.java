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

import java.util.UUID;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.logic.RestLogicNode;
import gov.vha.isaac.rest.api1.data.logic.RestLogicNodeFactory;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestSememeLogicGraphVersion}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeLogicGraphVersion extends RestSememeVersion {
	private static Logger LOG = LogManager.getLogger();

	/**
	 * The String text of the description of the associated concept
	 */
	@XmlElement
	String referencedConceptDescription;

	/**
	 * A boolean indicating whether the concept referred to by this
	 * RestSememeLogicGraphVersion is defined rather than primitive
	 */
	@XmlElement
	boolean isReferencedConceptDefined;

	/**
	 * The root node of the logical expression tree associated with the concept
	 */
	@XmlElement
	RestLogicNode rootLogicNode;

	protected RestSememeLogicGraphVersion() {
		// for Jaxb
	}

	/**
	 * @param lgs
	 *            - A LogicGraphSememe
	 * @param includeChronology
	 *            - A boolean value indicating whether or not the
	 *            RestSememeLogicGraphVersion should include a populated
	 *            chronology
	 * @param expandNested
	 *            - A boolean value indicating whether or not nested values
	 *            should be expanded
	 * @param expandNested
	 *            - A boolean value indicating whether or not LogicNode UUIDs
	 *            should be included
	 * @param stated
	 *            - A boolean value indicating whether a stated or inferred
	 *            logic graph should be retrieved
	 * @throws RestException
	 * 
	 *             Constructor for RestSememeLogicGraphVersion taking a
	 *             LogicGraphSememe
	 */
	public RestSememeLogicGraphVersion(LogicGraphSememe<?> lgs, boolean includeChronology, UUID processId) throws RestException {
		super();
		setup(lgs, includeChronology, false, false, null, processId);

		referencedConceptDescription = Get.conceptService()
				.getSnapshot(RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate()).conceptDescriptionText(lgs.getReferencedComponentNid());
		LOG.debug("Constructing REST logic graph for {} from LogicalExpression\n{}",
			new RestIdentifiedObject(lgs.getReferencedComponentNid()).toString(), lgs.getLogicalExpression().toString());
		rootLogicNode = constructRootRestLogicNodeFromLogicGraphSememe(lgs);
		try {
			isReferencedConceptDefined = Frills.isConceptFullyDefined(lgs);
		} catch (Exception e) {
			LOG.warn("Problem getting isConceptDefined value (defaulting to false) for LogicGraphSememe referencing {}",
					new RestIdentifiedObject(lgs.getReferencedComponentNid()).toString());
			isReferencedConceptDefined = false;
		}

		if (! RequestInfo.get().shouldExpand(ExpandUtil.logicNodeUuidsExpandable) && RequestInfo.get().returnExpandableLinks())
		{
			expandables.add(new Expandable(ExpandUtil.logicNodeUuidsExpandable,
					RestPaths.logicGraphVersionAppPathComponent + lgs.getSememeSequence() + "?" + RequestParameters.expand + "=" + ExpandUtil.logicNodeUuidsExpandable));
		}
	}

	/**
	 * @param lgs
	 *            - A LogicGraphSememe
	 * @return - A RestUntypedConnectorNode with NodeSemantic of DEFINITION_ROOT
	 * 
	 *         Constructs a RestUntypedConnectorNode with NodeSemantic of
	 *         DEFINITION_ROOT which is the root of the logic graph tree
	 */
	private static RestLogicNode constructRootRestLogicNodeFromLogicGraphSememe(LogicGraphSememe<?> lgs) {
		LogicalExpression le = lgs.getLogicalExpression();

		LOG.debug("Processing LogicalExpression for concept {}", Get.conceptService().getSnapshot(RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate()).conceptDescriptionText(le.getConceptSequence()));
		LOG.debug(le.toString());
		LOG.debug("Root is a {}", le.getRoot().getNodeSemantic().name());

		if (le.getNodeCount() > 0) {
			LOG.debug("Passed LogicalExpression with {} > 0 nodes", le.getNodeCount());
			for (int i = 0; i < le.getNodeCount(); ++i) {
				LOG.debug("{} node #{} of {}: class={}, {}", le.getNode(i).getNodeSemantic(), ((int) i + 1),
						le.getNodeCount(), le.getNode(i).getClass().getName(), le.getNode(i));
			}

			return RestLogicNodeFactory.create(le.getRoot());
		} else { // (le.getNodeCount() <= 0) {
			LOG.warn("Passed LogicalExpression with no children");
			throw new RuntimeException("No children found in LogicalExpression for " + Get.conceptService().getSnapshot(RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate()).conceptDescriptionText(le.getConceptSequence()) + ": " + lgs);
		}
	}
}
