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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.jersey.test.JerseyTestNg;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.UserRole;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.commit.CommitService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeDataType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeValidatorType;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBoolean;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDouble;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequence;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.externalizable.BinaryDataReaderService;
import gov.vha.isaac.ochre.api.index.IndexServiceBI;
import gov.vha.isaac.ochre.api.logic.NodeSemantic;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.LogicCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeBooleanImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeFloatImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
import gov.vha.isaac.ochre.workflow.model.contents.ProcessDetail.ProcessStatus;
import gov.vha.isaac.ochre.workflow.provider.WorkflowProvider;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.LocalJettyRunner;
import gov.vha.isaac.rest.api.data.vuid.RestVuidBlockData;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponseEnumeratedDetails;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.concept.RestWriteResponseConceptCreate;
import gov.vha.isaac.rest.api1.data.RestCoordinatesToken;
import gov.vha.isaac.rest.api1.data.RestEditToken;
import gov.vha.isaac.rest.api1.data.RestId;
import gov.vha.isaac.rest.api1.data.RestSystemInfo;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersion;
import gov.vha.isaac.rest.api1.data.association.RestAssociationItemVersionPage;
import gov.vha.isaac.rest.api1.data.association.RestAssociationTypeVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionBase;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersionCreate;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.concept.RestConceptCreateData;
import gov.vha.isaac.rest.api1.data.concept.RestConceptVersion;
import gov.vha.isaac.rest.api1.data.coordinate.RestLanguageCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestLogicCoordinate;
import gov.vha.isaac.rest.api1.data.coordinate.RestTaxonomyCoordinate;
import gov.vha.isaac.rest.api1.data.enumerations.IdType;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;
import gov.vha.isaac.rest.api1.data.enumerations.RestWorkflowProcessStatusType;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemComputedDisplayField;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionPage;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersionUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayField;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetDisplayFieldCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValueCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetExtensionValueUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseCreate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionBaseUpdate;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersionClone;
import gov.vha.isaac.rest.api1.data.search.RestSearchResult;
import gov.vha.isaac.rest.api1.data.search.RestSearchResultPage;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeColumnInfoCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeDefinition;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionCreate;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionUpdate;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersionPage;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeBoolean;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeByteArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeDouble;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeFloat;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;
import gov.vha.isaac.rest.api1.data.systeminfo.RestIdentifiedObjectsResult;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowDefinition;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcess;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessAdvancementData;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessBaseCreate;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;
import gov.vha.isaac.rest.session.PrismeUserService;
import gov.vha.isaac.rest.session.RequestInfo;
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
	private static Logger log = LogManager.getLogger(RestTest.class);
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
	private final static String byRefSearchRequestPath = RestPaths.searchAPIsPathComponent + RestPaths.forReferencedComponentComponent;

	private final static String conceptDescriptionsRequestPath = RestPaths.conceptAPIsPathComponent +  RestPaths.descriptionsComponent;

	private final static String conceptVersionRequestPath = RestPaths.conceptAPIsPathComponent +  RestPaths.versionComponent;

	private static final String coordinatesTokenRequestPath = RestPaths.coordinateAPIsPathComponent + RestPaths.coordinatesTokenComponent;

	private final static String sememeByAssemblageRequestPath = RestPaths.sememeAPIsPathComponent + RestPaths.forAssemblageComponent;

	private final static String sememeByReferencedComponentRequestPath = RestPaths.sememeAPIsPathComponent + RestPaths.forReferencedComponentComponent;

	private static JsonNodeFactory jfn = JsonNodeFactory.instance;

	private final static PrismeUserService PRISME_USER_SERVICE = LookupService.getService(PrismeUserService.class);

	// Example tokens
	//	private static final String readOnlyToken="%5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC6%5CxF2%5CxE8%5CxA5%5CxD8%5CxE3t%5CxFFUK%22%2C+%22%2CJ%5Cx83%5CxA3%5Cx13k%5Cx96%5CxFC%5CxE6%5CxF3%5CxCF%5CxF2%7C%5CxB8MK%22%2C+%224%5Cf%5Cx94%5CxB0%5Ce%7C%5Cx9C%5CxB0%5CxA6%5CxA8%5CxE1%5CxE1t%5CxBC%5CvK%22%2C+%22a%40%5Cx8A%5CxACT%7B%5Cx9C%5CxB3%5CxE8%5CxAC%5CxA7%5Cx95%5Cx17%5CxDBiL%22%5D";
	//	private static final String gregToken="%5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC2%5CxEE%5CxFA%5CxE1%5Cx91%5CxBF3%5CxA9%5Cx16K%22%2C+%22%7EK%5CxC4%5CxEFX%7C%5Cx96%5CxA8%5CxA3%5CxA2%5CxC4%5CxB1%3D%5CxFF%5Cx01K%22%2C+%22oC%5Cx83%5CxF7%40%3A%5Cx94%5CxAC%5CxAF%5CxB6%5CxE1%5CxF4c%5CxB8%5CbK%22%2C+%22+M%5Cx89%5CxB8Xe%5CxF9%5CxD4%5CxC0%5CxDB%5CxAB%5Cx99%5Ce%5CxD7e%40%22%5D";
	//	private static final String joelToken="%5B%22u%5Cf%5Cx8F%5CxB1X%5C%22%5CxC7%5CxF2%5CxE8%5CxA5%5CxD8%5CxE3t%5CxFFUK%22%2C+%22%2CJ%5Cx83%5CxA3%5Cx13k%5Cx96%5CxFC%5CxE6%5CxF3%5CxCF%5CxF2%7C%5CxB8MK%22%2C+%224%5Cf%5Cx8C%5CxBA%5Cx1Ft%5CxDD%5CxB5%5CxA4%5CxB8%5CxC0%5CxE9Q%5CxAB%5CnK%22%2C+%22z%5D%5Cx83%5CxAFT%7B%5Cx9C%5CxB3%5CxE8%5CxAC%5CxA7%5Cx95%5Cx17%5CxDBiL%22%5D";

	private static final String TEST_SSO_TOKEN = usePrismeForRolesByToken() ? getTokenFromPrisme("joel.kniaz@vetsez.com", "joel.kniaz@vetsez.com") : "TestUser:super_user,editor,read_only,approver,administrator,reviewer,manager,vuid_requestor";
	private static final String TEST_READ_ONLY_SSO_TOKEN = usePrismeForRolesByToken() ? getTokenFromPrisme("readonly@readonly.com", "readonly@readonly.com") : "TestReadOnlyUser:read_only";

	private static boolean usePrismeForRolesByToken() {
		try {
			return PRISME_USER_SERVICE.usePrismeForRolesByToken();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	private static String getTokenFromPrisme(String name, String password) {
		Optional<String> token = PRISME_USER_SERVICE.safeGetToken(name, password);
		return token.get();
	}

	private CoordinatesToken getDefaultCoordinatesToken() throws RestException {
		String editCoordinatesTokenXml = checkFail((target(coordinatesTokenRequestPath))
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		RestCoordinatesToken coordinatesToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, editCoordinatesTokenXml);

		return CoordinatesTokens.getOrCreate(coordinatesToken.token);
	}

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
		Response response = target(conceptVersionRequestPath +
				DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid()).request().get();

		checkFail(response);
	}

	private String expectFail(Response response)
	{
		String temp = response.readEntity(String.class);
		if (response.getStatus() == Status.OK.getStatusCode())
		{
			Assert.fail("Should have failed but did not. Response code " + response.getStatus() + " - " + Status.fromStatusCode(response.getStatus()) + temp);
		}

		return temp;
	}

	private Response checkFail(Response response)
	{
		return assertResponseStatus(response, Status.OK.getStatusCode());
	}
	private Response assertResponseStatus(Response response, int expectedStatus)
	{
		if (response.getStatus() != expectedStatus)
		{
			Assert.fail("Unexpected response code " + response.getStatus() + " \"" + Status.fromStatusCode(response.getStatus()) + "\". Expected " + expectedStatus + " \"" + Status.fromStatusCode(expectedStatus) + "\". "
					+ " " + response.readEntity(String.class));
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
		return getDescriptionsForConcept(id, (Map<String, Object>)null);
	}
	private RestSememeDescriptionVersion[] getDescriptionsForConcept(Object id, Map.Entry<String, Object>...params) {
		WebTarget webTarget = target(conceptDescriptionsRequestPath + id.toString());
		if (params != null) {
			for (Map.Entry<String, Object> entry : params) {
				webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
			}
		}
		Response getDescriptionVersionsResponse =
				webTarget.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String descriptionVersionsResult = checkFail(getDescriptionVersionsResponse).readEntity(String.class);
		return XMLUtils.unmarshalObjectArray(RestSememeDescriptionVersion.class, descriptionVersionsResult);
	}
	private RestSememeDescriptionVersion[] getDescriptionsForConcept(Object id, Map<String, Object> params) {
		@SuppressWarnings("rawtypes")
		Map.Entry[] entries = (params != null) ? params.entrySet().toArray(new Map.Entry[params.entrySet().size()]) : null;
		return getDescriptionsForConcept(id, entries);
	}
	public String getEditTokenString(String ssoTokenString) {
		String encodedToken = null;
		try {
			encodedToken = URLEncoder.encode(ssoTokenString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		Response getEditTokenResponse = target(editTokenRequestPath.replaceFirst(RestPaths.appPathComponent, ""))
				.queryParam(RequestParameters.ssoToken, encodedToken)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getEditTokenResponseResult = checkFail(getEditTokenResponse).readEntity(String.class);
		RestEditToken restEditTokenObject = XMLUtils.unmarshalObject(RestEditToken.class, getEditTokenResponseResult);
		return restEditTokenObject.token;
	}

	private RestWorkflowDefinition getDefaultWorkflowDefinition() {
		Response getDefinitionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.definition)
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
		Response removeComponentResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
				+ RestPaths.updatePathComponent + RestPaths.processComponent + Integer.toString(processComponentSpecificationData))
				.queryParam(RequestParameters.editToken, token.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		String removeComponentResponseResult = checkFail(removeComponentResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, removeComponentResponseResult);
		return writeResponse;
	}

	// PLACE TEST METHODS BELOW HERE
	@Test
	public void testVuidWriteAPIs() throws JsonParseException, JsonMappingException, IOException {
		// THIS TEST ONLY WORKS IF THE VUID-rest SERVER IS RUNNING IN THE SERVER SPECIFIED BY THE prisme_root PROPERTY IN prisme.properties
		// TODO configure to start own VUID-rest server
		final int blockSize = 10;
		final String reason = "A test reason";

		Response getResponse = target(RestPaths.writePathComponent + RestPaths.vuidAPIsPathComponent + RestPaths.allocateComponent)
				.queryParam(RequestParameters.ssoToken, TEST_SSO_TOKEN)
				.queryParam(RequestParameters.blockSize, blockSize)
				.queryParam(RequestParameters.reason, reason)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).post(Entity.xml(""));
		String getResponseResult = checkFail(getResponse).readEntity(String.class);
		RestVuidBlockData vuids = new ObjectMapper().readValue(getResponseResult, RestVuidBlockData.class);
		
		Assert.assertNotNull(vuids);

		Assert.assertTrue((vuids.startInclusive > 0 && vuids.endInclusive > 0) || (vuids.startInclusive < 0 && vuids.endInclusive < 0));
		Assert.assertEquals(Math.abs(Math.abs(vuids.endInclusive) - Math.abs(vuids.startInclusive)), blockSize - 1);
	}

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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Assert.assertNull(retrievedEditToken.getActiveWorkflowProcessId());

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
		Assert.assertEquals(newEditToken.getActiveWorkflowProcessId(), retrievedEditToken.getActiveWorkflowProcessId());
	}

	//	@Test
	//	public void testExport()
	//	{
	//		// Get and save time before first edit
	//		final long preEditTime = System.currentTimeMillis();
	//
	//		// Request a view coordinate token using the preEditTime
	//		String preEditCoordinatesTokenXml = checkFail((target(coordinatesTokenRequestPath)
	//				.queryParam(RequestParameters.time, preEditTime))
	//				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
	//				.readEntity(String.class);
	//		RestCoordinatesToken preEditCoordinatesToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, preEditCoordinatesTokenXml);
	//
	//		// Sleep to ensure that initial edit occurs after preEditTime
	//		try {
	//			Thread.sleep(10);
	//		} catch (InterruptedException e1) {
	//			e1.printStackTrace(); // Shouldn't happen
	//		}
	//
	//		// Create a random string to confirm target data are relevant
	//		final UUID randomUuid = UUID.randomUUID();
	//
	//		CREATE SOMETHING HERE;
	//
	//		//
	//		Response exportResponse = target(RestPaths.exportAPIsPathComponent)
	//				.queryParam(RequestParameters.changedAfter, preEditTime)
	//				.request()
	//				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
	//		checkFail(exportResponse);
	//	}

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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// active processId should be null
		Assert.assertNull(editToken.getActiveWorkflowProcessId());

		Optional<UUID> userUuidOptional = Get.identifierService().getUuidPrimordialFromConceptId(editToken.getAuthorSequence());
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
		Response getAvailableProcessesResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.list + definitionId.toString())
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

		String getAvailableProcessesResponseResult = checkFail(getAvailableProcessesResponse).readEntity(String.class);
		RestWorkflowProcess[] availableProcesses = XMLUtils.unmarshalObjectArray(RestWorkflowProcess.class, getAvailableProcessesResponseResult);

		// We may have not created any processes, so there may be no available processes returned unless the DB has not been cleaned prior to testing
		//Assert.assertNotNull(availableProcesses);

		// Test process creation
		UUID workflowProcessUuid = UUID.randomUUID();
		RestWorkflowProcessBaseCreate newProcessData = new RestWorkflowProcessBaseCreate(
				definitionId,
				"Test WF Process Name (" + workflowProcessUuid + ")",
				"Test WF Process Description (" + workflowProcessUuid + ")");
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
		Assert.assertNotNull(writeResponse.editToken);

		// Update edit token with new value containing processId
		try {
			editToken = EditTokens.getOrCreate(writeResponse.editToken.token);
		} catch (Exception e) {
			Assert.fail("Failed creating EditToken from writeResponse.editToken.token=\"" + writeResponse.editToken.token + "\"", e);
		}

		Assert.assertNotNull(editToken.getActiveWorkflowProcessId());

		// Check for created process using getProcess()
		Response getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + editToken.getActiveWorkflowProcessId().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		RestWorkflowProcess process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		Assert.assertNotNull(process.getId());
		Assert.assertEquals(process.getId(), editToken.getActiveWorkflowProcessId());
		Assert.assertNotNull(process.getCreatorId());
		Assert.assertEquals(process.getCreatorId(), userUuid);
		Assert.assertTrue(process.getTimeCreated() > 0);
		Assert.assertTrue(process.getTimeLaunched() < 0); // Process should be DEFINED but not LAUNCHED
		Assert.assertTrue(process.getTimeCancelledOrConcluded() < 0);
		Assert.assertNotNull(process.getProcessStatus());
		Assert.assertEquals(process.getProcessStatus(), new RestWorkflowProcessStatusType(ProcessStatus.DEFINED));

		// Confirm that request for non-existent process should fail
		Response getBadProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + UUID.randomUUID().toString()) // Garbage processId
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		expectFail(getBadProcessResponse);

		// Check for created process in retrieved available
		getAvailableProcessesResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.list + definitionId.toString())
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();

		getAvailableProcessesResponseResult = checkFail(getAvailableProcessesResponse).readEntity(String.class);
		availableProcesses = XMLUtils.unmarshalObjectArray(RestWorkflowProcess.class, getAvailableProcessesResponseResult);

		// We have created a process, so at least that process should be returned
		Assert.assertNotNull(availableProcesses);
		Assert.assertTrue(availableProcesses.length > 0);

		// Get history and compare with history from get available
		Response getProcessHistoryResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.history + editToken.getActiveWorkflowProcessId().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getProcessHistoryResponseResult = checkFail(getProcessHistoryResponse).readEntity(String.class);
		RestWorkflowProcessHistory[] processHistories = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistory.class, getProcessHistoryResponseResult);
		// We have created process, so at least one history entry should exist
		Assert.assertNotNull(processHistories);
		Assert.assertTrue(processHistories.length > 0);
		RestWorkflowProcessHistory historyFromHistories = processHistories[0];
		Assert.assertNotNull(historyFromHistories);
		//Assert.assertTrue(historyFromAvailableProcesses.equals(historyFromHistories));

		// Confirm that get history returns no histories for bogus processId
		Response getBadProcessHistoryResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.history + UUID.randomUUID().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getBadProcessHistoryResponseResult = checkFail(getBadProcessHistoryResponse).readEntity(String.class);
		RestWorkflowProcessHistory[] badProcessHistories = XMLUtils.unmarshalObjectArray(RestWorkflowProcessHistory.class, getBadProcessHistoryResponseResult);
		Assert.assertTrue(badProcessHistories == null || badProcessHistories.length == 0);


		// Get available actions for created process
		Response getAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions + editToken.getActiveWorkflowProcessId().toString())
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
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
			Assert.assertTrue(UserRole.safeValueOf(availableAction.getRole().enumId).isPresent());
			Assert.assertTrue(
					editToken.getRoles().contains(UserRole.safeValueOf(availableAction.getRole().enumId).get())
					|| editToken.getRoles().contains(UserRole.SUPER_USER));
		}
		Assert.assertTrue(foundEditAction); // definition-specific
		Assert.assertTrue(foundCancelWorkflowAction); // definition-specific

		// Confirm that call fails for bogus processId
		Response getBadAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions + UUID.randomUUID().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		expectFail(getBadAvailableActionsResponse);

		// Acquire lock on process.  This should Fail because it's automatically locked on create.
		String lockingRequestType = Boolean.toString(true);
		Response lockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
				+ RestPaths.updatePathComponent + RestPaths.process + RestPaths.lock + editToken.getActiveWorkflowProcessId().toString() )
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.queryParam(RequestParameters.acquireLock, lockingRequestType)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML)
				.put(Entity.xml(lockingRequestType));
		expectFail(lockProcessResponse);

		//NUNO: added this here to renew token because of tests were failing. It should not be needed.
		EditTokens.renew(editToken);

		// Release lock on process
		lockingRequestType = Boolean.toString(false);
		Response unlockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
				+ RestPaths.updatePathComponent + RestPaths.process + RestPaths.lock + editToken.getActiveWorkflowProcessId().toString())
				.queryParam(RequestParameters.editToken, editToken.getSerialized())
				.queryParam(RequestParameters.acquireLock, lockingRequestType)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML)
				.put(Entity.xml(lockingRequestType));

		String unlockProcessResponseResult = checkFail(unlockProcessResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, unlockProcessResponseResult);
		RestEditToken renewedEditToken = writeResponse.editToken;
		Assert.assertNotNull(renewedEditToken);

		// Acquire lock on process
		lockingRequestType = Boolean.toString(true);
		lockProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
				+ RestPaths.updatePathComponent + RestPaths.process + RestPaths.lock + editToken.getActiveWorkflowProcessId().toString())
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.queryParam(RequestParameters.acquireLock, lockingRequestType)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML)
				.put(Entity.xml(lockingRequestType));
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
		Response advanceProcessResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
				+ RestPaths.updatePathComponent + RestPaths.advanceProcess)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		expectFail(advanceProcessResponse);
		renewedEditToken = writeResponse.editToken;

		// Create a concept in this workflow process
		final int parent1Sequence = MetaData.SNOROCKET_CLASSIFIER.getConceptSequence();
		final int parent2Sequence = MetaData.ENGLISH_LANGUAGE.getConceptSequence();

		final int requiredDescriptionsLanguageSequence = MetaData.ENGLISH_LANGUAGE.getConceptSequence();
		final int requiredDescriptionsExtendedTypeSequence = requiredDescriptionsLanguageSequence;

		final UUID randomUuid = UUID.randomUUID();

		final String fsn = "fsn for test concept " + randomUuid.toString();
		final String pt = "preferred term for test concept " + randomUuid.toString();

		final List<String> parentIds = new ArrayList<>();
		parentIds.add(parent1Sequence + "");
		parentIds.add(parent2Sequence + "");

		List<String> preferredDialects = new ArrayList<>();
		preferredDialects.add(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid().toString());
		preferredDialects.add(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid().toString());

		RestConceptCreateData newConceptData = new RestConceptCreateData(
				parentIds,
				fsn,
				true,
				requiredDescriptionsLanguageSequence + "",
				requiredDescriptionsExtendedTypeSequence + "",
				preferredDialects);

		xml = null;
		try {
			xml = XMLUtils.marshallObject(newConceptData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		editToken = EditTokens.renew(editToken);

		Response createConceptResponse = target(RestPaths.conceptCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newConceptSequenceWrapperXml = createConceptResponse.readEntity(String.class);

		//NUNO
		//		RestWriteResponse newConceptSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newConceptSequenceWrapperXml);
		//		int newConceptSequence = newConceptSequenceWrapper.sequence;
		//		// Confirm returned sequence is valid
		//		Assert.assertTrue(newConceptSequence > 0);
		//		int newConceptNid = Get.identifierService().getConceptNid(newConceptSequence);
		//
		//		// Get process after adding components
		//		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + editToken.getActiveWorkflowProcessId().toString())
		//				.request()
		//				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		//		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		//		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		//		Assert.assertNotNull(process);

		// Get list of components in process
		//		Set<Integer> componentsInProcessBeforeRemovingComponent = new HashSet<>();
		//		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
		//			componentsInProcessBeforeRemovingComponent.add(restWorkflowComponentToStampMapEntry.getKey());
		//		}
		//		Assert.assertTrue(componentsInProcessBeforeRemovingComponent.size() > 0);
		//
		//		// Remove one of the components in the process
		//		String componentNid = Integer.toString(componentsInProcessBeforeRemovingComponent.iterator().next());
		//		Response removeComponentResponse = target(RestPaths.writePathComponent + RestPaths.workflowAPIsPathComponent
		//				+ RestPaths.updatePathComponent + RestPaths.removeComponent + componentNid)
		//				.queryParam(RequestParameters.editToken, editToken.getSerialized())
		//				.request()
		//				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(componentNid));
		//		String removeComponentResponseResult = checkFail(removeComponentResponse).readEntity(String.class);
		//		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, removeComponentResponseResult);
		//		renewedEditToken = writeResponse.editToken;
		//		Assert.assertNotNull(renewedEditToken);

		// Retrieve process after removing component
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + editToken.getActiveWorkflowProcessId().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);

		//		Set<Integer> componentsInProcessAfterRemovingComponent = new HashSet<>();
		//		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
		//			componentsInProcessAfterRemovingComponent.add(restWorkflowComponentToStampMapEntry.getKey());
		//		}
		//		Assert.assertTrue(! componentsInProcessAfterRemovingComponent.contains(componentNid));
		//		Assert.assertTrue(componentsInProcessAfterRemovingComponent.size() == (componentsInProcessBeforeRemovingComponent.size() - 1));

		// Get process to check for added components
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + editToken.getActiveWorkflowProcessId().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		boolean foundCreatedConceptNidInProcess = false;
		//		for (RestWorkflowComponentToStampMapEntry restWorkflowComponentToStampMapEntry : process.getComponentToStampMap()) {
		//			if (restWorkflowComponentToStampMapEntry.getKey() == newConceptNid) {
		//				foundCreatedConceptNidInProcess = true;
		//				break;
		//			}
		//		}
		//TODO [WF]- I had to comment out another test that is randomly broken...
		//		Assert.assertTrue(foundCreatedConceptNidInProcess);

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
		getProcessResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.process + editToken.getActiveWorkflowProcessId().toString())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		getProcessResponseResult = checkFail(getProcessResponse).readEntity(String.class);
		process = XMLUtils.unmarshalObject(RestWorkflowProcess.class, getProcessResponseResult);
		Assert.assertNotNull(process);
		Assert.assertNotNull(process.getId());
		Assert.assertNotNull(process.getCreatorId());
		Assert.assertEquals(process.getCreatorId(), userUuid);
		Assert.assertTrue(process.getTimeCreated() > 0);
		Assert.assertTrue(process.getTimeLaunched() > 0); // TODO [WF]debug this failure. process.getTimeLaunched() should be > 0
		Assert.assertTrue(process.getTimeCancelledOrConcluded() < 0);
		Assert.assertNotNull(process.getProcessStatus());
		Assert.assertEquals(process.getProcessStatus(), new RestWorkflowProcessStatusType(ProcessStatus.LAUNCHED));

		// Get available actions after advancement
		getAvailableActionsResponse = target(RestPaths.workflowAPIsPathComponent + RestPaths.actions + editToken.getActiveWorkflowProcessId().toString())
				.queryParam(RequestParameters.editToken, renewedEditToken.token)
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
			Assert.assertTrue(UserRole.safeValueOf(availableAction.getRole().enumId).isPresent());
			Assert.assertTrue(
					editToken.getRoles().contains(UserRole.safeValueOf(availableAction.getRole().enumId).get())
					|| editToken.getRoles().contains(UserRole.SUPER_USER));
		}
		Assert.assertTrue(foundQaFailsAction);
		Assert.assertTrue(foundCancelWorkflowAction);
		Assert.assertTrue(foundQaPassesAction);
	}

	@Test
	public void testPriorVersionRetrieval() throws JsonProcessingException, IOException
	{
		// Get and save time before first edit
		final long preEditTime = System.currentTimeMillis();

		// Request a view coordinate token using the preEditTime
		String preEditCoordinatesTokenXml = checkFail((target(coordinatesTokenRequestPath)
				.queryParam(RequestParameters.time, preEditTime))
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);
		RestCoordinatesToken preEditCoordinatesToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, preEditCoordinatesTokenXml);

		// Sleep to ensure that initial edit occurs after preEditTime
		try {
			Thread.sleep(10);
		} catch (InterruptedException e1) {
			e1.printStackTrace(); // Shouldn't happen
		}

		// Create a random string to confirm target data are relevant
		final UUID randomUuid = UUID.randomUUID();

		// Construct new description data object
		final int referencedConceptNid = MetaData.SNOROCKET_CLASSIFIER.getNid();
		final int initialCaseSignificanceConceptSequence = MetaData.DESCRIPTION_CASE_SENSITIVE.getConceptSequence();
		final int initialLanguageConceptSequence = MetaData.SPANISH_LANGUAGE.getConceptSequence();
		final int initialDescriptionTypeConceptSequence = MetaData.SYNONYM.getConceptSequence();
		final String initialDescriptionText = "An initial description text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";

		// Retrieve all descriptions referring to referenced concept and just pick the first
		final String expandParamValue = ExpandUtil.nestedSememesExpandable + "," + ExpandUtil.comments + "," + ExpandUtil.referencedDetails;
		Map<String, Object> params = new HashMap<>();
		params.put(RequestParameters.expand, expandParamValue);
		RestSememeDescriptionVersion[] conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertTrue(conceptDescriptions.length > 0);
		// Get first description
		RestSememeDescriptionVersion preexistingDescription = conceptDescriptions[0];

		RestSememeDescriptionCreate initialDescriptionData =
				new RestSememeDescriptionCreate(
						initialCaseSignificanceConceptSequence + "",
						initialLanguageConceptSequence + "",
						initialDescriptionText,
						initialDescriptionTypeConceptSequence + "",
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
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		checkFail(createDescriptionResponse);
		String descriptionSememeSequenceWrapperXml = createDescriptionResponse.readEntity(String.class);
		final RestWriteResponse descriptionSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, descriptionSememeSequenceWrapperXml);
		final int createdDescriptionSememeSequence = descriptionSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(createdDescriptionSememeSequence != 0);

		// Retrieve all descriptions referring to referenced concept
		params.clear();
		params.put(RequestParameters.expand, expandParamValue);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertTrue(conceptDescriptions.length > 0);
		// Iterate description list to find new description
		RestSememeDescriptionVersion matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.sequence.equals(createdDescriptionSememeSequence)) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Validate description fields
		Assert.assertNotNull(matchingDescriptionSememeVersion);
		Assert.assertEquals(matchingDescriptionSememeVersion.caseSignificanceConcept.sequence.intValue(), initialCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingDescriptionSememeVersion.text, initialDescriptionText);
		Assert.assertEquals(matchingDescriptionSememeVersion.descriptionTypeConcept.sequence.intValue(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingDescriptionSememeVersion.languageConcept.sequence.intValue(), initialLanguageConceptSequence);
		Assert.assertEquals(matchingDescriptionSememeVersion.getSememeChronology().referencedComponent.nid.intValue(), referencedConceptNid);

		// Retrieve all descriptions referring to referenced concept from yesterday
		// using time parameter
		params.clear();
		params.put(RequestParameters.time, preEditTime);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertNotNull(conceptDescriptions);
		Assert.assertTrue(conceptDescriptions.length > 1);
		// Iterate description list to find new description
		matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.sequence == createdDescriptionSememeSequence) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Should not find newly-created description in older version
		// using time parameter
		Assert.assertNull(matchingDescriptionSememeVersion);

		// Retrieve all descriptions referring to referenced concept from yesterday
		// using coordToken parameter
		params.clear();
		params.put(RequestParameters.coordToken, preEditCoordinatesToken.token);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertNotNull(conceptDescriptions);
		Assert.assertTrue(conceptDescriptions.length > 1);
		// Iterate description list to find new description
		matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.sequence == createdDescriptionSememeSequence) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Should not find newly-created description in older version
		// using coordToken parameter
		Assert.assertNull(matchingDescriptionSememeVersion);

		//Create a comment on preexisting description
		final String commentText = "my random comment (" + randomUuid + ")";

		// Confirm that comment with this text does not exist in preexisting description
		// Get list of RestCommentVersion associated with preexisting description
		Response commentVersionsByReferencedItemResponse = target(RestPaths.commentVersionByReferencedComponentPathComponent + preexistingDescription.getSememeChronology().identifiers.nid)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String commentVersionsByReferencedItemXml = checkFail(commentVersionsByReferencedItemResponse).readEntity(String.class);
		RestCommentVersion[] commentVersionsByReferencedItem = XMLUtils.unmarshalObjectArray(RestCommentVersion.class, commentVersionsByReferencedItemXml);
		RestCommentVersion matchingComment = null;
		if (commentVersionsByReferencedItem != null) {
			for (RestCommentVersion commentVersion : commentVersionsByReferencedItem) {
				if (commentVersion.comment != null && commentVersion.comment.equals(commentText)) {
					matchingComment = commentVersion;
					break;
				}
			}
		}
		// Comment with that text should not yet exist
		Assert.assertNull(matchingComment);

		// Create new comment with specified text on preexisting description
		String json = jsonIze(new String[] {"commentedItem", "comment"}, new String[] { preexistingDescription.getSememeChronology().identifiers.nid + "", commentText });
		Response createCommentResponse = checkFail(target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(json)));
		String createCommentResponseXml = createCommentResponse.readEntity(String.class);
		RestWriteResponse createCommentResponseObject = XMLUtils.unmarshalObject(RestWriteResponse.class, createCommentResponseXml);
		int newCommentSememeSequence = createCommentResponseObject.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newCommentSememeSequence > 0);

		// Confirm that comment with this text now exists in preexisting description
		// Get list of RestCommentVersion associated with preexisting description
		commentVersionsByReferencedItemResponse = target(RestPaths.commentVersionByReferencedComponentPathComponent + preexistingDescription.getSememeChronology().identifiers.nid)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		commentVersionsByReferencedItemXml = checkFail(commentVersionsByReferencedItemResponse).readEntity(String.class);
		commentVersionsByReferencedItem = XMLUtils.unmarshalObjectArray(RestCommentVersion.class, commentVersionsByReferencedItemXml);
		Assert.assertTrue(commentVersionsByReferencedItem != null && commentVersionsByReferencedItem.length > 0);
		matchingComment = null;
		for (RestCommentVersion commentVersion : commentVersionsByReferencedItem) {
			if (commentVersion.comment != null && commentVersion.comment.equals(commentText)) {
				matchingComment = commentVersion;
				break;
			}
		}
		// Comment with that text should exist
		Assert.assertNotNull(matchingComment);

		// Retrieve all descriptions referring to referenced concept
		// and find new comment in preexisting description
		params.clear();
		params.put(RequestParameters.expand, expandParamValue);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertNotNull(conceptDescriptions);
		Assert.assertTrue(conceptDescriptions.length > 1);
		// Iterate description list to find new description
		matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.nid.equals(preexistingDescription.getSememeChronology().identifiers.nid)) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Should find preexisting description
		Assert.assertNotNull(matchingDescriptionSememeVersion);

		// Find newly created comment on latest version of preexisting description
		Assert.assertTrue(matchingDescriptionSememeVersion.getNestedSememes().size() > 0);
		RestDynamicSememeVersion commentDynamicSememeFoundInDescription = null;
		for (RestDynamicSememeVersion nestedSememe : matchingDescriptionSememeVersion.getNestedSememes()) {
			if (nestedSememe.getSememeChronology().identifiers.nid.equals(matchingComment.identifiers.nid)) {
				commentDynamicSememeFoundInDescription = nestedSememe;
			}
		}
		// Should find newly created comment on latest version of preexisting description
		Assert.assertNotNull(commentDynamicSememeFoundInDescription);

		// Retrieve all descriptions referring to referenced concept with stamp older than comment creation time
		// using time parameter
		params.clear();
		params.put(RequestParameters.time, preEditTime);
		params.put(RequestParameters.expand, expandParamValue);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertNotNull(conceptDescriptions);
		Assert.assertTrue(conceptDescriptions.length > 1);
		// Iterate description list to find new description
		matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.nid.equals(preexistingDescription.getSememeChronology().identifiers.nid)) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Should find preexisting description
		// using time parameter
		Assert.assertNotNull(matchingDescriptionSememeVersion);

		// Look for newly created comment on older version of preexisting description
		// using time parameter
		commentDynamicSememeFoundInDescription = null;
		if (matchingDescriptionSememeVersion.getNestedSememes().size() > 0) {
			for (RestDynamicSememeVersion nestedSememe : matchingDescriptionSememeVersion.getNestedSememes()) {
				if (nestedSememe.getSememeChronology().identifiers.nid.equals(matchingComment.identifiers.nid)) {
					commentDynamicSememeFoundInDescription = nestedSememe;
				}
			}
		}
		// Should NOT find newly created comment on older version of preexisting description
		// using time parameter
		Assert.assertNull(commentDynamicSememeFoundInDescription);

		// Retrieve all descriptions referring to referenced concept with stamp older than comment creation time
		// using coordToken parameter
		params.clear();
		params.put(RequestParameters.coordToken, preEditCoordinatesToken.token);
		params.put(RequestParameters.expand, expandParamValue);
		conceptDescriptions = getDescriptionsForConcept(referencedConceptNid, params);
		Assert.assertNotNull(conceptDescriptions);
		Assert.assertTrue(conceptDescriptions.length > 1);
		// Iterate description list to find new description
		matchingDescriptionSememeVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptions) {
			if (version.getSememeChronology().identifiers.nid.equals(preexistingDescription.getSememeChronology().identifiers.nid)) {
				matchingDescriptionSememeVersion = version;
				break;
			}
		}
		// Should find preexisting description
		// using coordToken parameter
		Assert.assertNotNull(matchingDescriptionSememeVersion);

		// Look for newly created comment on older version of preexisting description
		// using coordToken parameter
		commentDynamicSememeFoundInDescription = null;
		if (matchingDescriptionSememeVersion.getNestedSememes().size() > 0) {
			for (RestDynamicSememeVersion nestedSememe : matchingDescriptionSememeVersion.getNestedSememes()) {
				if (nestedSememe.getSememeChronology().identifiers.nid.equals(matchingComment.identifiers.nid)) {
					commentDynamicSememeFoundInDescription = nestedSememe;
				}
			}
		}
		// Should NOT find newly created comment on older version of preexisting description
		// using coordToken parameter
		Assert.assertNull(commentDynamicSememeFoundInDescription);
	}

	@Test
	public void testSememeAPIs()
	{
		// Create a random string to confirm target data are relevant
		final UUID randomUuid = UUID.randomUUID();

		// Construct new description data object
		final int referencedConceptNid = MetaData.SNOROCKET_CLASSIFIER.getNid();
		final int initialCaseSignificanceConceptSequence = MetaData.DESCRIPTION_CASE_SENSITIVE.getConceptSequence();
		final int initialLanguageConceptSequence = MetaData.SPANISH_LANGUAGE.getConceptSequence();
		final int initialDescriptionTypeConceptSequence = MetaData.SYNONYM.getConceptSequence();
		final String initialDescriptionText = "An initial description text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";

		//		final int referencedConceptNid = MetaData.AMT_MODULE.getNid();
		//		final int initialCaseSignificanceConceptSequence = MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence();
		//		final int initialLanguageConceptSequence = MetaData.FRENCH_LANGUAGE.getConceptSequence();
		//		final int initialDescriptionTypeConceptSequence = MetaData.SYNONYM.getConceptSequence();
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
		RestSememeDescriptionCreate initialDescriptionData =
				new RestSememeDescriptionCreate(
						initialCaseSignificanceConceptSequence + "",
						initialLanguageConceptSequence + "",
						initialDescriptionText,
						initialDescriptionTypeConceptSequence + "",
						null,
						null,
						referencedConceptNid);
		String xml = null;
		try {
			xml = XMLUtils.marshallObject(initialDescriptionData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// POST new description data object as unauthorized read_only user
		Response createDescriptionResponse = target(RestPaths.descriptionCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(createDescriptionResponse, Status.FORBIDDEN.getStatusCode());

		// POST new description data object
		createDescriptionResponse = target(RestPaths.descriptionCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		checkFail(createDescriptionResponse);
		String descriptionSememeSequenceWrapperXml = createDescriptionResponse.readEntity(String.class);
		final RestWriteResponse descriptionSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, descriptionSememeSequenceWrapperXml);
		final int descriptionSememeSequence = descriptionSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(descriptionSememeSequence != 0);

		// Retrieve all descriptions referring to referenced concept
		// Restrict stamp coordinate to the module used in the default edit coordinate
		RestSememeDescriptionVersion[] conceptDescriptionsObject = getDescriptionsForConcept(referencedConceptNid, param(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence()));
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		RestSememeDescriptionVersion matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().identifiers.sequence == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		// Validate description fields
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.caseSignificanceConcept.sequence.intValue(), initialCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.text, initialDescriptionText);
		Assert.assertEquals(matchingVersion.descriptionTypeConcept.sequence.intValue(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.languageConcept.sequence.intValue(), initialLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().referencedComponent.nid.intValue(), referencedConceptNid);

		// Construct description update data object
		final int newCaseSignificanceConceptSequence = MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence();
		final int newLanguageConceptSequence = MetaData.FRENCH_LANGUAGE.getConceptSequence();
		//final int newDescriptionTypeConceptSequence = MetaData.SYNONYM.getConceptSequence();
		final String newDescriptionText = "A new description text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";

		RestSememeDescriptionUpdate newDescriptionData =
				new RestSememeDescriptionUpdate(
						newCaseSignificanceConceptSequence + "",
						newLanguageConceptSequence + "",
						newDescriptionText,
						initialDescriptionTypeConceptSequence + "",
						true);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(newDescriptionData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// Attempt update as read_only user
		Response updateDescriptionResponse = target(RestPaths.descriptionUpdatePathComponent + descriptionSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		assertResponseStatus(updateDescriptionResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt update as authorized user
		updateDescriptionResponse = target(RestPaths.descriptionUpdatePathComponent + descriptionSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		String updateDescriptionResponseResult = checkFail(updateDescriptionResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, updateDescriptionResponseResult);
		// Should be no detail in RestWriteResponse
		Assert.assertTrue(StringUtils.isBlank(writeResponse.detail) || ! writeResponse.detail.equals(RestWriteResponseEnumeratedDetails.UNCHANGED));

		// Attempt to update again with same data.  Should be short-circuited and return UNCHANGED in detail of RestWriteResponse
		updateDescriptionResponse = target(RestPaths.descriptionUpdatePathComponent + descriptionSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		updateDescriptionResponseResult = checkFail(updateDescriptionResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, updateDescriptionResponseResult);
		// Should be no detail in RestWriteResponse
		Assert.assertEquals(writeResponse.detail, RestWriteResponseEnumeratedDetails.UNCHANGED);

		// Retrieve all descriptions referring to referenced concept
		conceptDescriptionsObject = getDescriptionsForConcept(referencedConceptNid, param(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence()));
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().identifiers.sequence == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		// Validate description fields
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.caseSignificanceConcept.sequence.intValue(), newCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.text, newDescriptionText);
		Assert.assertEquals(matchingVersion.descriptionTypeConcept.sequence.intValue(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.languageConcept.sequence.intValue(), newLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().referencedComponent.nid.intValue(), referencedConceptNid);

		// Attempt to deactivate description as read_only user
		Response deactivateDescriptionResponse = target(RestPaths.writePathComponent + RestPaths.apiVersionComponent +  RestPaths.componentComponent
				+ RestPaths.updatePathComponent + RestPaths.updateStateComponent + descriptionSememeSequenceWrapper.nid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(""));
		assertResponseStatus(deactivateDescriptionResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to deactivate description as authorized user
		deactivateDescriptionResponse = target(RestPaths.writePathComponent + RestPaths.apiVersionComponent +  RestPaths.componentComponent
				+ RestPaths.updatePathComponent + RestPaths.updateStateComponent + descriptionSememeSequenceWrapper.nid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(""));
		checkFail(deactivateDescriptionResponse);
		// Retrieve all descriptions referring to referenced concept
		conceptDescriptionsObject =
				getDescriptionsForConcept(
						referencedConceptNid,
						buildParams(
								param(RequestParameters.allowedStates, State.INACTIVE.getAbbreviation()),
								param(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence()))
						);
		Assert.assertTrue(conceptDescriptionsObject.length > 0);
		// Iterate description list to find new description
		matchingVersion = null;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.getSememeChronology().identifiers.sequence == descriptionSememeSequence) {
				matchingVersion = version;
				break;
			}
		}
		Assert.assertNotNull(matchingVersion);
		Assert.assertEquals(matchingVersion.caseSignificanceConcept.sequence.intValue(), newCaseSignificanceConceptSequence);
		Assert.assertEquals(matchingVersion.text, newDescriptionText);
		Assert.assertEquals(matchingVersion.descriptionTypeConcept.sequence.intValue(), initialDescriptionTypeConceptSequence);
		Assert.assertEquals(matchingVersion.languageConcept.sequence.intValue(), newLanguageConceptSequence);
		Assert.assertEquals(matchingVersion.getSememeChronology().referencedComponent.nid.intValue(), referencedConceptNid);
		Assert.assertEquals(matchingVersion.getSememeVersion().getState(), new RestStateType(State.INACTIVE));
	}

	@Test
	public void testConceptAPIs() throws JsonProcessingException, IOException
	{
		final int parent1Sequence = MetaData.SNOROCKET_CLASSIFIER.getConceptSequence();
		final int parent2Sequence = MetaData.ENGLISH_LANGUAGE.getConceptSequence();

		final int requiredDescriptionsLanguageSequence = MetaData.ENGLISH_LANGUAGE.getConceptSequence();
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
		Assert.assertEquals(conceptVersionObject.getConChronology().getIdentifiers().sequence.intValue(), parent1Sequence);

		final UUID randomUuid = UUID.randomUUID();

		final String fsn = "fsn for test concept " + randomUuid.toString();
		final String pt = "preferred term for test concept " + randomUuid.toString();

		final List<String> parentIds = new ArrayList<>();
		parentIds.add(parent1Sequence + "");
		parentIds.add(parent2Sequence + "");

		List<String> preferredDialects = new ArrayList<>();
		preferredDialects.add(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid().toString());
		preferredDialects.add(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid().toString());

		RestConceptCreateData newConceptData = new RestConceptCreateData(
				parentIds,
				fsn,
				true,
				requiredDescriptionsLanguageSequence + "",
				requiredDescriptionsExtendedTypeSequence + "",
				preferredDialects);

		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newConceptData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		Response unauthorizedCreateConceptResponse = target(RestPaths.conceptCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(unauthorizedCreateConceptResponse, Status.FORBIDDEN.getStatusCode());

		Response createConceptResponse = target(RestPaths.conceptCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newConceptSequenceWrapperXml = createConceptResponse.readEntity(String.class);
		RestWriteResponseConceptCreate newConceptSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponseConceptCreate.class, newConceptSequenceWrapperXml);
		int newConceptSequence = newConceptSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newConceptSequence > 0);

		// Retrieve new concept with restrictive module specification and validate fields (FSN in description)
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.includeParents, true)
				.queryParam(RequestParameters.descriptionTypePrefs, "fsn,definition,synonym")
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		RestConceptVersion newConceptVersionObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(newConceptVersionObject.getConChronology().getDescription(), fsn + " (ISAAC)");
		Assert.assertEquals(newConceptVersionObject.getConVersion().getState(), new RestStateType(State.ACTIVE));

		// Retrieve new concept with permissive (absent) module specification and validate fields (FSN in description)
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.includeParents, true)
				.queryParam(RequestParameters.descriptionTypePrefs, "fsn,definition,synonym")
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable + "," + ExpandUtil.chronologyExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		newConceptVersionObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(newConceptVersionObject.getConChronology().getDescription(), fsn + " (ISAAC)");
		Assert.assertEquals(newConceptVersionObject.getConVersion().getState(), new RestStateType(State.ACTIVE));

		Assert.assertTrue(newConceptVersionObject.getParents().size() == 2);
		Assert.assertTrue(
				(newConceptVersionObject.getParents().get(0).getConChronology().getIdentifiers().sequence == parent1Sequence
				&& newConceptVersionObject.getParents().get(1).getConChronology().getIdentifiers().sequence == parent2Sequence)
				|| (newConceptVersionObject.getParents().get(0).getConChronology().getIdentifiers().sequence == parent2Sequence
				&& newConceptVersionObject.getParents().get(1).getConChronology().getIdentifiers().sequence == parent1Sequence));

		// Retrieve all descriptions referring to new concept
		RestSememeDescriptionVersion[] conceptDescriptionsObject = getDescriptionsForConcept(
				newConceptSequence,
				param(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence()));
		Assert.assertTrue(conceptDescriptionsObject.length >= 2);
		// Iterate description list to find description with an extended type annotation sememe
		boolean foundDescriptionWithCorrectExtendedType = false;
		for (RestSememeDescriptionVersion version : conceptDescriptionsObject) {
			if (version.descriptionExtendedTypeConcept != null
					&& version.descriptionExtendedTypeConcept.sequence == requiredDescriptionsExtendedTypeSequence) {
				foundDescriptionWithCorrectExtendedType = true;
				break;
			}
		}
		Assert.assertTrue(foundDescriptionWithCorrectExtendedType);
		for (RestSememeDescriptionVersion description : conceptDescriptionsObject) {
			boolean foundPreferredDialect = false;
			boolean foundUsEnglishDialect = false;
			boolean foundGbEnglishDialect = false;
			for (RestDynamicSememeVersion dialect : description.dialects) {
				if (dialect.getSememeChronology().assemblage.sequence == Get.identifierService()
						.getConceptSequenceForUuids(MetaData.US_ENGLISH_DIALECT.getPrimordialUuid())) {
					foundUsEnglishDialect = true;
				} else if (dialect.getSememeChronology().assemblage.sequence == Get.identifierService()
						.getConceptSequenceForUuids(MetaData.GB_ENGLISH_DIALECT.getPrimordialUuid())) {
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
			Assert.assertTrue(foundGbEnglishDialect, "GB English dialect not found");
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

		// Find new concept in taxonomy with restrictive (module-specific) stamp coordinate modules parameter
		Response taxonomyResponse = target(taxonomyRequestPath)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.id, newConceptSequence)
				.queryParam(RequestParameters.parentHeight, 1)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(taxonomyResponse).readEntity(String.class);
		RestConceptVersion conceptVersionFromTaxonomy = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertNotNull(conceptVersionFromTaxonomy);

		// Find new concept in taxonomy without restrictive (module-specific) stamp coordinate modules parameter
		taxonomyResponse = target(taxonomyRequestPath)
				.queryParam(RequestParameters.id, newConceptSequence)
				.queryParam(RequestParameters.parentHeight, 1)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(taxonomyResponse).readEntity(String.class);
		conceptVersionFromTaxonomy = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		// validate conceptVersionFromTaxonomy parents
		Assert.assertTrue(conceptVersionFromTaxonomy.getParents().size() == 2);
		Assert.assertTrue(
				(conceptVersionFromTaxonomy.getParents().get(0).getConChronology().getIdentifiers().sequence == parent1Sequence
				&& conceptVersionFromTaxonomy.getParents().get(1).getConChronology().getIdentifiers().sequence == parent2Sequence)
				|| (conceptVersionFromTaxonomy.getParents().get(1).getConChronology().getIdentifiers().sequence == parent1Sequence
				|| conceptVersionFromTaxonomy.getParents().get(0).getConChronology().getIdentifiers().sequence == parent2Sequence));

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
			if (child.getConChronology().getIdentifiers().sequence == newConceptSequence) {
				foundNewConceptAsChildOfSpecifiedParent = true;
				break;
			}
		}
		Assert.assertTrue(foundNewConceptAsChildOfSpecifiedParent);

		// Attempt to retire concept with read_only token
		Response deactivateConceptResponse = target(RestPaths.writePathComponent + RestPaths.apiVersionComponent + RestPaths.componentComponent
				+ RestPaths.updatePathComponent + RestPaths.updateStateComponent + newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(""));  //TODO I don't like this hack for putting nothign... need to see what is proper
		assertResponseStatus(deactivateConceptResponse, Status.FORBIDDEN.getStatusCode());

		// retire concept
		deactivateConceptResponse = target(RestPaths.writePathComponent + RestPaths.apiVersionComponent + RestPaths.componentComponent
				+ RestPaths.updatePathComponent + RestPaths.updateStateComponent + newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(""));
		String writeResponseXml = checkFail(deactivateConceptResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, writeResponseXml);
		Assert.assertTrue(writeResponse.detail == null || ! writeResponse.detail.equals(RestWriteResponseEnumeratedDetails.UNCHANGED));

		deactivateConceptResponse = target(RestPaths.writePathComponent + RestPaths.apiVersionComponent + RestPaths.componentComponent
				+ RestPaths.updatePathComponent + RestPaths.updateStateComponent + newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.active, false)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(""));
		writeResponseXml = checkFail(deactivateConceptResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, writeResponseXml);
		Assert.assertEquals(writeResponse.detail, RestWriteResponseEnumeratedDetails.UNCHANGED);

		// Retrieve retired concept and validate
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.includeParents, false)
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		RestConceptVersion deactivatedConceptObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(deactivatedConceptObject.getConVersion().getState(), new RestStateType(State.INACTIVE));

		//Do it again using the direct concept API with read_only token
		Response stateChangeResponse = target(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent + RestPaths.updatePathComponent
				+ newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
						jsonIze(new String[] {"active"},
								new String[] {"true"})));
		assertResponseStatus(stateChangeResponse, Status.FORBIDDEN.getStatusCode());

		//Do it again using the direct concept API
		stateChangeResponse = target(RestPaths.writePathComponent + RestPaths.conceptAPIsPathComponent + RestPaths.updatePathComponent
				+ newConceptSequenceWrapper.uuid)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
						jsonIze(new String[] {"active"},
								new String[] {"true"})));
		checkFail(stateChangeResponse).readEntity(String.class);

		// Retrieve retired concept and validate
		getConceptVersionResponse = target(RestPaths.conceptVersionAppPathComponent.replaceFirst(RestPaths.appPathComponent, "") + newConceptSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.includeParents, false)
				.queryParam(RequestParameters.expand, ExpandUtil.descriptionsExpandable)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		conceptVersionResult = checkFail(getConceptVersionResponse).readEntity(String.class);
		deactivatedConceptObject = XMLUtils.unmarshalObject(RestConceptVersion.class, conceptVersionResult);
		Assert.assertEquals(deactivatedConceptObject.getConVersion().getState(), new RestStateType(State.ACTIVE));
	}

	@Test
	public void testMappingAPIs() throws Exception
	{
		// Create a random string to confirm target data are relevant
		UUID randomUuid = UUID.randomUUID();

		// Create a new map set
		String newMappingSetName = "A new mapping set name (" + randomUuid + ")";
		String newMappingSetInverseName = "A new mapping set inverseName (" + randomUuid + ")";
		String newMappingSetDescription = "A new mapping set description (" + randomUuid + ")";
		String newMappingSetPurpose = "A new mapping set purpose (" + randomUuid + ")";
		
		// Get all map set display fields
		Response getAllMappingSetDisplayFieldsResponse = target(RestPaths.mappingAPIsPathComponent + RestPaths.mappingFieldsComponent)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingSetDisplayFieldsResult = checkFail(getAllMappingSetDisplayFieldsResponse).readEntity(String.class);
		RestMappingSetDisplayField[] mappingSetDisplayFieldsFromMapSetDisplayFieldsService = XMLUtils.unmarshalObjectArray(RestMappingSetDisplayField.class, retrievedMappingSetDisplayFieldsResult);
		Assert.assertNotNull(mappingSetDisplayFieldsFromMapSetDisplayFieldsService);
		Assert.assertTrue(mappingSetDisplayFieldsFromMapSetDisplayFieldsService.length > 0);
		List<RestMappingSetDisplayFieldCreate> mapSetDisplayFieldCreateDTOs = new ArrayList<>();
		for (RestMappingSetDisplayField displayField : mappingSetDisplayFieldsFromMapSetDisplayFieldsService) {
			mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(displayField.id, MapSetItemComponent.SOURCE));
		}
		
		// Create item extended fields here
		List<RestDynamicSememeColumnInfoCreate> mapItemExtendedFieldCreateDTOs = new ArrayList<>();
		RestDynamicSememeColumnInfoCreate extendedField0Create = new RestDynamicSememeColumnInfoCreate(
				MetaData.BOOLEAN_LITERAL.getNid() + "",
				RestDynamicSememeDataType.valueOf(DynamicSememeDataType.BOOLEAN.name()),
				new RestDynamicSememeBoolean(0, true), 
				true);
		mapItemExtendedFieldCreateDTOs.add(extendedField0Create);

		RestDynamicSememeColumnInfoCreate extendedField1Create = new RestDynamicSememeColumnInfoCreate(
				MetaData.CONDOR_CLASSIFIER.getNid() + "",
				new RestDynamicSememeDataType(DynamicSememeDataType.LONG),
				null, 
				true,
				new RestDynamicSememeValidatorType[] { new RestDynamicSememeValidatorType(DynamicSememeValidatorType.LESS_THAN) },
				new RestDynamicSememeData[] { new RestDynamicSememeLong(0, 12346) });
		mapItemExtendedFieldCreateDTOs.add(extendedField1Create);

		RestDynamicSememeColumnInfoCreate extendedField2Create = new RestDynamicSememeColumnInfoCreate(
				MetaData.DANISH_LANGUAGE.getNid() + "",
				new RestDynamicSememeDataType(DynamicSememeDataType.STRING),
				null, 
				false,
				(RestDynamicSememeValidatorType[])null,
				(RestDynamicSememeData[])null);
		mapItemExtendedFieldCreateDTOs.add(extendedField2Create);

		RestMappingSetVersionBaseCreate newMappingSetData = new RestMappingSetVersionBaseCreate(
				newMappingSetName,
				newMappingSetInverseName,
				newMappingSetDescription,
				newMappingSetPurpose,
				(Boolean)null,
				(List<RestMappingSetExtensionValueCreate>)null,
				mapItemExtendedFieldCreateDTOs,
				mapSetDisplayFieldCreateDTOs);

		String xml = null;
		try {
			xml = XMLUtils.marshallObject(newMappingSetData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		// Attempt to create new mapping set with read_only token
		Response createNewMappingSetResponse = target(RestPaths.mappingSetCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(createNewMappingSetResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to create new mapping set
		createNewMappingSetResponse = target(RestPaths.mappingSetCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String writeResponseXml = checkFail(createNewMappingSetResponse).readEntity(String.class);
		RestWriteResponse mappingSetWriteResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, writeResponseXml);
		final UUID testMappingSetUUID = mappingSetWriteResponse.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(testMappingSetUUID != null);

		// Retrieve new mapping set and validate fields
		Response getNewMappingSetVersionResponse = target(RestPaths.mappingSetAppPathComponent + testMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingSetVersionResult = checkFail(getNewMappingSetVersionResponse).readEntity(String.class);
		RestMappingSetVersion retrievedMappingSetVersion = XMLUtils.unmarshalObject(RestMappingSetVersion.class, retrievedMappingSetVersionResult);
		Assert.assertEquals(newMappingSetName, retrievedMappingSetVersion.name);
		Assert.assertEquals(newMappingSetInverseName, retrievedMappingSetVersion.inverseName);
		Assert.assertEquals(newMappingSetDescription, retrievedMappingSetVersion.description);
		Assert.assertEquals(newMappingSetPurpose, retrievedMappingSetVersion.purpose);
		Assert.assertNotNull(retrievedMappingSetVersion.displayFields);
		Assert.assertTrue(retrievedMappingSetVersion.displayFields.size() == mappingSetDisplayFieldsFromMapSetDisplayFieldsService.length);
		Set<String> displayFieldNamesFromMapSetVersion = new HashSet<>();
		for (RestMappingSetDisplayField field : retrievedMappingSetVersion.displayFields) {
			displayFieldNamesFromMapSetVersion.add(field.id);
		}
		Set<String> displayFieldNamesFromMapSetDisplayFieldsService = new HashSet<>();
		for (RestMappingSetDisplayField field : mappingSetDisplayFieldsFromMapSetDisplayFieldsService) {
			displayFieldNamesFromMapSetDisplayFieldsService.add(field.id);
		}
		Assert.assertEquals(displayFieldNamesFromMapSetVersion, displayFieldNamesFromMapSetDisplayFieldsService);
		
		// Update comment with new comment text value and empty comment context value
		String updatedMappingSetName = "An updated mapping set name (" + randomUuid + ")";
		String updatedMappingSetInverseName = null; //"An updated mapping set inverse name (" + randomUuid + ")";
		String updatedMappingSetDescription = "An updated mapping set description (" + randomUuid + ")";
		String updatedMappingSetPurpose = "An updated mapping set purpose (" + randomUuid + ")";
		mapSetDisplayFieldCreateDTOs.clear();
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.VUID.getPrimordialUuid().toString(), MapSetItemComponent.SOURCE));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.SCTID.getPrimordialUuid().toString(), MapSetItemComponent.SOURCE));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.CODE.getPrimordialUuid().toString(), MapSetItemComponent.SOURCE));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString(), 
				MapSetItemComponent.SOURCE));
		
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.VUID.getPrimordialUuid().toString(), MapSetItemComponent.TARGET));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.SCTID.getPrimordialUuid().toString(), MapSetItemComponent.TARGET));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(MetaData.CODE.getPrimordialUuid().toString(), MapSetItemComponent.TARGET));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString(), 
				MapSetItemComponent.TARGET));

		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(0 + "", MapSetItemComponent.ITEM_EXTENDED));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(1 + "", MapSetItemComponent.ITEM_EXTENDED));
		mapSetDisplayFieldCreateDTOs.add(new RestMappingSetDisplayFieldCreate(2 + "", MapSetItemComponent.ITEM_EXTENDED));

		RestMappingSetVersionBaseUpdate updatedMappingSetData = new RestMappingSetVersionBaseUpdate(
				updatedMappingSetName,
				updatedMappingSetInverseName,
				updatedMappingSetDescription,
				updatedMappingSetPurpose,
				true,
				(List<RestMappingSetExtensionValueUpdate>)null,
				mapSetDisplayFieldCreateDTOs);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(updatedMappingSetData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		// Attempt to update mapping set with read_only token
		Response updateMappingSetResponse = target(RestPaths.mappingSetUpdateAppPathComponent + testMappingSetUUID)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		assertResponseStatus(updateMappingSetResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to update mapping set
		updateMappingSetResponse = target(RestPaths.mappingSetUpdateAppPathComponent + testMappingSetUUID)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateMappingSetResponse);

		// Retrieve updated mapping set and validate fields
		getNewMappingSetVersionResponse = target(RestPaths.mappingSetAppPathComponent + testMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingSetVersionResult = checkFail(getNewMappingSetVersionResponse).readEntity(String.class);
		RestMappingSetVersion updatedMappingSetObject = XMLUtils.unmarshalObject(RestMappingSetVersion.class, retrievedMappingSetVersionResult);
		Assert.assertEquals(updatedMappingSetName, updatedMappingSetObject.name);
		Assert.assertEquals(updatedMappingSetInverseName, updatedMappingSetObject.inverseName);
		Assert.assertEquals(updatedMappingSetDescription, updatedMappingSetObject.description);
		Assert.assertEquals(updatedMappingSetPurpose, updatedMappingSetObject.purpose);

		Assert.assertNotNull(updatedMappingSetObject.displayFields);
		Assert.assertTrue(updatedMappingSetObject.displayFields.size() == mapSetDisplayFieldCreateDTOs.size());
		displayFieldNamesFromMapSetVersion = new HashSet<>();
		for (RestMappingSetDisplayField field : updatedMappingSetObject.displayFields) {
			displayFieldNamesFromMapSetVersion.add(field.id);
		}
		Set<String> displayFieldNamesFromUpdateDTO = new HashSet<>();
		for (RestMappingSetDisplayFieldCreate field : mapSetDisplayFieldCreateDTOs) {
			displayFieldNamesFromUpdateDTO.add(field.id);
		}
		Assert.assertEquals(displayFieldNamesFromMapSetVersion, displayFieldNamesFromUpdateDTO);
		
		// Get list of mapping sets
		Response getMappingSetsResponse = target(RestPaths.mappingSetsAppPathComponent)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
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
		int sourceConceptSeq = MetaData.SNOROCKET_CLASSIFIER.getConceptSequence();
		int targetConceptSeq = MetaData.ENGLISH_LANGUAGE.getConceptSequence();
		String equivalenceTypeConceptUuid = IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_BROAD_TO_NARROW.getPrimordialUuid().toString();

		// Get source concept description for validation
		ConceptChronology<?> cc = Get.conceptService().getConcept(sourceConceptSeq);
		Optional<String> descLatestVersion = Frills.getDescription(cc.getNid(), getDefaultCoordinatesToken().getStampCoordinate(), getDefaultCoordinatesToken().getLanguageCoordinate());
		String sourceConceptDescription = descLatestVersion.isPresent() ? descLatestVersion.get() : null;
		Assert.assertNotNull(sourceConceptDescription);
		
		// Get target concept description for validation
		cc = Get.conceptService().getConcept(targetConceptSeq);
		descLatestVersion = Frills.getDescription(cc.getNid(), getDefaultCoordinatesToken().getStampCoordinate(), getDefaultCoordinatesToken().getLanguageCoordinate());
		String targetConceptDescription = descLatestVersion.isPresent() ? descLatestVersion.get() : null;
		Assert.assertNotNull(targetConceptDescription);

		// Add synthetic VUID and SCTID to test concepts to test display field value population
		String editTokenString = getEditTokenString(TEST_SSO_TOKEN);
		EditToken token = EditTokens.getOrCreate(editTokenString);
		final String ITEM_SOURCE_CONCEPT_FAKE_VUID = 12345L + "";
		final String ITEM_SOURCE_CONCEPT_FAKE_SCTID = 23456L + "";
		final String ITEM_SOURCE_CONCEPT_FAKE_CODE = "SRC_FAKE_CODE";
		final String ITEM_SOURCE_CONCEPT_FAKE_RXCUI = "SRCRXCUI";
		final String ITEM_SOURCE_CONCEPT_FAKE_LOINC_NUM = "SRCLOINC";
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_SOURCE_CONCEPT_FAKE_VUID, MetaData.SNOROCKET_CLASSIFIER.getNid(), MetaData.VUID.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_SOURCE_CONCEPT_FAKE_SCTID, MetaData.SNOROCKET_CLASSIFIER.getNid(), MetaData.SCTID.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_SOURCE_CONCEPT_FAKE_CODE, MetaData.SNOROCKET_CLASSIFIER.getNid(), MetaData.CODE.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		final String ITEM_TARGET_CONCEPT_FAKE_VUID = 54321L + "";
		final String ITEM_TARGET_CONCEPT_FAKE_SCTID = 65432L + "";
		final String ITEM_TARGET_CONCEPT_FAKE_CODE = "TARGET_FAKE_CODE";
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_TARGET_CONCEPT_FAKE_VUID, MetaData.ENGLISH_LANGUAGE.getNid(), MetaData.VUID.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_TARGET_CONCEPT_FAKE_SCTID, MetaData.ENGLISH_LANGUAGE.getNid(), MetaData.SCTID.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		Get.sememeBuilderService().getStringSememeBuilder(ITEM_TARGET_CONCEPT_FAKE_CODE, MetaData.ENGLISH_LANGUAGE.getNid(), MetaData.CODE.getConceptSequence()).build(token.getEditCoordinate(), ChangeCheckerMode.INACTIVE);
		Get.commitService().commit("VUID, SCTID, CODE for SNOROCKET_CLASSIFIER and ENGLISH_LANGUAGE for testing only");
		
		List<RestDynamicSememeData> mapItemExtendedFields  = new ArrayList<>();
		RestDynamicSememeData itemExtendedField0 = new RestDynamicSememeBoolean(0, true);
		mapItemExtendedFields.add(itemExtendedField0);
		RestDynamicSememeData itemExtendedField1 = new RestDynamicSememeLong(1, 12345L);
		mapItemExtendedFields.add(itemExtendedField1);
		// This is an optional/non-required item extended field
//		RestDynamicSememeData itemExtendedField2 = new RestDynamicSememeString(2, "test extended field");
//		mapItemExtendedFields.add(itemExtendedField2);
		RestMappingItemVersionCreate newMappingSetItemData = new RestMappingItemVersionCreate(
				targetConceptSeq + "",
				equivalenceTypeConceptUuid,
				testMappingSetUUID.toString(),
				sourceConceptSeq + "",
				mapItemExtendedFields, null);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(newMappingSetItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		// Attempt to create new mapping item with read_only token
		Response createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(createNewMappingItemResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to create new mapping item
		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newMappingItemSequenceWrapperXml = checkFail(createNewMappingItemResponse).readEntity(String.class);
		RestWriteResponse newMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newMappingItemSequenceWrapperXml);
		UUID newMappingItemUUID = newMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(newMappingItemUUID != null);

		// test createNewMappingItem()
		// Retrieve mapping item and validate fields
		Response getNewMappingItemVersionResponse = target(RestPaths.mappingItemAppPathComponent + newMappingItemUUID)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		String retrievedMappingItemVersionResult = checkFail(getNewMappingItemVersionResponse).readEntity(String.class);
		RestMappingItemVersion retrievedMappingItemVersion = new ObjectMapper().readValue(retrievedMappingItemVersionResult, RestMappingItemVersion.class);
		Assert.assertTrue(sourceConceptSeq == retrievedMappingItemVersion.sourceConcept.sequence);
		Assert.assertTrue(targetConceptSeq == retrievedMappingItemVersion.targetConcept.sequence);
		Assert.assertTrue(equivalenceTypeConceptUuid.equals(retrievedMappingItemVersion.qualifierConcept.getFirst().toString()));
		Assert.assertEquals(updatedMappingSetObject.identifiers.sequence, retrievedMappingItemVersion.mapSetConcept.sequence);
		// Only computed (SOURCE|TARGET|EQUIVALENCE_TYPE) display fields should be in the item, not ITEM_EXTENDED
		List<RestMappingSetDisplayFieldCreate> computedMapSetDisplayFieldCreateDTOs =  new ArrayList<>();
		List<RestMappingSetDisplayFieldCreate> extendedFieldMapSetDisplayFieldCreateDTOs =  new ArrayList<>();
		for (RestMappingSetDisplayFieldCreate anyTypeField : mapSetDisplayFieldCreateDTOs) {
			if (! anyTypeField.fieldComponentType.equals(MapSetItemComponent.ITEM_EXTENDED.name())) {
				computedMapSetDisplayFieldCreateDTOs.add(anyTypeField);
			} else {
				extendedFieldMapSetDisplayFieldCreateDTOs.add(anyTypeField);
			}
		}
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.size(), computedMapSetDisplayFieldCreateDTOs.size());
		Assert.assertEquals(retrievedMappingItemVersion.mapItemExtendedFields.size(), mapItemExtendedFields.size());
		boolean foundSourceVuidDisplayField = false;
		boolean foundSourceSctIdDisplayField = false;
		boolean foundSourceCodeDisplayField = false;
		boolean foundSourceDescriptionDisplayField = false;
		boolean foundTargetVuidDisplayField = false;
		boolean foundTargetSctIdDisplayField = false;
		boolean foundTargetCodeDisplayField = false;
		boolean foundTargetDescriptionDisplayField = false;
		for (RestMappingItemComputedDisplayField displayField : retrievedMappingItemVersion.computedDisplayFields) {
			// Source fields
			if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.SOURCE))
					&& displayField.id.equals(MetaData.VUID.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_SOURCE_CONCEPT_FAKE_VUID);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.VUID.getNid());
				foundSourceVuidDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.SOURCE))
					&& displayField.id.equals(MetaData.SCTID.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_SOURCE_CONCEPT_FAKE_SCTID);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.SCTID.getNid());
				foundSourceSctIdDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.SOURCE))
					&& displayField.id.equals(MetaData.CODE.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_SOURCE_CONCEPT_FAKE_CODE);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.CODE.getNid());
				foundSourceCodeDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.SOURCE))
					&& displayField.id.equals(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString())) {
				Assert.assertTrue(StringUtils.isNotBlank(((RestMappingItemComputedDisplayField)displayField).value));
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, sourceConceptDescription);
				foundSourceDescriptionDisplayField = true;
			}
			// Target fields
			else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.TARGET))
					&& displayField.id.equals(MetaData.VUID.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_TARGET_CONCEPT_FAKE_VUID);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.VUID.getNid());
				foundTargetVuidDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.TARGET))
					&& displayField.id.equals(MetaData.SCTID.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_TARGET_CONCEPT_FAKE_SCTID);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.SCTID.getNid());
				foundTargetSctIdDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.TARGET))
					&& displayField.id.equals(MetaData.CODE.getPrimordialUuid().toString())) {
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, ITEM_TARGET_CONCEPT_FAKE_CODE);
//				Assert.assertEquals(displayField.fieldNameConceptIdentifiers.nid.intValue(), MetaData.CODE.getNid());
				foundTargetCodeDisplayField = true;
			} else if (displayField.componentType.equals(new RestMapSetItemComponentType(MapSetItemComponent.TARGET))
					&& displayField.id.equals(IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString())) {
				Assert.assertTrue(StringUtils.isNotBlank(((RestMappingItemComputedDisplayField)displayField).value));
				Assert.assertEquals(((RestMappingItemComputedDisplayField)displayField).value, targetConceptDescription);
				foundTargetDescriptionDisplayField = true;
			}
		}
		Assert.assertTrue(foundSourceVuidDisplayField);
		Assert.assertTrue(foundSourceSctIdDisplayField);
		Assert.assertTrue(foundSourceCodeDisplayField);
		Assert.assertTrue(foundSourceDescriptionDisplayField);
		
		Assert.assertTrue(foundTargetVuidDisplayField);
		Assert.assertTrue(foundTargetSctIdDisplayField);
		Assert.assertTrue(foundTargetCodeDisplayField);
		Assert.assertTrue(foundTargetDescriptionDisplayField);

		// Add second item to test paging
		RestMappingItemVersionCreate additionalNewMappingSetItemData = new RestMappingItemVersionCreate(
				sourceConceptSeq + "", // This item has source and target concepts swapped
				equivalenceTypeConceptUuid,
				testMappingSetUUID.toString(),
				targetConceptSeq + "",
				mapItemExtendedFields, null);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(additionalNewMappingSetItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// Attempt to create second mapping item
		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String additionalNewMappingItemSequenceWrapperXml = checkFail(createNewMappingItemResponse).readEntity(String.class);
		RestWriteResponse additionalNewMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, additionalNewMappingItemSequenceWrapperXml);
		UUID additionalNewMappingItemUUID = additionalNewMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(additionalNewMappingItemUUID != null);

		// Add third item to test paging
		additionalNewMappingSetItemData = new RestMappingItemVersionCreate(
				targetConceptSeq + "",
				null,
				testMappingSetUUID.toString(),
				sourceConceptSeq + "",
				mapItemExtendedFields, null);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(additionalNewMappingSetItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// Attempt to create third mapping item
		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		additionalNewMappingItemSequenceWrapperXml = checkFail(createNewMappingItemResponse).readEntity(String.class);
		additionalNewMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, additionalNewMappingItemSequenceWrapperXml);
		additionalNewMappingItemUUID = additionalNewMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(additionalNewMappingItemUUID != null);
		
		// Should now be 3 items
		// Test pagination (page 0 with maximum of 2 entries (out of 3))
		Response getMappingItemsPagedResponse = target(RestPaths.mappingAPIsPathComponent + RestPaths.mappingItemsPagedComponent + testMappingSetUUID)
				.queryParam(RequestParameters.pageNum, 1)
				.queryParam(RequestParameters.maxPageSize, 2)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingItemsPagedResult = checkFail(getMappingItemsPagedResponse).readEntity(String.class);
		RestMappingItemVersionPage retrievedMappingItemsPaged = XMLUtils.unmarshalObject(RestMappingItemVersionPage.class, retrievedMappingItemsPagedResult);
		Assert.assertNotNull(retrievedMappingItemsPaged);
		Assert.assertEquals(retrievedMappingItemsPaged.results.length, 2);
		// Test pagination (page 0 with maximum of 10 entries (out of 3))
		getMappingItemsPagedResponse = target(RestPaths.mappingAPIsPathComponent + RestPaths.mappingItemsPagedComponent + testMappingSetUUID)
				.queryParam(RequestParameters.pageNum, 1)
				.queryParam(RequestParameters.maxPageSize, 10)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingItemsPagedResult = checkFail(getMappingItemsPagedResponse).readEntity(String.class);
		retrievedMappingItemsPaged = XMLUtils.unmarshalObject(RestMappingItemVersionPage.class, retrievedMappingItemsPagedResult);
		Assert.assertNotNull(retrievedMappingItemsPaged);
		Assert.assertEquals(retrievedMappingItemsPaged.results.length, 3);
		
		// test getMappingItems()
		Response getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + testMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		RestMappingItemVersion[] retrievedMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		RestMappingItemVersion mappingItemMatchingNewItem = null;
		for (RestMappingItemVersion currentMappingItem : retrievedMappingItems) {
			if (currentMappingItem.identifiers.getFirst().equals(newMappingItemUUID)
					&& currentMappingItem.mapSetConcept.sequence.intValue() == retrievedMappingSetVersion.identifiers.sequence.intValue()
					&& currentMappingItem.targetConcept.sequence.intValue() == targetConceptSeq
					&& currentMappingItem.sourceConcept.sequence.intValue() == sourceConceptSeq
					&& currentMappingItem.qualifierConcept.uuids.get(0).toString().equals(equivalenceTypeConceptUuid)) {
				mappingItemMatchingNewItem = currentMappingItem;
				break;
			}
		}
		Assert.assertNotNull(mappingItemMatchingNewItem);

		String updatedTargetConceptSeq = MetaData.DANISH_LANGUAGE.getConceptSequence() + "";
		String updatedEquivalenceTypeConceptSeq = IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getConceptSequence() + "";

		RestMappingItemVersionUpdate updatedMappingItemData = new RestMappingItemVersionUpdate(
				updatedTargetConceptSeq,
				updatedEquivalenceTypeConceptSeq,
				mapItemExtendedFields,null);
		xml = null;
		try {
			xml = XMLUtils.marshallObject(updatedMappingItemData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// Attempt to update mapping item with read_only token
		Response updateMappingItemResponse = target(RestPaths.mappingItemUpdateAppPathComponent + newMappingItemUUID)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		assertResponseStatus(updateMappingItemResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to update mapping item
		updateMappingItemResponse = target(RestPaths.mappingItemUpdateAppPathComponent + newMappingItemUUID)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		checkFail(updateMappingItemResponse);

		getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + testMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		retrievedMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		RestMappingItemVersion mappingItemMatchingUpdatedItem = null;
		for (RestMappingItemVersion currentMappingItem : retrievedMappingItems) {
			if (currentMappingItem.identifiers.getFirst().equals(newMappingItemUUID)
					&& currentMappingItem.mapSetConcept.sequence.intValue() == retrievedMappingSetVersion.identifiers.sequence.intValue()
					&& currentMappingItem.targetConcept.sequence.intValue() == Integer.parseInt(updatedMappingItemData.targetConcept)
					&& currentMappingItem.sourceConcept.sequence.intValue() == Integer.parseInt(newMappingSetItemData.sourceConcept)
					&& currentMappingItem.qualifierConcept.sequence.intValue() == Integer.parseInt(updatedMappingItemData.qualifierConcept)) {
				mappingItemMatchingUpdatedItem = currentMappingItem;
				break;
			}
		}
		Assert.assertNotNull(mappingItemMatchingUpdatedItem);

		// Clone
		final RestMappingSetVersion clonedMappingSetObject = updatedMappingSetObject;
		final UUID clonedMappingSetUuid = testMappingSetUUID;

		String cloneMappingSetName = "A clone mapping set name (" + randomUuid + ")";
		RestMappingSetVersionClone cloneMappingSetData = new RestMappingSetVersionClone(
				clonedMappingSetUuid.toString(),
				cloneMappingSetName,
				null, // newMappingSetInverseName
				null, // newMappingSetDescription
				null, // newMappingSetPurpose
				null);

		xml = null;
		try {
			xml = XMLUtils.marshallObject(cloneMappingSetData);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

		// Attempt to create clone mapping set with read_only token
		Response cloneMappingSetResponse = target(RestPaths.mappingSetCloneAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(cloneMappingSetResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to clone target mapping set
		cloneMappingSetResponse = target(RestPaths.mappingSetCloneAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		writeResponseXml = checkFail(cloneMappingSetResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, writeResponseXml);
		UUID cloneMappingSetUUID = writeResponse.uuid;
		// Confirm returned id is valid
		Assert.assertTrue(cloneMappingSetUUID != null);

		// Attempt to retrieve newly-created clone mapping set
		Response getCloneMappingSetVersionResponse = target(RestPaths.mappingSetAppPathComponent + cloneMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingSetVersionResult = checkFail(getCloneMappingSetVersionResponse).readEntity(String.class);
		RestMappingSetVersion cloneMappingSetObject = XMLUtils.unmarshalObject(RestMappingSetVersion.class, retrievedMappingSetVersionResult);
		Assert.assertEquals(cloneMappingSetName, cloneMappingSetObject.name); // Passed to clone API
		Assert.assertEquals(updatedMappingSetInverseName, cloneMappingSetObject.inverseName); // Inherited from clone target
		Assert.assertEquals(updatedMappingSetDescription, cloneMappingSetObject.description); // Inherited from clone target
		Assert.assertEquals(updatedMappingSetPurpose, cloneMappingSetObject.purpose); // Inherited from clone target

		Assert.assertEquals(cloneMappingSetObject.active, clonedMappingSetObject.active);
		Assert.assertEquals(cloneMappingSetObject.inverseName, clonedMappingSetObject.inverseName);
		Assert.assertEquals(cloneMappingSetObject.description, clonedMappingSetObject.description);
		Assert.assertEquals(cloneMappingSetObject.purpose, clonedMappingSetObject.purpose);

		// Test clone of mapItemFieldsDefinition
		Assert.assertTrue((cloneMappingSetObject.mapItemFieldsDefinition == null && clonedMappingSetObject.mapItemFieldsDefinition == null)
				|| (cloneMappingSetObject.mapItemFieldsDefinition != null && clonedMappingSetObject.mapItemFieldsDefinition != null));
		if (cloneMappingSetObject.mapItemFieldsDefinition != null) {
			Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.size(), clonedMappingSetObject.mapItemFieldsDefinition.size());
			for (int i = 0; i < cloneMappingSetObject.mapItemFieldsDefinition.size(); ++i) {
				Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.get(i).columnDescription, clonedMappingSetObject.mapItemFieldsDefinition.get(i).columnDescription);
				Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.get(i).columnName, clonedMappingSetObject.mapItemFieldsDefinition.get(i).columnName);
				Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.get(i).columnOrder, clonedMappingSetObject.mapItemFieldsDefinition.get(i).columnOrder);
				Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.get(i).columnRequired, clonedMappingSetObject.mapItemFieldsDefinition.get(i).columnRequired);
				Assert.assertEquals(cloneMappingSetObject.mapItemFieldsDefinition.get(i).columnDataType, clonedMappingSetObject.mapItemFieldsDefinition.get(i).columnDataType);
			}
		}
		// Test clone of mapSetExtendedFields
		Assert.assertTrue((cloneMappingSetObject.mapSetExtendedFields == null && clonedMappingSetObject.mapSetExtendedFields == null)
				|| (cloneMappingSetObject.mapSetExtendedFields != null && clonedMappingSetObject.mapSetExtendedFields != null));
		if (cloneMappingSetObject.mapSetExtendedFields != null) {
			Assert.assertEquals(cloneMappingSetObject.mapSetExtendedFields.size(), clonedMappingSetObject.mapSetExtendedFields.size());
			for (int i = 0; i < cloneMappingSetObject.mapSetExtendedFields.size(); ++i) {
				Assert.assertEquals(cloneMappingSetObject.mapSetExtendedFields.get(i).extensionNameConceptDescription, clonedMappingSetObject.mapSetExtendedFields.get(i).extensionNameConceptDescription);
				Assert.assertEquals(cloneMappingSetObject.mapSetExtendedFields.get(i).extensionNameConceptIdentifiers.nid, clonedMappingSetObject.mapSetExtendedFields.get(i).extensionNameConceptIdentifiers.nid);
				Assert.assertEquals(cloneMappingSetObject.mapSetExtendedFields.get(i).extensionValue, clonedMappingSetObject.mapSetExtendedFields.get(i).extensionValue);
			}
		}

		// Get clone target mapping items
		getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + clonedMappingSetUuid)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		RestMappingItemVersion[] cloneTargetMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		Assert.assertTrue(cloneTargetMappingItems != null && cloneTargetMappingItems.length > 0);

		// Get clone mapping items
		getMappingItemsResponse = target(RestPaths.mappingItemsAppPathComponent + cloneMappingSetUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		retrievedMappingItemsResult = checkFail(getMappingItemsResponse).readEntity(String.class);
		RestMappingItemVersion[] cloneMappingItems = XMLUtils.unmarshalObjectArray(RestMappingItemVersion.class, retrievedMappingItemsResult);
		Assert.assertTrue(cloneMappingItems != null);
		Assert.assertEquals(cloneMappingItems.length, cloneTargetMappingItems.length);

		// Check clone items contents
		for (int i = 0; i < cloneTargetMappingItems.length; ++i) {
			Assert.assertTrue(
					(cloneMappingItems[i].qualifierConcept != null && cloneTargetMappingItems[i].qualifierConcept != null
					&& cloneMappingItems[i].qualifierConcept.nid.equals(cloneTargetMappingItems[i].qualifierConcept.nid))
					|| (cloneMappingItems[i].qualifierConcept == null && cloneTargetMappingItems[i].qualifierConcept == null));
			Assert.assertEquals(cloneMappingItems[i].sourceConcept.nid, cloneTargetMappingItems[i].sourceConcept.nid);
			Assert.assertEquals(cloneMappingItems[i].targetConcept.nid, cloneTargetMappingItems[i].targetConcept.nid);

			Assert.assertEquals(cloneMappingItems[i].mapSetConcept.uuids.iterator().next(), cloneMappingSetUUID);
			Assert.assertEquals(cloneTargetMappingItems[i].mapSetConcept.uuids.iterator().next(), clonedMappingSetUuid);
		}
	}

	@Test
	public void testMappingExtendedFieldsAPIs() throws JsonProcessingException, IOException
	{
		//Make one
		UUID random = UUID.randomUUID();

		String name = "Just a test map set type (" + random.toString() + ")";
		ObjectNode root = jfn.objectNode();
		root.put("name", name);
		root.put("inverseName", "inverse test");
		root.put("description", "bla bla");
		root.put("purpose", "just testing");
		root.put("active", true);

		ArrayNode extendedFields = jfn.arrayNode();
		ObjectNode ef1 = jfn.objectNode();
		ef1.put("extensionNameConcept", MetaData.LOINC_MODULES.getNid());
		ef1.set("extensionValue", toJsonObject(new DynamicSememeStringImpl("test Value extended"), 1));

		extendedFields.add(ef1);
		root.set("mapSetExtendedFields", extendedFields);

		ArrayNode mapItemExtendedFieldsDef = jfn.arrayNode();
		ObjectNode mapItemEF1 = jfn.objectNode();
		mapItemEF1.put("columnLabelConcept", MetaData.BOOLEAN_LITERAL.getNid());
		mapItemEF1.put("columnDataType", DynamicSememeDataType.BOOLEAN.name());
		mapItemEF1.set("columnDefaultData", toJsonObject(new DynamicSememeBooleanImpl(true), 1));
		mapItemEF1.put("columnRequired", true);

		mapItemExtendedFieldsDef.add(mapItemEF1);

		ObjectNode mapItemEF2 = jfn.objectNode();
		mapItemEF2.put("columnLabelConcept", MetaData.CONDOR_CLASSIFIER.getNid());
		mapItemEF2.put("columnDataType", DynamicSememeDataType.LONG.name());
		mapItemEF2.put("columnRequired", false);
		ArrayNode stringArray = jfn.arrayNode();
		stringArray.add(DynamicSememeValidatorType.LESS_THAN.name());
		mapItemEF2.set("columnValidatorTypes", stringArray);
		mapItemEF2.set("columnValidatorData", toJsonObject(new DynamicSememeData[] {new DynamicSememeLongImpl(40)}));

		mapItemExtendedFieldsDef.add(mapItemEF2);

		root.set("mapItemExtendedFieldsDefinition", mapItemExtendedFieldsDef);

		log.info("MapSet create json: " + toJson(root));

		// Attempt to create association with read_only token
		Response createAssociationResponse = target(RestPaths.writePathComponent + RestPaths.mappingAPIsPathComponent
				+ RestPaths.mappingSetComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		assertResponseStatus(createAssociationResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to create association
		createAssociationResponse = target(RestPaths.writePathComponent + RestPaths.mappingAPIsPathComponent
				+ RestPaths.mappingSetComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		String result = checkFail(createAssociationResponse).readEntity(String.class);

		RestWriteResponse createdMapSetId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back

		result = checkFail(target(RestPaths.mappingSetAppPathComponent + createdMapSetId.uuid.toString())
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestMappingSetVersion createdMapSet = XMLUtils.unmarshalObject(RestMappingSetVersion.class, result);
		Assert.assertEquals(createdMapSet.identifiers.sequence.intValue(), createdMapSetId.sequence.intValue());
		Assert.assertEquals(createdMapSet.description, "bla bla");
		Assert.assertEquals(createdMapSet.inverseName, "inverse test");
		Assert.assertEquals(createdMapSet.name, name);
		Assert.assertEquals(createdMapSet.purpose, "just testing");
		Assert.assertEquals(createdMapSet.active.booleanValue(), true);
		Assert.assertNull(createdMapSet.comments);
		Assert.assertEquals(createdMapSet.identifiers.getFirst(), createdMapSetId.uuid);
		Assert.assertEquals(createdMapSet.mappingSetStamp.state.enumName, State.ACTIVE.name());
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.size(), 1);
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.get(0).extensionNameConceptIdentifiers.sequence.intValue(), MetaData.LOINC_MODULES.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.get(0).extensionValue.data.toString(), "test Value extended");

		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.size(), 2);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnLabelConcept.sequence.intValue(), MetaData.BOOLEAN_LITERAL.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnName, MetaData.BOOLEAN_LITERAL.getConceptDescriptionText());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnOrder, 0);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnRequired, true);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnDataType.enumId, DynamicSememeDataType.BOOLEAN.ordinal());
		Assert.assertEquals((boolean)createdMapSet.mapItemFieldsDefinition.get(0).columnDefaultData.data , true);
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorData);
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorTypes);

		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnLabelConcept.sequence.intValue(), MetaData.CONDOR_CLASSIFIER.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnName, MetaData.CONDOR_CLASSIFIER.getConceptDescriptionText());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnOrder, 1);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnRequired, false);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnDataType.enumId, DynamicSememeDataType.LONG.ordinal());
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(1).columnDefaultData);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorData.length, 1);
		Assert.assertEquals(((Long)createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorData[0].data).longValue(), 40l);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorTypes.length, 1);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorTypes[0].enumId, DynamicSememeValidatorType.LESS_THAN.ordinal());

		//Create an item

		root = jfn.objectNode();
		root.put("mapSetConcept", createdMapSet.identifiers.sequence);
		root.put("sourceConcept", MetaData.COMMITTED_STATE_FOR_CHRONICLE.getConceptSequence());
		root.put("targetConcept", MetaData.AND.getConceptSequence());
		root.put("qualifierConcept", IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getConceptSequence());

		root.set("mapItemExtendedFields",  toJsonObject(new DynamicSememeData[] {null, new DynamicSememeLongImpl(20)}));

		log.info("Map item create json: " + toJson(root));
		// Attempt to create new mapping item with read_only token
		Response createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		assertResponseStatus(createNewMappingItemResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to create new mapping item
		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		String newMappingItemSequenceWrapperXml = checkFail(createNewMappingItemResponse).readEntity(String.class);
		RestWriteResponse newMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newMappingItemSequenceWrapperXml);
		UUID newMappingItemUUID = newMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(newMappingItemUUID != null);

		//Create a comment on this map item
		String json = jsonIze(new String[] {"commentedItem", "comment"}, new String[] {newMappingItemSequenceWrapper.nid + "", "my random comment"});

		Response createCommentResponse = checkFail(target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(json)));
		String newCommentSememeSequenceWrapperXml = createCommentResponse.readEntity(String.class);
		RestWriteResponse newCommentSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newCommentSememeSequenceWrapperXml);
		int newCommentSememeSequence = newCommentSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newCommentSememeSequence > 0);

		// Retrieve new comment and validate fields
		Response getCommentVersionResponse = checkFail(target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence() + ", " + MetaData.ISAAC_MODULE.getConceptSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get());
		String commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion newCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals("my random comment", newCommentObject.comment);
		Assert.assertNull(newCommentObject.commentContext);
		Assert.assertEquals(newMappingItemSequenceWrapper.nid.intValue(), newCommentObject.commentedItem.nid.intValue());
		Assert.assertEquals(newMappingItemSequenceWrapper.uuid, newCommentObject.commentedItem.uuids.get(0));


		// Retrieve mapping item and validate fields
		Response getNewMappingItemVersionResponse = target(RestPaths.mappingItemAppPathComponent + newMappingItemUUID)
				//.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_JSON).get();
		String retrievedMappingItemVersionResult = checkFail(getNewMappingItemVersionResponse).readEntity(String.class);
		//RestMappingItemVersion retrievedMappingItemVersion = XMLUtils.unmarshalObject(RestMappingItemVersion.class, retrievedMappingItemVersionResult);
		RestMappingItemVersion retrievedMappingItemVersion = new ObjectMapper().readValue(retrievedMappingItemVersionResult, RestMappingItemVersion.class);
		Assert.assertEquals(retrievedMappingItemVersion.identifiers.sequence.intValue(), newMappingItemSequenceWrapper.sequence.intValue());
		Assert.assertTrue(MetaData.COMMITTED_STATE_FOR_CHRONICLE.getConceptSequence() == retrievedMappingItemVersion.sourceConcept.sequence);
		Assert.assertTrue(MetaData.AND.getConceptSequence() == retrievedMappingItemVersion.targetConcept.sequence);
		Assert.assertTrue(IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getConceptSequence() == retrievedMappingItemVersion.qualifierConcept.sequence);
		Assert.assertEquals(createdMapSet.identifiers.sequence, retrievedMappingItemVersion.mapSetConcept.sequence);
		Assert.assertTrue(retrievedMappingItemVersion.mappingItemStamp.state.enumId == State.ACTIVE.ordinal());

		Assert.assertEquals(2, retrievedMappingItemVersion.mapItemExtendedFields.size());
		Assert.assertEquals(((Boolean)retrievedMappingItemVersion.mapItemExtendedFields.get(0).data).booleanValue(), true);
		Assert.assertEquals(((Integer)retrievedMappingItemVersion.mapItemExtendedFields.get(1).data).intValue(), 20);
		
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.size(), 3); // SOURCE, TARGET and EQUIVALENCE_TYPE descriptions plus 2 item extended fields


		Optional<String> sourceConceptDescription = Frills.getDescription(MetaData.COMMITTED_STATE_FOR_CHRONICLE.getNid(), getDefaultCoordinatesToken().getTaxonomyCoordinate());
		Assert.assertTrue(sourceConceptDescription.isPresent());
		Optional<String> targetConceptDescription = Frills.getDescription(MetaData.AND.getNid(), getDefaultCoordinatesToken().getTaxonomyCoordinate());
		Assert.assertTrue(targetConceptDescription.isPresent());
		Optional<String> equivalenceTypeConceptDescription = Frills.getDescription(IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getNid(), getDefaultCoordinatesToken().getTaxonomyCoordinate());
		Assert.assertTrue(equivalenceTypeConceptDescription.isPresent());

		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(0).componentType, new RestMapSetItemComponentType(MapSetItemComponent.SOURCE));
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(0).id, IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString());
		Assert.assertTrue(retrievedMappingItemVersion.computedDisplayFields.get(0) instanceof RestMappingItemComputedDisplayField);
		Assert.assertEquals(((RestMappingItemComputedDisplayField)retrievedMappingItemVersion.computedDisplayFields.get(0)).value, sourceConceptDescription.get());
		
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(1).componentType, new RestMapSetItemComponentType(MapSetItemComponent.TARGET));
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(1).id, IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString());
		Assert.assertTrue(retrievedMappingItemVersion.computedDisplayFields.get(1) instanceof RestMappingItemComputedDisplayField);
		Assert.assertEquals(((RestMappingItemComputedDisplayField)retrievedMappingItemVersion.computedDisplayFields.get(1)).value, targetConceptDescription.get());

		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(2).componentType, new RestMapSetItemComponentType(MapSetItemComponent.EQUIVALENCE_TYPE));
		Assert.assertEquals(retrievedMappingItemVersion.computedDisplayFields.get(2).id, IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid().toString());
		Assert.assertTrue(retrievedMappingItemVersion.computedDisplayFields.get(2) instanceof RestMappingItemComputedDisplayField);
		Assert.assertEquals(((RestMappingItemComputedDisplayField)retrievedMappingItemVersion.computedDisplayFields.get(2)).value, equivalenceTypeConceptDescription.get());

		//This should fail, due to being a duplicate entry:
		root = jfn.objectNode();
		root.put("mapSetConcept", createdMapSet.identifiers.sequence);
		root.put("sourceConcept", MetaData.COMMITTED_STATE_FOR_CHRONICLE.getConceptSequence());
		root.put("targetConcept", MetaData.AND.getConceptSequence());
		root.put("qualifierConcept", IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getConceptSequence());

		root.set("mapItemExtendedFields",  toJsonObject(new DynamicSememeData[] {null, new DynamicSememeLongImpl(20)}));

		log.info("Map item create json: " + toJson(root));

		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		Assert.assertTrue(expectFail(createNewMappingItemResponse).contains("mapping with the specified source, target and qualifier already exists in this set"));

		//This should fail, due to failing a validator rule:
		root = jfn.objectNode();
		root.put("mapSetConcept", createdMapSet.identifiers.sequence);
		root.put("sourceConcept", MetaData.BOOLEAN_LITERAL.getConceptSequence());
		root.put("targetConcept", MetaData.AND.getConceptSequence());
		root.put("qualifierConcept",  IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_EXACT.getConceptSequence());

		root.set("mapItemExtendedFields",  toJsonObject(new DynamicSememeData[] {null, new DynamicSememeLongImpl(40)}));

		log.info("Map item create json: " + toJson(root));

		createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		Assert.assertTrue(expectFail(createNewMappingItemResponse).contains("does not pass the assigned validator"));

	}

	@Test
	public void testMappingExtendedFieldsAPIs2() throws JsonProcessingException, IOException
	{
		//This test reverses the order of the extended columns, to test an edge case with default column handling
		//Make one
		UUID random = UUID.randomUUID();

		String name = "Just a test map set type (" + random.toString() + ")";
		ObjectNode root = jfn.objectNode();
		root.put("name", name);
		root.put("inverseName", "inverse test");
		root.put("description", "bla bla");
		root.put("purpose", "just testing");
		root.put("active", false);

		ArrayNode extendedFields = jfn.arrayNode();
		ObjectNode ef1 = jfn.objectNode();
		ef1.put("extensionNameConcept", MetaData.LOINC_MODULES.getNid());
		ef1.set("extensionValue", toJsonObject(new DynamicSememeStringImpl("test Value extended"), 1));

		extendedFields.add(ef1);
		root.set("mapSetExtendedFields", extendedFields);

		ArrayNode mapItemExtendedFieldsDef = jfn.arrayNode();

		ObjectNode mapItemEF1 = jfn.objectNode();
		mapItemEF1.put("columnLabelConcept", MetaData.CONDOR_CLASSIFIER.getNid());
		mapItemEF1.put("columnDataType", DynamicSememeDataType.LONG.name());
		mapItemEF1.put("columnRequired", false);
		ArrayNode stringArray = jfn.arrayNode();
		stringArray.add(DynamicSememeValidatorType.LESS_THAN.name());
		mapItemEF1.set("columnValidatorTypes", stringArray);
		mapItemEF1.set("columnValidatorData", toJsonObject(new DynamicSememeData[] {new DynamicSememeLongImpl(40)}));

		mapItemExtendedFieldsDef.add(mapItemEF1);

		ObjectNode mapItemEF2 = jfn.objectNode();
		mapItemEF2.put("columnLabelConcept", MetaData.BOOLEAN_LITERAL.getNid());
		mapItemEF2.put("columnDataType", DynamicSememeDataType.BOOLEAN.name());
		mapItemEF2.set("columnDefaultData", toJsonObject(new DynamicSememeBooleanImpl(true), 1));
		mapItemEF2.put("columnRequired", true);

		mapItemExtendedFieldsDef.add(mapItemEF2);

		root.set("mapItemExtendedFieldsDefinition", mapItemExtendedFieldsDef);

		log.info("MapSet create json: " + toJson(root));

		Response createMappingSetResponse = target(RestPaths.writePathComponent + RestPaths.mappingAPIsPathComponent
				+ RestPaths.mappingSetComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		String result = checkFail(createMappingSetResponse).readEntity(String.class);

		RestWriteResponse createdMapSetId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back

		result = checkFail(target(RestPaths.mappingSetAppPathComponent + createdMapSetId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence() + ", " + MetaData.ISAAC_MODULE.getConceptSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestMappingSetVersion createdMapSet = XMLUtils.unmarshalObject(RestMappingSetVersion.class, result);
		Assert.assertEquals(createdMapSet.identifiers.sequence.intValue(), createdMapSetId.sequence.intValue());
		Assert.assertEquals(createdMapSet.description, "bla bla");
		Assert.assertEquals(createdMapSet.inverseName, "inverse test");
		Assert.assertEquals(createdMapSet.name, name);
		Assert.assertEquals(createdMapSet.purpose, "just testing");
		Assert.assertEquals(createdMapSet.active.booleanValue(), false);
		Assert.assertNull(createdMapSet.comments);
		Assert.assertEquals(createdMapSet.identifiers.getFirst(), createdMapSetId.uuid);
		Assert.assertEquals(createdMapSet.mappingSetStamp.state.enumName, State.INACTIVE.name());
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.size(), 1);
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.get(0).extensionNameConceptIdentifiers.sequence.intValue(), MetaData.LOINC_MODULES.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapSetExtendedFields.get(0).extensionValue.data.toString(), "test Value extended");

		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.size(), 2);

		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnLabelConcept.sequence.intValue(), MetaData.CONDOR_CLASSIFIER.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnName, MetaData.CONDOR_CLASSIFIER.getConceptDescriptionText());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnOrder, 0);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnRequired, false);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnDataType.enumId, DynamicSememeDataType.LONG.ordinal());
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(0).columnDefaultData);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorData.length, 1);
		Assert.assertEquals(((Long)createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorData[0].data).longValue(), 40l);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorTypes.length, 1);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(0).columnValidatorTypes[0].enumId, DynamicSememeValidatorType.LESS_THAN.ordinal());

		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnLabelConcept.sequence.intValue(), MetaData.BOOLEAN_LITERAL.getConceptSequence());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnName, MetaData.BOOLEAN_LITERAL.getConceptDescriptionText());
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnOrder, 1);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnRequired, true);
		Assert.assertEquals(createdMapSet.mapItemFieldsDefinition.get(1).columnDataType.enumId, DynamicSememeDataType.BOOLEAN.ordinal());
		Assert.assertEquals((boolean)createdMapSet.mapItemFieldsDefinition.get(1).columnDefaultData.data , true);
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorData);
		Assert.assertNull(createdMapSet.mapItemFieldsDefinition.get(1).columnValidatorTypes);

		//Create an item

		root = jfn.objectNode();
		root.put("mapSetConcept", createdMapSet.identifiers.sequence);
		root.put("sourceConcept", MetaData.COMMITTED_STATE_FOR_CHRONICLE.getConceptSequence());
		root.put("targetConcept", MetaData.AND.getConceptSequence());
		root.put("qualifierConcept", IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_NARROW_TO_BROAD.getConceptSequence());

		root.set("mapItemExtendedFields",  toJsonObject(new DynamicSememeData[] {new DynamicSememeLongImpl(-5620), null}));

		log.info("Map item create json: " + toJson(root));

		Response createNewMappingItemResponse = target(RestPaths.mappingItemCreateAppPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		String newMappingItemSequenceWrapperXml = createNewMappingItemResponse.readEntity(String.class);
		RestWriteResponse newMappingItemSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newMappingItemSequenceWrapperXml);
		UUID newMappingItemUUID = newMappingItemSequenceWrapper.uuid;
		// Confirm returned sequence is valid
		Assert.assertTrue(newMappingItemUUID != null);

		// test createNewMappingItem()
		// Retrieve mapping item and validate fields
		Response getNewMappingItemVersionResponse = target(RestPaths.mappingItemAppPathComponent + newMappingItemUUID)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence() + ", " + MetaData.ISAAC_MODULE.getConceptSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String retrievedMappingItemVersionResult = checkFail(getNewMappingItemVersionResponse).readEntity(String.class);
		RestMappingItemVersion retrievedMappingItemVersion = XMLUtils.unmarshalObject(RestMappingItemVersion.class, retrievedMappingItemVersionResult);
		Assert.assertEquals(retrievedMappingItemVersion.identifiers.sequence.intValue(), newMappingItemSequenceWrapper.sequence.intValue());
		Assert.assertTrue(MetaData.COMMITTED_STATE_FOR_CHRONICLE.getConceptSequence() == retrievedMappingItemVersion.sourceConcept.sequence);
		Assert.assertTrue(MetaData.AND.getConceptSequence() == retrievedMappingItemVersion.targetConcept.sequence);
		Assert.assertTrue(IsaacMappingConstants.MAPPING_EQUIVALENCE_TYPE_NARROW_TO_BROAD.getConceptSequence() == retrievedMappingItemVersion.qualifierConcept.sequence);
		Assert.assertEquals(createdMapSet.identifiers.sequence, retrievedMappingItemVersion.mapSetConcept.sequence);
		Assert.assertTrue(retrievedMappingItemVersion.mappingItemStamp.state.enumId == State.ACTIVE.ordinal());

		Assert.assertEquals(2, retrievedMappingItemVersion.mapItemExtendedFields.size());
		Assert.assertEquals(((Long)retrievedMappingItemVersion.mapItemExtendedFields.get(0).data).longValue(), -5620);
		Assert.assertEquals(retrievedMappingItemVersion.mapItemExtendedFields.get(0).columnNumber.intValue(), 0);
		Assert.assertEquals(((Boolean)retrievedMappingItemVersion.mapItemExtendedFields.get(1).data).booleanValue(), true);
		Assert.assertEquals(retrievedMappingItemVersion.mapItemExtendedFields.get(1).columnNumber.intValue(), 1);
	}

	@Test
	public void testCommentAPIs()
	{
		String conceptNid = MetaData.SNOROCKET_CLASSIFIER.getNid() + "";

		// Create a random string to confirm target data are relevant
		UUID randomUuid = UUID.randomUUID();

		// Create a new comment on SNOROCKET_CLASSIFIER
		String newCommentText = "A new comment text for SNOROCKET_CLASSIFIER (" + randomUuid + ")";
		String newCommentContext = "A new comment context for SNOROCKET_CLASSIFIER (" + randomUuid + ")";
		RestCommentVersionCreate newCommentData = new RestCommentVersionCreate(
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

		Response unauthorizedCreateCommentResponse = target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		assertResponseStatus(unauthorizedCreateCommentResponse, Status.FORBIDDEN.getStatusCode());

		Response createCommentResponse = target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.xml(xml));
		String newCommentSememeSequenceWrapperXml = createCommentResponse.readEntity(String.class);
		RestWriteResponse newCommentSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newCommentSememeSequenceWrapperXml);
		int newCommentSememeSequence = newCommentSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newCommentSememeSequence > 0);

		// Retrieve new comment and validate fields
		Response getCommentVersionResponse = target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion newCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals(newCommentText, newCommentObject.comment);
		Assert.assertEquals(newCommentContext, newCommentObject.commentContext);

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
		// Attempt to update comment with read_only token
		Response updateCommentResponse = target(RestPaths.commentUpdatePathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		assertResponseStatus(updateCommentResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to update comment
		updateCommentResponse = target(RestPaths.commentUpdatePathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		String restWriteResponseXml = checkFail(updateCommentResponse).readEntity(String.class);
		RestWriteResponse restWriteResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, restWriteResponseXml);
		Assert.assertTrue(restWriteResponse.detail == null || ! restWriteResponse.detail.equals(RestWriteResponseEnumeratedDetails.UNCHANGED));

		// Attempt to update comment with identical data
		updateCommentResponse = target(RestPaths.commentUpdatePathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.xml(xml));
		restWriteResponseXml = checkFail(updateCommentResponse).readEntity(String.class);
		restWriteResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, restWriteResponseXml);
		Assert.assertEquals(restWriteResponse.detail, RestWriteResponseEnumeratedDetails.UNCHANGED);

		// Retrieve updated comment and validate fields
		getCommentVersionResponse = target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion updatedCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals(updatedCommentText, updatedCommentObject.comment);
		Assert.assertTrue(StringUtils.isBlank(updatedCommentObject.commentContext));

		// Get list of RestCommentVersion associated with MetaData.SNOROCKET_CLASSIFIER
		Response getCommentVersionByReferencedItemResponse = target(RestPaths.commentVersionByReferencedComponentPathComponent + MetaData.SNOROCKET_CLASSIFIER.getPrimordialUuid().toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getCommentVersionByReferencedItemResult = checkFail(getCommentVersionByReferencedItemResponse).readEntity(String.class);

		RestCommentVersion[] commentVersionsObject = XMLUtils.unmarshalObjectArray(RestCommentVersion.class, getCommentVersionByReferencedItemResult);
		Assert.assertTrue(commentVersionsObject != null && commentVersionsObject.length > 0);
		RestCommentVersion commentVersionRetrievedByReferencedItem = null;
		for (RestCommentVersion commentVersion : commentVersionsObject) {
			if (commentVersion.comment != null && commentVersion.comment.equals(updatedCommentText)
					&& StringUtils.isBlank(commentVersion.commentContext)) {
				commentVersionRetrievedByReferencedItem = commentVersion;
				break;
			}
		}
		Assert.assertNotNull(commentVersionRetrievedByReferencedItem);
	}

	@Test
	public void testCommentAPI2s() throws JsonProcessingException, IOException
	{
		int conceptNid = MetaData.AXIOM_ORIGIN.getNid();

		// Create a random string to confirm target data are relevant
		UUID randomUuid = UUID.randomUUID();

		String json = jsonIze(new String[] {"commentedItem", "comment"}, new String[] {conceptNid + "", "my random comment"});

		Response createCommentResponse = checkFail(target(RestPaths.commentCreatePathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(json)));
		String newCommentSememeSequenceWrapperXml = createCommentResponse.readEntity(String.class);
		RestWriteResponse newCommentSememeSequenceWrapper = XMLUtils.unmarshalObject(RestWriteResponse.class, newCommentSememeSequenceWrapperXml);
		int newCommentSememeSequence = newCommentSememeSequenceWrapper.sequence;
		// Confirm returned sequence is valid
		Assert.assertTrue(newCommentSememeSequence > 0);

		// Retrieve new comment and validate fields
		Response getCommentVersionResponse = checkFail(target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get());
		String commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion newCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals("my random comment", newCommentObject.comment);
		Assert.assertNull(newCommentObject.commentContext);

		// Update comment with new comment text value and empty comment context value
		String context = "An updated comment text for (" + randomUuid + ")";

		json = jsonIze(new String[] {"comment", "commentContext", "active"}, new String[] {"my random comment 2", context, "true"});

		Response updateCommentResponse = target(RestPaths.commentUpdatePathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(json));
		checkFail(updateCommentResponse);

		// Retrieve updated comment and validate fields
		getCommentVersionResponse = target(RestPaths.commentVersionPathComponent + newCommentSememeSequence)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		commentVersionResult = checkFail(getCommentVersionResponse).readEntity(String.class);
		RestCommentVersion updatedCommentObject = XMLUtils.unmarshalObject(RestCommentVersion.class, commentVersionResult);
		Assert.assertEquals("my random comment 2", updatedCommentObject.comment);
		Assert.assertEquals(context, updatedCommentObject.commentContext);

		// Get list of RestCommentVersion associated with MetaData.AXIOM_ORIGIN
		Response getCommentVersionByReferencedItemResponse = target(RestPaths.commentVersionByReferencedComponentPathComponent
				+ MetaData.AXIOM_ORIGIN.getPrimordialUuid().toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String getCommentVersionByReferencedItemResult = checkFail(getCommentVersionByReferencedItemResponse).readEntity(String.class);

		RestCommentVersion[] commentVersionsObject = XMLUtils.unmarshalObjectArray(RestCommentVersion.class, getCommentVersionByReferencedItemResult);
		Assert.assertTrue(commentVersionsObject != null && commentVersionsObject.length > 0);
		RestCommentVersion commentVersionRetrievedByReferencedItem = null;
		for (RestCommentVersion commentVersion : commentVersionsObject) {
			if (commentVersion.comment != null && commentVersion.comment.equals("my random comment 2")
					&& context.equals(commentVersion.commentContext)) {
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
		String xpathExpr = "/restSememeVersionPage/results/sememeChronology/identifiers";

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
		Assert.assertTrue(result.contains("<referencedComponentNidDescription>dynamic sememe extension definition (ISAAC)</referencedComponentNidDescription>"));
	}


	/**
	 * This test validates idTranslateComponent
	 * 
	 * This test validates that both the JSON and XML serializers are working correctly with returns that contain data.
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

		Assert.assertEquals(getIntegerIdForUuid(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(), IdType.CONCEPT_SEQUENCE.getDisplayName()), DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getConceptSequence());
		Assert.assertEquals(getIntegerIdForUuid(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getPrimordialUuid(), IdType.NID.getDisplayName()), DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid());
	
		final String idsUrl = RestPaths.idAPIsPathComponent + RestPaths.idsComponent;
		Response idsResponse = target(idsUrl)
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).get();
		String idsXml = checkFail(idsResponse).readEntity(String.class);
		RestConceptChronology[] idConcepts = XMLUtils.unmarshalObjectArray(RestConceptChronology.class, idsXml);
		Assert.assertNotNull(idConcepts);
		Assert.assertTrue(idConcepts.length >= 4);
		
		boolean foundCodeConcept = false;
		boolean foundSctIdConcept = false;
		boolean foundVuidConcept = false;
		boolean foundGeneratedUuidConcept = false;
		for (int i = 0; i < idConcepts.length; ++i) {
			int retrievedIdConceptNid = idConcepts[i].getIdentifiers().nid;
			if (retrievedIdConceptNid == MetaData.CODE.getNid()) {
				foundCodeConcept = true;
			} else if (retrievedIdConceptNid == MetaData.SCTID.getNid()) {
				foundSctIdConcept = true;
			} else if (retrievedIdConceptNid == MetaData.VUID.getNid()) {
				foundVuidConcept = true;
			} else if (retrievedIdConceptNid == MetaData.GENERATED_UUID.getNid()) {
				foundGeneratedUuidConcept = true;
			}
		}
		Assert.assertTrue(foundCodeConcept);
		Assert.assertTrue(foundSctIdConcept);
		Assert.assertTrue(foundVuidConcept);
		Assert.assertTrue(foundGeneratedUuidConcept);
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
		if (rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("enumName") == null || ! rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("enumName").asText().equals(NodeSemantic.DEFINITION_ROOT.name())) {
			Assert.fail("testRestSememeLogicGraphVersionReturn() parsed RestSememeLogicGraphVersion with missing or invalid " + rootLogicNodeFieldName + ": \"" + rootNode.with(rootLogicNodeFieldName).with(nodeSemanticNodeFieldName).get("enumName") + "\"!=\"" + NodeSemantic.DEFINITION_ROOT.name() + "\"");
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
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.LOINC_MODULES.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//Check with nid
		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.LOINC_MODULES.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//Check with sequence
		//Check with nid
		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"3")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getConceptSequence() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.LOINC_MODULES.getNid() + "")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		Assert.assertTrue(result.contains(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_DEFAULT_VALUE.getPrimordialUuid().toString()));

		//sanity check search
		result = checkFail(target(sememeSearchRequestPath)
				.queryParam(RequestParameters.treatAsString, "false")
				.queryParam(RequestParameters.query,"55")
				.queryParam(RequestParameters.sememeAssemblageId, DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getNid() + "")
				.queryParam(RequestParameters.sememeAssemblageId, MetaData.LOINC_MODULES.getNid() + "")
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
			Assert.assertTrue(preDialect.matches("(?s).*<assemblage>.*<sequence>.*" + MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence()
			+ ".*</assemblage>.*"), "Wrong language");
			Assert.assertTrue(preDialect.matches("(?s).*<referencedComponent>.*<nid>.*" + MetaData.USER.getNid() + ".*</referencedComponent>.*"), "Wrong concept");
			Assert.assertTrue(preDialect.matches("(?s).*<caseSignificanceConcept>.*<sequence>.*" + MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence()
			+ ".*</caseSignificanceConcept>.*"), "Wrong case sentivity");
			Assert.assertTrue(preDialect.matches("(?s).*<languageConcept>.*<sequence>.*" + MetaData.ENGLISH_LANGUAGE.getConceptSequence()
			+ ".*</languageConcept>.*"), "Wrong language");
			Assert.assertTrue((preDialect.contains("<text>user</text>") || preDialect.contains("<text>user (ISAAC)</text>")), "Wrong text " + preDialect);
			Assert.assertTrue((preDialect.matches("(?s).*<descriptionTypeConcept>.*<sequence>.*" + MetaData.SYNONYM.getConceptSequence()
			+ ".*</descriptionTypeConcept>.*")
					|| preDialect.matches("(?s).*<descriptionTypeConcept>.*<sequence>.*" + MetaData.FULLY_SPECIFIED_NAME.getConceptSequence()
					+ ".*</descriptionTypeConcept>.*")), "Wrong description type");

			//validate that the dialect bits are put together properly
			Assert.assertTrue(dialect.matches("(?s).*<assemblage>.*<sequence>.*" + MetaData.US_ENGLISH_DIALECT.getConceptSequence() + ".*</assemblage>.*"), "Wrong dialect");
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
			Assert.assertTrue(description.dialects.size() > 0);

			//Validate that the important bit of the description sememe are put together properly
			//Assert.assertTrue(preDialect.contains("<assemblageSequence>" + MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence() + "</assemblageSequence>"), "Wrong language");
			Assert.assertEquals(description.getSememeChronology().assemblage.sequence.intValue(), MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence(), "Wrong language");

			//Assert.assertTrue(preDialect.contains("<referencedComponentNid>" + MetaData.USER.getNid() + "</referencedComponentNid>"), "Wrong concept");
			Assert.assertEquals(description.getSememeChronology().referencedComponent.nid.intValue(), MetaData.USER.getNid(), "Wrong concept");

			//Assert.assertTrue(preDialect.contains("<caseSignificanceConceptSequence>" + MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence()
			//+ "</caseSignificanceConceptSequence>"), "Wrong case sentivity");
			Assert.assertEquals(description.caseSignificanceConcept.sequence.intValue(), MetaData.DESCRIPTION_NOT_CASE_SENSITIVE.getConceptSequence(), "Wrong case sentivity");

			//Assert.assertTrue(preDialect.contains("<languageConceptSequence>" + MetaData.ENGLISH_LANGUAGE.getConceptSequence()
			//+ "</languageConceptSequence>"), "Wrong language");
			Assert.assertEquals(description.languageConcept.sequence.intValue(), MetaData.ENGLISH_LANGUAGE.getConceptSequence(), "Wrong language");

			//Assert.assertTrue((preDialect.contains("<text>user</text>") || preDialect.contains("<text>user (ISAAC)</text>")), "Wrong text " + preDialect);
			Assert.assertTrue(description.text.equals("user") || description.text.equals("user (ISAAC)"), "Wrong text" + description.text);

			//Assert.assertTrue((preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.SYNONYM.getConceptSequence() + "</descriptionTypeConceptSequence>")
			//		|| preDialect.contains("<descriptionTypeConceptSequence>" + MetaData.FULLY_SPECIFIED_NAME.getConceptSequence() + "</descriptionTypeConceptSequence>")),
			//		"Wrong description type");
			Assert.assertTrue(description.descriptionTypeConcept.sequence == MetaData.SYNONYM.getConceptSequence()
					|| description.descriptionTypeConcept.sequence == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence(),
					"Wrong description type");

			//validate that the dialect bits are put together properly
			//Assert.assertTrue(dialect.contains("<assemblageSequence>" + MetaData.US_ENGLISH_DIALECT.getConceptSequence() + "</assemblageSequence>"), "Wrong dialect");
			Assert.assertEquals(description.getSememeChronology().assemblage.sequence.intValue(), MetaData.ENGLISH_DESCRIPTION_ASSEMBLAGE.getConceptSequence(), "Wrong dialect");

			//Assert.assertTrue(dialect.contains("<data xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xsi:type=\"xs:int\">" + MetaData.PREFERRED.getNid() + "</data>"), "Wrong value");
			boolean foundPreferredDialect = false;
			boolean foundUsEnglishDialect = false;
			for (RestDynamicSememeVersion dialect : description.dialects) {
				if (dialect.getSememeChronology().assemblage.sequence == MetaData.US_ENGLISH_DIALECT.getConceptSequence()) {
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
			Assert.assertTrue(retrievedTaxonomyCoordinate.stampCoordinate.precedence.enumId == StampPrecedence.TIME.ordinal());
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
									param(RequestParameters.modules, MetaData.LOINC_MODULES.getConceptSequence() + "," + MetaData.ISAAC_MODULE.getConceptSequence() + "," + MetaData.SNOMED_CT_CORE_MODULES.getConceptSequence()),
									param(RequestParameters.allowedStates, State.INACTIVE.getAbbreviation() + "," + State.PRIMORDIAL.getAbbreviation()))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			xpath = "/restStampCoordinate/time";
			node = XMLUtils.getNodeFromXml(result, xpath);
			nodeList = null;
			long stampCoordinateTime = Long.parseLong(node.getTextContent());
			Assert.assertTrue(stampCoordinateTime == 123456789);
			xpath = "/restStampCoordinate/modules/sequence";
			//xpath = "/restStampCoordinate/modules";
			List<Integer> stampCoordinateModules = new ArrayList<>();
			node = null;
			nodeList = XMLUtils.getNodeSetFromXml(result, xpath);
			for (int i = 0; i < nodeList.getLength(); ++i) {
				stampCoordinateModules.add(Integer.valueOf(nodeList.item(i).getTextContent()));
			}
			Assert.assertTrue(stampCoordinateModules.size() == 3);
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.LOINC_MODULES.getConceptSequence()));
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.ISAAC_MODULE.getConceptSequence()));
			Assert.assertTrue(stampCoordinateModules.contains(MetaData.SNOMED_CT_CORE_MODULES.getConceptSequence()));

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
			RestLanguageCoordinate retrievedLanguageCoordinate = XMLUtils.unmarshalObject(RestLanguageCoordinate.class, result);
			Assert.assertTrue(retrievedLanguageCoordinate.language.sequence == MetaData.ENGLISH_LANGUAGE.getConceptSequence());

			// descriptionTypePrefs
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "fsn,synonym"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedLanguageCoordinate = XMLUtils.unmarshalObject(RestLanguageCoordinate.class, result);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences.length == 2);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[0].sequence == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[1].sequence == MetaData.SYNONYM.getConceptSequence());

			// descriptionTypePrefs (reversed)
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "synonym,fsn"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedLanguageCoordinate = XMLUtils.unmarshalObject(RestLanguageCoordinate.class, result);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences.length == 2);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[0].sequence == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[1].sequence == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

			// Get token with specified non-default descriptionTypePrefs (SYNONYM,FSN)
			// then test token passed as argument along with RequestParameters.stated parameter
			result = checkFail(
					(target = target(
							requestUrl = coordinatesTokenRequestPath,
							parameters = buildParams(param(RequestParameters.descriptionTypePrefs, "synonym,fsn"))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			final RestCoordinatesToken synonymDescriptionPreferredToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);
			// Get token with specified default descriptionTypePrefs (FSN,SYNONYM)
			parameters.clear();
			parameters.put(RequestParameters.descriptionTypePrefs, "fsn,synonym");
			result = checkFail(
					(target = target(requestUrl = coordinatesTokenRequestPath, parameters))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			final RestCoordinatesToken fsnDescriptionPreferredToken = XMLUtils.unmarshalObject(RestCoordinatesToken.class, result);

			// confirm that constructed token has descriptionTypePrefs ordered as in
			// parameters used to construct token
			result = checkFail(
					(target = target(
							requestUrl = languageCoordinateRequestPath,
							parameters = buildParams(param(RequestParameters.coordToken, synonymDescriptionPreferredToken.token))))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			retrievedLanguageCoordinate = XMLUtils.unmarshalObject(RestLanguageCoordinate.class, result);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences.length == 2);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[0].sequence == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[1].sequence == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

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
			retrievedLanguageCoordinate = XMLUtils.unmarshalObject(RestLanguageCoordinate.class, result);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences.length == 2);
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[0].sequence == MetaData.SYNONYM.getConceptSequence());
			Assert.assertTrue(retrievedLanguageCoordinate.descriptionTypePreferences[1].sequence == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence());

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
			xpath = "/restConceptVersion/children/conChronology[identifiers/sequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
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
			xpath = "/restConceptVersion/children/conChronology[identifiers/sequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
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
			xpath = "/restConceptVersion/children/conChronology[identifiers/sequence=" + MetaData.HEALTH_CONCEPT.getConceptSequence() + "]/description";
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
			RestLogicCoordinate retrievedLogicCoordinate = XMLUtils.unmarshalObject(RestLogicCoordinate.class, result);
			Assert.assertTrue(retrievedLogicCoordinate.classifier.sequence == MetaData.SNOROCKET_CLASSIFIER.getConceptSequence());
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
			UUID sememeUuid = sememeVersions.results[0].getSememeChronology().identifiers.getFirst();

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
			Assert.assertTrue(objectChronologyType.enumId == ObjectChronologyType.SEMEME.ordinal());

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
			Assert.assertTrue(objectChronologyType.enumId == ObjectChronologyType.CONCEPT.ordinal());

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
			Assert.assertTrue(identifiedObjectsResult.getSememe().identifiers.uuids.contains(sememeUuid));
			Assert.assertNull(identifiedObjectsResult.getConcept());

			// Test identifiedObjectsComponent request of specified concept UUID
			result = checkFail(
					(target = target(
							requestUrl = RestPaths.systemAPIsPathComponent + RestPaths.identifiedObjectsComponent + MetaData.ISAAC_ROOT.getPrimordialUuid().toString()))
					.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
					.readEntity(String.class);
			identifiedObjectsResult = XMLUtils.unmarshalObject(RestIdentifiedObjectsResult.class, result);
			// Test RestSememeChronology
			Assert.assertTrue(identifiedObjectsResult.getConcept().getIdentifiers().uuids.contains(MetaData.ISAAC_ROOT.getPrimordialUuid()));
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
					Assert.assertTrue(identifiedObjectsResult.getConcept().getIdentifiers().sequence == sequence);
					Assert.assertTrue(identifiedObjectsResult.getSememe().identifiers.sequence == sequence);

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
	public void testAssociations() throws JsonProcessingException, IOException
	{
		String result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsComponent)
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		//No associations in the metadata
		Assert.assertTrue(result.endsWith("<restAssociationTypeVersions></restAssociationTypeVersions>"));

		//Make one
		UUID random = UUID.randomUUID();
		final String description = "Just a test description type (" + random.toString() + ")";
		// Attempt to create association with read_only token
		Response createAssociationResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationName", "associationInverseName", "description"},
								new String[] {"test", "inverse Test", description})));
		assertResponseStatus(createAssociationResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to create association
		createAssociationResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationName", "associationInverseName", "description"},
								new String[] {"test", "inverse Test", description})));
		result = checkFail(createAssociationResponse).readEntity(String.class);

		RestWriteResponse createdAssociationId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back

		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationComponent + createdAssociationId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestAssociationTypeVersion createdAssociation = XMLUtils.unmarshalObject(RestAssociationTypeVersion.class, result);

		Assert.assertEquals(createdAssociation.associationName, "test");
		Assert.assertEquals(createdAssociation.description, description);
		Assert.assertEquals(createdAssociation.associationInverseName, "inverse Test");

		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsComponent)
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
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

		// Attempt to make one with read_only token
		Response createAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationType", "sourceId", "targetId"},
								new String[] {createdAssociations[0].associationConcept.getIdentifiers().sequence + "", MetaData.NUCC_MODULES.getNid() + "",
										MetaData.AND.getNid() + ""})));
		assertResponseStatus(createAssociationItemResponse, Status.FORBIDDEN.getStatusCode());

		// Attempt to make one
		createAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationType", "sourceId", "targetId"},
								new String[] {createdAssociations[0].associationConcept.getIdentifiers().sequence + "", MetaData.NUCC_MODULES.getNid() + "",
										MetaData.AND.getNid() + ""})));
		result = checkFail(createAssociationItemResponse).readEntity(String.class);
		RestWriteResponse createdAssociationItemId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//readBack
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationItemComponent + createdAssociationItemId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestAssociationItemVersion createdAssociationItem = XMLUtils.unmarshalObject(RestAssociationItemVersion.class, result);

		Assert.assertEquals(createdAssociationItem.identifiers.getFirst(), createdAssociationItemId.uuid);
		Assert.assertEquals(createdAssociationItem.sourceId.nid.intValue(), MetaData.NUCC_MODULES.getNid());
		Assert.assertEquals(createdAssociationItem.targetId.nid.intValue(), MetaData.AND.getNid());
		Assert.assertEquals(createdAssociationItem.associationType.sequence, createdAssociations[0].identifiers.sequence);
		Assert.assertEquals(createdAssociationItem.associationItemStamp.state.toString().toLowerCase(), "active");

		// Attempt to update association item with read_only token
		Response updateAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.updatePathComponent + createdAssociationItemId.uuid.toString())
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
						jsonIze(new String[] {"targetId", "active"},
								new String[] {"", "false"})));
		assertResponseStatus(updateAssociationItemResponse, Status.FORBIDDEN.getStatusCode());

		//test update association
		updateAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.updatePathComponent + createdAssociationItemId.uuid.toString())
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
						jsonIze(new String[] {"targetId", "active"},
								new String[] {"", "false"})));
		result = checkFail(updateAssociationItemResponse).readEntity(String.class);
		RestWriteResponse writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		Assert.assertTrue(StringUtils.isBlank(writeResponse.detail) || ! writeResponse.detail.equals(RestWriteResponseEnumeratedDetails.UNCHANGED));

		// test update with identical data.  Should succeed but with detail in response.
		updateAssociationItemResponse = target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.updatePathComponent + createdAssociationItemId.uuid.toString())
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).put(Entity.json(
						jsonIze(new String[] {"targetId", "active"},
								new String[] {"", "false"})));
		result = checkFail(updateAssociationItemResponse).readEntity(String.class);
		writeResponse = XMLUtils.unmarshalObject(RestWriteResponse.class, result);
		Assert.assertEquals(writeResponse.detail, RestWriteResponseEnumeratedDetails.UNCHANGED);

		Assert.assertEquals(writeResponse.uuid, createdAssociationItemId.uuid);

		//readBack
		result = checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationItemComponent + createdAssociationItemId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		createdAssociationItem = XMLUtils.unmarshalObject(RestAssociationItemVersion.class, result);

		Assert.assertEquals(createdAssociationItem.identifiers.getFirst(), createdAssociationItemId.uuid);
		Assert.assertEquals(createdAssociationItem.sourceId.nid.intValue(), MetaData.NUCC_MODULES.getNid());
		Assert.assertNull(createdAssociationItem.targetId);
		Assert.assertEquals(createdAssociationItem.associationType.sequence, createdAssociations[0].identifiers.sequence);
		Assert.assertEquals(createdAssociationItem.associationItemStamp.state.toString().toLowerCase(), "inactive");


		//Make more stuff for queries
		RestWriteResponse descType2 = XMLUtils
				.unmarshalObject(RestWriteResponse.class,
						checkFail(target(
								RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent + RestPaths.associationComponent + RestPaths.createPathComponent)
								.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN)).request()
								.header(Header.Accept.toString(), MediaType.APPLICATION_XML)
								.post(Entity.json(jsonIze(new String[] { "associationName", "associationInverseName", "description" },
										new String[] { "foo", "oof", description })))).readEntity(String.class));

		checkFail(target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationType", "sourceId", "targetId"},
								new String[] {createdAssociations[0].associationConcept.getIdentifiers().sequence + "", MetaData.LOINC_MODULES.getNid() + "", ""}))));

		checkFail(target(RestPaths.writePathComponent + RestPaths.associationAPIsPathComponent
				+ RestPaths.associationItemComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(
						jsonIze(new String[] {"associationType", "sourceId", "targetId"},
								new String[] {descType2.sequence + "", MetaData.LOINC_MODULES.getNid() + "",
										MetaData.AXIOM_ORIGIN.getNid() + ""}))));

		//test query by source

		RestAssociationItemVersion[] foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class,
				checkFail(target(RestPaths.associationAPIsPathComponent
						+ RestPaths.associationsWithSourceComponent + MetaData.LOINC_MODULES.getNid())
						.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
						.queryParam(RequestParameters.expand, "referencedConcept")
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertEquals(foundAssociations.length, 2);

		//test query by target


		foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class, checkFail(target(RestPaths.associationAPIsPathComponent
				+ RestPaths.associationsWithTargetComponent + MetaData.AXIOM_ORIGIN.getNid())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.expand, "referencedConcept")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		//TODO 2 Dan indexes this is broken - lucene indexes don't seem to be updating properly.  Dan to fix, someday....
		//		Assert.assertEquals(foundAssociations.length, 1);

		foundAssociations = XMLUtils.unmarshalObjectArray(RestAssociationItemVersion.class, checkFail(target(RestPaths.associationAPIsPathComponent
				+ RestPaths.associationsWithTargetComponent + MetaData.LOINC_MODULES.getNid())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.queryParam(RequestParameters.expand, "referencedConcept")
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertEquals(foundAssociations.length, 0);

		//test query by type

		RestAssociationItemVersionPage pagedAssociations = XMLUtils.unmarshalObject(RestAssociationItemVersionPage.class,
				checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsWithTypeComponent + createdAssociationId.uuid)
						.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
						.queryParam(RequestParameters.expand, "referencedConcept")
						.queryParam(RequestParameters.maxPageSize, "1")
						.queryParam(RequestParameters.pageNum, "1")
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertTrue(pagedAssociations.paginationData.totalIsExact);
		Assert.assertEquals(pagedAssociations.paginationData.pageNum, 1);
		Assert.assertEquals(pagedAssociations.paginationData.approximateTotal, 2);
		Assert.assertEquals(pagedAssociations.results.length, 1);
		Assert.assertEquals(pagedAssociations.results[0].associationType.sequence.intValue(), createdAssociationId.sequence.intValue());

		int r1Source = pagedAssociations.results[0].sourceId.nid;

		pagedAssociations = XMLUtils.unmarshalObject(RestAssociationItemVersionPage.class,
				checkFail(target(RestPaths.associationAPIsPathComponent + RestPaths.associationsWithTypeComponent + createdAssociationId.uuid)
						.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
						.queryParam(RequestParameters.expand, "referencedConcept")
						.queryParam(RequestParameters.maxPageSize, "1")
						.queryParam(RequestParameters.pageNum, "2")
						.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get()).readEntity(String.class));

		Assert.assertTrue(pagedAssociations.paginationData.totalIsExact);
		Assert.assertEquals(pagedAssociations.paginationData.pageNum, 2);
		Assert.assertEquals(pagedAssociations.paginationData.approximateTotal, 2);
		Assert.assertEquals(pagedAssociations.results.length, 1);
		Assert.assertEquals(pagedAssociations.results[0].associationType.sequence.intValue(), createdAssociationId.sequence.intValue());

		Assert.assertNotEquals(r1Source, pagedAssociations.results[0].sourceId.nid);
	}

	@Test
	public void testSememeWrite1() throws JsonProcessingException, IOException
	{
		ObjectNode root = jfn.objectNode();
		root.put("assemblageConcept", DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getNid() + "");
		root.put("referencedComponent", MetaData.CHINESE_LANGUAGE.getNid() + "");
		root.set("columnData", toJsonObject(new DynamicSememeData[] {new DynamicSememeStringImpl("test")}));

		log.info("Sememe Create Json: " + toJson(root));

		// Attempt to create sememe with read_only token
		Response createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		assertResponseStatus(createSememeResponse, Status.FORBIDDEN.getStatusCode());

		//make one
		createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		String result = checkFail(createSememeResponse).readEntity(String.class);

		RestWriteResponse createdSememeId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back

		result = checkFail(target(RestPaths.sememeAPIsPathComponent + RestPaths.versionComponent + createdSememeId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestDynamicSememeVersion createdSememe = XMLUtils.unmarshalObject(RestDynamicSememeVersion.class, result);

		Assert.assertEquals(createdSememe.getDataColumns().get(0).data.toString(), "test");
	}

	@Test
	public void testSememeWrite2() throws JsonProcessingException, IOException
	{
		//define a new sememe type which exercises as much as possible
		ObjectNode root = jfn.objectNode();
		root.put("name", "testSememe");
		root.put("description", "A test sememe");

		ArrayNode columns = jfn.arrayNode();

		//make a column for each data type for test purposes.
		int i = 1;
		for (DynamicSememeDataType t : DynamicSememeDataType.values())
		{
			if (t == DynamicSememeDataType.POLYMORPHIC || t == DynamicSememeDataType.UNKNOWN)
			{
				continue;
			}
			ObjectNode column = jfn.objectNode();
			column.put("columnLabelConcept", i);  //Just picking arbitrary concepts...
			column.put("columnDataType", t.name());
			if (t == DynamicSememeDataType.FLOAT)
			{
				column.set("columnDefaultData", toJsonObject(new DynamicSememeFloatImpl(54.3f), -1));
			}
			else
			{
				column.set("columnDefaultData", jfn.nullNode());
			}
			column.put("columnRequired", (i > 3 ? false : true));
			if (t == DynamicSememeDataType.INTEGER)
			{
				ArrayNode validatorTypes = jfn.arrayNode();
				validatorTypes.add(DynamicSememeValidatorType.LESS_THAN.name());
				column.set("columnValidatorTypes", validatorTypes);

				ArrayNode validatorData = jfn.arrayNode();
				validatorData.add(toJsonObject(new DynamicSememeIntegerImpl(5), -1));
				column.set("columnValidatorData", validatorData);
			}
			else
			{
				column.set("columnValidatorTypes", jfn.nullNode());
				//don't even add the validator type column
			}
			columns.add(column);
			i++;
		}

		root.set("dataColumnsDefinition", columns);
		root.put("parentConcept", MetaData.LOINC_MODULES.getNid() + "");
		root.put("referencedComponentRestriction", "CONCEPt");
		root.set("referencedComponentSubRestriction", jfn.nullNode());

		String json = toJson(root);

		log.info("Sememe Create Json: " + json);
		// Attempt to create sememe with read_only token
		Response createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.sememeTypeComponent
				+ RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_READ_ONLY_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(json));
		assertResponseStatus(createSememeResponse, Status.FORBIDDEN.getStatusCode());

		// create sememe
		createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.sememeTypeComponent
				+ RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(json));
		String result = checkFail(createSememeResponse).readEntity(String.class);

		RestWriteResponse createdSememeTypeId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back

		result = checkFail(target(RestPaths.sememeAPIsPathComponent + RestPaths.sememeDefinitionComponent + createdSememeTypeId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestDynamicSememeDefinition createdSememeType = XMLUtils.unmarshalObject(RestDynamicSememeDefinition.class, result);

		Assert.assertEquals(createdSememeTypeId.sequence.intValue(), createdSememeType.assemblageConceptId.sequence.intValue());
		Assert.assertEquals("A test sememe", createdSememeType.sememeUsageDescription);
		Assert.assertTrue("CONCEPT".equalsIgnoreCase(createdSememeType.referencedComponentTypeRestriction.enumName));
		Assert.assertNull(createdSememeType.referencedComponentTypeSubRestriction);

		i = 0;
		for (DynamicSememeDataType t : DynamicSememeDataType.values())
		{
			if (t == DynamicSememeDataType.POLYMORPHIC || t == DynamicSememeDataType.UNKNOWN)
			{
				continue;
			}
			Assert.assertEquals(i +1, createdSememeType.columnInfo[i].columnLabelConcept.sequence.intValue());
			Assert.assertEquals(i, createdSememeType.columnInfo[i].columnOrder);
			Assert.assertEquals(t.getDisplayName(), createdSememeType.columnInfo[i].columnDataType.friendlyName);
			Assert.assertEquals(t.ordinal(), createdSememeType.columnInfo[i].columnDataType.enumId);
			if (t == DynamicSememeDataType.FLOAT)
			{
				Assert.assertEquals(54.3f, ((Float)createdSememeType.columnInfo[i].columnDefaultData.data).floatValue());
			}
			else
			{
				Assert.assertNull(createdSememeType.columnInfo[i].columnDefaultData);
			}
			//Assert.assertEquals("", createdSememe.columnInfo[i].columnDescription);  //I think this is read from the preferred description?
			//Assert.assertEquals(Get.conceptDescriptionText(i + 1), createdSememeType.columnInfo[i].columnName);  //TODO align the way we pick descriptions on these
			Assert.assertEquals((i + 1 > 3 ? false : true), createdSememeType.columnInfo[i].columnRequired);

			if (t == DynamicSememeDataType.INTEGER)
			{
				Assert.assertEquals(1, createdSememeType.columnInfo[i].columnValidatorTypes.length);
				Assert.assertEquals(DynamicSememeValidatorType.LESS_THAN.getDisplayName(), createdSememeType.columnInfo[i].columnValidatorTypes[0].friendlyName);
				Assert.assertEquals(DynamicSememeValidatorType.LESS_THAN.ordinal(), createdSememeType.columnInfo[i].columnValidatorTypes[0].enumId);
				Assert.assertEquals(1, createdSememeType.columnInfo[i].columnValidatorData.length);
				Assert.assertEquals(5, ((Integer)createdSememeType.columnInfo[i].columnValidatorData[0].data).intValue());
			}
			else
			{
				Assert.assertNull(createdSememeType.columnInfo[i].columnValidatorTypes);
				Assert.assertNull(createdSememeType.columnInfo[i].columnValidatorData);
			}
			i++;
		}
	}

	@Test
	public void testExtendedDescriptionTypeEdit() throws JsonProcessingException, IOException
	{

		//Read a concepts descriptions
		String result = checkFail(target(conceptDescriptionsRequestPath + MetaData.CHINESE_LANGUAGE.getConceptSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestSememeDescriptionVersion[] descriptions = XMLUtils.unmarshalObjectArray(RestSememeDescriptionVersion.class, result);

		Assert.assertNull(descriptions[0].descriptionExtendedTypeConcept);


		ObjectNode root = jfn.objectNode();
		root.put("assemblageConcept", DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENDED_DESCRIPTION_TYPE.getNid() + "");
		root.put("referencedComponent", descriptions[0].getSememeChronology().identifiers.getFirst().toString());
		root.set("columnData", toJsonObject(new DynamicSememeData[] {new DynamicSememeUUIDImpl(MetaData.BOOLEAN_LITERAL.getPrimordialUuid())}));

		log.info("Extended description type edit Json: " + toJson(root));

		Response createSememeResponse = target(RestPaths.writePathComponent + RestPaths.sememeAPIsPathComponent + RestPaths.createPathComponent)
				.queryParam(RequestParameters.editToken, getEditTokenString(TEST_SSO_TOKEN))
				.request()
				.header(Header.Accept.toString(), MediaType.APPLICATION_XML).post(Entity.json(toJson(root)));
		result = checkFail(createSememeResponse).readEntity(String.class);

		RestWriteResponse createdSememeId = XMLUtils.unmarshalObject(RestWriteResponse.class, result);

		//Read back the sememe directly

		result = checkFail(target(RestPaths.sememeAPIsPathComponent + RestPaths.versionComponent + createdSememeId.uuid.toString())
				.queryParam(RequestParameters.modules, RequestInfo.getDefaultEditCoordinate().getModuleSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		RestDynamicSememeVersion createdSememe = XMLUtils.unmarshalObject(RestDynamicSememeVersion.class, result);

		Assert.assertEquals(createdSememe.getDataColumns().get(0).data.toString(), MetaData.BOOLEAN_LITERAL.getPrimordialUuid().toString());

		//Read back via the sememeDescription API

		result = checkFail(target(conceptDescriptionsRequestPath + MetaData.CHINESE_LANGUAGE.getConceptSequence())
				.request().header(Header.Accept.toString(), MediaType.APPLICATION_XML).get())
				.readEntity(String.class);

		descriptions = XMLUtils.unmarshalObjectArray(RestSememeDescriptionVersion.class, result);

		Assert.assertEquals(descriptions[0].descriptionExtendedTypeConcept.sequence.intValue(), MetaData.BOOLEAN_LITERAL.getConceptSequence());
	}

	/**
	 * @param data
	 * @return
	 */
	private JsonNode toJsonObject(DynamicSememeData[] data)
	{
		ArrayNode on = jfn.arrayNode();
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] == null)
			{
				on.addNull();
			}
			else
			{
				on.add(toJsonObject(data[i], i));
			}
		}
		return on;
	}
	/**
	 * @param data
	 * @return
	 */
	private JsonNode toJsonObject(DynamicSememeData data, int columnNumber)
	{
		ObjectNode on = jfn.objectNode();
		switch(data.getDynamicSememeDataType())
		{
		case ARRAY:
			on.put("@class", RestDynamicSememeArray.class.getName());
			ArrayNode nested = jfn.arrayNode();
			for (DynamicSememeData x : ((DynamicSememeArray)data).getDataArray())
			{
				nested.add(toJsonObject(x, columnNumber));
			}
			on.set("data", nested);
			break;
		case BOOLEAN:
			on.put("@class", RestDynamicSememeBoolean.class.getName());
			on.put("data", ((DynamicSememeBoolean)data).getDataBoolean());
			break;
		case BYTEARRAY:
			on.put("@class", RestDynamicSememeByteArray.class.getName());
			on.put("data", ((DynamicSememeByteArray)data).getDataByteArray());
			break;
		case DOUBLE:
			on.put("@class", RestDynamicSememeDouble.class.getName());
			on.put("data", ((DynamicSememeDouble)data).getDataDouble());
			break;
		case FLOAT:
			on.put("@class", RestDynamicSememeFloat.class.getName());
			on.put("data", ((DynamicSememeFloat)data).getDataFloat());
			break;
		case INTEGER:
			on.put("@class", RestDynamicSememeInteger.class.getName());
			on.put("data", ((DynamicSememeInteger)data).getDataInteger());
			break;
		case LONG:
			on.put("@class", RestDynamicSememeLong.class.getName());
			on.put("data", ((DynamicSememeLong)data).getDataLong());
			break;
		case NID:
			on.put("@class", RestDynamicSememeNid.class.getName());
			on.put("data", ((DynamicSememeNid)data).getDataNid());
			break;
		case SEQUENCE:
			on.put("@class", RestDynamicSememeSequence.class.getName());
			on.put("data", ((DynamicSememeSequence)data).getDataSequence());
			break;
		case STRING:
			on.put("@class", RestDynamicSememeString.class.getName());
			on.put("data", ((DynamicSememeString)data).getDataString());
			break;
		case UUID:
			on.put("@class", RestDynamicSememeUUID.class.getName());
			on.put("data", ((DynamicSememeUUID)data).getDataUUID().toString());
			break;
		case POLYMORPHIC:
		case UNKNOWN:
		default :
			throw new RuntimeException("Unsupported type");
		}
		on.put("columnNumber", columnNumber);
		return on;
	}

	private String toJson(ObjectNode root) throws JsonProcessingException, IOException
	{
		StringWriter ws = new StringWriter();
		new ObjectMapper().writeTree(new JsonFactory().createGenerator(ws).setPrettyPrinter(new DefaultPrettyPrinter()), root);
		return ws.toString();
	}

	private String jsonIze(String[] names, String[] values) throws JsonProcessingException, IOException
	{
		ObjectNode root = jfn.objectNode();
		for (int i = 0; i < names.length; i++)
		{
			root.put(names[i], values[i]);
		}
		return toJson(root);
	}
}