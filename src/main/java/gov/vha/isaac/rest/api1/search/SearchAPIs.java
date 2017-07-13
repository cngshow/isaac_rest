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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.UserRoleConstants;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.api.index.ComponentSearchResult;
import gov.vha.isaac.ochre.api.index.ConceptSearchResult;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.util.Interval;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.impl.utility.NumberUtilities;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.query.provider.lucene.LuceneDescriptionType;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.DescriptionIndexer;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexer;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.PaginationUtils;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;
import gov.vha.isaac.rest.api1.data.search.RestSearchResultPage;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestInfoUtils;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.session.SecurityUtils;

/**
 * {@link SearchAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Path(RestPaths.searchAPIsPathComponent)
@RolesAllowed({UserRoleConstants.AUTOMATED, UserRoleConstants.SUPER_USER, UserRoleConstants.ADMINISTRATOR, UserRoleConstants.READ_ONLY, UserRoleConstants.EDITOR, UserRoleConstants.REVIEWER, UserRoleConstants.APPROVER, UserRoleConstants.MANAGER})
public class SearchAPIs
{
	private static Logger log = LogManager.getLogger();
	private static final UUID codeConstant = UUID.fromString("803af596-aea8-5184-b8e1-45f801585d17");  //TODO this goes away when we can identify static sememes

	@Context
	private SecurityContext securityContext;

	/**
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param pageNum The pagination page number >= 1 to return
	 * @return limit to pass to query to return requested subset without blowing out lucene.
	 * If requested batch size is less than truncationThreshold then sets limit to truncationThreshold,
	 * making total returned value for small searches more accurate
	 */
	private static int calculateQueryLimit(int maxPageSize, int pageNum) {
		int requestedBatch = maxPageSize * pageNum;
		int calculatedLimit = (int) Math.round(requestedBatch * 3);
		int truncationThreshold = 100;
		return requestedBatch < truncationThreshold ? truncationThreshold : calculatedLimit;
	}
	
	private RestSearchResultPage getRestSearchResultsFromOchreSearchResults(
			List<SearchResult> ochreSearchResults,
			int pageNum,
			int maxPageSize,
			String restPath,
			String query) throws RestException {
		List<RestSearchResult> restSearchResults = new ArrayList<>();
		for (SearchResult ochreSearchResult : PaginationUtils.getResults(ochreSearchResults, pageNum, maxPageSize)) {
			Optional<RestSearchResult> restSearchResultOptional = createRestSearchResult(ochreSearchResult, query);
			if (restSearchResultOptional.isPresent()) {
				restSearchResults.add(restSearchResultOptional.get());
			}
		}
		
		return new RestSearchResultPage(pageNum, maxPageSize, ochreSearchResults.size(), false, ochreSearchResults.size() > (pageNum * maxPageSize),
				restPath, restSearchResults);
	}
	/**
	 * A simple search interface which is evaluated across all indexed descriptions in the terminology.   
	 * @param query The query to be evaluated.  Will be parsed by the Lucene Query Parser: 
	 * http://lucene.apache.org/core/5_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview
	 * @param descriptionType - optional - may not be combined with extendedDescriptionTypeId.  
	 * can be specified as 'fsn', 'synonym' or 'definition' to restrict to a particular description type.
	 * @param extendedDescriptionTypeId - optional - may not be combined with descriptionType.  This would typically be
	 * a concept identifier of a concept that was a LEAF child of the concept 'description type in source terminology (ISAAC)'
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param expand Optional Comma separated list of fields to expand or include directly in the results.  Supports:
	 *  - 'uuid' (return the UUID of the matched sememe, rather than just the nid)
	 *  - 'referencedConcept' (return the conceptChronology  of the nearest concept found by following the referencedComponent references 
	 *  of the matched sememe.  In most cases, this concept  will be the concept that directly contains the sememe - but in some cases, 
	 *  sememes may be nested under other sememes causing this to walk up until it finds a concept)
	 *  - 'versionsLatestOnly' if 'referencedConcept' is included in the expand list, you may also include 'versionsLatestOnly' to return the 
	 *  latest version of the referenced concept chronology.
	 *  - 'versionsAll' if 'referencedConcept is included in the expand list, you may also include 'versionsAll' to return all versions of the 
	 *  referencedConcept.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be 
	 * obtained by a separate (prior) call to getCoordinatesToken().
	 * 
	 * @return the list of descriptions that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the description that matched is not active on the default path.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionsComponent)
	public RestSearchResultPage descriptionSearch(
			@QueryParam(RequestParameters.query) String query,
			@QueryParam(RequestParameters.descriptionType) String descriptionType, 
			@QueryParam(RequestParameters.extendedDescriptionTypeId) String extendedDescriptionTypeId,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.query,
				RequestParameters.descriptionType,
				RequestParameters.extendedDescriptionTypeId,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		if (StringUtils.isBlank(query))
		{
			throw new RestException("The parameter 'query' must contain at least one character");
		}
		LuceneDescriptionType dt = null;
		if (StringUtils.isNotBlank(descriptionType))
		{
			dt = LuceneDescriptionType.parse(descriptionType);
			if (dt == null)
			{
				throw new RestException("If 'descriptionType' is specified, it must be fsn, synonym or definition");
			}
		}
		
		final String restPath =
				RestPaths.searchAppPathComponent + RestPaths.descriptionsComponent
				+ "?" + RequestParameters.query + "=" + query;
		if (StringUtils.isNotBlank(extendedDescriptionTypeId))
		{
			if (dt != null)
			{
				throw new RestException("The parameter 'extendedDescriptionTypeId' may not be combined with a 'descriptionType' parameter");
			}
			UUID extendedDescTypeSequence = Util.convertToConceptUUID(extendedDescriptionTypeId);
			
			int limit = calculateQueryLimit(maxPageSize, pageNum);
			List<SearchResult> ochreSearchResults = LookupService.get().getService(DescriptionIndexer.class).query(query, extendedDescTypeSequence, limit, Long.MAX_VALUE);
			
			return getRestSearchResultsFromOchreSearchResults(
					ochreSearchResults,
					pageNum,
					maxPageSize,
					restPath,
					query);
		} else {
			log.debug("Performing description search for '" + query + "'");
			int limit = calculateQueryLimit(maxPageSize, pageNum);
			List<SearchResult> ochreSearchResults = LookupService.get().getService(DescriptionIndexer.class).query(query, dt, limit, Long.MAX_VALUE);
			return getRestSearchResultsFromOchreSearchResults(
					ochreSearchResults,
					pageNum,
					maxPageSize,
					restPath,
					query);
		}
	}
	
	/**
	 * A search interface that is optimized for prefix searching, such as the searching
	 * that would be done to implement a type-ahead style search. Does not use the Lucene Query parser. 
	 * Every term (or token) that is part of the query string will be required to be found in the result.
	 * 
	 * Note, it is useful to NOT trim the text of the query before it is sent in - if the last word of the query has a
	 * space character following it, that word will be required as a complete term. If the last word of the query does not
	 * have a space character following it, that word will be required as a prefix match only.
	 * 
	 * For example:
	 * The query "family test" will be evaluated as if it were "family test*" - returning results that contain 'Family Testudinidae'
	 * The query "family test " will not match on 'Testudinidae', as test is considered a complete token, and no * is appended.
	 * 
	 * @param query The query to be evaluated. 
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param restrictTo Optional a feature that will restrict the results descriptions attached to a concept that meet one of the specified
	 *   criteria.  Currently, this can be set to 
	 *   - "association" - to only return concepts that define association types
	 *   - "mapset" - to only return concepts that define mapsets
	 *   - "sememe" - to only return concepts that define sememes
	 *   - "metadata" - to only return concepts that are defined in the metadata hierarchy.
	 *  This option can only be set to a single value per call - no combinations are allowed.  
	 * @param mergeOnConcept - Optional - if set to true - only one result will be returned per concept - even if that concept had 2 or more descriptions 
	 *   that matched the query.  When false, you will get a search result for EACH matching description.  When true, you will only get one search result, 
	 *   which is the search result with the best score for that concept (compared to the other search results for that concept)
	 * @param expand Optional Comma separated list of fields to expand or include directly in the results.  Supports:
	 *  - 'uuid' (return the UUID of the matched sememe, rather than just the nid)
	 *  - 'referencedConcept' (return the conceptChronology  of the nearest concept found by following the referencedComponent references 
	 *  of the matched sememe.  In most cases, this concept  will be the concept that directly contains the sememe - but in some cases, 
	 *  sememes may be nested under other sememes causing this to walk up until it finds a concept)
	 *  - 'versionsLatestOnly' if 'referencedConcept' is included in the expand list, you may also include 'versionsLatestOnly' to return the 
	 *  latest version of the referenced concept chronology.
	 *  - 'versionsAll' if 'referencedConcept is included in the expand list, you may also include 'versionsAll' to return all versions of the 
	 *  referencedConcept.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 *
	 * @return the list of descriptions that matched, along with their score. Note that the textual value may _NOT_ be included,
	 * if the description that matched is not active on the default path.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.prefixComponent)
	public RestSearchResultPage prefixSearch(
			@QueryParam(RequestParameters.query) String query,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.restrictTo) String restrictTo,
			@QueryParam(RequestParameters.mergeOnConcept) String mergeOnConcept,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.query,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.restrictTo,
				RequestParameters.mergeOnConcept,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		if (StringUtils.isBlank(query))
		{
			throw new RestException("The parameter 'query' must contain at least one character");
		}
		log.debug("Performing prefix search for '" + query + "'");
		
		boolean mergeOnConcepts = StringUtils.isBlank(mergeOnConcept) ? false : RequestInfoUtils.parseBooleanParameter(RequestParameters.mergeOnConcept, mergeOnConcept);
		boolean metadataRestrict = false;
		
		Predicate<Integer> filter = null;
		if (StringUtils.isNotBlank(restrictTo))
		{
			String temp = restrictTo.toLowerCase(Locale.ENGLISH).trim();
			metadataRestrict = true;
			switch (temp)
			{
				case "association" :
					filter = (nid -> 
					{
						int conSequence = Frills.findConcept(nid);
						if (conSequence >= 0)
						{
							return Frills.definesAssociation(conSequence);
						}
						return false;
					});
					break;
				case "mapset" :
					filter = (nid -> 
					{
						int conSequence = Frills.findConcept(nid);
						if (conSequence >= 0)
						{
							return Frills.definesMapping(conSequence);
						}
						return false;
					});
					break;
				case "sememe" :
					filter = (nid -> 
					{
						int conSequence = Frills.findConcept(nid);
						if (conSequence >= 0)
						{
							//TODO add a sememe on all static sememes so we can identify them.  For now, hard code a few common ones.
							if (MetaData.VUID.getConceptSequence() == conSequence || MetaData.SCTID.getConceptSequence() == conSequence 
									|| Get.identifierService().getConceptSequenceForUuids(codeConstant) == conSequence)
							{
								return true;
							}
							return Frills.definesDynamicSememe(conSequence);
						}
						return false;
					});
					break;
				case "metadata" :
					//metadata restrict is now part of the query construction.
					filter = null;
					break;
				default :
					throw new RestException("restrictTo", "Invalid restriction.  Must be 'association', 'mapset', 'sememe' or 'metadata'");
			}
		}

		DescriptionIndexer indexer = LookupService.get().getService(DescriptionIndexer.class);
		
		int limit = calculateQueryLimit(maxPageSize, pageNum);
		List<SearchResult> ochreSearchResults = indexer.query(query, true, null, limit, Long.MAX_VALUE, filter, metadataRestrict);
		
		if (mergeOnConcepts)
		{
			List<ConceptSearchResult> temp = indexer.mergeResultsOnConcept(ochreSearchResults);
			ochreSearchResults = new ArrayList<>(temp.size());
			for (ConceptSearchResult csr : temp)
			{
				ochreSearchResults.add((SearchResult) csr);
			}
		}
		
		String restPath = RestPaths.searchAppPathComponent + RestPaths.prefixComponent + "?" + RequestParameters.query + "=" + query;
		return getRestSearchResultsFromOchreSearchResults(
				ochreSearchResults,
				pageNum,
				maxPageSize,
				restPath,
				query);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Optional<RestSearchResult> createRestSearchResult(SearchResult sr, String query)
	{
		switch (Get.identifierService().getChronologyTypeForNid(sr.getNid()))
		{
			case CONCEPT:
				ConceptChronology cc = ((ConceptChronology) Get.conceptService().getConcept(sr.getNid()));
				Optional<LatestVersion<ConceptVersion>> concept = cc.getLatestVersion(ConceptVersion.class, RequestInfo.get().getStampCoordinate());
				if (concept.isPresent())
				{
					return Optional.of(new RestSearchResult(sr.getNid(), query, sr.getScore(), concept.get().value().getState(), cc.getConceptSequence()));
				}
				break;
			case SEMEME:
			{
				SememeChronology sc = ((SememeChronology) Get.sememeService().getSememe(sr.getNid()));
				Integer conceptSequence = null;
				if (sr instanceof ConceptSearchResult)
				{
					conceptSequence = ((ConceptSearchResult) sr).getConceptSequence();
				}

				switch (sc.getSememeType())
				{
					case DESCRIPTION:
						Optional<LatestVersion<DescriptionSememe>> text = sc.getLatestVersion(DescriptionSememe.class, RequestInfo.get().getStampCoordinate());
						if (text.isPresent())
						{
							//TODO handle contradictions
							return Optional
									.of(new RestSearchResult(sr.getNid(), text.get().value().getText(), sr.getScore(), text.get().value().getState(), conceptSequence));
						}
						break;
					case LONG:
						Optional<LatestVersion<LongSememe>> longSememe = sc.getLatestVersion(LongSememe.class, RequestInfo.get().getStampCoordinate());
						if (longSememe.isPresent())
						{
							//TODO handle contradictions
							return Optional.of(new RestSearchResult(sr.getNid(), longSememe.get().value().getLongValue() + "", sr.getScore(),
									longSememe.get().value().getState(), conceptSequence));
						}
						break;
					case STRING:
						Optional<LatestVersion<StringSememe>> stringSememe = sc.getLatestVersion(StringSememe.class, RequestInfo.get().getStampCoordinate());
						if (stringSememe.isPresent())
						{
							return Optional.of(new RestSearchResult(sr.getNid(), stringSememe.get().value().getString(), sr.getScore(),
									stringSememe.get().value().getState(), conceptSequence));
						}
						break;
					case DYNAMIC:
						Optional<LatestVersion<DynamicSememe>> ds = sc.getLatestVersion(DynamicSememe.class, RequestInfo.get().getStampCoordinate());
						if (ds.isPresent())
						{
							return Optional
									.of(new RestSearchResult(sr.getNid(), ds.get().value().dataToString(), sr.getScore(), ds.get().value().getState(), conceptSequence));
						}
						break;
					//No point in reading back details on these, they will be exactly what was searched for
					case COMPONENT_NID: case LOGIC_GRAPH:
						//Should never match on these, just let them fall through
					case UNKNOWN: case MEMBER: case RELATIONSHIP_ADAPTOR:
					default :
						Optional<LatestVersion<SememeVersion>> sv = sc.getLatestVersion(SememeVersion.class, RequestInfo.get().getStampCoordinate());
						if (sv.isPresent())
						{
							return Optional.of(new RestSearchResult(sr.getNid(), query.trim(), sr.getScore(), sv.get().value().getState(), conceptSequence));
						}
						break;
				}
				break;
			}
			case UNKNOWN_NID:
			default :
				log.error("Unexpected case of unknown nid type in search result handling! nid: " + sr.getNid());
				break;
			
		}
		return Optional.empty();
	}

	/**
	 * @param query The query to be evaluated.  If the query is numeric (int, float, long, double) , it will be treated as a numeric search.
	 * If the query is a mathematical interval - [4,6] or (5,10] or [4,] it will be handled as a numeric interval.  
	 * If the query is not numeric, and is not a valid interval, it will be treated as a string and parsed by the Lucene Query Parser: 
	 * http://lucene.apache.org/core/5_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview
	 * @param treatAsString Treat the query as a string search, even if it is parseable as a number.  This is useful because 
	 * 'id' type sememes in the data model are always represented as a string, even if they are numeric.
	 * @param sememeAssemblageId (optional) restrict the search to only match on members of the provided sememe assemblage identifier(s).
	 * This should be the identifier of a concept that defines a sememe.  This parameter can be passed multiple times to pass
	 *  multiple sememe assemblage identifiers.  This accepts UUIDs, nids or concept sequences.
	 * @param dynamicSememeColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
	 * ONE and only one sememeAssemblageSequence is provided.  May not be provided if 0 or more than 1 sememeAssemblageSequence values are provided.
	 * This parameter can be passed multiple times to pass multiple column references.  This should be a 0 indexed column number - such as 
	 * 0 or 4.  Information about the columns for a particular sememe (and their index numbers) can be found via the 
	 * sememe/sememe/sememeDefinition/{id}  call.  It only makes sense to pass this parameter when searching within a specific sememe that 
	 * has multiple columns of data.
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param expand Optional Comma separated list of fields to expand or include directly in the results.  Supports:
	 *  - 'uuid' (return the UUID of the matched sememe, rather than just the nid)
	 *  - 'referencedConcept' (return the conceptChronology  of the nearest concept found by following the referencedComponent references 
	 *  of the matched sememe.  In most cases, this concept  will be the concept that directly contains the sememe - but in some cases, 
	 *  sememes may be nested under other sememes causing this to walk up until it finds a concept)
	 *  - 'versionsLatestOnly' if 'referencedConcept' is included in the expand list, you may also include 'versionsLatestOnly' to return the 
	 *  latest version of the referenced concept chronology.
	 *  - 'versionsAll' if 'referencedConcept is included in the expand list, you may also include 'versionsAll' to return all versions of the 
	 *  referencedConcept.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 *
	 * @return  the list of sememes that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the sememe that matched is not active on the default path.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememesComponent)
	public RestSearchResultPage sememeSearch(
			@QueryParam(RequestParameters.query) String query,
			@QueryParam(RequestParameters.treatAsString) Boolean treatAsString,
			@QueryParam(RequestParameters.sememeAssemblageId) Set<String> sememeAssemblageId, 
			@QueryParam(RequestParameters.dynamicSememeColumns) Set<Integer> dynamicSememeColumns, 
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.query,
				RequestParameters.treatAsString,
				RequestParameters.sememeAssemblageId,
				RequestParameters.dynamicSememeColumns,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		String restPath = RestPaths.searchAppPathComponent + RestPaths.sememesComponent
				+ "?" + RequestParameters.query + "=" + query
				+ "&" + RequestParameters.treatAsString + "=" + treatAsString;
		if (sememeAssemblageId != null) {
			for (String id : sememeAssemblageId) {
				restPath += "&" + RequestParameters.sememeAssemblageId + "=" + id;
			}
		}
		if (dynamicSememeColumns != null) {
			for (int col : dynamicSememeColumns) {
				restPath += "&" + RequestParameters.dynamicSememeColumns + "=" + col;
			}
		}
		restPath += (! StringUtils.isBlank(expand) ? ("&" + RequestParameters.expand + "=" + expand) : "");
		
		String searchString = query != null ? query.trim() : null;
		if (StringUtils.isBlank(searchString))
		{
			throw new RestException("The query must contain at least one character");
		}

		int limit = calculateQueryLimit(maxPageSize, pageNum);
	
		if (treatAsString != null && treatAsString.booleanValue())
		{
			//We want to send in this query text as a string, even if it is parseable as a number, because 
			//all "IDs" are stored as string sememes for consistency.
			log.debug("Performing sememe search for '" + query + "' - treating it as a string");
			
			List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
					.query(new DynamicSememeStringImpl(searchString),false, processAssemblageRestrictions(sememeAssemblageId), toArray(dynamicSememeColumns), 
					limit, Long.MAX_VALUE);
			return getRestSearchResultsFromOchreSearchResults(
					ochreSearchResults,
					pageNum,
					maxPageSize,
					restPath,
					query);
		}
		else
		{
			//Try to determine the most sensible way to search.
			//Is it a number?
			boolean wasNumber = true;
			boolean wasInterval = true;
			try
			{			
				List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
						.query(NumberUtilities.wrapIntoRefexHolder(NumberUtilities.parseUnknown(query)), false, 
								processAssemblageRestrictions(sememeAssemblageId), toArray(dynamicSememeColumns), limit, Long.MAX_VALUE);
				return getRestSearchResultsFromOchreSearchResults(
						ochreSearchResults,
						pageNum,
						maxPageSize,
						restPath,
						query);
			}
			catch (NumberFormatException e)
			{
				wasNumber = false;
				//Not a number.  Is it an interval?
				try
				{
					Interval interval = new Interval(searchString);
					List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
							.queryNumericRange(NumberUtilities.wrapIntoRefexHolder(interval.getLeft()), interval.isLeftInclusive(), 
									NumberUtilities.wrapIntoRefexHolder(interval.getRight()), interval.isRightInclusive(),
									processAssemblageRestrictions(sememeAssemblageId), toArray(dynamicSememeColumns), limit, Long.MAX_VALUE);
					return getRestSearchResultsFromOchreSearchResults(
							ochreSearchResults,
							pageNum,
							maxPageSize,
							restPath,
							query);
				}
				catch (NumberFormatException e1)
				{
					wasInterval = false;
					//nope	Run it as a string search.
					List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
							.query(new DynamicSememeStringImpl(searchString),false, processAssemblageRestrictions(sememeAssemblageId), toArray(dynamicSememeColumns), 
									limit, Long.MAX_VALUE);
					return getRestSearchResultsFromOchreSearchResults(
							ochreSearchResults,
							pageNum,
							maxPageSize,
							restPath,
							query);
				}
			}
			finally
			{
				if (wasNumber)
				{
					log.debug("Performed sememe search for '" + query + "' - treating it as a number");
				}
				else if (wasInterval)
				{
					log.debug("Performed sememe search for '" + query + "' - treating it as an interval");
				}
				else
				{
					log.debug("Performed sememe search for '" + query + "' - treating it as a string");
				}
			}
		}
	}
	
	/**
	 * @param nid The nid to search for. Note that this does NOT locate sememes that reference a component as part of the standard 
	 * sememe triplet - (sememeID / Assemblage ID / Referenced Component Id) - those lookups are handled by the  sememe/byReferencedComponent/{id}
	 * API or sememe/byAssemblage/{id} API.  This search locates sememe instances that have a DATA COLUMN that make reference to a sememe, 
	 * such as a ComponentNidSememe, or a Logic Graph.  
	 * An example usage of this API would be to locate the concept that contains a graph that references another concept.  The input value must 
	 * be a nid, sequences and UUIDs are not supported for this operation.
	 * @param sememeAssemblageId (optional) restrict the search to only match on members of the provided sememe assemblage identifier(s).
	 * This should be the identifier of a concept that defines a sememe.  This parameter can be passed multiple times to pass
	 * multiple sememe assemblage identifiers.  This accepts UUIDs, nids or concept sequences.  An example usage would be to restrict the search to 
	 * static logic graphs, as opposed to inferred logic graphs.  To restrict to stated, you would pass the id for the 
	 * 'EL++ stated form assemblage (ISAAC)' concept
	 * @param dynamicSememeColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
	 * ONE and only one sememeAssemblageSequence is provided.  May not be provided if 0 or more than 1 sememeAssemblageSequence values are provided.
	 * This parameter can be passed multiple times to pass multiple column references.  This should be a 0 indexed column number - such as 
	 * 0 or 4.  Information about the columns for a particular sememe (and their index numbers) can be found via the 
	 * sememe/sememe/sememeDefinition/{id}  call.  It only makes sense to pass this parameter when searching within a specific sememe that 
	 * has multiple columns of data.
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param start The index within the full result set from which to begin this result set
	 * @param expand Optional Comma separated list of fields to expand or include directly in the results.  Supports:
	 *  - 'uuid' (return the UUID of the matched sememe, rather than just the nid)
	 *  - 'referencedConcept' (return the conceptChronology  of the nearest concept found by following the referencedComponent references 
	 *  of the matched sememe.  In most cases, this concept  will be the concept that directly contains the sememe - but in some cases, 
	 *  sememes may be nested under other sememes causing this to walk up until it finds a concept)
	 *  - 'versionsLatestOnly' if 'referencedConcept' is included in the expand list, you may also include 'versionsLatestOnly' to return the 
	 *  latest version of the referenced concept chronology.
	 *  - 'versionsAll' if 'referencedConcept is included in the expand list, you may also include 'versionsAll' to return all versions of the 
	 *  referencedConcept.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().
	 *  
	 * @return  the list of sememes that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the sememe that matched is not active on the default path.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.forReferencedComponentComponent)
	public RestSearchResultPage nidReferences(
			@QueryParam(RequestParameters.nid) int nid,
			@QueryParam(RequestParameters.sememeAssemblageId) Set<String> sememeAssemblageId, 
			@QueryParam(RequestParameters.dynamicSememeColumns) Set<Integer> dynamicSememeColumns,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.nid,
				RequestParameters.sememeAssemblageId,
				RequestParameters.dynamicSememeColumns,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		String restPath = RestPaths.searchAppPathComponent + RestPaths.forReferencedComponentComponent
				+ "?" + RequestParameters.nid + "=" + nid;
		if (sememeAssemblageId != null) {
			for (String id : sememeAssemblageId) {
				restPath += "&" + RequestParameters.sememeAssemblageId + "=" + id;
			}
		}
		if (dynamicSememeColumns != null) {
			for (int col : dynamicSememeColumns) {
				restPath += "&" + RequestParameters.dynamicSememeColumns + "=" + col;
			}
		}
		restPath += (! StringUtils.isBlank(expand) ? ("&" + RequestParameters.expand + "=" + expand) : "");
		
		int limit = calculateQueryLimit(maxPageSize, pageNum);

		List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
				.query(nid, processAssemblageRestrictions(sememeAssemblageId), toArray(dynamicSememeColumns), limit, Long.MAX_VALUE);
		return getRestSearchResultsFromOchreSearchResults(
				ochreSearchResults,
				pageNum,
				maxPageSize,
				restPath,
				nid + "");
	}
	

	/**
	 * Do a lookup, essentially, of a component by an internal identifier.  This supports UUIDs, NIDs, and Sequences.
	 * Note that, despite the name of the method, this should not be used to search by external identifiers such as VUIDs or SCTIDs - for those, use 
	 * the sememeSearch api call.
	 * @param query The identifier to look for.  Expected to be parseable as a UUID, or an integer.  
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param expand Optional Comma separated list of fields to expand or include directly in the results.  Supports:
	 *  - 'uuid' (return the UUID of the matched sememe, rather than just the nid)
	 *  - 'referencedConcept' (return the conceptChronology  of the nearest concept found by following the referencedComponent references 
	 *  of the matched sememe.  In most cases, this concept  will be the concept that directly contains the sememe - but in some cases, 
	 *  sememes may be nested under other sememes causing this to walk up until it finds a concept)
	 *  - 'versionsLatestOnly' if 'referencedConcept' is included in the expand list, you may also include 'versionsLatestOnly' to return the 
	 *  latest version of the referenced concept chronology.
	 *  - 'versionsAll' if 'referencedConcept is included in the expand list, you may also include 'versionsAll' to return all versions of the 
	 *  referencedConcept.
	 * @param coordToken specifies an explicit serialized CoordinatesToken string specifying all coordinate parameters. A CoordinatesToken may be obtained by a separate (prior) call to getCoordinatesToken().

	 * @return - the list of items that were found that matched - note that if the passed in UUID matched on a sememe - the returned top level object will
	 * be the concept that references the sememe with the hit.  Scores are irrlevant with this call, you will either have an exact match, or no result.
	 * Typically, there will only be one result - but if you pass a sequence, it may map to a concept and a sememe, so you may get two results.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.idComponent)
	public RestSearchResultPage idSearch(
			@QueryParam(RequestParameters.query) String query,
			@QueryParam(RequestParameters.pageNum) @DefaultValue(RequestParameters.pageNumDefault) int pageNum,
			@QueryParam(RequestParameters.maxPageSize) @DefaultValue(RequestParameters.maxPageSizeDefault) int maxPageSize,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		SecurityUtils.validateRole(securityContext, getClass());

		RequestParameters.validateParameterNamesAgainstSupportedNames(
				RequestInfo.get().getParameters(),
				RequestParameters.query,
				RequestParameters.PAGINATION_PARAM_NAMES,
				RequestParameters.expand,
				RequestParameters.COORDINATE_PARAM_NAMES);

		List<SearchResult> results = new ArrayList<>();
		final String restPath = RestPaths.searchAppPathComponent + RestPaths.idComponent + "?" + RequestParameters.query + "=" + query;
		
		if (StringUtils.isBlank(query))
		{
			throw new RestException("The parameter 'query' must be a UUID or an integer for an id query");
		}
		String temp = query.trim();
		Optional<UUID> uuid = UUIDUtil.getUUID(temp);
		if (uuid.isPresent())
		{
			if (Get.identifierService().hasUuid(uuid.get()))
			{
				results.add(new ComponentSearchResult(Get.identifierService().getNidForUuids(uuid.get()), 1));
			}
		}
		else
		{
			Optional<Integer> intValue = NumericUtils.getInt(temp);
			if (intValue.isPresent())
			{
				if (intValue.get() < 0 && Get.identifierService().getChronologyTypeForNid(intValue.get()) != ObjectChronologyType.UNKNOWN_NID)
				{
					Optional<? extends ObjectChronology<? extends StampedVersion>> obj = Get.identifiedObjectService().getIdentifiedObjectChronology(intValue.get());
					if (obj.isPresent())
					{
						results.add(new ComponentSearchResult(obj.get().getNid(), 1));
					}
				}
				else if (intValue.get() > 0)
				{
					if (Get.conceptService().hasConcept(intValue.get()))
					{
						results.add(new ComponentSearchResult(Get.identifierService().getConceptNid(intValue.get()), 1));
					}
					if (Get.sememeService().hasSememe(intValue.get()))
					{
						results.add(new ComponentSearchResult(Get.identifierService().getSememeNid(intValue.get()), 1));
					}
				}
			}
		}
		
		List<SearchResult> ochreSearchResults = new ArrayList<>();
		for (ConceptSearchResult csr : LookupService.get().getService(DescriptionIndexer.class).mergeResultsOnConcept(results))
		{
			ochreSearchResults.add((SearchResult) csr);
		}

		return getRestSearchResultsFromOchreSearchResults(
				ochreSearchResults,
				pageNum,
				maxPageSize,
				restPath,
				temp);
	}
	
	private Integer[] processAssemblageRestrictions(Set<String> sememeAssemblageIds) throws RestException
	{
		Set<Integer> sequences = new HashSet<>(sememeAssemblageIds.size());
		
		for (String id : sememeAssemblageIds)
		{
			sequences.add(Util.convertToConceptSequence(id));
		}
		
		return toArray(sequences);
	}

	private Integer[] toArray(Set<Integer> ints)
	{
		if (ints == null)
		{
			return null;
		}
		return ints.toArray(new Integer[ints.size()]);
	}
}
