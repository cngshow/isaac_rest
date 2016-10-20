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
package gov.vha.isaac.rest.api1.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.util.AlphanumComparator;
import gov.vha.isaac.ochre.associations.AssociationInstance;
import gov.vha.isaac.ochre.associations.AssociationType;
import gov.vha.isaac.ochre.associations.AssociationUtilities;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersion;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersionPage;
import gov.vha.isaac.rest.api1.data.association.RestAssociationTypeVersion;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link AssociationAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.associationAPIsPathComponent)
public class AssociationAPIs
{
	/**
	 * Get all defined association types in the system.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'referencedConcept'  If 'referencedConcept' is passed, you can also pass 
	 * 'versionsAll' or 'versionsLatestOnly'
	 * @return the latest version of each unique association definition found in the system on the specified coordinates, sorted by the association name.
	 * 
	 * @throws RestException 
	 */
	//TODO add a filter capability to this by terminology
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationsComponent)
	public RestAssociationTypeVersion[] getAssociations(
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestAssociationTypeVersion> results = new ArrayList<>();
		
		
		Set<Integer> associationConcepts = AssociationUtilities.getAssociationConceptSequences();
		
		for (int i : associationConcepts)
		{
			results.add(new RestAssociationTypeVersion(AssociationType.read(i, RequestInfo.get().getStampCoordinate(), 
					RequestInfo.get().getLanguageCoordinate())));
		}
		
		Collections.sort(results, new Comparator<RestAssociationTypeVersion>()
		{

			@Override
			public int compare(RestAssociationTypeVersion o1, RestAssociationTypeVersion o2)
			{
				return AlphanumComparator.compare(o1.associationName, o2.associationName, true);
			}
		});

		return results.toArray(new RestAssociationTypeVersion[results.size()]);
	}
	
	/**
	 * Return the description of a particular association type
	 * 
	 * @param id - A UUID, nid, or concept sequence of a concept that defines an association type
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'referencedConcept'  If 'referencedConcept' is passed, you can also pass 
	 * 'versionsAll' or 'versionsLatestOnly'
	 * @return the latest version of the specified associationType 
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationComponent+ "{" + RequestParameters.id +"}")
	public RestAssociationTypeVersion getAssociationType(
		@PathParam(RequestParameters.id) String id, 
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		return new RestAssociationTypeVersion(AssociationType.read(Util.convertToConceptSequence(id), RequestInfo.get().getStampCoordinate(), 
				RequestInfo.get().getLanguageCoordinate()));
	}
	
	/**
	 * Return all instances of a particular type of association.  This may return a very large result.  The returned pagination data
	 * will return an exact number of total results (not an estimate, as indicated in the API)
	 * 
	 * @param id - A UUID, nid, or concept sequence of a concept that defines an association type
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'source', 'target', 'nestedSememes'
	 * When 'source' or 'target' is expanded, the following expand options are supported for expanded source and/or target: 'versionsAll', 'versionsLatestOnly'
	 * When 'nestedSememes' is expanded, the following expand options are supported for the nested sememes: 'referencedDetails', 'chronology'
	 * @return the latest version of each unique association instance of type associationType 
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationsWithTypeComponent + "{" + RequestParameters.id +"}")
	public RestAssociationItemVersionPage getAssociationsOfType(
		@PathParam(RequestParameters.id) String id, 
		@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
		@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		ArrayList<RestAssociationItemVersion> results;
		try
		{
			results = new ArrayList<>();
			AtomicInteger total = new AtomicInteger(0);
			int start = (pageNum * maxPageSize) - maxPageSize;
			Get.sememeService().getSememesFromAssemblage(Util.convertToConceptSequence(id))
				.forEach(associationC -> 
					{
						total.incrementAndGet();
						try
						{
							@SuppressWarnings({ "unchecked", "rawtypes" })
							Optional<LatestVersion<DynamicSememe<?>>> latest = ((SememeChronology)associationC).getLatestVersion(DynamicSememe.class, 
									RequestInfo.get().getStampCoordinate());
							if (latest.isPresent())
							{
								if (total.get() <= start  || results.size() >= maxPageSize)
								{
									//noop - can't short-circuit (plus want the count)
								}
								else
								{
									results.add(new RestAssociationItemVersion(AssociationInstance.read(latest.get().value(), RequestInfo.get().getStampCoordinate())));
								}
							}
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					});
			return new RestAssociationItemVersionPage(pageNum, maxPageSize, total.get(), true, total.get() > (pageNum * maxPageSize),
					RestPaths.associationAPIsPathComponent + RestPaths.associationsWithTypeComponent + id, results.toArray(new RestAssociationItemVersion[results.size()]));
		}
		catch (RuntimeException e)
		{
			if (e.getCause() != null && e.getCause() instanceof RestException)
			{
				throw (RestException)e.getCause();
			}
			else
			{
				throw e;
			}
		}
	}
	
	/**
	 * Return all association instances that involve the specified source component.
	 * @param id - A UUID or nid (of a concept or sememe) that must be the source portion of the returned association item.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'source', 'target', 'nestedSememes'
	 * When 'source' or 'target' is expanded, the following expand options are supported for expanded source and/or target: 'versionsAll', 'versionsLatestOnly'
	 * When 'nestedSememes' is expanded, the following expand options are supported for the nested sememes: 'referencedDetails', 'chronology'
	 * @return the latest version of each unique association that has a source component equal to 'id' 
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationsWithSourceComponent + "{" + RequestParameters.id +"}")
	public RestAssociationItemVersion[] getSourceAssociations(
		@PathParam(RequestParameters.id) String id, 
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		List<AssociationInstance> results = AssociationUtilities.getSourceAssociations(Util.convertToNid(id), RequestInfo.get().getStampCoordinate());
		RestAssociationItemVersion[] finalResult = new RestAssociationItemVersion[results.size()];
		for (int i = 0; i < results.size(); i++)
		{
			finalResult[i] = new RestAssociationItemVersion(results.get(i));
		}
		return finalResult;
		
	}
	
	/**
	 * Return all association instances that involve the specified target component.
	 * @param id - A UUID or nid (of a concept or sememe) that must be the target portion of the returned association item.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'source', 'target', 'nestedSememes'
	 * When 'source' or 'target' is expanded, the following expand options are supported for expanded source and/or target: 'versionsAll', 'versionsLatestOnly'
	 * When 'nestedSememes' is expanded, the following expand options are supported for the nested sememes: 'referencedDetails', 'chronology'
	 * @return the latest version of each unique association that has a source component equal to 'id' 
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationsWithTargetComponent + "{" + RequestParameters.id +"}")
	public RestAssociationItemVersion[] getTargetAssociations(
		@PathParam(RequestParameters.id) String id, 
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		//TODO lookup by target performance is not good at the moment, not sure why
		List<AssociationInstance> results = AssociationUtilities.getTargetAssociations(Util.convertToNid(id), RequestInfo.get().getStampCoordinate());
		RestAssociationItemVersion[] finalResult = new RestAssociationItemVersion[results.size()];
		for (int i = 0; i < results.size(); i++)
		{
			finalResult[i] = new RestAssociationItemVersion(results.get(i));
		}
		return finalResult;
		
	}
	
	/**
	 * Return a particular association item
	 * @param id - A UUID or nid of a sememe association instance.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * @param expand - the optional items to be expanded.  Supports 'source', 'target', 'nestedSememes'
	 * When 'source' or 'target' is expanded, the following expand options are supported for expanded source and/or target: 'versionsAll', 'versionsLatestOnly'
	 * When 'nestedSememes' is expanded, the following expand options are supported for the nested sememes: 'referencedDetails', 'chronology'
	 * @return the latest version of the requested association on the provided coordinate.  May return null, if the association isn't available on 
	 * the specified coordinate.
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.associationItemComponent + "{" + RequestParameters.id +"}")
	public RestAssociationItemVersion getAssociation(
		@PathParam(RequestParameters.id) String id, 
		@QueryParam(RequestParameters.coordToken) String coordToken,
		@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);
		
		Optional<AssociationInstance> result = AssociationUtilities.getAssociation(Util.convertToNid(id), RequestInfo.get().getStampCoordinate());
		
		return result.isPresent() ? new RestAssociationItemVersion(result.get()) : null;
		
	}
}
