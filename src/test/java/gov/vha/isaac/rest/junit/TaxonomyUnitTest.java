/**
 * 
 */
package gov.vha.isaac.rest.junit;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.grizzly.http.util.Header;
import org.glassfish.jersey.logging.LoggingFeature;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import gov.vha.isaac.rest.api1.RestPaths;

/**
 * @author vikas.sharma
 *
 */
public class TaxonomyUnitTest  extends SSLWSRestClientHelper {
	Logger logger = Logger.getLogger(getClass().getName());
	public static void main(String[] args) throws Exception
	{
		TaxonomyUnitTest taxonomyUnitTest = new TaxonomyUnitTest();
		taxonomyUnitTest.testGetConceptVersionTaxonomy(args[0]);
	}
	
	@Parameters(value="baseURI")
	public void testGetConceptVersionTaxonomy(@Optional("http://localhost:8080/isaac-rest/rest/") String baseURI) throws Exception {
		final String url = baseURI + RestPaths.taxonomyAPIsPathComponent + RestPaths.versionComponent;
		Random randomGenerator = new Random();
		Feature feature = new LoggingFeature(logger, Level.INFO, null, null);
		WebTarget wt = getWebTarget(url);
		wt.register(feature);
		for (int idx = 1; idx <= 30; ++idx){
			try {
				Response response = wt.queryParam("childDepth", ""+randomGenerator.nextInt(5))
						.queryParam("countChildren", "true")
						.queryParam("countChildren", "true")
						.queryParam("expand", "chronology")
						.queryParam("id",""+randomGenerator.nextInt(50000))
						.queryParam("parentHeight","1").queryParam("stated","true")
						.request()
						.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
				checkFail(response).readEntity(String.class);
				} catch (Exception e) {
					System.out.println(e.getMessage());
					throw new Exception(e);
			}
		}
	}
	private Response checkFail(Response response)
	{
		if (response.getStatus() != Status.OK.getStatusCode())
		{
			Assert.fail("Response code " + response.getStatus() + " - " + Status.fromStatusCode(response.getStatus())
			+ response.readEntity(String.class));
		}
		return response;
	}
}
