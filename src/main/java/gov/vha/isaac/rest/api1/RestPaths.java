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

	public static final String chronologyComponent = "chronology/";
	public static final String versionsComponent = "versions/";	
	public static final String versionComponent = "version/";
	public static final String objectChronologyTypeComponent = "objectChronologyType/";
	public static final String sememeTypeComponent = "sememeType/";
	public static final String idTranslateComponent = "translate/";
	public static final String idTypesComponent = "types/";
	public static final String descriptionsComponent = "descriptions/";
	public static final String sememesComponent = "sememes/";
	public static final String prefixComponent = "prefix/";
	public static final String byAssemblageComponent = "byAssemblage/";
	public static final String byReferencedComponentComponent = "byReferencedComponent/";
	public static final String sememeDefinitionComponent = "sememeDefinition/";
	public static final String systemInfoComponent = "systemInfo/";
	public static final String termRequestComponent = "termRequest/";
	
	public static final String enumerationComponent = "enumeration/";
	public static final String enumerationRestDynamicSememeDataTypeComponent = enumerationComponent + "restDynamicSememeDataType/";
	public static final String enumerationRestDynamicSememeValidatorTypeComponent = enumerationComponent + "restDynamicSememeValidatorType/";
	public static final String enumerationRestObjectChronologyTypeComponent = enumerationComponent + "restObjectChronologyType/";
	public static final String enumerationRestSememeTypeComponent = enumerationComponent + "restSememeType/";
	public static final String enumerationRestConcreteDomainOperatorTypes = enumerationComponent + "restConcreteDomainOperatorTypes/";
	public static final String enumerationRestNodeSemanticTypes = enumerationComponent + "restNodeSemanticType/";
	public static final String enumerationRestSupportedIdTypes = enumerationComponent + "restSupportedIdTypes/";
	
	public static final String conceptPathComponent = apiVersionComponent + "concept/";
	public static final String conceptChronologyAppPathComponent = appPathComponent + conceptPathComponent + chronologyComponent;
	public static final String conceptVersionsAppPathComponent = appPathComponent + conceptPathComponent + versionsComponent;
	public static final String conceptVersionAppPathComponent = appPathComponent + conceptPathComponent +versionComponent;
	public static final String conceptDescriptionsAppPathComponent = appPathComponent + conceptPathComponent +descriptionsComponent;

	public static final String sememePathComponent = apiVersionComponent + "sememe/";
	public static final String sememeChronologyAppPathComponent = appPathComponent + sememePathComponent + chronologyComponent;
	public static final String sememeVersionsAppPathComponent = appPathComponent + sememePathComponent + versionsComponent;
	public static final String sememeVersionAppPathComponent = appPathComponent + sememePathComponent +versionComponent;
	public static final String sememeByAssemblageAppPathComponent = appPathComponent + sememePathComponent +byAssemblageComponent;

	
	public static final String searchPathComponent = apiVersionComponent + "search/";
	public static final String searchAppPathComponent = appPathComponent + searchPathComponent;
	
	public static final String taxonomyPathComponent = apiVersionComponent + "taxonomy/";
	
	public static final String idPathComponent = apiVersionComponent + "id/";
	public static final String idAppPathComponent = appPathComponent + apiVersionComponent + "id/";
	
	public static final String systemPathComponent = apiVersionComponent + "system/";
	public static final String requestPathComponent = apiVersionComponent + "request/";

	public static final String logicGraphPathComponent = apiVersionComponent + "logicGraph/";
	public static final String logicGraphVersionAppPathComponent = appPathComponent + logicGraphPathComponent + versionComponent;
	public static final String logicGraphChronologyAppPathComponent = appPathComponent + logicGraphPathComponent + chronologyComponent;
}
