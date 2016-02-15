package gov.vha.isaac.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.glassfish.hk2.api.MultiException;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;

//TODO make this class go away when these ops get moved

@Path("ts/1.0")
public class RestApi
{

	/**
	 * A simple search interface which is evaluated across all indexed descriptions in the terminology.   
	 * @param query The query to be evaluated.  Will be parsed by the Lucene Query Parser: 
	 * http://lucene.apache.org/core/5_3_1/queryparser/org/apache/lucene/queryparser/classic/package-summary.html#Overview
	 * @return the list of descriptions that matched, along with their score.  Note that the textual value may _NOT_ be included,
	 * if the description that matched is not active on the default path.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/search")
	public List<RestSearchResult> search(@QueryParam("query") String query)
	{
		ArrayList<RestSearchResult> temp = new ArrayList<>();
		try
		{
			for (SearchResult x : LookupService.get().getService(IndexServiceBI.class, "description indexer").query(query, 10))
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<DescriptionSememeImpl>> text = ((SememeChronology) Get.sememeService().getSememe(x.getNid()))
						.getLatestVersion(DescriptionSememeImpl.class, StampCoordinates.getDevelopmentLatest());
				
				if (text.isPresent())
				{
					temp.add(new RestSearchResult(x.getNid(), text.get().value().getText(), x.getScore()));
				}
				else
				{
					temp.add(new RestSearchResult(x.getNid(), "", x.getScore()));
				}
			}
		}
		catch (MultiException e)
		{
			e.printStackTrace();
		}
		return temp;
	}


}
