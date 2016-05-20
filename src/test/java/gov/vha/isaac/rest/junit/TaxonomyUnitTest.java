/**
 * 
 */
package gov.vha.isaac.rest.junit;

import java.net.URI;
import java.util.Random;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;

/**
 * @author vikas.sharma
 *
 */
public class TaxonomyUnitTest  extends SSLWSRestClientHelper {
	public static final String BASE_URI = "http://localhost:8080/isaac-rest/rest/"+RestPaths.taxonomyPathComponent + RestPaths.versionComponent;
	
	@Test
	public void testGetConceptVersionTaxonomy() {
		//final String url ="http://localhost:8080/isaac-rest/rest/1/taxonomy/version";
		//RestPaths.taxonomyPathComponent + RestPaths.versionComponent;
		RestConceptVersion response = null;
		Random randomGenerator = new Random();
		 for (int idx = 1; idx <= 30; ++idx){
		try {
			WebResource wr = getWebResource(BASE_URI);
			UriBuilder builder = UriBuilder.fromPath(BASE_URI);
			URI uri = builder.build();
			System.out.println(uri);
			ClientResponse clientResponse = wr.uri(uri).queryParam("childDepth", ""+randomGenerator.nextInt(5)).queryParam("countChildren", "true").queryParam("countChildren", "true")
					.queryParam("expand", "chronology").queryParam("id",""+randomGenerator.nextInt(50000))
					.queryParam("parentHeight","1").queryParam("stated","true").type(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.get(ClientResponse.class);
			String jsonResponse = clientResponse.getEntity(String.class);
			System.out.println("Got response status:" + clientResponse.getStatus()
			+ ", with jsonResponse:" + jsonResponse);
			if (clientResponse.getClientResponseStatus() == ClientResponse.Status.OK) {
				response = new Gson().fromJson(jsonResponse, RestConceptVersion.class);
				System.out.println("response:"+response.toString());
				if (response != null) {
					System.out
							.println("==========================================================");
/*					for (String key : response.keySet()) {
						System.out.println(key);
						System.out.println(response.get(key));
						System.out
								.println("==========================================================");
					}*/
				}
			} else {
			}		
				 
				//checkFail(clientResponse);
				System.out.println("------------------Taxonomy------------------------");
				System.out.println(clientResponse.getEntity(String.class));
				System.out.println("-------------------------------------------");
			
		} catch (Exception e) {
			System.out.println(e);
		}
		 }
	}
}
