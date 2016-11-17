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
import java.util.concurrent.ExecutionException;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowUpdater;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBase;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionCreate;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;
import gov.vha.isaac.rest.tokens.EditTokens;


/**
 * APIs for creating and editing comments
 * 
 * {@link CommentWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.commentAPIsPathComponent)
@RolesAllowed({UserRoleConstants.SUPER_USER, UserRoleConstants.EDITOR})
public class CommentWriteAPIs
{
	@Context
	private SecurityContext securityContext;

	/**
	 * Create a new comment according to the 
	 * @param dataToCreateComment - {@link RestCommentVersionCreate} object containing data used to construct a new comment
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return the {@link RestWriteResponse} wrapper identifying the created sememe which stores the comment data
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public RestWriteResponse createNewComment(
			RestCommentVersionCreate dataToCreateComment,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		int commentedItemNid = RequestInfoUtils.getNidFromUuidOrNidParameter("dataToCreateComment.commentedItem", dataToCreateComment.commentedItem);

		Optional<UUID> uuid = Get.identifierService().getUuidPrimordialForNid(commentedItemNid);

		if (StringUtils.isBlank(dataToCreateComment.comment)) 
		{
			throw new RestException("The field 'comment' is required");
		}

		SememeBuilder<? extends SememeChronology<? extends DynamicSememe<?>>> sb = Get.sememeBuilderService().getDynamicSememeBuilder(
				commentedItemNid,  
				DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getSequence(), 
				new DynamicSememeData[] {
						new DynamicSememeStringImpl(dataToCreateComment.comment),
						(StringUtils.isBlank(dataToCreateComment.commentContext) ? null : new DynamicSememeStringImpl(dataToCreateComment.commentContext))}
				);

		if (dataToCreateComment.active != null && !dataToCreateComment.active)
		{
			sb.setState(State.INACTIVE);
		}

		SememeChronology<? extends DynamicSememe<?>> built = sb.build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE).getNoThrow();

		try {
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Added comment for " + (uuid.isPresent() ? uuid.get() : commentedItemNid)).get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}

			return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), built.getPrimordialUuid());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Failed creating new comment " + dataToCreateComment + ". Caught " + e.getClass().getName() + " " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param id - The id (nid, sequence or UUID) of the comment to be updated 
	 * @param dataToUpdateComment - RestCommentVersionBase object containing data for updating a comment
	 * @param editToken - 
	 *            EditToken string returned by previous call to getEditToken()
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @throws RestException
	 */
	@PUT
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public RestWriteResponse updateComment(
			RestCommentVersionBase dataToUpdateComment,
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.editToken,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		if (id == null) {
			throw new RestException(RequestParameters.id, null, "invalid (null) comment id");
		}
		
		State stateToUse = (dataToUpdateComment.active == null || dataToUpdateComment.active) ? State.ACTIVE : State.INACTIVE;
		
		if (StringUtils.isBlank(dataToUpdateComment.comment)) 
		{
			throw new RestException("The field 'comment' is required");
		}

		@SuppressWarnings("rawtypes")
		SememeChronology sc = SememeAPIs.findSememeChronology(id);
		
		@SuppressWarnings("unchecked")
		DynamicSememeImpl editVersion = (DynamicSememeImpl)sc.createMutableVersion(DynamicSememeImpl.class, stateToUse, RequestInfo.get().getEditCoordinate());
		
		editVersion.setData(
			new DynamicSememeData[] {new DynamicSememeStringImpl(dataToUpdateComment.comment),
			(StringUtils.isBlank(dataToUpdateComment.commentContext) ? null : new DynamicSememeStringImpl(dataToUpdateComment.commentContext))});
		
		try
		{
			Get.commitService().addUncommitted(sc).get();
			Optional<CommitRecord> commitRecord = Get.commitService().commit("Update comment").get();
			if (RequestInfo.get().getActiveWorkflowProcessId() != null)
			{
				LookupService.getService(WorkflowUpdater.class).addCommitRecordToWorkflow(RequestInfo.get().getActiveWorkflowProcessId(), commitRecord);
			}
		}
		catch (Exception e)
		{
			throw new RestException("Failed updating comment id=" + id + ", new=" + dataToUpdateComment);
		}
		return new RestWriteResponse(EditTokens.renew(RequestInfo.get().getEditToken()), sc.getPrimordialUuid());
	}
}