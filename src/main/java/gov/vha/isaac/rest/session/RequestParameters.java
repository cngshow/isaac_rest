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

	// CoordinatesToken
	public final static String coordToken = "coordToken";

	// Taxonomy Coordinate
	public final static String stated = "stated";
	public final static String statedDefault = "true";

	// Language Coordinate
	public static enum LanguageCoordinateParamNames {
		langCoordLang,
		langCoordDialectsPref,
		langCoordDescTypesPref
	};
	public final static String langCoordLang = LanguageCoordinateParamNames.langCoordLang.name();
	public final static String langCoordLangDefault = "english";
	public final static String langCoordDialectsPref = LanguageCoordinateParamNames.langCoordDialectsPref.name();
	public final static String langCoordDialectsPrefDefault = "us,gb";
	public final static String langCoordDescTypesPref = LanguageCoordinateParamNames.langCoordDescTypesPref.name();
	public final static String langCoordDescTypesPrefDefault = "fsn,synonym";
	
	// Stamp Coordinate
	public static enum StampCoordinateParamNames {
		stampCoordTime,
		stampCoordPath,
		stampCoordPrecedence,
		stampCoordModules,
		stampCoordStates
	};
	public final static String stampCoordTime = StampCoordinateParamNames.stampCoordTime.name();
	public final static String stampCoordTimeDefault = "latest";
	public final static String stampCoordPath = StampCoordinateParamNames.stampCoordPath.name();
	public final static String stampCoordPathDefault = "development";
	public final static String stampCoordPrecedence = StampCoordinateParamNames.stampCoordPrecedence.name();
	public final static String stampCoordPrecedenceDefault = "path";
	public final static String stampCoordModules = StampCoordinateParamNames.stampCoordModules.name();
	public final static String stampCoordModulesDefault = "";
	public final static String stampCoordStates = StampCoordinateParamNames.stampCoordStates.name();
	public final static String stampCoordStatesDefault = "active";

	// Logic Coordinate
	public static enum LogicCoordinateParamNames {
		logicCoordStated,
		logicCoordInferred,
		logicCoordDesc,
		logicCoordClassifier
	};
	public final static String logicCoordStated = LogicCoordinateParamNames.logicCoordStated.name();
	public final static String logicCoordStatedDefault = "1f201994-960e-11e5-8994-feff819cdc9f";
	public final static String logicCoordInferred = LogicCoordinateParamNames.logicCoordInferred.name();
	public final static String logicCoordInferredDefault = "1f20182c-960e-11e5-8994-feff819cdc9f";
	public final static String logicCoordDesc = LogicCoordinateParamNames.logicCoordDesc.name();
	public final static String logicCoordDescDefault = "1f201e12-960e-11e5-8994-feff819cdc9f";
	public final static String logicCoordClassifier = LogicCoordinateParamNames.logicCoordClassifier.name();
	public final static String logicCoordClassifierDefault = "1f201fac-960e-11e5-8994-feff819cdc9f";

	public final static String id = "id";
	public final static String nid = "nid";
	public final static String expand = "expand";

	public final static String useFsn = "useFsn";
	public final static String useFsnDefault = "true";

	public final static String pageNum = "pageNum";
	public final static String pageNumDefault = "1";

	public final static String maxPageSize = "maxPageSize";
	public final static String maxPageSizeDefault = "10";

	public final static String assemblage = "assemblage";
	public final static String includeDescriptions = "includeDescriptions";
	public final static String includeAttributes = "includeAttributes";
	public final static String includeAttributesDefault = "true";

	public final static String query = "query";
	public final static String treatAsString = "treatAsString";
	public final static String descriptionType = "descriptionType";
	public final static String extendedDescriptionTypeId = "extendedDescriptionTypeId";
	public final static String dynamicSememeColumns = "dynamicSememeColumns";
	public final static String sememeAssemblageId = "sememeAssemblageId";

}
