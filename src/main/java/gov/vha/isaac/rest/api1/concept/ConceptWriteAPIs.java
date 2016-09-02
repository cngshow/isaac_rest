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
package gov.vha.isaac.rest.api1.concept;

import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.And;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.ConceptAssertion;
import static gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder.NecessarySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilderService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilderService;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpressionBuilderService;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.concept.RestConceptCreateData;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link ConceptWriteAPIs}
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent)
public class ConceptWriteAPIs
{
	//private static Logger log = LogManager.getLogger(ConceptWriteAPIs.class);
	
	/**
	 * @param creationData - object containing data used to create new concept
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @return the sequence identifying the created concept
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public RestInteger createNewConcept(
			RestConceptCreateData creationData,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{

		if (StringUtils.isBlank(creationData.fsn)) {
			throw new RestException("RestConceptCreateData.fsn", creationData.fsn, "FSN required");
		}
		if (StringUtils.isBlank(creationData.preferredTerm)) {
			throw new RestException("RestConceptCreateData.preferredTerm", creationData.preferredTerm, "Preferred Term required");
		}
		
		if (creationData.parentIds.size() < 1) {
			throw new RestException("RestConceptCreateData.parentIds", creationData.parentIds + "", "At least one parent concept id required");
		}
		
		int index = 0;
		for (int parentId : creationData.parentIds) {
			if (! Get.conceptService().hasConcept(parentId)) {
				throw new RestException("RestConceptCreateData.parentIds[" + index + "]", parentId + "", "Integer id does not correspond to an existing concept");
			}
			
			index++;
		}

		try {
			int seq = createNewConcept(
					RequestInfo.get().getEditCoordinate(),
					creationData.fsn,
					creationData.preferredTerm,
					creationData.parentIds);
			
			return new RestInteger(seq);
		} catch (Exception e) {
			throw new RestException("Failed creating concept " + creationData + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	private static int createNewConcept(
			EditCoordinate editCoordinate,
			String fsn,
			String preferredTerm,
			List<Integer> parentConceptIds) throws RestException
	{
		try
		{
			ConceptBuilderService conceptBuilderService = LookupService.getService(ConceptBuilderService.class);
			conceptBuilderService.setDefaultLanguageForDescriptions(MetaData.ENGLISH_LANGUAGE);
			conceptBuilderService.setDefaultDialectAssemblageForDescriptions(MetaData.US_ENGLISH_DIALECT);
			conceptBuilderService.setDefaultLogicCoordinate(LogicCoordinates.getStandardElProfile());

			DescriptionBuilderService descriptionBuilderService = LookupService.getService(DescriptionBuilderService.class);
			LogicalExpressionBuilder defBuilder = LookupService.getService(LogicalExpressionBuilderService.class).getLogicalExpressionBuilder();

			for (int parentConceptNidOrSequence : parentConceptIds) {
				ConceptChronology<?> parentConcept = Get.conceptService().getConcept(parentConceptNidOrSequence);

				NecessarySet(And(ConceptAssertion(parentConcept, defBuilder)));
			}

			LogicalExpression parentDef = defBuilder.build();

			ConceptBuilder builder = conceptBuilderService.getDefaultConceptBuilder(fsn, null, parentDef);

			DescriptionBuilder<?, ?> definitionBuilder = descriptionBuilderService.getDescriptionBuilder(preferredTerm, builder,
							MetaData.SYNONYM,
							MetaData.ENGLISH_LANGUAGE);
			definitionBuilder.setPreferredInDialectAssemblage(MetaData.US_ENGLISH_DIALECT);
			builder.addDescription(definitionBuilder);
			
			ConceptChronology<? extends ConceptVersion<?>> newCon = builder.build(editCoordinate, ChangeCheckerMode.ACTIVE, new ArrayList<>());
			Get.commitService().addUncommitted(newCon);
			
			Get.commitService().commit("creating new concept: NID=" + newCon.getNid() + ", FSN=" + fsn 
					+ ", PT=" + preferredTerm).get();
			return newCon.getConceptSequence();
		}
		catch (Exception e)
		{
			throw new RestException("Creation of concept Failed. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}

	/**
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.activateComponent + "{" + RequestParameters.id + "}")
	public void activateConcept(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{	
		resetConceptState(id, State.ACTIVE);
	}

	/**
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + RestPaths.deactivateComponent + "{" + RequestParameters.id + "}")
	public void deactivateConcept(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{	
		resetConceptState(id, State.INACTIVE);
	}

	private static void resetConceptState(
			String id,
			State state) throws RestException {
		int conceptId = RequestInfoUtils.getConceptSequenceFromParameter(RequestParameters.id, id);

		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept = Get.conceptService().getOptionalConcept(conceptId);

		if (! concept.isPresent()) {
			throw new RestException(RequestParameters.id, id, "no concept exists corresponding to concept id " + conceptId + " parameter value " + id);
		}
		try {
			// TODO put in check for current Status to avoid resaving concept with same status
			concept.get().createMutableVersion(state, RequestInfo.get().getEditCoordinate());
			
			Get.commitService().addUncommitted(concept.get());
			
			Get.commitService().commit("committing concept with state=" + state + ": SEQ=" + concept.get().getConceptSequence() + ", UUID=" + concept.get().getPrimordialUuid() + ", DESC=" + concept.get().getConceptDescriptionText());
		} catch (Exception e) {
			throw new RestException("Failed setting to state " + state + " concept " + id + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}
	}
}