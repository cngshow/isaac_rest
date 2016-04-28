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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.PrimitiveIterator;
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
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
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
import gov.vha.isaac.rest.api.data.PaginationUtils;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeDefinition;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersions;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link SememeAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.sememePathComponent)
public class SememeAPIs
{
	/**
	 * Return the RestSememeType of the sememe corresponding to the passed id
	 * @param id The id for which to determine RestSememeType
	 * If an int then assumed to be a sememe NID or sequence
	 * If a String then parsed and handled as a sememe UUID
	 * @return RestSememeType of the sememe corresponding to the passed id. if no corresponding sememe found a RestException is thrown.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeTypeComponent + "{" + RequestParameters.id + "}")  
	public RestSememeType getSememeType(@PathParam(RequestParameters.id) String id) throws RestException
	{
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (Get.sememeService().hasSememe(intId.get()))
			{
				return new RestSememeType(Get.sememeService().getSememe(intId.get()).getSememeType());
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified sememe int id NID or sequence does not correspond to an existing sememe chronology. Must pass a UUID or integer NID or sequence that corresponds to an existing sememe chronology.");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer sememeSequence = null;
				if (Get.identifierService().hasUuid(uuidId.get()) && (sememeSequence = Get.identifierService().getSememeSequenceForUuids(uuidId.get())) != 0 && Get.sememeService().hasSememe(sememeSequence))
				{
					return new RestSememeType(Get.sememeService().getSememe(sememeSequence).getSememeType());
				}
				else
				{
					throw new RestException(RequestParameters.id, id, "Specified sememe UUID does not correspond to an existing sememe chronology. Must pass a UUID or integer NID or sequence that corresponds to an existing sememe chronology.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified sememe string id is not a valid UUID identifier.  Must be a UUID, or integer NID or sequence.");
			}
		}
	}

	/**
	 * Returns the chronology of a sememe.  
	 * @param id - A UUID, nid or sememe sequence
	 * @param expand - A comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * 'referencedDetails' causes it to include the type for the referencedComponent, and, if it is a concept, the description of that concept.
	 * @return the sememe chronology object
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{" + RequestParameters.id + "}")
	public RestSememeChronology getSememeChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand
			) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		RestSememeChronology chronology =
				new RestSememeChronology(
						findSememeChronology(id),
						RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
		
		return chronology;
	}
	
	/**
	 * Returns a single version of a sememe.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * @return the sememe version object.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id +"}")
	public RestSememeVersion getSememeVersion(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestInfo.get().readExpandables(expand);

		@SuppressWarnings("rawtypes")
		SememeChronology sc = findSememeChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
		if (sv.isPresent())
		{
			return RestSememeVersion.buildRestSememeVersion(sv.get().value(), RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails));
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
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The pagination maximum page size >= 0 to return
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nested', 'referencedDetails'
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * TODO this needs to be paged 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.byAssemblageComponent + "{" + RequestParameters.id +  "}")
	public RestSememeVersions getByAssemblage(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		HashSet<Integer> temp = new HashSet<>();
		temp.add(Util.convertToConceptSequence(id));
		
		//we don't have a referenced component - our id is assemblage
		SememeVersions versions =
				get(
						null,
						temp,
						pageNum,
						maxPageSize,
						true);
		List<RestSememeVersion> restSememeVersions = new ArrayList<>();
		for (SememeVersion<?> sv : versions.getValues()) {
			restSememeVersions.add(
					RestSememeVersion.buildRestSememeVersion(sv,
							RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
		}
		RestSememeVersions results =
				new RestSememeVersions(
						maxPageSize,
						pageNum,
						versions.getTotal(),
						RestPaths.sememeByAssemblageAppPathComponent + id,
						restSememeVersions
						);
		
		return results;
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
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The pagination maximum page size >= 0 to return
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.byReferencedComponentComponent + "{" + RequestParameters.id + "}")
	public RestSememeVersions getByReferencedComponent(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.assemblage) Set<String> assemblage, 
			@QueryParam(RequestParameters.includeDescriptions) @DefaultValue("false") String includeDescriptions,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand) 
			throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		
		HashSet<Integer> allowedAssemblages = new HashSet<>();
		for (String a : assemblage)
		{
			allowedAssemblages.add(Util.convertToConceptSequence(a));
		}

		SememeVersions ochreSememeVersions = 
				get(
						id,
						allowedAssemblages,
						pageNum,
						maxPageSize,
						Boolean.parseBoolean(includeDescriptions.trim()));
		List<RestSememeVersion> restSememeVersions = new ArrayList<>();
		for (SememeVersion<?> sv : ochreSememeVersions.getValues()) {
			restSememeVersions.add(
					RestSememeVersion.buildRestSememeVersion(
							sv,
							RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
		}
		return new RestSememeVersions(
				pageNum,
				maxPageSize,
				ochreSememeVersions.getTotal(),
				RestPaths.sememeByAssemblageAppPathComponent + id,
				restSememeVersions
				);
	}

	/**
	 * Return the full description of a particular sememe - including its intended use, the types of any data columns that will be attached, etc.
	 * @param id - The UUID, nid or concept sequence of the concept that represents the sememe assemblage.
	 * @return - the full description
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeDefinitionComponent + "{" + RequestParameters.id + "}")
	public RestDynamicSememeDefinition getSememeDefinition(@PathParam(RequestParameters.id) String id) throws RestException
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

	public static Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblagesFilteredBySememeType(int componentNid, Set<Integer> allowedAssemblageSequences, Set<SememeType> typesToExclude) {
		SememeSequenceSet sememeSequences = Get.sememeService().getSememeSequencesForComponentFromAssemblages(componentNid, allowedAssemblageSequences);
		if (typesToExclude == null || typesToExclude.size() == 0) {
			return sememeSequences.stream().mapToObj((int sememeSequence) -> Get.sememeService().getSememe(sememeSequence));
		} else {
			final ArrayList<SememeChronology<? extends SememeVersion<?>>> filteredList = new ArrayList<>();
			for (PrimitiveIterator.OfInt it = sememeSequences.getIntIterator(); it.hasNext();) {
				SememeChronology<? extends SememeVersion<?>> chronology = Get.sememeService().getSememe(it.nextInt());
				boolean exclude = false;
				for (SememeType type : typesToExclude) {
					if (chronology.getSememeType() == type) {
						exclude = true;
						break;
					}
				}

				if (! exclude) {
					filteredList.add(chronology);
				}
			}

			return filteredList.stream();
		}
	}
//
//	private static Stream<SememeChronology<? extends SememeVersion<?>>> getSememesFromAssemblageFilteredBySememeType(int assemblageConceptSequence, Set<SememeType> typesToExclude) throws RestException {
//		Stream<SememeChronology<? extends SememeVersion<?>>> unfilteredStream = Get.sememeService().getSememesFromAssemblage(assemblageConceptSequence);
//		if (typesToExclude == null || typesToExclude.size() == 0) {
//			return unfilteredStream;
//		}
//		final ArrayList<SememeChronology<? extends SememeVersion<?>>> filteredList = new ArrayList<>();
//		Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
//		{
//			@Override
//			public void accept(@SuppressWarnings("rawtypes") SememeChronology sc)
//			{
//				if (typesToExclude != null) {
//					for (SememeType type : typesToExclude) {
//						if (sc.getSememeType() == type) {
//							return;
//						}
//					}
//				}
//				
//				filteredList.add(sc);
//			}
//		};
//		
//		unfilteredStream.forEach(consumer);
//
//        return filteredList.stream();
//    }
	
	public static class SememeVersions {
		private final List<SememeVersion<?>> values;
		private final int total;
		
		public SememeVersions(List<SememeVersion<?>> values, int total) {
			this.values = values;
			this.total = total;
		}

		public List<SememeVersion<?>> getValues() {
			return values;
		}
		public int getTotal() {
			return total;
		}
	}
	/**
	 * @param referencedComponent - optional - if provided - takes precedence
	 * @param assemblage - optional - if provided, either limits the referencedComponent search by this type, or, if 
	 * referencedComponent is not provided - focuses the search on just this assemblage
	 * @param expandChronology
	 * @param expandNested
	 * @param expandReferenced
	 * @param allowDescriptions true to include description type sememes, false to skip
	 * @return
	 * @throws RestException
	 */
	public static SememeVersions get(
			String referencedComponent,
			Set<Integer> allowedAssemblages,
			final int pageNum,
			final int maxPageSize,
			boolean allowDescriptions) throws RestException
	{
		PaginationUtils.validateParameters(pageNum, maxPageSize);

		Set<SememeType> excludedSememeTypes = new HashSet<>();
		excludedSememeTypes.add(SememeType.LOGIC_GRAPH);
		excludedSememeTypes.add(SememeType.RELATIONSHIP_ADAPTOR);
		if (! allowDescriptions) {
			excludedSememeTypes.add(SememeType.DESCRIPTION);
		}
		
		final List<SememeVersion<?>> ochreResults = new ArrayList<>();

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
				Stream<SememeChronology<? extends SememeVersion<?>>> sememes = getSememesForComponentFromAssemblagesFilteredBySememeType(refCompNid.get(), allowedAssemblages, excludedSememeTypes);
				
				for (Iterator<SememeChronology<? extends SememeVersion<?>>> it = sememes.iterator(); it.hasNext();) {
					if (ochreResults.size() > (pageNum * maxPageSize)) {
						break;
					} else {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Optional<LatestVersion<SememeVersion<?>>> sv = ((SememeChronology)it.next()).getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
						if (sv.isPresent()) {
							ochreResults.add(sv.get().value());
						}
					}
				}

				int lowerBound = (pageNum - 1) * maxPageSize;
				int upperBound = pageNum * maxPageSize;
				if (lowerBound >= ochreResults.size()) {
					// If lowerBound larger than entire list return empty list
					lowerBound = 0;
					upperBound = 0;
				} else if (upperBound > ochreResults.size()) {
					// if upperBound larger than entire list return only to end of list
					upperBound = ochreResults.size();
				}
				return new SememeVersions(ochreResults.subList(lowerBound, upperBound), sememes.toArray().length);
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
			
			SememeSequenceSet allSememeSequences = new SememeSequenceSet();
			for (int assemblageId : allowedAssemblages)
			{
				allSememeSequences.addAll(Get.sememeService().getSememeSequencesFromAssemblage(assemblageId).stream());
			}
			
			for (PrimitiveIterator.OfInt it = allSememeSequences.getIntIterator(); it.hasNext();) {
				if (ochreResults.size() > (pageNum * maxPageSize)) {
					break;
				} else {
					SememeChronology<? extends SememeVersion<?>> chronology = Get.sememeService().getSememe(it.nextInt());
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<LatestVersion<SememeVersion<?>>> sv = ((SememeChronology)chronology).getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());
					if (sv.isPresent()) {
						ochreResults.add(sv.get().value());
					}
				}
			}

