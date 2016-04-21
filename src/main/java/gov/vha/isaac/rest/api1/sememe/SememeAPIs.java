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
package gov.vha.isaac.rest.api1.sememe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeDefinition;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;
import gov.vha.isaac.rest.api1.session.RequestParameters;


/**
 * {@link SememeAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.sememePathComponent)
public class SememeAPIs
{

	/**
	 * Returns the chronology of a sememe.  
	 * @param id - A UUID, nid or sememe sequence
	 * @param expand - A comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly', 'nestedSememes'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * @return the sememe chronology object
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{id}")
	public RestSememeChronology getSememeChronology(
			@PathParam("id") String id,
			@QueryParam("expand") String expand
			) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		RestSememeChronology chronology =
				new RestSememeChronology(
						findSememeChronology(id),
						RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable));
		
		return chronology;
	}
	
	/**
	 * Returns a single version of a sememe.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes'
	 * @return the sememe version object.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{id}")
	public RestSememeVersion getSememeVersion(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		RequestInfo.get().readExpandables(expand);

		@SuppressWarnings("rawtypes")
		SememeChronology sc = findSememeChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
		if (sv.isPresent())
		{
			return RestSememeVersion.buildRestSememeVersion(sv.get().value(), RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable));
		}
		else
		{
			throw new RestException("id", id, "No sememe was found");
		}
	}
	
	public static SememeChronology<? extends SememeVersion<?>> findSememeChronology(String id) throws RestException
	{
		SememeService sememeService = Get.sememeService();
		
		Optional<UUID> uuidId = UUIDUtil.getUUID(id);
		Optional<Integer> intId = Optional.empty();
		if (uuidId.isPresent())
		{
			if (Get.identifierService().hasUuid(uuidId.get()))
			{
				intId = Optional.of(Get.identifierService().getNidForUuids(uuidId.get()));
			}
			else
			{
				throw new RestException("id", id, "Is not known by the system");
			}
		}
		else
		{
			intId = NumericUtils.getInt(id);
		}
		
		if (intId.isPresent())
		{
			Optional<? extends SememeChronology<? extends SememeVersion<?>>> sc = sememeService.getOptionalSememe(intId.get());
			if (sc.isPresent())
			{
				return sc.get();
			}
			else
			{
				throw new RestException("id", id, "No Sememe was located with the given identifier");
			}
		}
		throw new RestException("id", id, "Is not a sememe identifier.  Must be a UUID or an integer");
	}
	
	/**
	 * Returns all sememe instances with the given assemblage
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence of an assemblage concept
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nested'
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * TODO this needs to be paged 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.byAssemblageComponent + "{id}")
	public List<RestSememeVersion> getByAssemblage(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		HashSet<Integer> temp = new HashSet<>();
		temp.add(Util.convertToConceptSequence(id));
		
		//we don't have a referenced component - our id is assemblage
		return get(null, temp, RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
			true);
	}
	
	/**
	 * Returns all sememe instances attached to the specified referenced component
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID or nid of a component.  Note that this could be a concept or a sememe reference, hence, sequences are not allowed here.
	 * @param assemblage - An optional assemblage UUID, nid or concept sequence to restrict the type of sememes returned.  If ommitted, assemblages
	 * of all types will be returned.  May be specified multiple times to allow multiple assemblages
	 * @param includeDescriptions - an optional flag to request that description type sememes are returned.  By default, description type 
	 * sememes are not returned, as these are typically retreived via a getDescriptions call on the Concept APIs.
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes'
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.byReferencedComponentComponent + "{id}")
	public List<RestSememeVersion> getByReferencedComponent(@PathParam("id") String id, @QueryParam("assemblage") Set<String> assemblage, 
		@QueryParam("includeDescriptions") @DefaultValue("false") String includeDescriptions, @QueryParam("expand") String expand) 
			throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		HashSet<Integer> allowedAssemblages = new HashSet<>();
		for (String a : assemblage)
		{
			allowedAssemblages.add(Util.convertToConceptSequence(a));
		}
		
		return get(id, allowedAssemblages, RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
			RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), Boolean.parseBoolean(includeDescriptions.trim()) );
	}
	

	/**
	 * Return the full description of a particular sememe - including its intended use, the types of any data columns that will be attached, etc.
	 * @param id - The UUID, nid or concept sequence of the concept that represents the sememe assemblage.
	 * @return - the full description
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeDefinitionComponent + "{id}")
	public RestDynamicSememeDefinition getSememeDefinition(@PathParam("id") String id) throws RestException
	{
		int conceptSequence = Util.convertToConceptSequence(id);
		if (DynamicSememeUsageDescriptionImpl.isDynamicSememe(conceptSequence))
		{
			return new RestDynamicSememeDefinition(DynamicSememeUsageDescriptionImpl.read(conceptSequence));
		}
		else
		{
			//Not annotated as a dynamic sememe.  We have to find a real value to determine if this is used as a static sememe.
			//TODO someday, we will fix the underlying APIs to allow us to know the static sememe typing up front....
			Optional<SememeChronology<? extends SememeVersion<?>>> sc = Get.sememeService().getSememesFromAssemblage(conceptSequence).findAny();
			if (sc.isPresent())
			{
				return new RestDynamicSememeDefinition(DynamicSememeUsageDescriptionImpl.mockOrRead(sc.get()));
			}
		}
		throw new RestException("The specified concept identifier is not configured as a dynamic sememe, and it is not used as a static sememe");
	}
	
	/**
	 * @param referencedComponent - optional - if provided - takes precedence
	 * @param assemblage - optional - if provided, either limits the referencedComponent search by this type, or, if 
	 * referencedComponent is not provided - focuses the search on just this assemblage
	 * @param expandChronology
	 * @param expandNested
	 * @param allowDescriptions true to include description type sememes, false to skip
	 * @return
	 * @throws RestException
	 */
	public static List<RestSememeVersion> get(String referencedComponent, Set<Integer> allowedAssemblages, boolean expandChronology, boolean expandNested, 
		boolean allowDescriptions) throws RestException
	{
		final ArrayList<RestSememeVersion> results = new ArrayList<>();
		Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
		{
			@Override
			public void accept(@SuppressWarnings("rawtypes") SememeChronology sc)
			{
				@SuppressWarnings("unchecked")
				Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
				if (sv.isPresent() && sv.get().value().getChronology().getSememeType() != SememeType.LOGIC_GRAPH 
						&& sv.get().value().getChronology().getSememeType() != SememeType.RELATIONSHIP_ADAPTOR
						&& (allowDescriptions || sv.get().value().getChronology().getSememeType() != SememeType.DESCRIPTION))
				{
					try
					{
						results.add(RestSememeVersion.buildRestSememeVersion(sv.get().value(), expandChronology, expandNested));
					}
					catch (RestException e)
					{
						throw new RuntimeException("Unexpected error", e);
					}
				}
			}
		};

		if (StringUtils.isNotBlank(referencedComponent))
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(referencedComponent);
			Optional<Integer> refCompNid = Optional.empty();
			if (uuidId.isPresent())
			{
				if (Get.identifierService().hasUuid(uuidId.get()))
				{
					refCompNid = Optional.of(Get.identifierService().getNidForUuids(uuidId.get()));
				}
				else
				{
					throw new RestException("referencedComponent", referencedComponent, "Is not known by the system");
				}
			}
			else
			{
				refCompNid = NumericUtils.getInt(referencedComponent);
			}
			
			if (refCompNid.isPresent() && refCompNid.get() < 0)
			{
				Stream<SememeChronology<? extends SememeVersion<?>>> sememes = Get.sememeService().getSememesForComponentFromAssemblages(refCompNid.get(), allowedAssemblages);
				sememes.forEach(consumer);
			}
			else
			{
				throw new RestException("referencedComponent", referencedComponent, "Must be a NID or a UUID");
			}
		}
		else
		{
			if (allowedAssemblages == null || allowedAssemblages.size() == 0)
			{
				throw new RestException("If a referenced component is not provided, then an allowedAssemblage must be provided");
			}
			for (int assemblageId : allowedAssemblages)
			{
				Get.sememeService().getSememesFromAssemblage(assemblageId).forEach(consumer);
			}
		}
		return results;
	}
}
