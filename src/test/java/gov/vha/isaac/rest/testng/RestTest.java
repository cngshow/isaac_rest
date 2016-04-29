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
package gov.vha.isaac.rest.testng;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.grizzly.http.util.Header;
import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.LocalJettyRunner;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.CoordinatesToken;

/**
 * {@link RestTest}
 * Testing framework for doing full cycle testing - this launches the REST server in a grizzly container, and makes REST requests via a loop
 * back call.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RestTest extends JerseyTestNg.ContainerPerClassTest
{
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

	@Test
	public void testMe()
	{
		//TODO write many more interesting tests, using some sort of pattern like this....
		Response response = target(RestPaths.conceptPathComponent + RestPaths.versionComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request().get();
		
		checkFail(response);
		
		//System.out.println(response.readEntity(String.class));
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
	
	
	
	/**
	 * This test validates that the XML serializers, sememe by-assemblage API and pagination are working correctly 
	 */
	@Test
	public void testPaginatedSememesByAssemblage()
	{
		String xpathExpr = "/restSememeVersions/results/sememeChronology/sememeSequence";
				
		// Test to confirm that requested maxPageSize of results returned
		for (int pageSize : new int[] { 1, 5, 10 }) {
			Response response = target(
					RestPaths.sememePathComponent + RestPaths.byAssemblageComponent + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.maxPageSize, pageSize)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

			String resultXmlString = checkFail(response).readEntity(String.class);

			NodeList nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			
			Assert.assertTrue(nodeList != null && nodeList.getLength() == pageSize);
		}
		
		// Test to confirm that pageNum works
		{
			// Get first page of 10 results
			Response response = target(
					RestPaths.sememePathComponent + RestPaths.byAssemblageComponent + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.maxPageSize, 10)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
			String resultXmlString = checkFail(response).readEntity(String.class);
			NodeList nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			String idOfTenthResultOfFirstTenResultPage = nodeList.item(9).getTextContent();
			
			// Get 10th page of 1 result
			response = target(
					RestPaths.sememePathComponent + RestPaths.byAssemblageComponent + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.pageNum, 10)
					.queryParam(RequestParameters.maxPageSize, 1)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
			
			resultXmlString = checkFail(response).readEntity(String.class);
			nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			String idOfOnlyResultOfTenthResultPage = nodeList.item(0).getTextContent();
			
			Assert.assertTrue(idOfTenthResultOfFirstTenResultPage.equals(idOfOnlyResultOfTenthResultPage));
		}
	}
	
	/**
	 * This test validates that the XML serializers, description search API and pagination are working correctly 
	 */
	@Test
	public void testPaginatedSearchResults()
	{
		String xpathExpr = "/restSearchResults/results/matchNid";
		
		final String descriptionSearch = RestPaths.searchPathComponent + RestPaths.descriptionsComponent;

		// Test to confirm that requested maxPageSize of results returned
		for (int pageSize : new int[] { 2, 3, 8 }) {
			String resultXmlString = checkFail(target(descriptionSearch)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.maxPageSize, pageSize)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			
			NodeList nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			
			Assert.assertTrue(nodeList != null && nodeList.getLength() == pageSize);
		}
		
		// Test to confirm that pageNum works
		{
			// Get first page of 7 results
			String resultXmlString = checkFail(target(descriptionSearch)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.maxPageSize, 7)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			NodeList nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			String idOf7thResultOfFirst7ResultPage = nodeList.item(6).getTextContent();
			
			// Get 7th page of 1 result
			resultXmlString = checkFail(target(descriptionSearch)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.pageNum, 7)
					.queryParam(RequestParameters.maxPageSize, 1)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			nodeList = RestTestUtils.getNodeList(resultXmlString, xpathExpr);
			String idOfOnlyResultOf7thResultPage = nodeList.item(0).getTextContent();
			
			Assert.assertTrue(idOf7thResultOfFirst7ResultPage.equals(idOfOnlyResultOf7thResultPage));
		}
	}
	
	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * nested array data, and various implementation types of the dynamic sememe types.
	 */
	@Test
	public void testArraySememeReturn()
	{
		Response response = target(RestPaths.sememePathComponent + RestPaths.byAssemblageComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		checkFail(response);
		
		response = target(RestPaths.sememePathComponent + RestPaths.byAssemblageComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		
		checkFail(response);
	}
	
	@Test
	public void testReferencedDetailsExpansion()
	{
		Response response = target(RestPaths.sememePathComponent + RestPaths.byReferencedComponentComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		String result = checkFail(response).readEntity(String.class);
		
		Assert.assertFalse(result.contains("<conceptDescription>preferred (ISAAC)</conceptDescription>"));
		Assert.assertFalse(result.contains("</referencedComponentNidObjectType>"));
		Assert.assertFalse(result.contains("<referencedComponentNidDescription>dynamic sememe extension definition (ISAAC)</referencedComponentNidDescription>"));
		
		response = target(RestPaths.sememePathComponent + RestPaths.byReferencedComponentComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
				.queryParam("expand", "chronology,referencedDetails,nestedSememes")
				.queryParam("includeDescriptions", "true")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		result = checkFail(response).readEntity(String.class);
		
		Assert.assertTrue(result.contains("<conceptDescription>preferred (ISAAC)</conceptDescription>"));
		Assert.assertTrue(result.contains("</referencedComponentNidObjectType>"));
		Assert.assertTrue(result.contains("<referencedComponentNidDescription>dynamic sememe extension definition (ISAAC)</referencedComponentNidDescription>"));
	}
	
	
	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * concept data.
	 */
	@Test
	public void testIdReturn()
	{
		final String url = RestPaths.idPathComponent + RestPaths.idTranslateComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString();
		
		Response response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		checkFail(response);

		response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		
		checkFail(response);
	}
	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * concept data.
	 */
	@Test
	public void testConceptReturn()
	{
		final String url = RestPaths.conceptPathComponent + RestPaths.versionComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString();
		
		Response response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		checkFail(response);

		response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		
		checkFail(response);
	}
	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * LogicGraph data.
	 */
	@Test
	public void testLogicGraphReturn()
	{
		final String url = RestPaths.logicGraphPathComponent + RestPaths.versionComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString();
		
		Response response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		
		checkFail(response);

		response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		
		checkFail(response);
	}

	/**
	 * This test validates that the JSON serializer is working correctly with returns that contain
	 * LogicGraph data and validates that the returned Response JSON contains a valid RestSememeLogicGraphVersion
	 */
	@Test
	public void testRestSememeLogicGraphVersionReturn()
	{
		final String url = RestPaths.logicGraphPathComponent + RestPaths.versionComponent +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString();
		Response returnedResponse = target(url)
				//.queryParam(RequestParameters.expand,"version")
				.request().get();

		String output = returnedResponse.readEntity(String.class);

		ObjectMapper mapper = new ObjectMapper();
		
		JsonNode rootNode = null;
		try {
			//System.out.println("testRestSememeLogicGraphVersionReturn() parsing json " + output);

			rootNode = mapper.readValue(output, JsonNode.class);
			
			//System.out.println("testRestSememeLogicGraphVersionReturn() parsed json as " + rootNode.getNodeType() + "\n" + rootNode);
		} catch (IOException e) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() FAILED parsing json.  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
		}

		JsonNode rootType = rootNode.get("@class");
		
		if (rootType.isMissingNode()) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed json object missing @class member");
		} else if (! rootType.asText().equals(RestSememeLogicGraphVersion.class.getName())) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed json of unexpected object type " + rootType.asText());
		} else {
			//System.out.println("testRestSememeLogicGraphVersionReturn() parsed " + rootType.asText() + " object");
		}
		
		final String referencedConceptDescriptionFieldName = "referencedConceptDescription";
		final String referencedConceptDescriptionExpectedValue = "dynamic sememe extension definition (ISAAC)";
		if (! rootNode.has(referencedConceptDescriptionFieldName)) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with no referencedConceptDescription");
		}
		if (rootNode.get(referencedConceptDescriptionFieldName) == null) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with null referencedConceptDescription");
		}
		JsonNode referencedConceptDescriptionNode = rootNode.get(referencedConceptDescriptionFieldName);
		if (! referencedConceptDescriptionNode.asText().equals(referencedConceptDescriptionExpectedValue)) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with unexpected referencedConceptDescription=\"" + referencedConceptDescriptionNode.asText() + "\"");
		}
		//System.out.println("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with referencedConceptDescription of type " + referencedConceptDescriptionNode.getNodeType());
		
		final String rootLogicNodeFieldName = "rootLogicNode";
		if (rootNode.with("rootLogicNode").with("nodeSemantic").get("name") == null || ! rootNode.with("rootLogicNode").with("nodeSemantic").get("name").asText().equals(NodeSemantic.DEFINITION_ROOT.name())) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with missing or invalid " + rootLogicNodeFieldName + ": \"" + rootNode.with("rootLogicNode").with("nodeSemantic").get("name") + "\"!=\"" + NodeSemantic.DEFINITION_ROOT.name() + "\"");
		}
	}

	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * taxonomy data.
	 */
	@Test
	public void testTaxonomyReturn()
	{
		final String url = RestPaths.taxonomyPathComponent + RestPaths.versionComponent;

		Response response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
	//	System.out.println(target(url).request().get().toString());
		
		checkFail(response);

		response = target(url).request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		
		checkFail(response);
	}
	
	
	@Test
	public void testSearchAssemblageRestriction1()
	{
		//Check with UUID
		final String url = RestPaths.searchPathComponent + RestPaths.sememesComponent;

		String result = checkFail(target(url)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getUUID().toString())
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));
		
		//Check with nid
		result = checkFail(target(url)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));
		
		//Check with sequence
		//Check with nid
		result = checkFail(target(url)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getConceptSequence() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));
		
		//sanity check search
		result = checkFail(target(url)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"55")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));
	}
	
	@Test
	public void testSearchExpandsUUID()
	{
		final String sememeSearch = RestPaths.searchPathComponent + RestPaths.sememesComponent;
		final String descriptionSearch = RestPaths.searchPathComponent + RestPaths.descriptionsComponent;
		final String prefixSearch = RestPaths.searchPathComponent + RestPaths.prefixComponent;
		final String byRefSearch = RestPaths.searchPathComponent + RestPaths.byReferencedComponentComponent;
		
		//Make sure it contains a random (type 4) UUID with this pattern...
		//<uuids>12604572-254c-49d2-8d9f-39d485af0fa0</uuids>
		final Pattern pXml = Pattern.compile(".*uuids.{9}-.{4}-4.{3}-.{4}-.{14}uuids.*", Pattern.DOTALL);
		
		//Test expand uuid on/off for each search type

		String result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"1")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());
		
		result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"1")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());
		
		result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic*")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());
		
		result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());
		
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());
		
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());
		
		result = checkFail(target(byRefSearch)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());
		
		result = checkFail(target(byRefSearch)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());
		
		//Spot check for JSON return support:
		//Make sure it contains a random (type 4) UUID with this pattern...
		// "uuids" : [ "bcf22234-a736-5f6b-9ce3-d016594ca5cd" ]
		final Pattern pJson = Pattern.compile(".*uuids.{15}-.{4}-4.{3}-.{4}-.{12}.*", Pattern.DOTALL);
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertTrue(pJson.matcher(result).matches());
		
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertFalse(pJson.matcher(result).matches());
	}
	
	@Test
	public void testSearchExpandsRefConcept()
	{
		final String sememeSearch = RestPaths.searchPathComponent + RestPaths.sememesComponent;
		final String descriptionSearch = RestPaths.searchPathComponent + RestPaths.descriptionsComponent;
		final String prefixSearch = RestPaths.searchPathComponent + RestPaths.prefixComponent;
		final String byRefSearch = RestPaths.searchPathComponent + RestPaths.byReferencedComponentComponent;
		
		//Test expand uuid on/off for each search type

		String result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.query, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_NAME.getPrimordialUuid().toString())
				.queryParam("expand", ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));
		
		result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.query, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_NAME.getPrimordialUuid().toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));
		
		result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));
		
		result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));
		
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));
		
		result = checkFail(target(prefixSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));
		
		result = checkFail(target(byRefSearch)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.queryParam(RequestParameters.maxPageSize, "100")
				.queryParam(RequestParameters.expand, "uuid," + ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(MetaData.MODULE.getPrimordialUuid().toString()));
		
		result = checkFail(target(byRefSearch)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.queryParam(RequestParameters.maxPageSize, "100")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(MetaData.MODULE.getPrimordialUuid().toString()));
	}
	
	@Test
	public void testSearchExpandsRefConceptVersion()
	{
		final String descriptionSearch = RestPaths.searchPathComponent + RestPaths.descriptionsComponent;
		//Test expand uuid on/off for each search type

		String result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept + "," + ExpandUtil.versionsLatestOnlyExpandable)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains("<state>ACTIVE</state>"));
		
		result = checkFail(target(descriptionSearch)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains("<state>ACTIVE</state>"));
	}
	
	@Test
	public void testSearchRecursiveRefComponentLookup()
	{
		final String sememeSearch = RestPaths.searchPathComponent + RestPaths.sememesComponent;

		String result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.query, MetaData.PREFERRED.getNid() + "")
				.queryParam(RequestParameters.treatAsString, "true")
				.queryParam(RequestParameters.maxPageSize, 500)
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));
		
		result = checkFail(target(sememeSearch)
				.queryParam(RequestParameters.query, MetaData.PREFERRED.getNid() + "")
				.queryParam(RequestParameters.treatAsString, "true")
				.queryParam(RequestParameters.maxPageSize, 500)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));
	}
	
	@Test
	public void testDescriptionsFetch()
	{
		final String descriptions = RestPaths.conceptPathComponent +  RestPaths.descriptionsComponent;

		String result = checkFail(target(descriptions + MetaData.USER.getConceptSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		String[] temp = result.split("<restSememeDescriptionVersion>");
		//[0] is header junk
		//[1] is the first dialect
		//[2] is the second dialect
		
		Assert.assertTrue(temp.length == 3);
		for (int i = 1; i < 3; i++)
		{
			String[] temp2 = temp[i].split("<dialects>");
			String preDialect = temp2[0];
			String dialect = temp2[1];
			Assert.assertEquals(temp2.length, 2);
			
			//Validate that the important bit of the description sememe are put together properly
			Assert.assertTrue(preDialect.contains("<assemblageSequence>" + MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence() + "</assemblageSequence>"), "Wrong language");
			Assert.assertTrue(preDialect.contains("<referencedComponentNid>" + MetaData.USER.getNid() + "</referencedComponentNid>"), "Wrong concept");
			Assert.assertTrue(preDialect.contains("<caseSignificanceConceptSequence>" + MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence() 
				+ "</caseSignificanceConceptSequence>"), "Wrong case sentivity");
			Assert.assertTrue(preDialect.contains("<languageConceptSequence>" + MetaData.ENGLISH_LANGUAGE.getConceptSequence() 
				+ "</languageConceptSequence>"), "Wrong language");
			Assert.assertTrue((preDialect.contains("<text>user</text>") || preDialect.contains("<text>user (ISAAC)</text>")), "Wrong text " + preDialect);
			Assert.assertTrue((preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.SYNONYM.getConceptSequence() + "</descriptionTypeConceptSequence>") 
					|| preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.FULLY_SPECIFIED_NAME.getConceptSequence() + "</descriptionTypeConceptSequence>")), 
					"Wrong description type");
			
			//validate that the dialect bits are put together properly
			Assert.assertTrue(dialect.contains("<assemblageSequence>" + MetaData.US_ENGLISH_DIALECT.getConceptSequence() + "</assemblageSequence>"), "Wrong dialect");
			Assert.assertTrue(dialect.contains("<data xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:int\">" + MetaData.PREFERRED.getNid() + "</data>"), "Wrong value");
		}
	}
	
	@Test
	public void testCoordinateTokenRoundTrip() throws Exception
	{
		CoordinatesToken t = new CoordinatesToken(StampCoordinates.getDevelopmentLatest(), LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate(), 
				TaxonomyCoordinates.getStatedTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest(), LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate(), 
						LogicCoordinates.getStandardElProfile()));
		
		String token = t.serialize();
		
		CoordinatesToken read = new CoordinatesToken(token);
		Assert.assertTrue(token.equals(read.serialize()));
	}
}
