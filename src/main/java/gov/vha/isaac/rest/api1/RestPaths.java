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
package gov.vha.isaac.rest.api1;
/**
 * 
 * {@link RestPaths}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RestPaths
{
	public static final String appPathComponent = "rest/";
	public static final String apiVersionComponent = "1/";
	public static final String writePathComponent = "write/";
	public static final String createPathComponent = "create/";
	public static final String updatePathComponent = "update/";

	public static final String chronologyComponent = "chronology/";
	public static final String updateStateComponent = "updateState/";
	public static final String activateComponent = "activate/";
	public static final String deactivateComponent = "deactivate/";
	public static final String versionsComponent = "versions/";	
	public static final String versionComponent = "version/";
	public static final String objectChronologyTypeComponent = "objectChronologyType/";
	public static final String identifiedObjectsComponent = "identifiedObjects/";
	public static final String sememeTypeComponent = "sememeType/";
	public static final String idTranslateComponent = "translate/";
	public static final String idTypesComponent = "types/";
	public static final String descriptionsComponent = "descriptions/";
	public static final String descriptionComponent = "description/";
	public static final String componentComponent = "component/";
	public static final String sememesComponent = "sememes/";
	public static final String prefixComponent = "prefix/";
	public static final String byAssemblageComponent = "byAssemblage/"; // TODO change to forAssemblage
	public static final String byReferencedComponentComponent = "byReferencedComponent/"; // TODO change to forReferencedComponent
	public static final String sememeDefinitionComponent = "sememeDefinition/";
	public static final String systemInfoComponent = "systemInfo/";
	public static final String termRequestComponent = "termRequest/";
	
	public static final String workflowAPIsPathComponent = apiVersionComponent + "workflow/";
	// WorkflowAccessor
	public static final String permissionsForDefinitionAndUserComponent = "permissionsForDefinitionAndUser/";
	public static final String actionsForProcessAndUserComponent = "actionsForProcessAndUser/";
	public static final String defaultDefinitionComponent = "defaultDefinition/";
	public static final String availableDefinitionsComponent = "availableDefinitions/";
	public static final String workflowLockStateComponent = "workflowLockState/";
	public static final String allRolesComponent = "allRoles/";

	public static final String historiesForProcessComponent = "historiesForProcess/";
	public static final String processComponent = "process/";
	public static final String definitionComponent = "definition/";
	public static final String advanceableProcessInformationComponent = "advanceableProcessInformation/";
//	public static final String isConceptInActiveWorkflowComponent = "isConceptInActiveWorkflow/";
	public static final String isComponentInActiveWorkflowComponent = "isComponentInActiveWorkflow/";
	// WorkflowProcessInitializerConcluder
	public static final String createWorkflowProcessComponent = "createWorkflowProcess/";
//	public static final String endWorkflowProcessComponent = "finishWorkflowProcess/";
//	public static final String launchWorkflowProcessComponent = "launchWorkflowProcess/";
//	public static final String cancelWorkflowProcessComponent = "cancelWorkflowProcess/";
//	public static final String concludeWorkflowProcessComponent = "concludeWorkflowProcess/";
	// WorkflowUpdater
	public static final String advanceWorkflowProcessComponent = "advanceWorkflowProcess/";
	public static final String addWorkflowUserRoleComponent = "addWorkflowUserRole/";
	public static final String removeComponentFromWorkflowComponent = "removeComponentFromWorkflow/";
	public static final String addComponentToWorkflowComponent = "addComponentToWorkflow/";
	public static final String acquireWorkflowLockComponent = "acquireWorkflowLock/";
	public static final String releaseWorkflowLockComponent = "releaseWorkflowLock/";
	
//	public static final String addStampToExistingWorkflowProcessComponent = "addStampToExistingWorkflowProcess/";
//	public static final String addConceptsToExistingWorkflowProcessComponent = "addStampsToExistingWorkflowProcess/";
//	public static final String removeWorkflowUserRoleComponent = "removeWorkflowUserRole/";

	
	public static final String mappingAPIsPathComponent = apiVersionComponent + "mapping/";
	public static final String mappingSetComponent = "mappingSet/";
	public static final String mappingSetsComponent = "mappingSets/";
	public static final String mappingItemComponent = "mappingItem/";
	public static final String mappingItemsComponent = "mappingItems/";
	public static final String mappingSetAppPathComponent = mappingAPIsPathComponent + mappingSetComponent;
	public static final String mappingSetsAppPathComponent = mappingAPIsPathComponent + mappingSetsComponent;
	public static final String mappingItemAppPathComponent = mappingAPIsPathComponent + mappingItemComponent;
	public static final String mappingItemsAppPathComponent = mappingAPIsPathComponent + mappingItemsComponent;

	public static final String mappingSetCreateAppPathComponent = writePathComponent+ mappingAPIsPathComponent + mappingSetComponent + createPathComponent;
	public static final String mappingSetUpdateAppPathComponent = writePathComponent+ mappingAPIsPathComponent + mappingSetComponent + updatePathComponent;
	public static final String mappingItemCreateAppPathComponent = writePathComponent + mappingAPIsPathComponent + mappingItemComponent + createPathComponent;
	public static final String mappingItemUpdateAppPathComponent = writePathComponent + mappingAPIsPathComponent + mappingItemComponent + updatePathComponent;
	
	public static final String associationAPIsPathComponent = apiVersionComponent + "association/";
	public static final String associationComponent = "type/";
	public static final String associationItemComponent = "item/";
	public static final String associationsComponent = "types/";
	public static final String associationsWithTypeComponent = "withType/";
	public static final String associationsWithSourceComponent = "withSource/";
	public static final String associationsWithTargetComponent = "withTarget/";
	
	public static final String enumerationComponent = "enumeration/";
	public static final String enumerationRestDynamicSememeDataTypeComponent = enumerationComponent + "restDynamicSememeDataType/";
	public static final String enumerationRestDynamicSememeValidatorTypeComponent = enumerationComponent + "restDynamicSememeValidatorType/";
	public static final String enumerationRestObjectChronologyTypeComponent = enumerationComponent + "restObjectChronologyType/";
	public static final String enumerationRestSememeTypeComponent = enumerationComponent + "restSememeType/";
	public static final String enumerationRestConcreteDomainOperatorTypes = enumerationComponent + "restConcreteDomainOperatorTypes/";
	public static final String enumerationRestNodeSemanticTypes = enumerationComponent + "restNodeSemanticType/";
	public static final String enumerationRestSupportedIdTypes = enumerationComponent + "restSupportedIdTypes/";
	
	public static final String conceptAPIsPathComponent = apiVersionComponent + "concept/";
	public static final String conceptChronologyAppPathComponent = conceptAPIsPathComponent + chronologyComponent;
	public static final String conceptVersionsAppPathComponent = appPathComponent + conceptAPIsPathComponent + versionsComponent;
	public static final String conceptVersionAppPathComponent = appPathComponent + conceptAPIsPathComponent +versionComponent;
	public static final String conceptDescriptionsAppPathComponent = appPathComponent + conceptAPIsPathComponent +descriptionsComponent;
	public static final String conceptCreateAppPathComponent = writePathComponent + conceptAPIsPathComponent + createPathComponent;
	public static final String conceptActivateAppPathComponent = writePathComponent + conceptAPIsPathComponent + updatePathComponent + activateComponent;
	public static final String conceptDeactivateAppPathComponent = writePathComponent + conceptAPIsPathComponent + updatePathComponent + deactivateComponent;
	public static final String conceptUpdateStateAppPathComponent = writePathComponent + conceptAPIsPathComponent + updatePathComponent + updateStateComponent;

	
	public static final String sememeAPIsPathComponent = apiVersionComponent + "sememe/";
	public static final String sememeChronologyAppPathComponent = appPathComponent + sememeAPIsPathComponent + chronologyComponent;
	public static final String sememeVersionsAppPathComponent = appPathComponent + sememeAPIsPathComponent + versionsComponent;
	public static final String sememeVersionAppPathComponent = appPathComponent + sememeAPIsPathComponent +versionComponent;
	public static final String sememeByAssemblageAppPathComponent = appPathComponent + sememeAPIsPathComponent +byAssemblageComponent;
	public static final String sememeUpdateStatePathComponent = writePathComponent + sememeAPIsPathComponent + updatePathComponent + updateStateComponent;

	public static final String descriptionCreatePathComponent = writePathComponent + sememeAPIsPathComponent + descriptionComponent + createPathComponent;
	public static final String descriptionUpdatePathComponent = writePathComponent + sememeAPIsPathComponent + descriptionComponent + updatePathComponent;
	public static final String descriptionDeactivatePathComponent = writePathComponent + sememeAPIsPathComponent + descriptionComponent + deactivateComponent;
	public static final String descriptionActivatePathComponent = writePathComponent + sememeAPIsPathComponent + descriptionComponent + activateComponent;
	
	public static final String searchComponent = "search/";
	public static final String searchAPIsPathComponent = apiVersionComponent + searchComponent;
	public static final String searchAppPathComponent = appPathComponent + searchAPIsPathComponent;
	
	public static final String taxonomyAPIsPathComponent = apiVersionComponent + "taxonomy/";
	
	public static final String idComponent = "id/";
	public static final String idAPIsPathComponent = apiVersionComponent + idComponent;
	public static final String idAppPathComponent = appPathComponent + apiVersionComponent + idComponent;
	
	public static final String systemAPIsPathComponent = apiVersionComponent + "system/";
	public static final String commentAPIsPathComponent = apiVersionComponent + "comment/";
	public static final String contentRequestAPIsPathComponent = apiVersionComponent + "request/";

	public static final String coordinateAPIsPathComponent = apiVersionComponent + "coordinate/";
	public static final String coordinatesComponent = "coordinates/";
	public static final String coordinatesTokenComponent = "coordinatesToken/";
	public static final String taxonomyCoordinatePathComponent = "taxonomyCoordinate/";
	public static final String languageCoordinatePathComponent = "languageCoordinate/";
	public static final String stampCoordinatePathComponent = "stampCoordinate/";
	public static final String logicCoordinatePathComponent = "logicCoordinate/";
	public static final String editTokenComponent = "editToken/";

	public static final String logicGraphAPIsPathComponent = apiVersionComponent + "logicGraph/";
	public static final String logicGraphVersionAppPathComponent = appPathComponent + logicGraphAPIsPathComponent + versionComponent;
	public static final String logicGraphChronologyAppPathComponent = appPathComponent + logicGraphAPIsPathComponent + chronologyComponent;

	public static final String commentCreatePathComponent = writePathComponent + commentAPIsPathComponent + createPathComponent;
	public static final String commentUpdatePathComponent = writePathComponent + commentAPIsPathComponent + updatePathComponent;
	public static final String commentVersionPathComponent = commentAPIsPathComponent + versionComponent;
	public static final String commentVersionByReferencedComponentPathComponent = commentAPIsPathComponent + versionComponent + byReferencedComponentComponent;
}
