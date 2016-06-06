/**
 * 
 */
package gov.vha.isaac.rest.junit;

import java.net.URI;
import java.util.Random;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.jersey.client.ClientResponse;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import gov.vha.isaac.rest.api1.RestPaths;

/**
 * @author vikas.sharma
 *
 */
public class TaxonomyUnitTest  extends SSLWSRestClientHelper {

	public static void main(String[] args) throws Exception
	{
		TaxonomyUnitTest taxonomyUnitTest = new TaxonomyUnitTest();
		taxonomyUnitTest.testGetConceptVersionTaxonomy(args[0]);
	}
	
	@Parameters(value="baseURI")
	public void testGetConceptVersionTaxonomy(@Optional("http://localhost:8080/isaac-rest/rest/") String baseURI) {
		final String url = baseURI + RestPaths.taxonomyAPIsPathComponent + RestPaths.versionComponent;
		Random randomGenerator = new Random();
		WebTarget wr = getWebTarget(url);
		UriBuilder builder = UriBuilder.fromPath(url);
		URI uri = builder.build();
		System.out.println(uri);
		for (int idx = 1; idx <= 30; ++idx){
			try {
				ClientResponse clientResponse = wr.queryParam("childDepth", ""+randomGenerator.nextInt(5)).queryParam("countChildren", "true").queryParam("countChildren", "true")
						.queryParam("expand", "chronology").queryParam("id",""+randomGenerator.nextInt(50000))
						.queryParam("parentHeight","1").queryParam("stated","true").request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.get(ClientResponse.class);
				System.out.println(uri);
				System.out.println("Got response status:" + clientResponse.getStatus()	+ ", with jsonResponse:" + clientResponse.readEntity(String.class));		
					//checkFail(clientResponse);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}
}
