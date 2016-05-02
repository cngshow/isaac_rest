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

package gov.vha.isaac.rest.session;

/**
 * 
 * {@link RequestParameters}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class RequestParameters {
	private RequestParameters() {}
	
	public final static String langCoordLang = "langCoordLang";
	public final static String langCoordLangDefault = "english";

	public final static String langCoordDialectsPref = "langCoordDialectsPref";
	public final static String langCoordDialectsPrefDefault = "us,gb";

	public final static String langCoordDescTypesPref = "langCoordDescTypesPref";
	public final static String langCoordDescTypesPrefDefault = "fsn,synonym";
	
	
	public final static String stampCoordTime = "stampCoordTime";
	public final static String stampCoordTimeDefault = "latest";

	public final static String stampCoordPath = "stampCoordPath";
	public final static String stampCoordPathDefault = "development";

	public final static String stampCoordPrecedence = "stampCoordPrecedence";
	public final static String stampCoordPrecedenceDefault = "path";
	
	public final static String stampCoordModules = "stampCoordModules";
	public final static String stampCoordModulesDefault = "";

	public final static String stampCoordStates = "stampCoordStates";
	public final static String stampCoordStatesDefault = "active";
	

	public final static String id = "id";
	public final static String nid = "nid";
	public final static String expand = "expand";

	public final static String stated = "stated";
	public final static String statedDefault = "true";

	public final static String useFsn = "useFsn";
	public final static String useFsnDefault = "true";

	public final static String pageNum = "pageNum";
	public final static String pageNumDefault = "1";

	public final static String maxPageSize = "maxPageSize";
	public final static String maxPageSizeDefault = "10";

	public final static String assemblage = "assemblage";
	public final static String includeDescriptions = "includeDescriptions";

	public final static String query = "query";
	public final static String treatAsString = "treatAsString";
	public final static String descriptionType = "descriptionType";
	public final static String extendedDescriptionTypeId = "extendedDescriptionTypeId";
	public final static String dynamicSememeColumns = "dynamicSememeColumns";
	public final static String sememeAssemblageId = "sememeAssemblageId";
}