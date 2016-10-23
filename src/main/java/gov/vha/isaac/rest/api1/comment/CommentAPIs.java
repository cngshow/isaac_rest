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

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.api1.workflow.WorkflowUtils;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link CommentAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.commentAPIsPathComponent)
public class CommentAPIs
{
	/**
	 * Returns a single version of a comment {@link RestCommentVersion}.
	 * 
	 * @param id - A UUID, nid, or sememe sequence that identifies the comment.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the comment version object {@link RestCommentVersion}.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id +"}")
	public RestCommentVersion getCommentVersion(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.processId) String processId,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		Optional<UUID> processIdOptional = RequestInfoUtils.parseUuidParameterIfNonBlank(RequestParameters.processId, processId);

		@SuppressWarnings("rawtypes")
		SememeChronology sc = SememeAPIs.findSememeChronologyConformingToEffectiveStamp(id, processIdOptional);
		
		@SuppressWarnings("rawtypes")
		Optional<DynamicSememe> commentVersion = Optional.empty();
		try {
			commentVersion = WorkflowUtils.getStampedVersion(DynamicSememe.class, processIdOptional, sc.getNid());
		} catch (Exception e) {
			throw new RestException(e);
		}
		if (commentVersion.isPresent())
		{
			return new RestCommentVersion(commentVersion.get());
		}

		throw new RestException("id", id, "No comment was found on the given coordinate");
	}
	
	/**
	 * Returns an array containing current version of any and all comments attached to a referenced component.  Note, this is not multiple versions 
	 * of a single comment, rather, multiple comments (0 to n) at the version specified by the view coordinate.
	 * 
	 * @param id - A UUID or nid of the component being referenced by a comment
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @return the comment version object.  
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + RestPaths.byReferencedComponentComponent + "{" + RequestParameters.id +"}")
	public RestCommentVersion[] getCommentsByReferencedItem(
		@PathParam(RequestParameters.id) String id,
		@QueryParam(RequestParameters.processId) String processId,
		@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		Optional<UUID> processIdOptional = RequestInfoUtils.parseUuidParameterIfNonBlank(RequestParameters.processId, processId);

		ArrayList<RestCommentVersion> temp = Util.readComments(id, processIdOptional.isPresent() ? processIdOptional.get() : null); 
		return temp.toArray(new RestCommentVersion[temp.size()]);
	}
}
