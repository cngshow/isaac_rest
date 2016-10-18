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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail.ProcessStatus;
import gov.vha.isaac.ochre.workflow.provider.WorkflowProvider;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.LocalJettyRunner;
import gov.vha.isaac.rest.api.data.wrappers.RestBoolean;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.RestEditToken;
import gov.vha.isaac.rest.api1.data.RestId;
import gov.vha.isaac.rest.api1.data.RestSystemInfo;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersion;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersionPage;
import gov.vha.isaac.rest.api1.data.association.RestAssociationTypeVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBase;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.concept.RestConceptCreateData;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.api1.data.enumerations.IdType;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBase;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;
import gov.vha.isaac.rest.api1.data.search.RestSearchResultPage;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreateData;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdateData;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersionPage;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.systeminfo.RestIdentifiedObjectsResult;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowComponentToStampMapEntry;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinition;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMapEntry;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.tokens.CoordinatesToken;
import gov.vha.isaac.rest.tokens.CoordinatesTokens;
import gov.vha.isaac.rest.tokens.EditToken;
import gov.vha.isaac.rest.tokens.EditTokens;

/**
 * {@link RestTest}
 * Testing framework for doing full cycle testing - this launches the REST server in a grizzly container, and makes REST requests via a loop
 * back call.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RestTest extends JerseyTestNg.ContainerPerClassTest
{
	private static final String BPMN_FILE_PATH = "/gov/vha/isaac/ochre/workflow/provider/StaticRestTestingDefinition.bpmn2";

	private final static String taxonomyCoordinateRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.taxonomyCoordinatePathComponent;
	private final static String stampCoordinateRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.stampCoordinatePathComponent;
	private final static String languageCoordinateRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.languageCoordinatePathComponent;
	private final static String logicCoordinateRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.logicCoordinatePathComponent;
	private final static String descriptionSearchRequestPath = RestPaths.searchAPIsPathComponent + RestPaths.descriptionsComponent;
	private final static String taxonomyRequestPath = RestPaths.taxonomyAPIsPathComponent + RestPaths.versionComponent;
	private final static String editTokenRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.editTokenComponent;

	private final static String sememeSearchRequestPath = RestPaths.searchAPIsPathComponent + RestPaths.sememesComponent;
	private final static String prefixSearchRequestPath = RestPaths.searchAPIsPathComponent + RestPaths.prefixComponent;
	private final static String byRefSearchRequestPath = RestPaths.searchAPIsPathComponent + RestPaths.byReferencedComponentComponent;
	
	private final static String conceptDescriptionsRequestPath = RestPaths.conceptAPIsPathComponent +  RestPaths.descriptionsComponent;
	
	private final static String conceptVersionRequestPath = RestPaths.conceptAPIsPathComponent +  RestPaths.versionComponent;

	private static final String coordinatesTokenRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.coordinatesTokenComponent;

	private final static String sememeByAssemblageRequestPath = RestPaths.sememeAPIsPathComponent + RestPaths.byAssemblageComponent;

	private final static String sememeByReferencedComponentRequestPath = RestPaths.sememeAPIsPathComponent + RestPaths.byReferencedComponentComponent;

	private static final String TEST_SSO_TOKEN = "Test_User:super_user,editor,read_only,approver,administrator,reviewer,manager";

	@Override
	protected Application configure()
	{
		try
		{
			System.out.println("Launching Jersey within Grizzley for tests");
			new File("target/test.data").mkdirs();
			System.setProperty(DATA_STORE_ROOT_LOCATION_PROPERTY, "target/test.data");
			WorkflowProvider.BPMN_PATH = BPMN_FILE_PATH;
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
			commitService.postProcessImportNoChecks();

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
		Response response = target(conceptVersionRequestPath +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request().get();

		checkFail(response);

		//System.out.println(response.readEntity(String.class));
	}

	private Response assertFail(Response response)
	{
		if (response.getStatus() == Status.OK.getStatusCode())
		{
			Assert.fail("Should have failed but did not. Response code " + response.getStatus() + " - " + Status.fromStatusCode(response.getStatus())
			+ response.readEntity(String.class));
		}
		
		return response;
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
	private WebTarget target(String url, Map<String, Object> testParameters)
	{
		WebTarget target = target(url);
		if (testParameters != null) {
			for (Map.Entry<String, Object> testParameter : testParameters.entrySet()) {
				target = target.queryParam(testParameter.getKey(), testParameter.getValue());
			}
		}

		return target;
	}

	private static Map<String, Object> buildParams(@SuppressWarnings("unchecked") Map.Entry<String, Object>...parameters)
	{
		Map<String, Object> map = new HashMap<>();
		for (Map.Entry<String, Object> testParameter : parameters) {
			map.put(testParameter.getKey(), testParameter.getValue());
		}

		return map;
	}
	private static Map.Entry<String, Object> param(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key,  value);
		return map.entrySet().iterator().next();
	}

	private static String getCaughtParameterValidationExceptionMessage(String badParamName, String badParamValue, Throwable t) {
		for (Throwable ex : getAllExceptionsAndCauses(t)) {
			if (ex.getLocalizedMessage().contains("The parameter '" + badParamName + "' with value '[" + badParamValue + "]'  resulted in the error: Invalid or unsupported parameter name")) {
				return ex.getLocalizedMessage();
			}
		}
		
		return null;
	}
	
	private static List<Throwable> getAllExceptionsAndCauses(Throwable t) {
		List<Throwable> list = new ArrayList<>();

		if (t != null) {
			if (t.getCause() == null || t.getCause() == t) {
				list.add(t);
			} else {
				list.addAll(getAllExceptionsAndCauses(t.getCause()));
			}
		}
		
		return list;
	}

	private int getIntegerIdForUuid(UUID uuid, String outputType) {
		final String url = RestPaths.idAPIsPathComponent + RestPaths.idTranslateComponent +
				uuid.toString();
		Response response = target(url)
				.queryParam(RequestParameters.inputType, "uuid")
				.queryParam(RequestParameters.outputType, outputType)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String idXml = checkFail(response).readEntity(String.class);
		RestId restId = XMLUtils.unmarshalObject(RestId.class, idXml);
		return Integer.parseInt(restId.value);
	}

	private RestSememeDescriptionVersion[] getDescriptionsForConcept(Object id) {
		return getDescriptionsForConcept((Map<String, Object>)null, id);
	}

	private RestSememeDescriptionVersion[] getDescriptionsForConcept(Map<String, Object> params, Object id) {
		WebTarget webTarget = target(conceptDescriptionsRequestPath + id.toString());
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
			}
		}
		Response getDescriptionVersionsResponse =
				webTarget.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String descriptionVersionsResult = checkFail(getDescriptionVersionsResponse).readEntity(String.class);
		return XMLUtils.unmarshalObjectArray(RestSememeDescriptionVersion.class, descriptionVersionsResult);
	}

	public static String DEFAULT_EDIT_TOKEN_STRING = null;
	public String getDefaultEditTokenString() {
		if (DEFAULT_EDIT_TOKEN_STRING == null) {
			Response getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
					.queryParam(RequestParameters.ssoToken, TEST_SSO_TOKEN)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
			String getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
			RestEditToken restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);

			DEFAULT_EDIT_TOKEN_STRING = restEditTokenObject.token;
		}

		return DEFAULT_EDIT_TOKEN_STRING;
	}
	
	private RestWorkflowDefinition getDefaultWorkflowDefinition() {
		Response getDefinitionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.allDefinitions)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getDefinitionsResponseResult = checkFail(getDefinitionsResponse).readEntity(String.class);
		RestWorkflowDefinition[] availableDefinitions = XMLUtils.unmarshalObjectArray(RestWorkflowDefinition.class, getDefinitionsResponseResult);

		Assert.assertNotNull(availableDefinitions);
		Assert.assertTrue(availableDefinitions.length > 0);
		Assert.assertNotNull(availableDefinitions[0]);
		
		return availableDefinitions[0];
	}
	
	private String getCurrentProcessState(UUID processId) {
		Response getProcessHistoryResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.history)
				.queryParam(RequestParameters.processId , processId.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getProcessHistoryResponseResult = checkFail(getProcessHistoryResponse).readEntity(String.class);
		RestWorkflowProcessHistory[] processHistories = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistory.class, getProcessHistoryResponseResult);

		if (processHistories != null && processHistories.length > 0) {
			return processHistories[processHistories.length - 1].getOutcomeState();
		} else {
			return null;
		}
	}
	
	private RestWriteResponse removeComponentFromProcess(EditToken token, int component) {
		Integer processComponentSpecificationData = new Integer(
				component);
		String xml = null;
		try {
			xml = XMLUtils.marshallObject(processComponentSpecificationData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response removeComponentResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.removeComponent)
				.queryParam(RequestParameters.editToken, token.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		String removeComponentResponseResult = checkFail(removeComponentResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, removeComponentResponseResult);
		return writeResponse;
	}

	// PLACE TEST METHODS BELOW HERE
	@Test
	public void testEditToken() {
		Response getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
				.queryParam(RequestParameters.ssoToken, TEST_SSO_TOKEN)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
		RestEditToken restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);
		
		EditToken retrievedEditToken = null;
		try {
			retrievedEditToken = EditTokens.getOrCreate(restEditTokenObject.token);
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
		
		Assert.assertNull(retrievedEditToken.getWorkflowProcessId());
		
		// Test EditToken serialization/deserialization
		String retrievedEditTokenString = retrievedEditToken.getSerialized();
		EditToken newEditToken = null;
		try {
			newEditToken = EditTokens.getOrCreate(retrievedEditTokenString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		Assert.assertNotNull(newEditToken, "Failed creating EditToken from serialized EditToken: token=" + retrievedEditToken + ", string=" + retrievedEditTokenString);
		Assert.assertEquals(newEditToken.getAuthorSequence(), retrievedEditToken.getAuthorSequence());
		Assert.assertEquals(newEditToken.getModuleSequence(), retrievedEditToken.getModuleSequence());
		Assert.assertEquals(newEditToken.getPathSequence(), retrievedEditToken.getPathSequence());
		Assert.assertEquals(newEditToken.getWorkflowProcessId(), retrievedEditToken.getWorkflowProcessId());
		
		// Test retrieval of an EditToken with a modifying processId parameter
		UUID testWfProcessUuid = UUID.randomUUID();
		getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
				.queryParam(RequestParameters.ssoToken, TEST_SSO_TOKEN)
				.queryParam(RequestParameters.processId , testWfProcessUuid.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
		restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);

		retrievedEditToken = null;
		try {
			retrievedEditToken = EditTokens.getOrCreate(restEditTokenObject.token);
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
		
		Assert.assertEquals(retrievedEditToken.getWorkflowProcessId(), testWfProcessUuid);
	}

	@Test
	public void testWorkflowAPIs()
	{
		// Get an editToken string
		Response getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
				.queryParam(RequestParameters.ssoToken, TEST_SSO_TOKEN)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
		RestEditToken restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);
		
		// Construct and EditToken object from editToken String
		EditToken editToken = null;
		try {
			editToken = EditTokens.getOrCreate(restEditTokenObject.token);
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
		
		Optional<UUID> userUuidOptional = Get.identifierService().getUuidPrimordialFromConceptSequence(editToken.getAuthorSequence());
		Assert.assertTrue(userUuidOptional.isPresent());
		UUID userUuid = userUuidOptional.get();
		RestWorkflowDefinition defaultDefinition = getDefaultWorkflowDefinition();
		Assert.assertNotNull(defaultDefinition.getId());
		Assert.assertEquals("VetzWorkflow", defaultDefinition.getName());
		Assert.assertEquals("VetzWorkflow", defaultDefinition.getBpmn2Id());
		Assert.assertEquals("org.jbpm", defaultDefinition.getNamespace());
		Assert.assertEquals("1.2", defaultDefinition.getVersion());
		Assert.assertEquals(4, defaultDefinition.getRoles().size());

		UUID definitionId = defaultDefinition.getId();
				
		// Pass new editToken string to available (processes)
		Response getAvailableProcessesResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.available)
				.queryParam(RequestParameters.definitionId , definitionId.toString())
				.queryParam(RequestParameters.editToken, editToken.serialize())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getAvailableProcessesResponseResult = checkFail(getAvailableProcessesResponse).readEntity(String.class);
		RestWorkflowProcessHistoriesMapEntry[] availableProcesses = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistoriesMapEntry.class, getAvailableProcessesResponseResult);
		// We may have not created any processes, so there may be no available processes returned unless the DB has not been cleaned prior to testing
		// Deactivating this test because restarting test without a mvn clean may otherwise fail
		//Assert.assertTrue(availableProcesses == null || availableProcesses.length == 0);
		
		// Test process creation
		UUID random = UUID.randomUUID();
		String wfProcessName = "Test WF Process Name (" + random + ")";
		String wfProcessDescription = "Test WF Process Description (" + random + ")";
		RestWorkflowProcessBaseCreate newProcessData = new RestWorkflowProcessBaseCreate(
				definitionId,
				wfProcessName,
				wfProcessDescription);
		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newProcessData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response createProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.createPathComponent + RestPaths.createProcess)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String createProcessResponseResult = checkFail(createProcessResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, createProcessResponseResult);
		
		Assert.assertNotNull(writeResponse);
		Assert.assertNotNull(writeResponse.uuid);
		UUID createdProcessUUID = writeResponse.uuid;
		Assert.assertNotNull(writeResponse.editToken);
		//RestEditToken renewedEditToken = writeResponse.editToken;
		
		// Get renewed EditToken with newly-created processId
		getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
				.queryParam(RequestParameters.editToken, writeResponse.editToken.token)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
		restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);
		
		// Construct and EditToken object from editToken String
		editToken = null;
		try {
			editToken = EditTokens.getOrCreate(restEditTokenObject.token);
		} catch (RestException e) {
			throw new RuntimeException(e);
		}

		// Check for created process using getProcess()
		Response getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		RestWorkflowProcess process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		Assert.assertNotNull(process.getId());
		Assert.assertNotNull(process.getCreatorId());
		Assert.assertEquals(process.getCreatorId(), userUuid);
		Assert.assertTrue(process.getTimeCreated() > 0);
		Assert.assertTrue(process.getTimeLaunched() < 0); // Process should be DEFINED but not LAUNCHED
		Assert.assertTrue(process.getTimeCancelledOrConcluded() < 0);
		Assert.assertNotNull(process.getProcessStatus());
		Assert.assertEquals(process.getProcessStatus(), new RestWorkflowProcessStatusType(ProcessStatus.DEFINED));
		
		// Confirm that request for non-existent process should fail
		Response getBadProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, UUID.randomUUID().toString()) // Garbage processId
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		assertFail(getBadProcessResponse);
		
		// Check for created process in retrieved available
		getAvailableProcessesResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.available)
				.queryParam(RequestParameters.definitionId , definitionId.toString())
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getAvailableProcessesResponseResult = checkFail(getAvailableProcessesResponse).readEntity(String.class);
		availableProcesses = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistoriesMapEntry.class, getAvailableProcessesResponseResult);
		// We have created a process, so at least that process should be returned
		Assert.assertNotNull(availableProcesses);
		Assert.assertTrue(availableProcesses.length > 0);
		RestWorkflowProcessHistory historyFromAvailableProcesses = null;
		boolean foundNewlyCreatedProcessAmongstRetrievedProcesses = false;
		for (RestWorkflowProcessHistoriesMapEntry restWorkflowProcessHistoriesMapEntry : availableProcesses) {
			if (restWorkflowProcessHistoriesMapEntry.getKey().getId().equals(createdProcessUUID)) {
				foundNewlyCreatedProcessAmongstRetrievedProcesses = true;
				Assert.assertNotNull(restWorkflowProcessHistoriesMapEntry.getValue());
				Assert.assertTrue(restWorkflowProcessHistoriesMapEntry.getValue().length > 0);
				historyFromAvailableProcesses = restWorkflowProcessHistoriesMapEntry.getValue()[0];
				Assert.assertNotNull(historyFromAvailableProcesses);
				break;
			}
		}
		Assert.assertTrue(foundNewlyCreatedProcessAmongstRetrievedProcesses);
		Assert.assertNotNull(historyFromAvailableProcesses);
		Assert.assertNotNull(historyFromAvailableProcesses.getId());
		Assert.assertNotNull(historyFromAvailableProcesses.getProcessId());
		Assert.assertEquals(historyFromAvailableProcesses.getProcessId(), createdProcessUUID);
		Assert.assertNotNull(historyFromAvailableProcesses.getUserId());
		Assert.assertEquals(historyFromAvailableProcesses.getUserId(), userUuid);
		//Assert.assertTrue(history.getTimeAdvanced() < 0); // Process created, but not advanced // TODO retest after bug fix
		Assert.assertNotNull(historyFromAvailableProcesses.getInitialState()); // i.e. "Assigned"
		Assert.assertEquals(historyFromAvailableProcesses.getInitialState(), "Assigned"); // Depends on definition
		Assert.assertNotNull(historyFromAvailableProcesses.getAction()); // i.e. "Create Workflow Process"
		Assert.assertEquals(historyFromAvailableProcesses.getAction(), "Create Workflow Process"); // Depends on definition
		Assert.assertNotNull(historyFromAvailableProcesses.getOutcomeState()); // i.e. "Ready for Edit"
		Assert.assertEquals(historyFromAvailableProcesses.getOutcomeState(), "Ready for Edit"); // Depends on definition
		Assert.assertTrue(historyFromAvailableProcesses.getComment() == null || StringUtils.isBlank(historyFromAvailableProcesses.getComment())); // TODO apply a content test when comment field added to RestWorkflowProcessBaseCreate
		
		// Get history and compare with history from get available
		Response getProcessHistoryResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.history)
				.queryParam(RequestParameters.processId , createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getProcessHistoryResponseResult = checkFail(getProcessHistoryResponse).readEntity(String.class);
		RestWorkflowProcessHistory[] processHistories = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistory.class, getProcessHistoryResponseResult);
		// We have created process, so at least one history entry should exist
		Assert.assertNotNull(processHistories);
		Assert.assertTrue(processHistories.length > 0);
		RestWorkflowProcessHistory historyFromHistories = processHistories[0];
		Assert.assertNotNull(historyFromHistories);
		Assert.assertTrue(historyFromAvailableProcesses.equals(historyFromHistories));
		
		// Confirm that get history returns no histories for bogus processId
		Response getBadProcessHistoryResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.history)
				.queryParam(RequestParameters.processId , UUID.randomUUID().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getBadProcessHistoryResponseResult = checkFail(getBadProcessHistoryResponse).readEntity(String.class);
		RestWorkflowProcessHistory[] badProcessHistories = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistory.class, getBadProcessHistoryResponseResult);
		Assert.assertTrue(badProcessHistories == null || badProcessHistories.length == 0);
		

		// Get available actions for created process
		Response getAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.queryParam(RequestParameters.processId , createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getAvailableActionsResponseResult = checkFail(getAvailableActionsResponse).readEntity(String.class);
		RestWorkflowAvailableAction[] availableActions = XMLUtils.unmarshalObjectArray(RestWorkflowAvailableAction.class, getAvailableActionsResponseResult);
		Assert.assertNotNull(availableActions);
		Assert.assertTrue(availableActions.length > 0);
		final String editAction = "Edit"; // definition-specific
		final String cancelWorkflowAction = "Cancel Workflow"; // definition-specific
		boolean foundEditAction = false; // definition-specific
		boolean foundCancelWorkflowAction = false; // definition-specific
		for (RestWorkflowAvailableAction availableAction : availableActions) {
			Assert.assertNotNull(availableAction.getId());
			Assert.assertNotNull(availableAction.getDefinitionId());
			Assert.assertEquals(availableAction.getDefinitionId(), definitionId);
			Assert.assertNotNull(availableAction.getInitialState());
			Assert.assertNotNull(availableAction.getAction());  // "Edit" or "Cancel Workflow" (definition-specific)
			Assert.assertTrue(availableAction.getAction().equals(editAction) || availableAction.getAction().equals(cancelWorkflowAction)); // definition-specific
			if (availableAction.getAction().equals(editAction)) { // definition-specific
				foundEditAction = true;
			} else if (availableAction.getAction().equals(cancelWorkflowAction)) { // definition-specific
				foundCancelWorkflowAction = true;
			} else {
				Assert.fail("Unexpected available action \"" +  availableAction.getAction() + "\"");
			}
			Assert.assertNotNull(availableAction.getOutcomeState());
			Assert.assertNotNull(availableAction.getRole());
			Assert.assertTrue(UserRole.safeValueOf(availableAction.getRole().getEnumId()).isPresent());
			Assert.assertTrue(
					editToken.getRoles().contains(UserRole.safeValueOf(availableAction.getRole().getEnumId()).get())
					|| editToken.getRoles().contains(UserRole.SUPER_USER));
		}
		Assert.assertTrue(foundEditAction); // definition-specific
		Assert.assertTrue(foundCancelWorkflowAction); // definition-specific

		// Confirm that call fails for bogus processId
		Response getBadAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions)
				.queryParam(RequestParameters.processId , UUID.randomUUID().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		assertFail(getBadAvailableActionsResponse);
		
		// Acquire lock on process.  This should Fail because it's automatically locked on create.
		String lockingRequestType = Boolean.toString(true);
		Response lockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.lock)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(lockingRequestType));
		assertFail(lockProcessResponse);

		// Release lock on process
		lockingRequestType = Boolean.toString(false);
		Response unlockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.lock)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(lockingRequestType));
		String unlockProcessResponseResult = checkFail(unlockProcessResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, unlockProcessResponseResult);
		RestEditToken renewedEditToken = writeResponse.editToken;
		Assert.assertNotNull(renewedEditToken);
		
		// Acquire lock on process
		lockingRequestType = Boolean.toString(true);
		lockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.lock)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(lockingRequestType));
		String lockProcessResponseResult = checkFail(lockProcessResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, lockProcessResponseResult);
		renewedEditToken = writeResponse.editToken;
		Assert.assertNotNull(renewedEditToken);
		
		// The following tests are definition-specific
		
		// Advance process to Edit.  Should fail because no components added yet.
		RestWorkflowProcessAdvancementData processAdvancementData = new RestWorkflowProcessAdvancementData(
				editAction,
				"An edit action comment");
		xml = null;
		try {
			xml = XMLUtils.marshallObject(processAdvancementData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// This should fail because no components have been added
		Response advanceProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.advanceProcess)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		assertFail(advanceProcessResponse);
		
		// Create a concept in this workflow process
		final int parent1Sequence = getIntegerIdForUuid(MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		final int parent2Sequence = getIntegerIdForUuid(MetaData.ENGLISH_LANGUAGE.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		
		final int requiredDescriptionsLanguageSequence = getIntegerIdForUuid(MetaData.ENGLISH_LANGUAGE.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		final int requiredDescriptionsExtendedTypeSequence = requiredDescriptionsLanguageSequence;
		
		final UUID randomUuid = UUID.randomUUID();

		final String fsn = "fsn for test concept " + randomUuid.toString();
		final String pt = "preferred term for test concept " + randomUuid.toString();
		
		final List<Integer> parentIds = new ArrayList<>();
		parentIds.add(parent1Sequence);
		parentIds.add(parent2Sequence);
		
		List<Integer> preferredDialects = new ArrayList<>();
		preferredDialects.add(Get.identifierService().getConceptSequenceForUuids(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid()));
		preferredDialects.add(Get.identifierService().getConceptSequenceForUuids(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid()));

		RestConceptCreateData newConceptData = new RestConceptCreateData(
				parentIds,
				fsn,
				requiredDescriptionsLanguageSequence,
				requiredDescriptionsExtendedTypeSequence,
				preferredDialects);

		xml = null;
		try {
			xml = XMLUtils.marshallObject(newConceptData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		Response createConceptResponse = target(RestPaths.conceptCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newConceptSequenceWrapperXml = createConceptResponse.readEntity(String.class);
		RestWriteResponse newConceptSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newConceptSequenceWrapperXml);
		int newConceptSequence = newConceptSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newConceptSequence > 0);
		
		try {
			editToken = EditTokens.renew(editToken);
		} catch (RestException e) {
			throw new RuntimeException(e);
		}
		
		int newConceptNid = Get.identifierService().getConceptNid(newConceptSequence);
		
		// Get process after adding components
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		
		// Get list of components in process
		Set<Integer> componentsInProcessBeforeRemovingComponent = new HashSet<>();
		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
			componentsInProcessBeforeRemovingComponent.add(restWorkflowComponentToStampMapEntry.getKey());
		}
		Assert.assertTrue(componentsInProcessBeforeRemovingComponent.size() > 0);
	
		// Remove one of the components in the process
		String componentNid = Integer.toString(componentsInProcessBeforeRemovingComponent.iterator().next());
		Response removeComponentResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.removeComponent)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(componentNid));
		String removeComponentResponseResult = checkFail(removeComponentResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, removeComponentResponseResult);
		renewedEditToken = writeResponse.editToken;
		Assert.assertNotNull(renewedEditToken);
		
		// Retrieve process after removing component
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		
		Set<Integer> componentsInProcessAfterRemovingComponent = new HashSet<>();
		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
			componentsInProcessAfterRemovingComponent.add(restWorkflowComponentToStampMapEntry.getKey());
		}
		Assert.assertTrue(! componentsInProcessAfterRemovingComponent.contains(componentNid));
		Assert.assertTrue(componentsInProcessAfterRemovingComponent.size() == (componentsInProcessBeforeRemovingComponent.size() - 1));
	
		// Get process to check for added components
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		boolean foundCreatedConceptNidInProcess = false;
		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
			if (restWorkflowComponentToStampMapEntry.getKey() == newConceptNid) {
				foundCreatedConceptNidInProcess = true;
				break;
			}
		}
		Assert.assertTrue(foundCreatedConceptNidInProcess);

		// Attempt to advance process to edit.  Should work now that components have been added.
		xml = null;
		try {
			xml = XMLUtils.marshallObject(processAdvancementData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		advanceProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent + RestPaths.updatePathComponent + RestPaths.advanceProcess)
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		String advanceProcessResponseResult = checkFail(advanceProcessResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, advanceProcessResponseResult);
		renewedEditToken = writeResponse.editToken;
		Assert.assertNotNull(renewedEditToken);
		
		// Get current process after advancement
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process)
				.queryParam(RequestParameters.processId, createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		Assert.assertNotNull(process.getId());
		Assert.assertNotNull(process.getCreatorId());
		Assert.assertEquals(process.getCreatorId(), userUuid);
		Assert.assertTrue(process.getTimeCreated() > 0);
		Assert.assertTrue(process.getTimeLaunched() > 0); // TODO debug this failure. process.getTimeLaunched() should be > 0
		Assert.assertTrue(process.getTimeCancelledOrConcluded() < 0);
		Assert.assertNotNull(process.getProcessStatus());
		Assert.assertEquals(process.getProcessStatus(), new RestWorkflowProcessStatusType(ProcessStatus.LAUNCHED));
		
		// Get available actions after advancement
		getAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.queryParam(RequestParameters.processId , createdProcessUUID.toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getAvailableActionsResponseResult = checkFail(getAvailableActionsResponse).readEntity(String.class);
		availableActions = XMLUtils.unmarshalObjectArray(RestWorkflowAvailableAction.class, getAvailableActionsResponseResult);
		Assert.assertNotNull(availableActions);
		Assert.assertTrue(availableActions.length > 0);
		final String qaFailsAction = "QA Fails";
		boolean foundQaFailsAction = false;
		//final String cancelWorkflowAction = "cancelWorkflowAction";
		foundCancelWorkflowAction = false;
		final String qaPassesAction = "QA Passes";
		boolean foundQaPassesAction = false;
		for (RestWorkflowAvailableAction availableAction : availableActions) {
			Assert.assertNotNull(availableAction.getId());
			Assert.assertNotNull(availableAction.getDefinitionId());
			Assert.assertEquals(availableAction.getDefinitionId(), definitionId);
			Assert.assertNotNull(availableAction.getInitialState()); // "Ready for Review"
			Assert.assertEquals(availableAction.getInitialState(), "Ready for Review");
			Assert.assertNotNull(availableAction.getAction()); 		// "QA Fails" or "Cancel Workflow" or "QA Passes"
			Assert.assertTrue(
					availableAction.getAction().equals(qaFailsAction)
					|| availableAction.getAction().equals(cancelWorkflowAction)
					|| availableAction.getAction().equals(qaPassesAction));
			if (availableAction.getAction().equals(qaFailsAction)) {
				foundQaFailsAction = true;
				Assert.assertEquals(availableAction.getOutcomeState(), "Ready for Edit");
			} else if (availableAction.getAction().equals(cancelWorkflowAction)) {
				foundCancelWorkflowAction = true;
				Assert.assertEquals(availableAction.getOutcomeState(), "Canceled During Review");
			} else if (availableAction.getAction().equals(qaPassesAction)) {
				foundQaPassesAction = true;
				Assert.assertEquals(availableAction.getOutcomeState(), "Ready for Approve");
			} else {
				Assert.fail("Unexpected available action \"" +  availableAction.getAction() + "\"");
			}
			Assert.assertNotNull(availableAction.getOutcomeState()); // "Ready for Edit" or "Canceled During Review" or "Ready for Approve"
			Assert.assertNotNull(availableAction.getRole());
			Assert.assertTrue(UserRole.safeValueOf(availableAction.getRole().getEnumId()).isPresent());
			Assert.assertTrue(
					editToken.getRoles().contains(UserRole.safeValueOf(availableAction.getRole().getEnumId()).get())
					|| editToken.getRoles().contains(UserRole.SUPER_USER));
		}
		Assert.assertTrue(foundQaFailsAction);
		Assert.assertTrue(foundCancelWorkflowAction);
		Assert.assertTrue(foundQaPassesAction);
	}
	
	@Test
	public void testSememeAPIs()
	{
		// Create a random string to confirm target data are relevant
		final UUID randomUuid = UUID.randomUUID();

		// Construct new description data object
		final int referencedConceptNid = getIntegerIdForUuid(MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid(), "nid");
		final int initialCaseSignificanceConceptSequence = getIntegerIdForUuid(MetaData.DESCRIPTION_CASE_SENSITIVE.getPrimordialUuid(), "conceptSequence");
		final int initialLanguageConceptSequence = getIntegerIdForUuid(MetaData.SPANISH_LANGUAGE.getPrimordialUuid(), "conceptSequence");
		final int initialDescriptionTypeConceptSequence = getIntegerIdForUuid(MetaData.SYNONYM.getPrimordialUuid(), "conceptSequence");
		final String initialDescriptionText = "An initial description text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";

//		final int referencedConceptNid = getIntegerIdForUuid(MetaData.AMT_MODULE.getPrimordialUuid(), "nid");
//		final int initialCaseSignificanceConceptSequence = getIntegerIdForUuid(MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getPrimordialUuid(), "conceptSequence");
//		final int initialLanguageConceptSequence = getIntegerIdForUuid(MetaData.FRENCH_LANGUAGE.getPrimordialUuid(), "conceptSequence");
//		final int initialDescriptionTypeConceptSequence = getIntegerIdForUuid(MetaData.SYNONYM.getPrimordialUuid(), "conceptSequence");
//		final String initialDescriptionText = "An initial description text for AMT_MODULE (" + randomUuid + ")";
		/*
		 * int caseSignificanceConceptSequence,
			int languageConceptSequence,
			String text,
			int descriptionTypeConceptSequence,
//			Integer extendedDescriptionTypeConceptSequence,
			Collection<Integer> preferredInDialectAssemblagesIds,
			Collection<Integer> acceptableInDialectAssemblagesIds,
			int referencedComponentNid
		 */
		RestSememeDescriptionCreateData initialDescriptionData =
				new RestSememeDescriptionCreateData(
						initialCaseSignificanceConceptSequence,
						initialLanguageConceptSequence,
						initialDescriptionText,
						initialDescriptionTypeConceptSequence,
						null,
						null,
						referencedConceptNid);
		String xml = null;
		try {
			xml = XMLUtils.marshallObject(initialDescriptionData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// POST new description data object
		Response createDescriptionResponse = target(RestPaths.descriptionCreatePathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		checkFail(createDescriptionResponse);
		String descriptionSememeSequenceWrapperXml = createDescriptionResponse.readEntity(String.class);
		final RestWriteResponse descriptionSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, descriptionSememeSequenceWrapperXml);
		final int descriptionSememeSequence = descriptionSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(descriptionSememeSequence != 0);
		
		// Retrieve all descriptions referring to referenced concept
		RestSememeDescriptionVersion[] conceptDescriptionsObject = getDescriptionsForConcept(referencedConceptNid);
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		RestSememeDescriptionVersion matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().getSememeSequence() == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		// Validate description fields
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.getCaseSignificanceConceptSequence(), initialCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.getText(), initialDescriptionText);
		Assert.assertEquals(matchingVersion.getDescriptionTypeConceptSequence(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.getLanguageConceptSequence(), initialLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().getReferencedComponentNid(), referencedConceptNid);
		
		// Construct description update data object
		final int newCaseSignificanceConceptSequence = getIntegerIdForUuid(MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getPrimordialUuid(), "conceptSequence");
		final int newLanguageConceptSequence = getIntegerIdForUuid(MetaData.FRENCH_LANGUAGE.getPrimordialUuid(), "conceptSequence");
		//final int newDescriptionTypeConceptSequence = getIntegerIdForUuid(MetaData.SYNONYM.getPrimordialUuid(), "conceptSequence");
		final String newDescriptionText = "A new description text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";

		RestSememeDescriptionUpdateData newDescriptionData =
				new RestSememeDescriptionUpdateData(
						newCaseSignificanceConceptSequence,
						newLanguageConceptSequence,
						newDescriptionText,
						initialDescriptionTypeConceptSequence,
						true);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(newDescriptionData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response updateDescriptionResponse = target(RestPaths.descriptionUpdatePathComponent + descriptionSememeSequence)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateDescriptionResponse);

		// Retrieve all descriptions referring to referenced concept
		conceptDescriptionsObject = getDescriptionsForConcept(referencedConceptNid);
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().getSememeSequence() == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		// Validate description fields
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.getCaseSignificanceConceptSequence(), newCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.getText(), newDescriptionText);
		Assert.assertEquals(matchingVersion.getDescriptionTypeConceptSequence(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.getLanguageConceptSequence(), newLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().getReferencedComponentNid(), referencedConceptNid);

		RestBoolean inactiveState = new RestBoolean(false);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(inactiveState);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response deactivateDescriptionResponse = target(RestPaths.writePathComponent +  RestPaths.componentComponent + RestPaths.updatePathComponent 
				+ RestPaths.updateStateComponent + descriptionSememeSequenceWrapper.nid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(deactivateDescriptionResponse);
		// Retrieve all descriptions referring to referenced concept
		conceptDescriptionsObject =
				getDescriptionsForConcept(
						buildParams(param(RequestParameters.allowedStates, State.INACTIVE.getAbbreviation())),
						referencedConceptNid);
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().getSememeSequence() == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.getCaseSignificanceConceptSequence(), newCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.getText(), newDescriptionText);
		Assert.assertEquals(matchingVersion.getDescriptionTypeConceptSequence(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.getLanguageConceptSequence(), newLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().getReferencedComponentNid(), referencedConceptNid);
		Assert.assertEquals(matchingVersion.getSememeVersion().getState(), new RestStateType(State.INACTIVE));
	}
	
	@Test
	public void testConceptAPIs()
	{
		final int parent1Sequence = getIntegerIdForUuid(MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		final int parent2Sequence = getIntegerIdForUuid(MetaData.ENGLISH_LANGUAGE.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		
		final int requiredDescriptionsLanguageSequence = getIntegerIdForUuid(MetaData.ENGLISH_LANGUAGE.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.name());
		final int requiredDescriptionsExtendedTypeSequence = requiredDescriptionsLanguageSequence;
		
		//Pull a different concept taxonomy, in order to validate that the caching in the TaxonomyProvider gets cleaned up properly when the new concept is created.
		Response getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid())
				.queryParam(RequestParameters.includeParents, true)
				.queryParam(RequestParameters.descriptionTypePrefs, "fsn,definition,synonym")
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		checkFail(getConceptVersionResponse).readEntity(String.class);

		//System.out.println("Trying to retrieve concept " + parent1Sequence + " from " + RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + parent1Sequence);
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + parent1Sequence)
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		RestConceptVersion conceptVersionObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(conceptVersionObject.getConChronology().getConceptSequence(), parent1Sequence);
		
		final UUID randomUuid = UUID.randomUUID();

		final String fsn = "fsn for test concept " + randomUuid.toString();
		final String pt = "preferred term for test concept " + randomUuid.toString();
		
		final List<Integer> parentIds = new ArrayList<>();
		parentIds.add(parent1Sequence);
		parentIds.add(parent2Sequence);
		
		List<Integer> preferredDialects = new ArrayList<>();
		preferredDialects.add(Get.identifierService().getConceptSequenceForUuids(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid()));
		preferredDialects.add(Get.identifierService().getConceptSequenceForUuids(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid()));

		RestConceptCreateData newConceptData = new RestConceptCreateData(
				parentIds,
				fsn,
				requiredDescriptionsLanguageSequence,
				requiredDescriptionsExtendedTypeSequence,
				preferredDialects);

		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newConceptData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		Response createConceptResponse = target(RestPaths.conceptCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newConceptSequenceWrapperXml = createConceptResponse.readEntity(String.class);
		RestWriteResponse newConceptSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newConceptSequenceWrapperXml);
		int newConceptSequence = newConceptSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newConceptSequence > 0);
		
		// Retrieve new concept and validate fields (FSN in description)
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.includeParents, true)
				.queryParam(RequestParameters.descriptionTypePrefs, "fsn,definition,synonym")
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		RestConceptVersion newConceptVersionObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(newConceptVersionObject.getConChronology().getDescription(), fsn);
		Assert.assertEquals(newConceptVersionObject.getConVersion().getState(), new RestStateType(State.ACTIVE));
		Assert.assertTrue(newConceptVersionObject.getParents().size() == 2);
		Assert.assertTrue(
				(newConceptVersionObject.getParents().get(0).getConChronology().getConceptSequence() == parent1Sequence
				&& newConceptVersionObject.getParents().get(1).getConChronology().getConceptSequence() == parent2Sequence)
				|| (newConceptVersionObject.getParents().get(0).getConChronology().getConceptSequence() == parent2Sequence
						&& newConceptVersionObject.getParents().get(1).getConChronology().getConceptSequence() == parent1Sequence));

		// Retrieve all descriptions referring to new concept
		RestSememeDescriptionVersion[] conceptDescriptionsObject = getDescriptionsForConcept(newConceptSequence);
		Assert.assertTrue(conceptDescriptionsObject.length >= 2);
		// Iterate description list to find description with an extended type annotation sememe
		boolean foundDescriptionWithCorrectExtendedType = false;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getDescriptionExtendedTypeConceptSequence() != null
					&& version.getDescriptionExtendedTypeConceptSequence() == requiredDescriptionsExtendedTypeSequence) {
				foundDescriptionWithCorrectExtendedType = true;
				break;
			}
		}
		// TODO (joel) determine why description extended type not populating
		//Assert.assertTrue(foundDescriptionWithCorrectExtendedType);
		for (RestSememeDescriptionVersion description : conceptDescriptionsObject) {
			boolean foundPreferredDialect = false;
			boolean foundUsEnglishDialect = false;
			boolean foundGbEnglishDialect = false;
			for (RestDynamicSememeVersion dialect : description.getDialects()) {
				if (dialect.getSememeChronology().getAssemblageSequence() == Get.identifierService().getConceptSequenceForUuids(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid())) {
					foundUsEnglishDialect = true;
				} else if (dialect.getSememeChronology().getAssemblageSequence() == Get.identifierService().getConceptSequenceForUuids(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid())) {
					foundGbEnglishDialect = true;
				}
				for (RestDynamicSememeData data : dialect.getDataColumns()) {
					if (data instanceof RestDynamicSememeNid) {
						if (((RestDynamicSememeNid)data).getNid() == MetaData.PREFERRED.getNid()) {
							foundPreferredDialect = true;
						}
					}
				}
			}
			Assert.assertTrue(foundPreferredDialect, "Preferred dialect not found");
			Assert.assertTrue(foundUsEnglishDialect, "US English dialect not found");
			//Assert.assertTrue(foundGbEnglishDialect, "GB English dialect not found"); // TODO (joel) find out why second dialect not being set
		}

