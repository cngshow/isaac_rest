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

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.SememeSequenceSet;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.associations.AssociationUtilities;
import gov.vha.isaac.ochre.mapping.data.MappingUtils;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.PaginationUtils;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeDefinition;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersionPage;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;


/**
 * {@link SememeAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.sememeAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class SememeAPIs
{
	private static Logger log = LogManager.getLogger(SememeAPIs.class);

	@Context
	private SecurityContext securityContext;

	/**
	 * Return the RestSememeType of the sememe corresponding to the passed id
	 * @param id The id for which to determine RestSememeType
	 * If an int then assumed to be a sememe NID or sequence
	 * If a String then parsed and handled as a sememe UUID
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A 
	 *  CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return RestSememeType of the sememe corresponding to the passed id. if no corresponding sememe found a RestException is thrown.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeTypeComponent + "{" + RequestParameters.id + "}")  
	public RestSememeType getSememeType(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES);

		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (Get.sememeService().hasSememe(intId.get()))
			{
				return new RestSememeType(Get.sememeService().getSememe(intId.get()).getSememeType());
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified sememe int id NID or sequence does not correspond to"
						+ " an existing sememe chronology. Must pass a UUID or integer NID or sequence that corresponds to an existing sememe chronology.");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer sememeSequence = null;
				if (Get.identifierService().hasUuid(uuidId.get()) && (sememeSequence = Get.identifierService().getSememeSequenceForUuids(uuidId.get())) != 0 
						&& Get.sememeService().hasSememe(sememeSequence))
				{
					return new RestSememeType(Get.sememeService().getSememe(sememeSequence).getSememeType());
				}
				else
				{
					throw new RestException(RequestParameters.id, id, "Specified sememe UUID does not correspond to an existing sememe chronology. Must pass a "
							+ "UUID or integer NID or sequence that corresponds to an existing sememe chronology.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified sememe string id is not a valid UUID identifier.  Must be a UUID, or integer NID or "
						+ "sequence.");
			}
		}
	}

	/**
	 * Returns the chronology of a sememe.  
	 * @param id - A UUID, nid or sememe sequence
	 * @param expand - A comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly', 'nestedSememes', 'referencedDetails'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * 'referencedDetails' causes it to include the type for the referencedComponent, and, if it is a concept or a description sememe, the description of that 
	 * concept - or the description value.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained 
	 * by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the sememe chronology object
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{" + RequestParameters.id + "}")
	public RestSememeChronology getSememeChronology(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken
			) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);

		RestSememeChronology chronology =
				new RestSememeChronology(
						findSememeChronology(id),
						RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable), 
						RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
						Util.validateWorkflowProcess(processId));
		
		return chronology;
	}
	
	/**
	 * Returns a single version of a sememe.
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
	 * if they represent a concept or a description sememe.
	 * @return the sememe version object.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may 
	 * be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{" + RequestParameters.id +"}")
	public RestSememeVersion getSememeVersion(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);

		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		
		@SuppressWarnings("rawtypes")
		SememeChronology sc = findSememeChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, Util.getPreWorkflowStampCoordinate(processIdUUID, sc.getNid()));
		if (sv.isPresent())
		{
			//TODO handle contradictions
			return RestSememeVersion.buildRestSememeVersion(sv.get().value(), RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable), 
					RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable), RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails), processIdUUID);
		}
		else
		{
			throw new RestException(RequestParameters.id, id, "No sememe was found");
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
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence of an assemblage concept
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nested', 'referencedDetails'
	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
	 * if they represent a concept or a description sememe.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken 
	 * may be obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.forAssemblageComponent + "{" + RequestParameters.id +  "}")
	public RestSememeVersionPage getForAssemblage(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.COORDINATE_PARAM_NAMES);

		HashSet<Integer> singleAllowedAssemblage = new HashSet<>();
		singleAllowedAssemblage.add(Util.convertToConceptSequence(id));
		
		UUID processIdUUID = Util.validateWorkflowProcess(processId);
		
		//we don't have a referenced component - our id is assemblage
		SememeVersions versions =
				get(
						null,
						singleAllowedAssemblage,
						pageNum,
						maxPageSize,
						true,
						processIdUUID);

		List<RestSememeVersion> restSememeVersions = new ArrayList<>();
		for (SememeVersion<?> sv : versions.getValues()) {
			restSememeVersions.add(
					RestSememeVersion.buildRestSememeVersion(sv,
							RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
							RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
							processIdUUID));
		}
		RestSememeVersionPage results =
				new RestSememeVersionPage(
						pageNum,
						maxPageSize,
						versions.getTotal(),
						true,
						versions.getTotal() > (pageNum * maxPageSize),
						RestPaths.sememeByAssemblageAppPathComponent + id,
						restSememeVersions.toArray(new RestSememeVersion[restSememeVersions.size()])
						);
		
		return results;
	}
	
	/**
	 * Returns all sememe instances attached to the specified referenced component
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID or nid of a component.  Note that this could be a concept or a sememe reference, hence, sequences are not allowed here.
	 * @param assemblage - An optional assemblage UUID, nid or concept sequence to restrict the type of sememes returned.  If ommitted, assemblages
	 * of all types will be returned.  May be specified multiple times to allow multiple assemblages
	 * @param includeDescriptions - an optional flag to request that description type sememes are returned.  By default, description type 
	 * sememes are not returned, as these are typically retrieved via a getDescriptions call on the Concept APIs.
	 * @param includeAssociations - an optional flag to request that sememes that represent associations are returned.  By default, sememes that represent
	 * associations are not returned, as these are typically retrieved via a getSourceAssociations call on the Association APIs.
	 * @param includeMappings - an optional flag to request that sememes that represent mappings are returned.  By default, sememes that represent
	 * mappings are not returned, as these are typically retrieved via a the Mapping APIs.
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * When referencedDetails is passed, nids will include type information, and certain nids will also include their descriptions,
	 * if they represent a concept or a description sememe.
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be 
	 * obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the sememe version objects.  Note that the returned type here - RestSememeVersion is actually an abstract base class, 
	 * the actual return type will be either a RestDynamicSememeVersion or a RestSememeDescriptionVersion.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.forReferencedComponentComponent + "{" + RequestParameters.id + "}")
	public RestSememeVersion[] getForReferencedComponent(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.assemblage) Set<String> assemblage, 
			@QueryParam(RequestParameters.includeDescriptions) @DefaultValue("false") String includeDescriptions,
			@QueryParam(RequestParameters.includeAssociations) @DefaultValue("false") String includeAssociations,
			@QueryParam(RequestParameters.includeMappings) @DefaultValue("false") String includeMappings,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.processId) String processId,
			@QueryParam(RequestParameters.coordToken) String coordToken) 
			throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.assemblage,
				RequestParameters.includeDescriptions,
				RequestParameters.includeAssociations,
				RequestParameters.includeMappings,
				RequestParameters.expand,
				RequestParameters.processId,
				RequestParameters.COORDINATE_PARAM_NAMES);

		HashSet<Integer> allowedAssemblages = new HashSet<>();
		for (String a : assemblage)
		{
			allowedAssemblages.add(Util.convertToConceptSequence(a));
		}

		return
				get(
						id,
						allowedAssemblages,
						null,  //TODO add API support for the new skip assemblage feature
						RequestInfo.get().shouldExpand(ExpandUtil.chronologyExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
						RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails),
						Boolean.parseBoolean(includeDescriptions.trim()),
						Boolean.parseBoolean(includeAssociations.trim()),
						Boolean.parseBoolean(includeMappings.trim()),
						Util.validateWorkflowProcess(processId));
	}

	/**
	 * Return the full description of a particular sememe - including its intended use, the types of any data columns that will be attached, etc.
	 * @param id - The UUID, nid or concept sequence of the concept that represents the sememe assemblage.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be 
	 * obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return - the full description
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememeDefinitionComponent + "{" + RequestParameters.id + "}")
	public RestDynamicSememeDefinition getSememeDefinition(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.id,
				RequestParameters.COORDINATE_PARAM_NAMES);

		int conceptSequence = Util.convertToConceptSequence(id);
		if (DynamicSememeUsageDescriptionImpl.isDynamicSememe(conceptSequence))
		{
			return new RestDynamicSememeDefinition(DynamicSememeUsageDescriptionImpl.read(conceptSequence));
		}
		else
		{
			//Not annotated as a dynamic sememe.  We have to find a real value to determine if this is used as a static sememe.
			//TODO 3 Dan someday, we will fix the underlying APIs to allow us to know the static sememe typing up front....
			Optional<SememeChronology<? extends SememeVersion<?>>> sc = Get.sememeService().getSememesFromAssemblage(conceptSequence).findAny();
			if (sc.isPresent())
			{
				return new RestDynamicSememeDefinition(DynamicSememeUsageDescriptionImpl.mockOrRead(sc.get()));
			}
		}
		throw new RestException("The specified concept identifier is not configured as a dynamic sememe, and it is not used as a static sememe");
	}

	public static Stream<SememeChronology<? extends SememeVersion<?>>> getSememesForComponentFromAssemblagesFilteredBySememeType(int componentNid, 
			Set<Integer> allowedAssemblageSequences, Set<SememeType> typesToExclude) {
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
	
	public static class SememeVersions {
		private final List<SememeVersion<?>> values;
		private final int approximateTotal;
		
		public SememeVersions(List<SememeVersion<?>> values, int approximateTotal) {
			this.values = values;
			this.approximateTotal = approximateTotal;
		}

		public SememeVersion<?>[] getValues() {
			return values.toArray(new SememeVersion[values.size()]);
		}
		public int getTotal() {
			return approximateTotal;
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
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @return
	 * @throws RestException
	 */
	public static SememeVersions get(
			String referencedComponent,
			Set<Integer> allowedAssemblages,
			final int pageNum,
			final int maxPageSize,
			boolean allowDescriptions,
			UUID processId) throws RestException
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
				Stream<SememeChronology<? extends SememeVersion<?>>> sememes = getSememesForComponentFromAssemblagesFilteredBySememeType(refCompNid.get(), 
						allowedAssemblages, excludedSememeTypes);
				
				int approximateTotal = 0;
				for (Iterator<SememeChronology<? extends SememeVersion<?>>> it = sememes.iterator(); it.hasNext();) {
					if (ochreResults.size() >= (pageNum * maxPageSize)) {
						it.next();
						continue;
					} else {
						@SuppressWarnings("rawtypes")
						SememeChronology chronology = it.next();
						@SuppressWarnings({ "unchecked" })
						Optional<LatestVersion<SememeVersion<?>>> sv = chronology.getLatestVersion(SememeVersionImpl.class, 
								Util.getPreWorkflowStampCoordinate(processId, chronology.getNid()));
						if (sv.isPresent()) {
							//TODO handle contradictions
							ochreResults.add(sv.get().value());
						}
					}

					approximateTotal++;
				}

				return new SememeVersions(PaginationUtils.getResults(PaginationUtils.getResults(ochreResults, pageNum, maxPageSize), pageNum, maxPageSize), approximateTotal);
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
				if (ochreResults.size() >= (pageNum * maxPageSize)) {
					break;
				} else {
					SememeChronology<? extends SememeVersion<?>> chronology = Get.sememeService().getSememe(it.nextInt());
					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<LatestVersion<SememeVersion<?>>> sv = ((SememeChronology)chronology).getLatestVersion(SememeVersionImpl.class, 
							Util.getPreWorkflowStampCoordinate(processId, chronology.getNid()));
					if (sv.isPresent()) {
						ochreResults.add(sv.get().value());
					}
				}
			}

			return new SememeVersions(PaginationUtils.getResults(ochreResults, pageNum, maxPageSize), allSememeSequences.size());
		}
	}
	
	/**
	 * @param referencedComponent - optional - if provided - takes precedence
	 * @param allowedAssemblages - optional - if provided, either limits the referencedComponent search by this type, or, if 
	 * referencedComponent is not provided - focuses the search on just this assemblage
	 * @param skipAssemblages - optional - if provided, any assemblage listed here will not be part of the return.  This takes priority over the allowedAssemblages.
	 * @param expandChronology
	 * @param expandNested
	 * @param expandReferenced
	 * @param allowDescriptions true to include description type sememes, false to skip
	 * @param allowAssociations true to include sememes that represent associations, false to skip
	 * @param allowMappings true to include sememes that represent mappings, false to skip
	 * @param processId if set, specifies that retrieved components should be checked against the specified active
	 * workflow process, and if existing in the process, only the version of the corresponding object prior to the version referenced
	 * in the workflow process should be returned or referenced.  If no version existed prior to creation of the workflow process,
	 * then either no object will be returned or an exception will be thrown, depending on context.
	 * @return
	 * @throws RestException
	 */
	public static RestSememeVersion[] get(String referencedComponent, Set<Integer> allowedAssemblages, Set<Integer> skipAssemblages, boolean expandChronology, 
			boolean expandNested, boolean expandReferenced, boolean allowDescriptions, boolean allowAssociations, boolean allowMappings, UUID processId) 
					throws RestException
	{
		final ArrayList<RestSememeVersion> results = new ArrayList<>();
		Consumer<SememeChronology<? extends SememeVersion<?>>> consumer = new Consumer<SememeChronology<? extends SememeVersion<?>>>()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void accept(@SuppressWarnings("rawtypes") SememeChronology sc)
			{
				if (sc.getSememeType() != SememeType.LOGIC_GRAPH 
						&& sc.getSememeType() != SememeType.RELATIONSHIP_ADAPTOR
						&& (allowDescriptions || sc.getSememeType() != SememeType.DESCRIPTION))
				{
					if (!allowAssociations && AssociationUtilities.isAssociation(sc))
					{
						return;
					}
					if (!allowMappings && MappingUtils.isMapping(sc))
					{
						return;
					}
					if (skipAssemblages != null && skipAssemblages.contains(sc.getAssemblageSequence()))
					{
						return;
					}
					Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, 
							Util.getPreWorkflowStampCoordinate(processId, sc.getNid()));

					if (sv.isPresent()) {
						try
						{
							//TODO handle contradictions
							results.add(RestSememeVersion.buildRestSememeVersion(sv.get().value(), expandChronology, expandNested, expandReferenced, processId));
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
		return results.toArray(new RestSememeVersion[results.size()]);
	}
}
