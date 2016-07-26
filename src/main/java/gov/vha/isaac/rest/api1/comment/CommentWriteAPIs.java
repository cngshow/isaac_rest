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
import gov.vha.isaac.ochre.api.commit.CommitRecord;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.version.DynamicSememeImpl;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import javafx.concurrent.Task;


/**
 * {@link CommentWriteAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.writePathComponent + RestPaths.commentAPIsPathComponent)
public class CommentWriteAPIs
{
	/**
	 * @param itemToComment - the identifier (UUID or nid) of the item to be commented on
	 * @param commentText - the text to store as the comment
	 * @param commentContext - the optional additional text to store with the comment
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @return the sequence identifying the created sememe which stores the comment data
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.createPathComponent)
	public int createNewComment(
		@QueryParam(RequestParameters.id) String itemToComment, 
		@QueryParam("commentText") String commentText,
		@QueryParam("commentContext") String commentContext,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		
		int itemNid = Util.convertToNid(itemToComment);
		
		if (StringUtils.isBlank(commentText)) 
		{
			throw new RestException("The parameter 'commentText' is required");
		}
		
		SememeChronology<? extends DynamicSememe<?>> built =  Get.sememeBuilderService().getDynamicSememeBuilder(
			itemNid,  
			DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getSequence(), 
			new DynamicSememeData[] {
				new DynamicSememeStringImpl(commentText),
				(StringUtils.isBlank(commentContext) ? null : new DynamicSememeStringImpl(commentContext))}
			).build(RequestInfo.get().getEditCoordinate(), ChangeCheckerMode.ACTIVE);

		@SuppressWarnings("deprecation")
		Task<Optional<CommitRecord>> task = Get.commitService().commit("Added comment");
		
		try
		{
			task.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException();
		}
		return built.getSememeSequence();
	}
	
	/**
	 * All fields are overwritten with the provided values - for example, if there was previously a value for an optional field, and it is not 
	 * provided now, the new version will have that field stored as blank.
	 * 
	 * @param id - The id (nid, sequence or UUID) of the comment to be updated 
	 * @param commentText - The new comment text value
	 * @param commentContext - optional - the new comment context
	 * @param state - The state to put the comment into
	 * @param editToken - the edit coordinates identifying who is making the edit.  An EditToken must be obtained by a separate (prior) call to 
	 * getEditCoordinatesToken().
	 * @throws RestException
	 */
	//TODO fix the comments above around editToken 
	@PUT
	@Path(RestPaths.updatePathComponent + "{" + RequestParameters.id +"}")
	public void updateComment(
		@QueryParam(RequestParameters.id) String id, 
		@QueryParam("commentText") String commentText,
		@QueryParam("commentContext") String commentContext,
		@QueryParam("state") State state,
		@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		
		@SuppressWarnings("rawtypes")
		SememeChronology sc = SememeAPIs.findSememeChronology(id);
		
		@SuppressWarnings("unchecked")
		DynamicSememeImpl editVersion = (DynamicSememeImpl)sc.createMutableVersion(DynamicSememeImpl.class, state, RequestInfo.get().getEditCoordinate());
		
		editVersion.setData(
			new DynamicSememeData[] {new DynamicSememeStringImpl(commentText),
			(StringUtils.isBlank(commentContext) ? null : new DynamicSememeStringImpl(commentContext))});

		Get.commitService().addUncommitted(sc);
		
		@SuppressWarnings("deprecation")
		Task<Optional<CommitRecord>> task = Get.commitService().commit("Update comment");
		
		try
		{
			task.get();
		}
		catch (Exception e)
		{
			throw new RuntimeException();
		}
	}
}
