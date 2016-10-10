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
package gov.vha.isaac.rest.api1.comment;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.rest.api.data.wrappers.RestInteger;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBase;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * APIs for creating and editing comments
 * 
 * {@link CommentWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.commentAPIsPathComponent)
public class CommentWriteAPIs
{
	/**
	 * Create a new comment according to the 
	 * @param dataToCreateComment - {@link RestCommentVersionBaseCreate} object containing data used to construct a new comment
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @return the int Sememe sequence in a {@link RestInteger} wrapper identifying the created sememe which stores the comment data
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public RestInteger createNewComment(
			RestCommentVersionBaseCreate dataToCreateComment,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);

		try
		{
			Integer commentedItemNid = null;
			if (dataToCreateComment.getCommentedItem() == 0) {
				throw new RestException("dataToCreateComment.commentedItem", Integer.toString(dataToCreateComment.getCommentedItem()), "invalid specified id for commented item");
			} else {
				// Concept Sequence// NID
				Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept = Get.conceptService().getOptionalConcept(dataToCreateComment.getCommentedItem());
				if (concept.isPresent()) {
					commentedItemNid = concept.get().getNid();
				} else {
					Optional<? extends SememeChronology<? extends SememeVersion<?>>> sememe = Get.sememeService().getOptionalSememe(dataToCreateComment.getCommentedItem());
					if (sememe.isPresent()) {
						commentedItemNid = sememe.get().getNid();
					}
				}
				if (commentedItemNid == null) {
					throw new RestException("dataToCreateComment.commentedItem", Integer.toString(dataToCreateComment.getCommentedItem()), "no concept or sememe for specified id for commented item");
				}
			}

			Optional<UUID> uuid = Get.identifierService().getUuidPrimordialForNid(commentedItemNid);

			if (StringUtils.isBlank(dataToCreateComment.getCommentContext())) 
			{
				throw new RestException("The parameter 'commentText' is required");
			}

			SememeChronology<? extends DynamicSememe<?>> built =  Get.sememeBuilderService().getDynamicSememeBuilder(
					commentedItemNid,  
					DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getSequence(), 
					new DynamicSememeData[] {
							new DynamicSememeStringImpl(dataToCreateComment.getComment()),
							(StringUtils.isBlank(dataToCreateComment.getCommentContext()) ? null : new DynamicSememeStringImpl(dataToCreateComment.getCommentContext()))}
					).build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();

			Get.commitService().commit("Added comment for " + (uuid.isPresent() ? uuid.get() : commentedItemNid)).get();

			return new RestInteger(built.getSememeSequence());
		}
		catch (RestException e)
		{
			throw  e;
		}
		catch (Exception e)
		{
			throw new RestException("Failed creating new comment " + dataToCreateComment + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param dataToUpdateComment - RestCommentVersionBase object containing data for updating a comment
	 * @param id - The id (nid, sequence or UUID) of the comment to be updated 
	 * @param state - The state to put the comment into.  Valid values are (case insensitive) "INACTIVE"/"I" or "ACTIVE"/"A"
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@PUT
	@Path(RestPaths.updatePathComponent)
	public void updateComment(
			RestCommentVersionBase dataToUpdateComment,
			@QueryParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.state) String state,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.state,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		if (id == null) {
			throw new RestException(RequestParameters.id, null, "invalid (null) comment id");
		}
		
		RestStateType stateToUse = RestStateType.valueOf(state);

		@SuppressWarnings("rawtypes")
		SememeChronology sc = SememeAPIs.findSememeChronology(id);
		
		@SuppressWarnings("unchecked")
		DynamicSememeImpl editVersion = (DynamicSememeImpl)sc.createMutableVersion(DynamicSememeImpl.class, stateToUse.toState(), RequestInfo.get().getEditCoordinate());
		
		editVersion.setData(
			new DynamicSememeData[] {new DynamicSememeStringImpl(dataToUpdateComment.getComment()),
			(StringUtils.isBlank(dataToUpdateComment.getCommentContext()) ? null : new DynamicSememeStringImpl(dataToUpdateComment.getCommentContext()))});
		
		Get.commitService().addUncommitted(sc);
		
		try
		{		
			Get.commitService().commit("Update comment").get();
		}
		catch (Exception e)
		{
			throw new RestException("Failed updating comment id=" + id + ", state=" + state + ", new=" + dataToUpdateComment);
		}
	}
}