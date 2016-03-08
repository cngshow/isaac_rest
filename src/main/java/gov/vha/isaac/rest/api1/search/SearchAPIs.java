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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.util.Interval;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.impl.utility.NumberUtilities;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.query.provider.lucene.LuceneDescriptionType;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.DescriptionIndexer;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexer;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;
import gov.vha.isaac.rest.api1.session.RequestInfo;

/**
 * {@link SearchAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Path(RestPaths.searchPathComponent)
public class SearchAPIs
{
	private static Logger log = LogManager.getLogger();
	
	/**
	 * A simple search interface which is evaluated across all indexed descriptions in the terminology.   
	 * @param query The query to be evaluated.  Will be parsed by the Lucene Query Parser: 
	 * http://lucene.apache.org/core/5_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview
	 * @param descriptionType - optional - may not be combined with extendedDescriptionTypeId.  
	 * can be specified as 'fsn', 'synonym' or 'definition' to restrict to a particular description type.
	 * @param extendedDescriptionTypeId - optional - may not be combined with descriptionType.  This would typically be
	 * a concept identifier of a concept that was a LEAF child of the concept 'description type in source terminology (ISAAC)'
	 * @param limit The maximum number of results to return
	 * @return the list of descriptions that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the description that matched is not active on the default path.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.descriptionsComponent)
	public List<RestSearchResult> descriptionSearch(@QueryParam("query") String query, @QueryParam("descriptionType") String descriptionType, 
			@QueryParam("extendedDescriptionTypeId") String extendedDescriptionTypeId, @QueryParam("limit") @DefaultValue("10") int limit) throws RestException
	{
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
		
		if (StringUtils.isNotBlank(extendedDescriptionTypeId))
		{
			if (dt != null)
			{
				throw new RestException("The parameter 'extendedDescriptionTypeId' may not be combined with a 'descriptionType' parameter");
			}
			UUID extendedDescTypeSequence = convertToConceptUUID(extendedDescriptionTypeId);
			return processSearchResults(LookupService.get().getService(DescriptionIndexer.class).query(query, extendedDescTypeSequence, limit, null));
		}
		
		log.debug("Performing description serach for '" + query + "'");
		return processSearchResults(LookupService.get().getService(DescriptionIndexer.class).query(query, dt, limit, null));
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
	 * @param limit The maximum number of results to return
	 * @return the list of descriptions that matched, along with their score. Note that the textual value may _NOT_ be included,
	 * if the description that matched is not active on the default path.
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.prefixComponent)
	public List<RestSearchResult> prefixSearch(@QueryParam("query") String query, @QueryParam("limit") @DefaultValue("10") int limit) throws RestException
	{
		if (StringUtils.isBlank(query))
		{
			throw new RestException("The parameter 'query' must contain at least one character");
		}
		log.debug("Performing prefix search for '" + query + "'");
		return processSearchResults(LookupService.get().getService(IndexServiceBI.class, "description indexer").query(query, true, null, limit, null));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<RestSearchResult> processSearchResults(List<SearchResult> searchResults)
	{
		ArrayList<RestSearchResult> temp = new ArrayList<>();
		for (SearchResult sr : searchResults)
		{
			SememeChronology sc = ((SememeChronology) Get.sememeService().getSememe(sr.getNid()));
			
			switch(sc.getSememeType())
			{
				case DESCRIPTION:
					Optional<LatestVersion<DescriptionSememe>> text = sc.getLatestVersion(DescriptionSememe.class, 
							RequestInfo.get().getStampCoordinate());
					if (text.isPresent())
					{
						temp.add(new RestSearchResult(sr.getNid(), text.get().value().getText(), sr.getScore()));
					}
					break;
				case LONG:
					Optional<LatestVersion<LongSememe>> longSememe = sc.getLatestVersion(LongSememe.class, 
							RequestInfo.get().getStampCoordinate());
					if (longSememe.isPresent())
					{
						temp.add(new RestSearchResult(sr.getNid(), longSememe.get().value().getLongValue() + "", sr.getScore()));
					}
					break;
				case STRING:
					Optional<LatestVersion<StringSememe>> stringSememe = sc.getLatestVersion(StringSememe.class, 
							RequestInfo.get().getStampCoordinate());
					if (stringSememe.isPresent())
					{
						temp.add(new RestSearchResult(sr.getNid(), stringSememe.get().value().getString(), sr.getScore()));
					}
					break;
				case DYNAMIC:
					Optional<LatestVersion<DynamicSememe>> ds = sc.getLatestVersion(DynamicSememe.class, RequestInfo.get().getStampCoordinate());
					if (ds.isPresent())
					{
						temp.add(new RestSearchResult(sr.getNid(), ds.get().value().dataToString(), sr.getScore()));
					}
					break;
				//No point in putting details on these, they will be exactly what was searched for
				case COMPONENT_NID: case LOGIC_GRAPH:
				//Should never match on these, just let them fall through
				case UNKNOWN: case MEMBER: case RELATIONSHIP_ADAPTOR:
				default :
					temp.add(new RestSearchResult(sr.getNid(), "", sr.getScore()));
					break;
				
			}
		}
		return temp;
	}
	
	public static UUID convertToConceptUUID(String conceptId) throws RestException
	{
		Optional<UUID> uuid = UUIDUtil.getUUID(conceptId);
		if (uuid.isPresent())
		{
			if (Get.identifierService().hasUuid(uuid.get()) && Get.conceptService().getOptionalConcept(uuid.get()).isPresent())
			{
				return uuid.get();
			}
			else
			{
				throw new RestException("The UUID '" + conceptId + "' Is not known by the system");
			}
		}
		else
		{
			Optional<Integer> numId = NumericUtils.getInt(conceptId);
			if (numId.isPresent() && numId.get() < 0)
			{
				if (numId.get() < 0)
				{
					uuid = Get.identifierService().getUuidPrimordialForNid(numId.get());
					if (uuid.isPresent())
					{
						return uuid.get();
					}
					else
					{
						throw new RestException("The nid '" + conceptId + "' is not known by the system");
					}
				}
				else
				{
					Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = Get.conceptService().getOptionalConcept(numId.get());
					if (c.isPresent())
					{
						return c.get().getPrimordialUuid();
					}
					else
					{
						throw new RestException("The concept sequence '" + conceptId + "' is not known by the system");
					}
				}
			}
			else
			{
				throw new RestException("The id '" + conceptId + "' does not appear to be a valid UUID, NID or Concept Sequence");
			}
		}
	}
	
	/**
	 * @param query The query to be evaluated.  If the query is numeric (int, float, long, double) , it will be treated as a numeric search.
	 * If the query is a mathematical interval - [4,6] or (5,10] or [4,] it will be handled as a numeric interval.  
	 * If the query is not numeric, and is not a valid interval, it will be treated as a string and parsed by the Lucene Query Parser: 
	 * http://lucene.apache.org/core/5_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview
	 * @param treatAsString Treat the query as a string search, even if it is parseable as a number.  This is useful because 
	 * 'id' type sememes in the data model are always represented as a string, even if they are numeric.
	 * @param sememeAssemblageSequence (optional) restrict the search to only match on members of the provided sememe assemblage identifier(s).
	 * This should be the sequence number of the concept that defines the sememe.  This parameter can be passed multiple times to pass
	 *  multiple sememe assemblage identifiers.
	 * @param dynamicSememeColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
	 * ONE and only one sememeAssemblageSequence is provided.  May not be provided if 0 or more than 1 sememeAssemblageSequence values are provided.
	 * This parameter can be passed multiple times to pass multiple column references.  This should be a 0 indexed column number - such as 
	 * 0 or 4.  Information about the columns for a particular sememe (and their index numbers) can be found via the 
	 * sememe/sememe/sememeDefinition/{id}  call.  It only makes sense to pass this parameter when searching within a specific sememe that 
	 * has multiple columns of data.
	 * @param limit The maximum number of results to return
	 * @return  the list of sememes that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the sememe that matched is not active on the default path.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.sememesComponent)
	public List<RestSearchResult> sememeSearch(@QueryParam("query") String query,
			@QueryParam("treatAsString") Boolean treatAsString,
			@QueryParam("sememeAssemblageSequence") Set<Integer> sememeAssemblageSequence, 
			@QueryParam("dynamicSememeColumns") Set<Integer> dynamicSememeColumns, 
			@QueryParam("limit") @DefaultValue("10") int limit) throws RestException
	{
		String searchString = query.trim();
		if (StringUtils.isBlank(searchString))
		{
			throw new RestException("The query must contain at least one character");
		}
				
		if (treatAsString != null && treatAsString.booleanValue())
		{
			//We want to send in this query text as a string, even if it is parseable as a number, because 
			//all "IDs" are stored as string sememes for consistency.
			log.debug("Performing sememe search for '" + query + "' - treating it as a string");
			return processSearchResults(LookupService.get().getService(SememeIndexer.class)
					.query(new DynamicSememeStringImpl(searchString),false, toArray(sememeAssemblageSequence), toArray(dynamicSememeColumns), 
							limit, null));
		}
		else
		{
			//Try to determine the most sensible way to search.
			//Is it a number?
			boolean wasNumber = true;
			boolean wasInterval = true;
			try
			{
				return processSearchResults(LookupService.get().getService(SememeIndexer.class)
						.query(NumberUtilities.wrapIntoRefexHolder(NumberUtilities.parseUnknown(query)), false, 
								toArray(sememeAssemblageSequence), toArray(dynamicSememeColumns), limit, null));
			}
			catch (NumberFormatException e)
			{
				wasNumber = false;
				//Not a number.  Is it an interval?
				try
				{
					Interval interval = new Interval(searchString);
					return processSearchResults(LookupService.get().getService(SememeIndexer.class)
							.queryNumericRange(NumberUtilities.wrapIntoRefexHolder(interval.getLeft()), interval.isLeftInclusive(), 
									NumberUtilities.wrapIntoRefexHolder(interval.getRight()), interval.isRightInclusive(),
									toArray(sememeAssemblageSequence), toArray(dynamicSememeColumns), limit, null));
				}
				catch (NumberFormatException e1)
				{
					wasInterval = false;
					//nope	Run it as a string search.
					return processSearchResults(LookupService.get().getService(SememeIndexer.class)
							.query(new DynamicSememeStringImpl(searchString),false, toArray(sememeAssemblageSequence), toArray(dynamicSememeColumns), 
									limit, null));
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
	 * @param sememeAssemblageSequence (optional) restrict the search to only match on members of the provided sememe assemblage identifier(s).
	 * This should be the sequence number of the concept that defines the sememe.  This parameter can be passed multiple times to pass
	 *  multiple sememe assemblage identifiers.  An example usage would be to restrict the search to static logic graphs, as opposed to 
	 *  inferred logic graphs.  To restrict to stated, you would pass the nid for the 'EL++ stated form assemblage (ISAAC)' concept
	 * @param dynamicSememeColumns (optional) limit the search to the specified columns of attached data.  May ONLY be provided if 
	 * ONE and only one sememeAssemblageSequence is provided.  May not be provided if 0 or more than 1 sememeAssemblageSequence values are provided.
	 * This parameter can be passed multiple times to pass multiple column references.  This should be a 0 indexed column number - such as 
	 * 0 or 4.  Information about the columns for a particular sememe (and their index numbers) can be found via the 
	 * sememe/sememe/sememeDefinition/{id}  call.  It only makes sense to pass this parameter when searching within a specific sememe that 
	 * has multiple columns of data.
	 * @param limit The maximum number of results to return
	 * @return  the list of sememes that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the sememe that matched is not active on the default path.
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.byReferencedComponentComponent)
	public List<RestSearchResult> nidReferences(@QueryParam("nid") int nid,
			@QueryParam("sememeAssemblageSequence") Set<Integer> sememeAssemblageSequence, 
			@QueryParam("dynamicSememeColumns") Set<Integer> dynamicSememeColumns, 
			@QueryParam("limit") @DefaultValue("10") int limit) throws RestException
	{
		return processSearchResults(LookupService.get().getService(SememeIndexer.class)
				.query(nid, toArray(sememeAssemblageSequence), toArray(dynamicSememeColumns), limit, null));
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
