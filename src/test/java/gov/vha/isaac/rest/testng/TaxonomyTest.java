/**
 * 
 */
package gov.vha.isaac.rest.testng;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.LocalJettyRunner;
import gov.vha.isaac.rest.api1.RestPaths;

/**
 * @author vikas.sharma
 *
 */
public class TaxonomyTest extends JerseyTestNg.ContainerPerClassTest {
	
	@Override
	protected Application configure()
	{
		try
		{
			System.out.println("Launching Jersey within Grizzley for tests");
			new File("target/test.data").mkdirs();
			System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/test.data");
			return LocalJettyRunner.configureJerseyServer();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	@BeforeClass
	public void testDataLoad()
	{
		//Load in the test data
		try
		{
			while (!ApplicationConfig.getInstance().isIsaacReady())
			{
				Thread.sleep(50);
			}
			
			BinaryDataReaderService reader = Get.binaryDataReader(Paths.get("target", "data", "IsaacMetadataAuxiliary.ibdf"));
			CommitService commitService = Get.commitService();
			reader.getStream().forEach((object) -> {
				commitService.importNoChecks(object);
			});
			
			Get.startIndexTask((Class<IndexServiceBI>[])null).get();
		}
		catch (FileNotFoundException | InterruptedException | ExecutionException e)
		{
			Assert.fail("Test data file not found", e);
		}
		Assert.assertTrue(Get.conceptDescriptionText(MetaData.ASSEMBLAGE.getConceptSequence()).equals("assemblage (ISAAC)"));
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
	
	//@Test
	public void testGetConceptVersionTaxonomy() {
		//final String url ="http://localhost:8080/isaac-rest/rest/1/taxonomy/version";
		//RestPaths.taxonomyPathComponent + RestPaths.versionComponent;
		Random randomGenerator = new Random();
		 for (int idx = 1; idx <= 10; ++idx){
		try {
			 
				 
				WebTarget webTarget = target("isaac-rest/rest/"+RestPaths.taxonomyPathComponent + RestPaths.versionComponent).queryParam("childDepth", "1").queryParam("countChildren", "true").queryParam("countChildren", "true")
				.queryParam("expand", "chronology").queryParam("id",randomGenerator.nextInt(50000))
				.queryParam("parentHeight","1").queryParam("stated","true");
				System.out.println("--- URI ---"+webTarget.getUri());
				Invocation.Builder builder = webTarget.request();
				
				Response response = builder.get();
		
				checkFail(response);
				System.out.println("------------------Taxonomy------------------------");
				System.out.println(response.readEntity(String.class));
				System.out.println("-------------------------------------------");
			
		} catch (Exception e) {
			System.out.println(e);
		}
		 }
	}
}