//		// Retrieve new concept and validate fields (Preferred Term in description)
//		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
//				.queryParam(RequestParameters.includeParents, true)
//				.queryParam(RequestParameters.descriptionTypePrefs, "synonym,definition,fsn")
//				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
//				.request()
//				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
//		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
//		newConceptVersionObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
//		Assert.assertEquals(newConceptVersionObject.getConChronology().getDescription(), pt);
//		Assert.assertEquals(newConceptVersionObject.getConVersion().getState(), new RestStateType(State.ACTIVE));
//		Assert.assertTrue(newConceptVersionObject.getParents().size() == 2);
//		Assert.assertTrue(
//				(newConceptVersionObject.getParents().get(0).getConChronology().getConceptSequence() == parent1Sequence
//				&& newConceptVersionObject.getParents().get(1).getConChronology().getConceptSequence() == parent2Sequence)
//				|| (newConceptVersionObject.getParents().get(0).getConChronology().getConceptSequence() == parent2Sequence
//				&& newConceptVersionObject.getParents().get(1).getConChronology().getConceptSequence() == parent1Sequence));
		
		// Find new concept in taxonomy
		Response taxonomyResponse = target(taxonomyRequestPath)
				.queryParam(RequestParameters.id, newConceptSequence)
				.queryParam(RequestParameters.parentHeight, 1)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(taxonomyResponse).readEntity(String.class);
		RestConceptVersion conceptVersionFromTaxonomy = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		// validate conceptVersionFromTaxonomy parents
		Assert.assertTrue(conceptVersionFromTaxonomy.getParents().size() == 2);
		Assert.assertTrue(
				(conceptVersionFromTaxonomy.getParents().get(0).getConChronology().getConceptSequence() == parent1Sequence
				&& conceptVersionFromTaxonomy.getParents().get(1).getConChronology().getConceptSequence() == parent2Sequence)
				|| (conceptVersionFromTaxonomy.getParents().get(1).getConChronology().getConceptSequence() == parent1Sequence
						|| conceptVersionFromTaxonomy.getParents().get(0).getConChronology().getConceptSequence() == parent2Sequence));

		// Find new parent 1 concept in taxonomy
		taxonomyResponse = target(taxonomyRequestPath)
				.queryParam(RequestParameters.id, parent1Sequence)
				.queryParam(RequestParameters.childDepth, 1)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(taxonomyResponse).readEntity(String.class);
		conceptVersionFromTaxonomy = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		// validate conceptVersionFromTaxonomy child includes newConceptSequence
		Assert.assertTrue(conceptVersionFromTaxonomy.getChildren().size() > 0);
		boolean foundNewConceptAsChildOfSpecifiedParent = false;
		for (RestConceptVersion child : conceptVersionFromTaxonomy.getChildren()) {
			if (child.getConChronology().getConceptSequence() == newConceptSequence) {
				foundNewConceptAsChildOfSpecifiedParent = true;
				break;
			}
		}
		Assert.assertTrue(foundNewConceptAsChildOfSpecifiedParent);

		// retire concept
		RestBoolean isActive = new RestBoolean(false);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(isActive);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response deactivateConceptResponse = target(RestPaths.writePathComponent +  RestPaths.componentComponent + RestPaths.updatePathComponent 
				+ RestPaths.updateStateComponent + newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(isActive));
		checkFail(deactivateConceptResponse);
		
		// Retrieve retired concept and validate
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.includeParents, false)
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		RestConceptVersion deactivatedConceptObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(deactivatedConceptObject.getConVersion().getState(), new RestStateType(State.INACTIVE));
		
		//Do it again using the direct concept API
		
		Response stateChangeResponse = target(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent + RestPaths.updatePathComponent
				+ newConceptSequenceWrapper.uuid)
			.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
			.request()
			.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
					jsonIze(new String[] {"active"}, 
							new String[] {"true"})));
		checkFail(stateChangeResponse).readEntity(String.class);
		
		// Retrieve retired concept and validate
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.includeParents, false)
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		deactivatedConceptObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(deactivatedConceptObject.getConVersion().getState(), new RestStateType(State.ACTIVE));
	}

	@Test
	public void testMappingAPIs()
	{
		// Create a random string to confirm target data are relevant
		UUID randomUuid = UUID.randomUUID();
		
		// Create a new map set
		String newMappingSetName = "A new mapping set name (" + randomUuid + ")";
		String newMappingSetInverseName = "A new mapping set inverseName (" + randomUuid + ")";
		String newMappingSetDescription = "A new mapping set description (" + randomUuid + ")";
		String newMappingSetPurpose = "A new mapping set purpose (" + randomUuid + ")";
		RestMappingSetVersionBaseCreate newMappingSetData = new RestMappingSetVersionBaseCreate(
				newMappingSetName,
				newMappingSetInverseName,
				newMappingSetDescription,
				newMappingSetPurpose);

		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newMappingSetData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		Response createNewMappingSetResponse = target(RestPaths.mappingSetCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newMappingSetSequenceWrapperXml = checkFail(createNewMappingSetResponse).readEntity(String.class);
		RestWriteResponse newMappingSetSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newMappingSetSequenceWrapperXml);
		UUID testMappingSetUUID = newMappingSetSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(testMappingSetUUID != null);
		
		// Retrieve new mapping set and validate fields
		Response getNewMappingSetVersionResponse = target(RestPaths.mappingSetAppPathComponent + testMappingSetUUID)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingSetVersionResult = checkFail(getNewMappingSetVersionResponse).readEntity(String.class);
		RestMappingSetVersion retrievedMappingSetVersion = XMLUtils.unmarshalObject(RestMappingSetVersion.class, retrievedMappingSetVersionResult);
		Assert.assertEquals(newMappingSetName, retrievedMappingSetVersion.name);
		Assert.assertEquals(newMappingSetInverseName, retrievedMappingSetVersion.inverseName);
		Assert.assertEquals(newMappingSetDescription, retrievedMappingSetVersion.description);
		Assert.assertEquals(newMappingSetPurpose, retrievedMappingSetVersion.purpose);
		
		// Update comment with new comment text value and empty comment context value
		String updatedMappingSetName = "An updated mapping set name (" + randomUuid + ")";
		String updatedMappingSetInverseName = null; //"An updated mapping set inverse name (" + randomUuid + ")";
		String updatedMappingSetDescription = "An updated mapping set description (" + randomUuid + ")";
		String updatedMappingSetPurpose = "An updated mapping set purpose (" + randomUuid + ")";
		RestMappingSetVersionBase updatedMappingSetData = new RestMappingSetVersionBase(
				updatedMappingSetName,
				updatedMappingSetInverseName,
				updatedMappingSetDescription,
				updatedMappingSetPurpose);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(updatedMappingSetData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response updateMappingSetResponse = target(RestPaths.mappingSetUpdateAppPathComponent + testMappingSetUUID)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateMappingSetResponse);
		
		// Retrieve updated mapping set and validate fields
		getNewMappingSetVersionResponse = target(RestPaths.mappingSetAppPathComponent + testMappingSetUUID)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingSetVersionResult = checkFail(getNewMappingSetVersionResponse).readEntity(String.class);
		RestMappingSetVersion updatedMappingSetObject = XMLUtils.unmarshalObject(RestMappingSetVersion.class, retrievedMappingSetVersionResult);
		Assert.assertEquals(updatedMappingSetName, updatedMappingSetObject.name);
		Assert.assertEquals(updatedMappingSetInverseName, updatedMappingSetObject.inverseName);
		Assert.assertEquals(updatedMappingSetDescription, updatedMappingSetObject.description);
		Assert.assertEquals(updatedMappingSetPurpose, updatedMappingSetObject.purpose);

		// Get list of mapping sets
		Response getMappingSetsResponse = target(RestPaths.mappingSetsAppPathComponent)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getMappingSetsResult = checkFail(getMappingSetsResponse).readEntity(String.class);
		RestMappingSetVersion[] mappingSetsObject = XMLUtils.unmarshalObjectArray(RestMappingSetVersion.class, getMappingSetsResult);
		Assert.assertTrue(mappingSetsObject != null && mappingSetsObject.length > 0);
		RestMappingSetVersion testMappingSetVersion = null;
		for (RestMappingSetVersion currentMappingSetVersion : mappingSetsObject) {
			if (currentMappingSetVersion.name != null && currentMappingSetVersion.name.equals(updatedMappingSetObject.name)
				&& StringUtils.isBlank(currentMappingSetVersion.inverseName)
				&& currentMappingSetVersion.description != null && currentMappingSetVersion.description.equals(updatedMappingSetObject.description)
				&& currentMappingSetVersion.purpose != null && currentMappingSetVersion.purpose.equals(updatedMappingSetObject.purpose))
			{
				testMappingSetVersion = currentMappingSetVersion;
				break;
			}
		}
		Assert.assertNotNull(testMappingSetVersion);

		// RestMappingItemVersion
		// TODO test mapping item extended fields
		int sourceConceptSeq = getIntegerIdForUuid(MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid(), "conceptSequence");
		int targetConceptSeq = getIntegerIdForUuid(MetaData.ENGLISH_LANGUAGE.getPrimordialUuid(), "conceptSequence");
		int qualifierConceptSeq = getIntegerIdForUuid(MetaData.SPANISH_LANGUAGE.getPrimordialUuid(), "conceptSequence");

		RestMappingItemVersionBaseCreate newMappingSetItemData = new RestMappingItemVersionBaseCreate(
				targetConceptSeq,
				qualifierConceptSeq,
				testMappingSetUUID,
				sourceConceptSeq,
				null);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(newMappingSetItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response createNewMappingtemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newMappingItemSequenceWrapperXml = createNewMappingtemResponse.readEntity(String.class);
		RestWriteResponse newMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newMappingItemSequenceWrapperXml);
		UUID newMappingItemUUID = newMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(newMappingItemUUID != null);

		// test createNewMappingItem()
		// Retrieve mapping item and validate fields
//TODO do we really have a use case for this?  If yes, implement
//		Response getNewMappingItemVersionResponse = target(RestPaths.mappingItemAppPathComponent + newMappingItemSequence)
//				.request()
//				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
//		String retrievedMappingItemVersionResult = checkFail(getNewMappingItemVersionResponse).readEntity(String.class);
//		RestMappingItemVersion retrievedMappingItemVersion = XMLUtils.unmarshalObject(RestMappingItemVersion.class, retrievedMappingItemVersionResult);
//		Assert.assertTrue(sourceConceptSeq == retrievedMappingItemVersion.sourceConcept);
//		Assert.assertTrue(targetConceptSeq == retrievedMappingItemVersion.targetConcept);
//		Assert.assertTrue(qualifierConceptSeq == retrievedMappingItemVersion.qualifierConcept);
//		Assert.assertTrue(newMappingItemSequence == retrievedMappingItemVersion.mapSetConcept);
		
		// test getMappingItems() 
		Response getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + testMappingSetUUID)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		RestMappingItemVersion[] retrievedMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		RestMappingItemVersion mappingItemMatchingNewItem = null;
		for (RestMappingItemVersion currentMappingItem : retrievedMappingItems) {
			if (currentMappingItem.getIdentifiers().getUuids().get(0).equals(newMappingItemUUID)
					&& currentMappingItem.mapSetConcept == retrievedMappingSetVersion.conceptSequence
					&& currentMappingItem.targetConcept == targetConceptSeq
					&& currentMappingItem.sourceConcept == sourceConceptSeq
					&& currentMappingItem.qualifierConcept == qualifierConceptSeq) {
				mappingItemMatchingNewItem = currentMappingItem;
				break;
			}
		}
		Assert.assertNotNull(mappingItemMatchingNewItem);
	
		int updatedTargetConceptSeq = getIntegerIdForUuid(MetaData.DANISH_LANGUAGE.getPrimordialUuid(), "conceptSequence");
		int updatedQualifierConceptSeq = getIntegerIdForUuid(MetaData.FRENCH_LANGUAGE.getPrimordialUuid(), "conceptSequence");

		RestMappingItemVersionBase updatedMappingItemData = new RestMappingItemVersionBase(
				updatedTargetConceptSeq,
				updatedQualifierConceptSeq,
				null
				);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(updatedMappingItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response updateMappingtemResponse = target(RestPaths.mappingItemUpdateAppPathComponent + newMappingItemUUID)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateMappingtemResponse);

		getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + testMappingSetUUID)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		retrievedMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		RestMappingItemVersion mappingItemMatchingUpdatedItem = null;
		for (RestMappingItemVersion currentMappingItem : retrievedMappingItems) {
			if (currentMappingItem.getIdentifiers().getUuids().get(0).equals(newMappingItemUUID)
					&& currentMappingItem.mapSetConcept == retrievedMappingSetVersion.conceptSequence
					&& currentMappingItem.targetConcept == updatedMappingItemData.targetConcept
					&& currentMappingItem.sourceConcept == newMappingSetItemData.sourceConcept
					&& currentMappingItem.qualifierConcept == updatedMappingItemData.qualifierConcept) {
				mappingItemMatchingUpdatedItem = currentMappingItem;
				break;
			}
		}
		Assert.assertNotNull(mappingItemMatchingUpdatedItem);
	}

	@Test
	public void testCommentAPIs()
	{
		int conceptNid = getIntegerIdForUuid(MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid(), "nid");

		// Create a random string to confirm target data are relevant
		UUID randomUuid = UUID.randomUUID();
		
		// Create a new comment on SNOROCKET_CLASSIFIER
		String newCommentText = "A new comment text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";
		String newCommentContext = "A new comment context for SNOROCKET_CLASSIFIER (" + randomUuid + ")";
		RestCommentVersionBaseCreate newCommentData = new RestCommentVersionBaseCreate(
				conceptNid,
				newCommentText,
				newCommentContext
				);

		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newCommentData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		
		Response createCommentResponse = target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newCommentSememeSequenceWrapperXml = createCommentResponse.readEntity(String.class);
		RestWriteResponse newCommentSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newCommentSememeSequenceWrapperXml);
		int newCommentSememeSequence = newCommentSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newCommentSememeSequence > 0);
		
		// Retrieve new comment and validate fields
		Response getCommentVersionResponse = target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion newCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals(newCommentText, newCommentObject.getComment());
		Assert.assertEquals(newCommentContext, newCommentObject.getCommentContext());
		
		// Update comment with new comment text value and empty comment context value
		String updatedCommentText = "An updated comment text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";
		RestCommentVersionBase updatedCommentData = new RestCommentVersionBase(
				updatedCommentText,
				null
				);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(updatedCommentData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		Response updateCommentResponse = target(RestPaths.commentUpdatePathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.queryParam(RequestParameters.id, newCommentSememeSequence)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateCommentResponse);
		
		// Retrieve updated comment and validate fields
		getCommentVersionResponse = target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion updatedCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals(updatedCommentText, updatedCommentObject.getComment());
		Assert.assertTrue(StringUtils.isBlank(updatedCommentObject.getCommentContext()));

		// Get list of RestCommentVersion associated with MetaData.SNOROCKET_CLASSIFIER
		Response getCommentVersionByReferencedItemResponse = target(RestPaths.commentVersionByReferencedComponentPathComponent + MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getCommentVersionByReferencedItemResult = checkFail(getCommentVersionByReferencedItemResponse).readEntity(String.class);
		
		RestCommentVersion[] commentVersionsObject = XMLUtils.unmarshalObjectArray(RestCommentVersion.class, getCommentVersionByReferencedItemResult);
		Assert.assertTrue(commentVersionsObject != null && commentVersionsObject.length > 0);
		RestCommentVersion commentVersionRetrievedByReferencedItem = null;
		for (RestCommentVersion commentVersion : commentVersionsObject) {
			if (commentVersion.getComment() != null && commentVersion.getComment().equals(updatedCommentText)
					&& StringUtils.isBlank(commentVersion.getCommentContext())) {
				commentVersionRetrievedByReferencedItem = commentVersion;
				break;
			}
		}
		Assert.assertNotNull(commentVersionRetrievedByReferencedItem);
	}

	/**
	 * This test validates that the XML serializers, sememe by-assemblage API and pagination are working correctly 
	 */
	@Test
	public void testPaginatedSememesByAssemblage()
	{
		String xpathExpr = "/restSememeVersionPage/results/sememeChronology/sememeSequence";

		// Test to confirm that requested maxPageSize of results returned
		for (int pageSize : new int[] { 1, 5, 10 }) {
			Response response = target(
					sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.maxPageSize, pageSize)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

			String resultXmlString = checkFail(response).readEntity(String.class);

			NodeList nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);

			Assert.assertTrue(nodeList != null && nodeList.getLength() == pageSize);
		}

		// Test to confirm that pageNum works
		{
			// Get first page of 10 results
			Response response = target(
					sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.maxPageSize, 10)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
			String resultXmlString = checkFail(response).readEntity(String.class);
			NodeList nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);
			String idOfTenthResultOfFirstTenResultPage = nodeList.item(9).getTextContent();

			// Get 10th page of 1 result
			response = target(
					sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid())
					.queryParam(RequestParameters.expand, "chronology")
					.queryParam(RequestParameters.pageNum, 10)
					.queryParam(RequestParameters.maxPageSize, 1)
					.request()
					.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

			resultXmlString = checkFail(response).readEntity(String.class);
			nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);
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
		String xpathExpr = "/restSearchResultPage/results/matchNid";

		// Test to confirm that requested maxPageSize of results returned
		for (int pageSize : new int[] { 2, 3, 8 }) {
			String resultXmlString = checkFail(target(descriptionSearchRequestPath)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.maxPageSize, pageSize)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);

			NodeList nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);

			Assert.assertTrue(nodeList != null && nodeList.getLength() == pageSize);
		}

		// Test to confirm that pageNum works
		{
			// Get first page of 7 results
			String resultXmlString = checkFail(target(descriptionSearchRequestPath)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.maxPageSize, 7)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			NodeList nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);
			String idOf7thResultOfFirst7ResultPage = nodeList.item(6).getTextContent();

			// Get 7th page of 1 result
			resultXmlString = checkFail(target(descriptionSearchRequestPath)
					.queryParam(RequestParameters.query,"dynamic*")
					.queryParam(RequestParameters.expand, "uuid")
					.queryParam(RequestParameters.pageNum, 7)
					.queryParam(RequestParameters.maxPageSize, 1)
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			nodeList = XMLUtils.getNodeList(resultXmlString, xpathExpr);
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
		Response response = target(sememeByAssemblageRequestPath +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

		checkFail(response);

		response = target(sememeByAssemblageRequestPath +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();

		checkFail(response);
	}

	@Test
	public void testReferencedDetailsExpansion()
	{
		Response response = target(sememeByReferencedComponentRequestPath +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

		String result = checkFail(response).readEntity(String.class);

		Assert.assertFalse(result.contains("<conceptDescription>preferred (ISAAC)</conceptDescription>"));
		Assert.assertFalse(result.contains("</referencedComponentNidObjectType>"));
		Assert.assertFalse(result.contains("<referencedComponentNidDescription>dynamic sememe extension definition (ISAAC)</referencedComponentNidDescription>"));

		response = target(sememeByReferencedComponentRequestPath +
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
		final String url = RestPaths.idAPIsPathComponent + RestPaths.idTranslateComponent +
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
		final String url = conceptVersionRequestPath +
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
		final String url = RestPaths.logicGraphAPIsPathComponent + RestPaths.versionComponent +
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
		final String url = RestPaths.logicGraphAPIsPathComponent + RestPaths.versionComponent +
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
		final String nodeSemanticNodeFieldName = "nodeSemantic";
		if (rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("name") == null || ! rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("name").asText().equals(NodeSemantic.DEFINITION_ROOT.name())) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with missing or invalid " + rootLogicNodeFieldName + ": \"" + rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("name") + "\"!=\"" + NodeSemantic.DEFINITION_ROOT.name() + "\"");
		}
	}

	/**
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain
	 * taxonomy data.
	 */
	@Test
	public void testTaxonomyReturn()
	{

		Response response = target(taxonomyRequestPath).request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		//	System.out.println(target(taxonomyRequestUrl).request().get().toString());

		checkFail(response);

		response = target(taxonomyRequestPath).request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();

		checkFail(response);
	}


	@Test
	public void testSearchAssemblageRestriction1()
	{
		//Check with UUID

		String result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getUUID().toString())
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//Check with nid
		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//Check with sequence
		//Check with nid
		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getConceptSequence() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.AMT_MODULE.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//sanity check search
		result = checkFail(target(sememeSearchRequestPath)
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
		//Make sure it contains a random (type 4) UUID with this pattern...
		//<uuids>12604572-254c-49d2-8d9f-39d485af0fa0</uuids>
		final Pattern pXml = Pattern.compile(".*uuids.{9}-.{4}-4.{3}-.{4}-.{14}uuids.*", Pattern.DOTALL);

		//Test expand uuid on/off for each search type

		String result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"1")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());

		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"1")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());

		result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic*")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());

		result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());

		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());

		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());

		result = checkFail(target(byRefSearchRequestPath)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(pXml.matcher(result).matches());

		result = checkFail(target(byRefSearchRequestPath)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_ROOT.getNid())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(pXml.matcher(result).matches());

		//Spot check for JSON return support:
		//Make sure it contains a random (type 4) UUID with this pattern...
		// "uuids" : [ "bcf22234-a736-5f6b-9ce3-d016594ca5cd" ]
		final Pattern pJson = Pattern.compile(".*uuids.{15}-.{4}-4.{3}-.{4}-.{12}.*", Pattern.DOTALL);
		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertTrue(pJson.matcher(result).matches());

		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertFalse(pJson.matcher(result).matches());
	}

	@Test
	public void testSearchExpandsRefConcept()
	{
		//Test expand uuid on/off for each search type

		String result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.query, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_NAME.getPrimordialUuid().toString())
				.queryParam("expand", ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));

		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.query, DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_NAME.getPrimordialUuid().toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));

		result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));

		result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));

		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));

		result = checkFail(target(prefixSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_ASSEMBLAGES.getPrimordialUuid().toString()));

		result = checkFail(target(byRefSearchRequestPath)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_METADATA.getNid())
				.queryParam(RequestParameters.maxPageSize, "100")
				.queryParam(RequestParameters.expand, "uuid," + ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(MetaData.MODULE.getPrimordialUuid().toString()));

		result = checkFail(target(byRefSearchRequestPath)
				.queryParam(RequestParameters.nid, MetaData.ISAAC_METADATA.getNid())
				.queryParam(RequestParameters.maxPageSize, "100")
				.queryParam(RequestParameters.expand, "uuid")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get())
				.readEntity(String.class);
		Assert.assertFalse(result.contains(MetaData.MODULE.getPrimordialUuid().toString()));
	}

	@Test
	public void testSearchExpandsRefConceptVersion()
	{
		//Test expand uuid on/off for each search type

		String result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept + "," + ExpandUtil.versionsLatestOnlyExpandable)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestSearchResultPage resultsObject = XMLUtils.unmarshalObject(RestSearchResultPage.class, result);
		boolean foundActiveResult = false;
		for (RestSearchResult resultObject : resultsObject.getResults()) {
			if (resultObject.isActive()) {
				foundActiveResult = true;
				break;
			}
		}
		Assert.assertTrue(foundActiveResult);
		
		result = checkFail(target(descriptionSearchRequestPath)
				.queryParam(RequestParameters.query,"dynamic sememe Asse*")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		resultsObject = XMLUtils.unmarshalObject(RestSearchResultPage.class, result);
		foundActiveResult = false;
		for (RestSearchResult resultObject : resultsObject.getResults()) {
			if (resultObject.isActive()) {
				foundActiveResult = true;
				break;
			}
		}
		Assert.assertTrue(foundActiveResult);
	}

	@Test
	public void testSearchRecursiveRefComponentLookup()
	{
		String result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.query, MetaData.PREFERRED.getNid() + "")
				.queryParam(RequestParameters.treatAsString, "true")
				.queryParam(RequestParameters.maxPageSize, 500)
				.queryParam(RequestParameters.expand, ExpandUtil.referencedConcept)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid().toString()));

		result = checkFail(target(sememeSearchRequestPath)
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
		String result = checkFail(target(conceptDescriptionsRequestPath + MetaData.USER.getConceptSequence())
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
	public void testDescriptionObjectsFetch()
	{
		RestSememeDescriptionVersion[] descriptions = getDescriptionsForConcept(MetaData.USER.getConceptSequence());

		Assert.assertTrue(descriptions.length == 2);
		for (RestSememeDescriptionVersion description : descriptions)
		{
			Assert.assertTrue(description.getDialects().size() > 0);

			//Validate that the important bit of the description sememe are put together properly
			//Assert.assertTrue(preDialect.contains("<assemblageSequence>" + MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence() + "</assemblageSequence>"), "Wrong language");
			Assert.assertEquals(description.getSememeChronology().getAssemblageSequence(), MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence(), "Wrong language");
			
			//Assert.assertTrue(preDialect.contains("<referencedComponentNid>" + MetaData.USER.getNid() + "</referencedComponentNid>"), "Wrong concept");
			Assert.assertEquals(description.getSememeChronology().getReferencedComponentNid(), MetaData.USER.getNid(), "Wrong concept");

			//Assert.assertTrue(preDialect.contains("<caseSignificanceConceptSequence>" + MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence() 
			//+ "</caseSignificanceConceptSequence>"), "Wrong case sentivity");
			Assert.assertEquals(description.getCaseSignificanceConceptSequence(), MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence(), "Wrong case sentivity");

			//Assert.assertTrue(preDialect.contains("<languageConceptSequence>" + MetaData.ENGLISH_LANGUAGE.getConceptSequence() 
			//+ "</languageConceptSequence>"), "Wrong language");
			Assert.assertEquals(description.getLanguageConceptSequence(), MetaData.ENGLISH_LANGUAGE.getConceptSequence(), "Wrong language");

			//Assert.assertTrue((preDialect.contains("<text>user</text>") || preDialect.contains("<text>user (ISAAC)</text>")), "Wrong text " + preDialect);
			Assert.assertTrue(description.getText().equals("user") || description.getText().equals("user (ISAAC)"), "Wrong text" + description.getText());

			//Assert.assertTrue((preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.SYNONYM.getConceptSequence() + "</descriptionTypeConceptSequence>") 
			//		|| preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.FULLY_SPECIFIED_NAME.getConceptSequence() + "</descriptionTypeConceptSequence>")), 
			//		"Wrong description type");
			Assert.assertTrue(description.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence()
					|| description.getDescriptionTypeConceptSequence() == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence(),
					"Wrong description type");

			//validate that the dialect bits are put together properly
			//Assert.assertTrue(dialect.contains("<assemblageSequence>" + MetaData.US_ENGLISH_DIALECT.getConceptSequence() + "</assemblageSequence>"), "Wrong dialect");
			Assert.assertEquals(description.getSememeChronology().getAssemblageSequence(), MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence(), "Wrong dialect");
			
			//Assert.assertTrue(dialect.contains("<data xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:int\">" + MetaData.PREFERRED.getNid() + "</data>"), "Wrong value");
			boolean foundPreferredDialect = false;
			boolean foundUsEnglishDialect = false;
			for (RestDynamicSememeVersion dialect : description.getDialects()) {
				if (dialect.getSememeChronology().getAssemblageSequence() == MetaData.US_ENGLISH_DIALECT.getConceptSequence()) {
					foundUsEnglishDialect = true;
				}
				for (RestDynamicSememeData data : dialect.getDataColumns()) {
					if (data instanceof RestDynamicSememeNid) {
						if (((RestDynamicSememeNid)data).getNid() == MetaData.PREFERRED.getNid()) {
							foundPreferredDialect = true;
						}
					}
				}
			}
			Assert.assertTrue(foundPreferredDialect, "Preferred dialect not found");
			Assert.assertTrue(foundUsEnglishDialect, "US English dialect not found");
		}
	}

	@Test
	public void testCoordinateTokenRoundTrip() throws Exception
	{
		TaxonomyCoordinate taxonomyCoordinate =
				TaxonomyCoordinates.getStatedTaxonomyCoordinate(
						StampCoordinates.getDevelopmentLatest(),
						LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate(),
						LogicCoordinates.getStandardElProfile());
		CoordinatesToken t = CoordinatesTokens.getOrCreate(
				taxonomyCoordinate.getStampCoordinate(),
				taxonomyCoordinate.getLanguageCoordinate(),
				taxonomyCoordinate.getLogicCoordinate(),
				taxonomyCoordinate.getTaxonomyType()
				);

		String token = t.getSerialized();

		CoordinatesToken read = CoordinatesTokens.getOrCreate(token);
		Assert.assertTrue(token.equals(read.getSerialized()));
	}


	@Test
	public void testCoordinatesToken()
	{
		/*
		 * These tests display the following values on fail
		 * Each test should initialize or set-to-null each of the following values
		 */
		Map<String, Object> parameters = new HashMap<>();
		WebTarget target = null;
		String result = null;
		RestCoordinatesToken retrievedToken = null;
		CoordinatesToken defaultTokenObject = null;
		RestCoordinatesToken defaultToken = null;
		try {
			defaultToken = new RestCoordinatesToken(CoordinatesTokens.getDefaultCoordinatesToken());
			defaultTokenObject = CoordinatesTokens.getOrCreate(defaultToken.token);

			// Test no parameters against default token
			parameters.clear();
			result = checkFail((target = target(coordinatesTokenRequestPath))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			Assert.assertTrue(retrievedToken.equals(defaultToken));

			// Test default token passed as argument against default token
			result = checkFail((target = target(
					coordinatesTokenRequestPath,
					parameters = buildParams(param(RequestParameters.coordToken, defaultToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			Assert.assertTrue(retrievedToken.equals(defaultToken));

			// Test default token passed as argument against default token
			result = checkFail((target = target(
					coordinatesTokenRequestPath,
					parameters = buildParams(param(RequestParameters.coordToken, defaultToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			Assert.assertTrue(retrievedToken.equals(defaultToken));

			// Compare retrieved coordinates with default generated by default token
			parameters.clear();
			result = checkFail((target = target(taxonomyCoordinateRequestPath))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			RestTaxonomyCoordinate retrievedTaxonomyCoordinate = XMLUtils.unmarshalObject(RestTaxonomyCoordinate.class, result);
			RestTaxonomyCoordinate defaultTaxonomyCoordinate = new RestTaxonomyCoordinate(defaultTokenObject.getTaxonomyCoordinate());
			try {
				Assert.assertTrue(retrievedTaxonomyCoordinate.equals(defaultTaxonomyCoordinate));
			} catch (Error e) {
				System.out.println(retrievedTaxonomyCoordinate);
				System.out.println(defaultTaxonomyCoordinate);
				throw e;
			}

			// Test passing a custom parameter
			// First ensure that the parameters we pass are not already in the default
			try {
				Assert.assertTrue(defaultTokenObject.getStampCoordinate().getStampPrecedence() != StampPrecedence.TIME);
				Assert.assertTrue(defaultTokenObject.getTaxonomyCoordinate().getTaxonomyType() != PremiseType.INFERRED);
			} catch (Error e) {
				System.out.println(defaultTokenObject.getLogicCoordinate());
				System.out.println(defaultTokenObject.getTaxonomyCoordinate());
				throw e;
			}

			result = checkFail((target = target(
					coordinatesTokenRequestPath,
					parameters = buildParams(
							param(RequestParameters.precedence, StampPrecedence.TIME.name()),
							param(RequestParameters.stated, "false"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			Assert.assertTrue(CoordinatesTokens.getOrCreate(retrievedToken.token).getStampPrecedence() == StampPrecedence.TIME);
			Assert.assertTrue(CoordinatesTokens.getOrCreate(retrievedToken.token).getTaxonomyType() == PremiseType.INFERRED);

			// Test using a customized token with getStampPrecedence() == StampPrecedence.TIME
			// and getTaxonomyType() == PremiseType.INFERRED
			result = checkFail((target = target(
					taxonomyCoordinateRequestPath,
					parameters = buildParams(param(RequestParameters.coordToken, retrievedToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedTaxonomyCoordinate = XMLUtils.unmarshalObject(RestTaxonomyCoordinate.class, result);
			Assert.assertTrue(retrievedTaxonomyCoordinate.stampCoordinate.precedence.getEnumId() == StampPrecedence.TIME.ordinal());
			Assert.assertTrue(retrievedTaxonomyCoordinate.stated == false);
		} catch (Throwable error) {
			System.out.println("Failing target: " + target);
			System.out.println("Failing parameters: " + parameters);
			System.out.println("Failing result XML: " + result);
			System.out.println("Failing retrievedToken: " + retrievedToken);
			System.out.println("Failing defaultToken: " + defaultToken);
			System.out.println("Failing defaultTokenObject: " + defaultTokenObject);
			throw new Error(error);
		}
	}

	@Test
	public void testGetCoordinates()
	{
		/*
		 * These tests display the following values on fail
		 * Each test should initialize or set-to-null each of the following values
		 */
		Map<String, Object> parameters = new HashMap<>();
		WebTarget target = null;
		String xpath = null;
		Node node = null;
		NodeList nodeList = null;
		String result = null;
		String requestUrl = null;
		try {
			// RestTaxonomyCoordinate
			boolean taxonomyCoordinateStated;
			result = checkFail(
					(target = target(
							requestUrl = taxonomyCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.stated, "false"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restTaxonomyCoordinate/stated";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			taxonomyCoordinateStated = Boolean.valueOf(node.getTextContent());
			Assert.assertTrue(taxonomyCoordinateStated == false);

			result = checkFail(
					(target = target(
							requestUrl = taxonomyCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.stated, "true"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			taxonomyCoordinateStated = Boolean.valueOf(node.getTextContent());
			Assert.assertTrue(taxonomyCoordinateStated == true);

			// RestStampCoordinate
			result = checkFail(
					(target = target(
							requestUrl = stampCoordinateRequestPath,
							parameters = buildParams(
									param(RequestParameters.time, 123456789),
									param(RequestParameters.precedence, StampPrecedence.TIME),
									param(RequestParameters.modules, MetaData.AMT_MODULE.getConceptSequence() + "," + MetaData.ISAAC_MODULE.getConceptSequence() + "," + MetaData.SNOMED_CT_CORE_MODULE.getConceptSequence()),
									param(RequestParameters.allowedStates, State.INACTIVE.getAbbreviation() + "," + State.PRIMORDIAL.getAbbreviation()))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restStampCoordinate/time";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			long stampCoordinateTime = Long.parseLong(node.getTextContent());
			Assert.assertTrue(stampCoordinateTime == 123456789);
			xpath = "/restStampCoordinate/modules";
			List<Integer> stampCoordinateModules = new ArrayList<>();
			node = null;
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			for (int i = 0; i < nodeList.getLength(); ++i) {
				stampCoordinateModules.add(Integer.valueOf(nodeList.item(i).getTextContent()));
			}
			Assert.assertTrue(stampCoordinateModules.size() == 3);
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.AMT_MODULE.getConceptSequence()));
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.ISAAC_MODULE.getConceptSequence()));
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.SNOMED_CT_CORE_MODULE.getConceptSequence()));

			xpath = "/restStampCoordinate/allowedStates/enumId";
			List<Integer> allowedStates = new ArrayList<>();
			node = null;
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			for (int i = 0; i < nodeList.getLength(); ++i) {
				allowedStates.add(Integer.valueOf(nodeList.item(i).getTextContent()));
			}
			Assert.assertTrue(allowedStates.size() == 2);
			Assert.assertTrue(allowedStates.contains(0));
			Assert.assertTrue(allowedStates.contains(2));

			// LanguageCoordinate
			// language
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.language, MetaData.ENGLISH_LANGUAGE.getConceptSequence()))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLanguageCoordinate/language";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			int languageCoordinateLangSeq = Integer.parseInt(node.getTextContent());
			Assert.assertTrue(languageCoordinateLangSeq == MetaData.ENGLISH_LANGUAGE.getConceptSequence());

			// descriptionTypePrefs
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "fsn,synonym"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLanguageCoordinate/descriptionTypePreferences";
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			node = null;
			Assert.assertTrue(nodeList.getLength() == 2);
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(0).getTextContent()) == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(1).getTextContent()) == MetaData.SYNONYM.getConceptSequence());

			// descriptionTypePrefs (reversed)
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "synonym,fsn"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLanguageCoordinate/descriptionTypePreferences";
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			node = null;
			Assert.assertTrue(nodeList.getLength() == 2);
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(0).getTextContent()) == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(1).getTextContent()) == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

			// Get token with specified non-default descriptionTypePrefs (SYNONYM,FSN)
			// then test token passed as argument along with RequestParameters.stated parameter
			result = checkFail(
					(target = target(
							requestUrl = coordinatesTokenRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "synonym,fsn"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			final RestCoordinatesToken synonymDescriptionPreferredToken = (RestCoordinatesToken) XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			// Get token with specified default descriptionTypePrefs (FSN,SYNONYM)
			parameters.clear();
			parameters.put(RequestParameters.descriptionTypePrefs, "fsn,synonym");
			result = checkFail(
					(target = target(requestUrl = coordinatesTokenRequestPath, parameters))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			final RestCoordinatesToken fsnDescriptionPreferredToken = (RestCoordinatesToken) XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);

			// confirm that constructed token has descriptionTypePrefs ordered as in
			// parameters used to construct token
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.coordToken, synonymDescriptionPreferredToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLanguageCoordinate/descriptionTypePreferences";
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			node = null;
			Assert.assertTrue(nodeList.getLength() == 2);
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(0).getTextContent()) == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(1).getTextContent()) == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

			// test token passed as argument along with RequestParameters.stated parameter
			// ensure that descriptionTypePrefs order specified in token is maintained
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(
									param(RequestParameters.coordToken, synonymDescriptionPreferredToken.token),
									param(RequestParameters.stated, "true"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLanguageCoordinate/descriptionTypePreferences";
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			node = null;
			Assert.assertTrue(nodeList.getLength() == 2);
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(0).getTextContent()) == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(Integer.parseUnsignedInt(nodeList.item(1).getTextContent()) == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

			// test passed descriptionTypePrefs on taxonomy
			// test synonym as preference
			result = checkFail(
					(target = target(
							requestUrl = taxonomyRequestPath,
							parameters = buildParams(
									param(RequestParameters.childDepth, 1),
									param(RequestParameters.descriptionTypePrefs, "synonym,fsn"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restConceptVersion/children/conChronology[conceptSequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			Assert.assertTrue(node != null && node.getNodeType() == Node.ELEMENT_NODE);
			Assert.assertTrue(node.getTextContent().equals(MetaData.HEALTH_CONCEPT.getConceptDescriptionText()));

			// test fsn as preference using token
			result = checkFail(
					(target = target(
							requestUrl = taxonomyRequestPath,
							parameters = buildParams(
									param(RequestParameters.childDepth, 1),
									param(RequestParameters.coordToken, fsnDescriptionPreferredToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restConceptVersion/children/conChronology[conceptSequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			Assert.assertTrue(node != null && node.getNodeType() == Node.ELEMENT_NODE);
			Assert.assertTrue(node.getTextContent().equals(MetaData.HEALTH_CONCEPT.getConceptDescriptionText() + " (ISAAC)"));

			// test fsn as preference
			result = checkFail(
					(target = target(
							requestUrl = taxonomyRequestPath,
							parameters = buildParams(
									param(RequestParameters.childDepth, 1),
									param(RequestParameters.descriptionTypePrefs, "fsn,synonym"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restConceptVersion/children/conChronology[conceptSequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			Assert.assertTrue(node != null && node.getNodeType() == Node.ELEMENT_NODE);
			Assert.assertTrue(node.getTextContent().equals(MetaData.HEALTH_CONCEPT.getConceptDescriptionText() + " (ISAAC)"));

			// LogicCoordinate
			result = checkFail(
					(target = target(
							requestUrl = logicCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.classifier, MetaData.SNOROCKET_CLASSIFIER.getConceptSequence()))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restLogicCoordinate/classifier";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			int logicCoordinateClassifierSeq = Integer.parseInt(node.getTextContent());
			Assert.assertTrue(logicCoordinateClassifierSeq == MetaData.SNOROCKET_CLASSIFIER.getConceptSequence());
		} catch (Throwable error) {
			System.out.println("Failing request target: " + target);
			System.out.println("Failing request URL: " + requestUrl);
			System.out.println("Failing request parameters: " + parameters);
			System.out.println("Failing result XPath: " + xpath);
			System.out.println("Failing result Node: " + XMLUtils.toString(node));
			System.out.println("Failing result NodeList: " + XMLUtils.toString(nodeList));
			System.out.println("Failing result XML: " + result);

			throw error;
		}
	}

	@Test
	public void testSystemAPIs()
	{
		/*
		 * These tests display the following values on fail
		 * Each test should initialize or set-to-null each of the following values
		 */
		Map<String, Object> parameters = new HashMap<>();
		WebTarget target = null;
		String result = null;
		String requestUrl = null;
		RestIdentifiedObjectsResult identifiedObjectsResult = null;
		RestSystemInfo systemInfo = null;
		try {
			// Get a sememe chronology by assemblage and extract one of its UUIDs
			result = checkFail(
			(target = target(
					requestUrl = sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(),
					parameters = buildParams(
							param(RequestParameters.expand, "chronology"), // Expand the chronology
							param(RequestParameters.maxPageSize, 1)))) // Request exactly 1 result
			.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
			.readEntity(String.class);
			RestSememeVersionPage sememeVersions = XMLUtils.unmarshalObject(RestSememeVersionPage.class, result);
			UUID sememeUuid = sememeVersions.results[0].getSememeChronology().getIdentifiers().getUuids().get(0);

			// Test objectChronologyType of specified sememe UUID
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.objectChronologyTypeComponent + sememeUuid.toString()))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			RestObjectChronologyType objectChronologyType = XMLUtils.unmarshalObject(RestObjectChronologyType.class, result);
			// Test RestObjectChronologyType name
			Assert.assertTrue(objectChronologyType.toString().equalsIgnoreCase(ObjectChronologyType.SEMEME.name()));
			// Test RestObjectChronologyType enumId ordinal
			Assert.assertTrue(objectChronologyType.getEnumId() == ObjectChronologyType.SEMEME.ordinal());

			// Test objectChronologyType of specified concept UUID
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.objectChronologyTypeComponent + MetaData.ISAAC_ROOT.getPrimordialUuid().toString()))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			objectChronologyType = XMLUtils.unmarshalObject(RestObjectChronologyType.class, result);
			// Test RestObjectChronologyType name
			Assert.assertTrue(objectChronologyType.toString().equalsIgnoreCase(ObjectChronologyType.CONCEPT.name()));
			// Test RestObjectChronologyType enumId ordinal
			Assert.assertTrue(objectChronologyType.getEnumId() == ObjectChronologyType.CONCEPT.ordinal());

			// Test SystemInfo
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.systemInfoComponent))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			systemInfo = XMLUtils.unmarshalObject(RestSystemInfo.class, result);
			Assert.assertTrue(systemInfo.getSupportedAPIVersions().length > 0 && ! StringUtils.isBlank(systemInfo.getSupportedAPIVersions()[0]));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.apiImplementationVersion));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.isaacVersion));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.scmUrl));
//			Assert.assertNotNull(systemInfo.isaacDbDependency);
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.isaacDbDependency.groupId));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.isaacDbDependency.artifactId));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.isaacDbDependency.type));
//			Assert.assertTrue(! StringUtils.isBlank(systemInfo.isaacDbDependency.version));
//			Assert.assertTrue(systemInfo.appLicenses.size() > 0);
//			for (RestLicenseInfo licenseInfo : systemInfo.appLicenses) {
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.name));
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.url));
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.comments));
//			}
//			for (RestDependencyInfo dependencyInfo : systemInfo.dbDependencies) {
//				Assert.assertTrue(! StringUtils.isBlank(dependencyInfo.groupId));
//				Assert.assertTrue(! StringUtils.isBlank(dependencyInfo.artifactId));
//				Assert.assertTrue(! StringUtils.isBlank(dependencyInfo.type));
//				Assert.assertTrue(! StringUtils.isBlank(dependencyInfo.version));
//			}
//			for (RestLicenseInfo licenseInfo : systemInfo.dbLicenses) {
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.name));
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.url));
//				Assert.assertTrue(! StringUtils.isBlank(licenseInfo.comments));
//			}		
			
			// Test identifiedObjectsComponent request of specified sememe UUID
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.identifiedObjectsComponent + sememeUuid.toString()))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			identifiedObjectsResult = XMLUtils.unmarshalObject(RestIdentifiedObjectsResult.class, result);
			// Test RestSememeChronology
			Assert.assertTrue(identifiedObjectsResult.getSememe().getIdentifiers().getUuids().contains(sememeUuid));
			Assert.assertNull(identifiedObjectsResult.getConcept());
			
			// Test identifiedObjectsComponent request of specified concept UUID
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.identifiedObjectsComponent + MetaData.ISAAC_ROOT.getPrimordialUuid().toString()))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			identifiedObjectsResult = XMLUtils.unmarshalObject(RestIdentifiedObjectsResult.class, result);
			// Test RestSememeChronology
			Assert.assertTrue(identifiedObjectsResult.getConcept().getIdentifiers().getUuids().contains(MetaData.ISAAC_ROOT.getPrimordialUuid()));
			Assert.assertNull(identifiedObjectsResult.getSememe());

			// Iterate and test first 10000 sequence numbers until a value found which corresponds to both concept and sememe
			boolean foundSequenceCorrespondingToBothConceptAndSememe = false;
			for (int sequence = 1; sequence < 10000; ++sequence) {
				if (Get.sememeService().hasSememe(sequence) && Get.conceptService().hasConcept(sequence)) {
					foundSequenceCorrespondingToBothConceptAndSememe = true;
					result = checkFail(
							(target = target(
									requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.identifiedObjectsComponent + sequence))
							.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
							.readEntity(String.class);
					identifiedObjectsResult = XMLUtils.unmarshalObject(RestIdentifiedObjectsResult.class, result);
					// Test RestSememeChronology AND RestConceptChronology
					Assert.assertTrue(identifiedObjectsResult.getConcept().getConceptSequence() == sequence);
					Assert.assertTrue(identifiedObjectsResult.getSememe().getSememeSequence() == sequence);

					break;
				}
			}
			Assert.assertTrue(foundSequenceCorrespondingToBothConceptAndSememe);
		} catch (Throwable error) {
			System.out.println("Failing request target: " + target);
			System.out.println("Failing request URL: " + requestUrl);
			System.out.println("Failing request parameters: " + parameters);
			System.out.println("Failing result XML: " + result);
			System.out.println("Failing identified objects result: " + identifiedObjectsResult);
			System.out.println("Failing systemInfo result: " + systemInfo);

			throw error;
		}
	}
	

	@Test
	public void testParameterValidation()
	{
		/*
		 * These tests display the following values on fail
		 * Each test should initialize or set-to-null each of the following values
		 */
		Map<String, Object> parameters = new HashMap<>();
		WebTarget target = null;
		String result = null;
		String requestUrl = null;
		String caughtExceptionMessage = null;
		try {
			// Test any call with valid parameters
			result = checkFail(
					(target = target(
							requestUrl = sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(),
							parameters = buildParams(
									param(RequestParameters.expand, "chronology"), // Expand the chronology
									param(RequestParameters.maxPageSize, 1)))) // Request exactly 1 result
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			
			// Test same call with a bogus additional parameter
			String badParamName = "bogusParam";
			String badParamValue = "testValue";
			try {
				result = checkFail(
						(target = target(
								requestUrl = sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(),
								parameters = buildParams(
										param(RequestParameters.expand, "chronology"), // Expand the chronology
										param(RequestParameters.maxPageSize, 1),
										param("bogusParam", "testValue"))))
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
						.readEntity(String.class);
			} catch (Throwable t) {
				caughtExceptionMessage = getCaughtParameterValidationExceptionMessage(badParamName, badParamValue, t);
			}
			Assert.assertNotNull(caughtExceptionMessage);
			
			// Test same call with a valid parameter made uppercase when IGNORE_CASE_VALIDATING_PARAM_NAMES == false
			badParamName = RequestParameters.maxPageSize.toUpperCase();
			badParamValue = "1";
			RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES = false;
			caughtExceptionMessage = null;
			try {
				result = checkFail(
						(target = target(
								requestUrl = sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(),
								parameters = buildParams(
										param(RequestParameters.expand, "chronology"), // Expand the chronology
										param(badParamName, badParamValue))))
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
						.readEntity(String.class);
			} catch (Throwable t) {
				caughtExceptionMessage = getCaughtParameterValidationExceptionMessage(badParamName, badParamValue, t);
			}
			Assert.assertNotNull(caughtExceptionMessage);

			// Test same call with a valid parameter made uppercase when IGNORE_CASE_VALIDATING_PARAM_NAMES == true
			badParamName = RequestParameters.maxPageSize.toUpperCase();
			badParamValue = "1";
			RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES = true;
			caughtExceptionMessage = null;
			try {
				result = checkFail(
						(target = target(
								requestUrl = sememeByAssemblageRequestPath + DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(),
								parameters = buildParams(
										param(RequestParameters.expand, "chronology"), // Expand the chronology
										param(badParamName, badParamValue))))
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
						.readEntity(String.class);
			} catch (Throwable t) {
				caughtExceptionMessage = getCaughtParameterValidationExceptionMessage(badParamName, badParamValue, t);
			}
			Assert.assertNull(caughtExceptionMessage);
			RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES = RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES_DEFAULT;

		} catch (Throwable error) {
			System.out.println("Failing request target: " + target);
			System.out.println("Failing request URL: " + requestUrl);
			System.out.println("Failing request parameters: " + parameters);
			System.out.println("Failing result XML: " + result);
			System.out.println("Failing exception: " + caughtExceptionMessage);

			throw error;
		} finally {
			RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES = RequestParameters.IGNORE_CASE_VALIDATING_PARAM_NAMES_DEFAULT;
		}
	}
	
	@Test
	public void testAssociations()
	{
		String result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsComponent)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		//No associations in the metadata
		Assert.assertTrue(result.endsWith("<restAssociationTypeVersions></restAssociationTypeVersions>"));
		
		//Make one
		UUID random = UUID.randomUUID();
		final String description = "Just a test description type (" + random.toString() + ")";
		Response createAssociationResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent 
					+ RestPaths.associationComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationName", "associationInverseName", "description"}, 
								new String[] {"test", "inverse Test", description})));
		result = checkFail(createAssociationResponse).readEntity(String.class);
		
		RestWriteResponse createdAssociationId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		
		//Read back
		
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationComponent + createdAssociationId.uuid.toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		RestAssociationTypeVersion createdAssociation = XMLUtils.unmarshalObject(RestAssociationTypeVersion.class, result);
		
		Assert.assertEquals(createdAssociation.associationName, "test");
		Assert.assertEquals(createdAssociation.description, description);
		Assert.assertEquals(createdAssociation.associationInverseName, "inverse Test");
		
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsComponent)
					.queryParam(RequestParameters.expand, "referencedConcept")
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
		
		RestAssociationTypeVersion[] createdAssociations = XMLUtils.unmarshalObjectArray(RestAssociationTypeVersion.class, result);
		
		Assert.assertEquals(1, createdAssociations.length);
		Assert.assertEquals(createdAssociations[0].associationName, "test");
		Assert.assertEquals(createdAssociations[0].description, description);
		Assert.assertEquals(createdAssociations[0].associationInverseName, "inverse Test");
		Assert.assertEquals(createdAssociations[0].associationConcept.getIdentifiers().getFirst(), createdAssociationId.uuid);
		
		//test create on association item(s)
		
		//Make one
		Response createAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent 
					+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationTypeSequence", "sourceNid", "targetNid"}, 
								new String[] {createdAssociations[0].associationConcept.getConceptSequence() + "", MetaData.DOD_MODULE.getNid() + "", 
										MetaData.AND.getNid() + ""})));
		
		result = checkFail(createAssociationItemResponse).readEntity(String.class);
		RestWriteResponse createdAssociationItemId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		
		//readBack
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationItemComponent + createdAssociationItemId.uuid.toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		RestAssociationItemVersion createdAssociationItem = XMLUtils.unmarshalObject(RestAssociationItemVersion.class, result);
		
		Assert.assertEquals(createdAssociationItem.identifiers.getFirst(), createdAssociationItemId.uuid);
		Assert.assertEquals(createdAssociationItem.sourceNid, MetaData.DOD_MODULE.getNid());
		Assert.assertEquals(createdAssociationItem.targetNid.intValue(), MetaData.AND.getNid());
		Assert.assertEquals(createdAssociationItem.associationTypeSequence, createdAssociations[0].associationConceptSequence);
		Assert.assertEquals(createdAssociationItem.associationItemStamp.state.toString().toLowerCase(), "active");
		
		
		
		//test update association
		Response updateAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent 
				+ RestPaths.associationItemComponent + RestPaths.updatePathComponent + createdAssociationItemId.uuid.toString())
			.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
			.request()
			.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
					jsonIze(new String[] {"targetNid", "active"}, 
							new String[] {"", "false"})));
	
		result = checkFail(updateAssociationItemResponse).readEntity(String.class);
		RestWriteResponse updatedAssociationItemId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		
		Assert.assertEquals(updatedAssociationItemId.uuid, createdAssociationItemId.uuid);
		
		//readBack
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationItemComponent + createdAssociationItemId.uuid.toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		createdAssociationItem = XMLUtils.unmarshalObject(RestAssociationItemVersion.class, result);
		
		Assert.assertEquals(createdAssociationItem.identifiers.getFirst(), createdAssociationItemId.uuid);
		Assert.assertEquals(createdAssociationItem.sourceNid, MetaData.DOD_MODULE.getNid());
		Assert.assertNull(createdAssociationItem.targetNid);
		Assert.assertEquals(createdAssociationItem.associationTypeSequence, createdAssociations[0].associationConceptSequence);
		Assert.assertEquals(createdAssociationItem.associationItemStamp.state.toString().toLowerCase(), "inactive");
			
		
		//Make more stuff for queries
		RestWriteResponse descType2 = XMLUtils
				.unmarshalObject(RestWriteResponse.class,
						checkFail(target(
								RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent + RestPaths.associationComponent + RestPaths.createPathComponent)
										.queryParam(RequestParameters.editToken, getDefaultEditTokenString()).request()
										.header(Header.Accept.toString(), MediaType.APPLICATION_XML)
										.post(Entity.json(jsonIze(new String[] { "associationName", "associationInverseName", "description" },
												new String[] { "foo", "oof", description })))).readEntity(String.class));
	
		checkFail(target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent 
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
			.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
			.request()
			.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
					jsonIze(new String[] {"associationTypeSequence", "sourceNid", "targetNid"}, 
							new String[] {createdAssociations[0].associationConcept.getConceptSequence() + "", MetaData.AMT_MODULE.getNid() + "", ""}))));
		
		checkFail(target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent 
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
			.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
			.request()
			.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
					jsonIze(new String[] {"associationTypeSequence", "sourceNid", "targetNid"}, 
							new String[] {descType2.sequence + "", MetaData.AMT_MODULE.getNid() + "", 
									MetaData.AXIOM_ORIGIN.getNid() + ""}))));
		
		//test query by source
		
		RestAssociationItemVersion[] foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class, 
				checkFail(target(RestPaths.associationAPIsPathComponent 
					+ RestPaths.associationsWithSourceComponent + MetaData.AMT_MODULE.getNid())
				.queryParam(RequestParameters.expand, "referencedConcept")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertEquals(foundAssociations.length, 2);
		
		//test query by target
		

		foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class, checkFail(target(RestPaths.associationAPIsPathComponent 
				+ RestPaths.associationsWithTargetComponent + MetaData.AXIOM_ORIGIN.getNid())
			.queryParam(RequestParameters.expand, "referencedConcept")
			.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));
		
		//TODO this is broken - lucene indexes don't seem to be updating properly.  Dan to fix, someday....
//		Assert.assertEquals(foundAssociations.length, 1);
		
		foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class, checkFail(target(RestPaths.associationAPIsPathComponent 
				+ RestPaths.associationsWithTargetComponent + MetaData.AMT_MODULE.getNid())
			.queryParam(RequestParameters.expand, "referencedConcept")
			.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertEquals(foundAssociations.length, 0);
		
		//test query by type
		
		RestAssociationItemVersionPage pagedAssociations = XMLUtils.unmarshalObject(RestAssociationItemVersionPage.class, 
				checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsWithTypeComponent + createdAssociationId.uuid)
			.queryParam(RequestParameters.expand, "referencedConcept")
			.queryParam(RequestParameters.maxPageSize, "1")
			.queryParam(RequestParameters.pageNum, "1")
			.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertTrue(pagedAssociations.paginationData.totalIsExact);
		Assert.assertEquals(pagedAssociations.paginationData.pageNum, 1);
		Assert.assertEquals(pagedAssociations.paginationData.approximateTotal, 2);
		Assert.assertEquals(pagedAssociations.results.length, 1);
		Assert.assertEquals(pagedAssociations.results[0].associationTypeSequence, createdAssociationId.sequence.intValue());
		
		int r1Source = pagedAssociations.results[0].sourceNid;
		
		pagedAssociations = XMLUtils.unmarshalObject(RestAssociationItemVersionPage.class, 
				checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsWithTypeComponent + createdAssociationId.uuid)
			.queryParam(RequestParameters.expand, "referencedConcept")
			.queryParam(RequestParameters.maxPageSize, "1")
			.queryParam(RequestParameters.pageNum, "2")
			.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertTrue(pagedAssociations.paginationData.totalIsExact);
		Assert.assertEquals(pagedAssociations.paginationData.pageNum, 2);
		Assert.assertEquals(pagedAssociations.paginationData.approximateTotal, 2);
		Assert.assertEquals(pagedAssociations.results.length, 1);
		Assert.assertEquals(pagedAssociations.results[0].associationTypeSequence, createdAssociationId.sequence.intValue());
		
		Assert.assertNotEquals(r1Source, pagedAssociations.results[0].sourceNid);
	}
	
	@Test
	public void testSememeWrite()
	{
		//Make one
		
		
		String dataColumns = jsonIze(new DynamicSememeData[] {new DynamicSememeStringImpl("test"), new DynamicSememeLongImpl(5l)});
		System.out.println(jsonIze(new String[] {"assemblageConcept", "referencedComponent", "columnData"}, 
								new String[] {DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getNid() + "", 
										MetaData.CHINESE_LANGUAGE.getNid() + "", dataColumns}));
		
		Response createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getDefaultEditTokenString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"assemblageConcept", "referencedComponent", "columnData"}, 
								new String[] {DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getNid() + "", 
										MetaData.CHINESE_LANGUAGE.getNid() + "", dataColumns})));
		String result = checkFail(createSememeResponse).readEntity(String.class);
		
		RestWriteResponse createdSememeId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		
		//Read back
		
		result = checkFail(target(RestPaths.sememeAPIsPathComponent + RestPaths.versionComponent + createdSememeId.uuid.toString())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		
		RestDynamicSememeVersion createdSememe = XMLUtils.unmarshalObject(RestDynamicSememeVersion.class, result);

		Assert.assertEquals(createdSememe.getDataColumns().get(0).data.toString(), "test");
		Assert.assertEquals((Long)createdSememe.getDataColumns().get(1).data, new Long(5l));

	}
	

	private String jsonIze(DynamicSememeData[] dynamicSememeDatas)
	{
		String temp = "\n    ";
		StringBuilder sb = new StringBuilder();
		sb.append("[").append(temp);
		for (int i = 0; i < dynamicSememeDatas.length; i++)
		{
			sb.append("{").append(temp);
			sb.append("\"@class\" : \"" + mapClass(dynamicSememeDatas[i]) + "\",");
			sb.append(temp);
			sb.append("\"data\" : ");
			if (quoteWrap(dynamicSememeDatas[i]))
			{
				sb.append("\"");
			}
			sb.append(dynamicSememeDatas[i] == null ? "null" : dynamicSememeDatas[i].dataToString());
			if (quoteWrap(dynamicSememeDatas[i]))
			{
				sb.append("\"");
			}
			sb.append(temp);
			sb.append("},").append(temp);
		}
		sb.setLength(sb.length() - (temp.length() + 1));
		sb.append("]").append(temp);
		return sb.toString();
	}
	
	private boolean quoteWrap(DynamicSememeData data)
	{
		if (data == null)
		{
			return false;
		}
		switch(data.getDynamicSememeDataType())
		{
			case ARRAY:
			case BOOLEAN:
			case DOUBLE:
			case FLOAT:
			case INTEGER:
			case LONG:
			case NID:
			case SEQUENCE:
				return false;
			case STRING:
			case UUID:
				return true;
			case UNKNOWN:
			case BYTEARRAY:
			case POLYMORPHIC:
			default :
				throw new RuntimeException("not supported yet");
		}
	}
	
	public String mapClass(DynamicSememeData data)
	{
		if (data == null)
		{
			return RestDynamicSememeData.class.getName();
		}
		switch(data.getDynamicSememeDataType())
		{
			case ARRAY:
				break;
			case BOOLEAN:
				break;
			case BYTEARRAY:
				break;
			case DOUBLE:
				break;
			case FLOAT:
				break;
			case INTEGER:
				return RestDynamicSememeInteger.class.getName();
			case LONG:
				return RestDynamicSememeLong.class.getName();
			case NID:
				break;
			case POLYMORPHIC:
				break;
			case SEQUENCE:
				break;
			case STRING:
				return RestDynamicSememeString.class.getName();
			case UNKNOWN:
				break;
			case UUID:
				break;
			default :
				break;
			
		}
		throw new RuntimeException("Oops");
	}

	private String jsonIze(String[] names, String[] values)
	{
		String temp = "\n    ";
		StringBuilder sb = new StringBuilder();
		sb.append("{").append(temp);
		for (int i = 0; i < names.length; i++)
		{
			sb.append("\"");
			sb.append(names[i]);
			if (values[i].startsWith("["))
			{
				sb.append("\": ");
				sb.append(values[i]);
				sb.append(",");
			}
			else
			{
				sb.append("\":\"");
				sb.append(values[i]);
				sb.append("\",");
			}
			sb.append(temp);
		}
		sb.setLength(sb.length() - (temp.length() + 1));
		sb.append("}");
		return sb.toString();
	}
	
	

}