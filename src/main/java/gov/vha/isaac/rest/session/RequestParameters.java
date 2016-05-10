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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

	public final static String language = "language";
	public final static String dialectPrefs = "dialectPrefs";
	public final static String descriptionTypePrefs = "descriptionTypePrefs";
	public final static Set<String> LANGUAGE_COORDINATE_PARAM_NAMES =
			unmodifiableSet(language, dialectPrefs, descriptionTypePrefs);

	// Stamp Coordinate
	public final static String time = "time";
	public final static String path = "path";
	public final static String precedence = "precedence";
	public final static String modules = "modules";
	public final static String allowedStates = "allowedStates";
	public final static Set<String> STAMP_COORDINATE_PARAM_NAMES =
			unmodifiableSet(
					time,
					path,
					precedence,
					modules,
					allowedStates);

	public final static String logicStatedAssemblage = "logicStatedAssemblage";
	public final static String logicInferredAssemblage = "logicInferredAssemblage";
	public final static String descriptionLogicProfile = "descriptionLogicProfile";
	public final static String classifier = "classifier";
	public final static Set<String> LOGIC_COORDINATE_PARAM_NAMES =
			unmodifiableSet(
					logicStatedAssemblage,
					logicInferredAssemblage,
					descriptionLogicProfile,
					classifier);

	public final static String id = "id";
	public final static String nid = "nid";
	public final static String expand = "expand";
	public final static String expandables = "expandables";

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
	
	private final static <T> Set<T> unmodifiableSet(@SuppressWarnings("unchecked") T...elements) {
		Set<T> list = new HashSet<>(elements.length);
		for (T element : elements) {
			list.add(element);
		}
		return Collections.unmodifiableSet(list);
	}
}