			int lowerBound = (pageNum - 1) * maxPageSize;
			int upperBound = pageNum * maxPageSize;
			if (lowerBound >= ochreResults.size()) {
				// If lowerBound larger than entire list return empty list
				lowerBound = 0;
				upperBound = 0;
			} else if (upperBound > ochreResults.size()) {
				// if upperBound larger than entire list return only to end of list
				upperBound = ochreResults.size();
			}
			return new SememeVersions(ochreResults.subList(lowerBound, upperBound), allSememeSequences.size());
		}
	}
	
	/**
	 * @param referencedComponent - optional - if provided - takes precedence
	 * @param assemblage - optional - if provided, either limits the referencedComponent search by this type, or, if 
	 * referencedComponent is not provided - focuses the search on just this assemblage
	 * @param expandChronology
	 * @param expandNested
	 * @param expandReferenced
	 * @param allowDescriptions true to include description type sememes, false to skip
	 * @return
	 * @throws RestException
	 */
	public static List<RestSememeVersion> get(String referencedComponent, Set<Integer> allowedAssemblages, boolean expandChronology, boolean expandNested, 
		boolean expandReferenced, boolean allowDescriptions) throws RestException
	{
		final ArrayList<RestSememeVersion> results = new ArrayList<>();
		Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
		{
			@Override
			public void accept(@SuppressWarnings("rawtypes") SememeChronology sc)
			{
				if (sc.getSememeType() != SememeType.LOGIC_GRAPH 
						&& sc.getSememeType() != SememeType.RELATIONSHIP_ADAPTOR
						&& (allowDescriptions || sc.getSememeType() != SememeType.DESCRIPTION))
				{
					@SuppressWarnings("unchecked")
					Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, RequestInfo.get().getStampCoordinate());

					if (sv.isPresent()) {
						try
						{
							results.add(RestSememeVersion.buildRestSememeVersion(sv.get().value(), expandChronology, expandNested, expandReferenced));
						}
						catch (RestException e)
						{
							throw new RuntimeException("Unexpected error", e);
						}
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
