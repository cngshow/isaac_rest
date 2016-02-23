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
import java.util.UUID;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.codehaus.plexus.util.StringUtils;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.ochre.query.provider.lucene.LuceneDescriptionType;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.DescriptionIndexer;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;

/**
 * {@link SearchAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Path(RestPaths.searchPathComponent)
public class SearchAPIs
{
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
		return processSearchResults(LookupService.get().getService(IndexServiceBI.class, "description indexer").query(query, true, null, limit, null));
	}
	
	private List<RestSearchResult> processSearchResults(List<SearchResult> searchResults)
	{
		ArrayList<RestSearchResult> temp = new ArrayList<>();
		for (SearchResult sr : searchResults)
		{
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<DescriptionSememeImpl>> text = ((SememeChronology) Get.sememeService().getSememe(sr.getNid()))
					.getLatestVersion(DescriptionSememeImpl.class, StampCoordinates.getDevelopmentLatest());
			
			if (text.isPresent())
			{
				temp.add(new RestSearchResult(sr.getNid(), text.get().value().getText(), sr.getScore()));
			}
			else
			{
				temp.add(new RestSearchResult(sr.getNid(), "", sr.getScore()));
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
	
	//TODO implement sememe search APIS
}
