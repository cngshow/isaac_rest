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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mahout.math.Arrays;

import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RequestParameters}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 * All parameters should be added to ALL_VALID_PARAMETERS, whether grouped or individually
 * 
 */
public class RequestParameters {
	private RequestParameters() {}

	// CoordinatesToken
	public final static String coordToken = "coordToken";
	
	// CoordinatesToken
	public final static String editToken = "editToken";

	// Taxonomy Coordinate
	public final static String stated = "stated";

	// Language Coordinate
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

	// Logic Coordinate
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
	
	// All Coordinates
	public final static Set<String> COORDINATE_PARAM_NAMES;
	static {
		Set<String> params = new HashSet<>();
		params.add(coordToken);
		params.add(stated);
		params.addAll(LANGUAGE_COORDINATE_PARAM_NAMES);
		params.addAll(STAMP_COORDINATE_PARAM_NAMES);
		params.addAll(LOGIC_COORDINATE_PARAM_NAMES);
		
		COORDINATE_PARAM_NAMES = Collections.unmodifiableSet(params);
	}

	public final static String id = "id";
	public final static String nid = "nid";
	
	// Expandables
	public final static String expand = "expand";
	public final static String expandables = "expandables";
	public final static Set<String> EXPANDABLES_PARAM_NAMES = unmodifiableSet(expand, expandables);

	// Pagination
	public final static String pageNum = "pageNum";
	public final static String pageNumDefault = "1";
	public final static String maxPageSize = "maxPageSize";
	public final static String maxPageSizeDefault = "10";
	public final static Set<String> PAGINATION_PARAM_NAMES = unmodifiableSet(pageNum, maxPageSize);
	
	// Comment
	public final static String commentContext = "commentContext";
	public final static String commentText = "commentText";
	public final static Set<String> COMMENT_PARAM_NAMES = unmodifiableSet(commentContext, commentText);
	
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
	
	// Taxonomy
	public final static String childDepth = "childDepth";
	public final static String parentHeight = "parentHeight";
	public final static String countParents = "countParents";
	public final static String countChildren = "countChildren";
	public final static String sememeMembership = "sememeMembership";
	
	// Concept
	public final static String includeParents = "includeParents";
	public final static String includeChildren = "includeChildren";
	
	// IdAPIs
	public final static String inputType = "inputType";
	public final static String outputType = "outputType";

	public final static String state = "state";

	// Workflow
	public final static String wfUser = "wfUser"; // int
	public final static String wfState = "wfState"; // String i.e. "Reject Review", "Ready for Review", "Reject Edit"...
	public final static String wfProcess = "wfProcess";
	public final static String wfRole = "wfRole"; // String i.e. "Approver", "Reviewer"
	
	/**
	 * Set of all known parameters usable to detect malformed or incorrect parameters
	 */
	public final static Set<String> ALL_VALID_PARAMETERS;
	static {
		Set<String> params = new HashSet<>();
		params.addAll(COORDINATE_PARAM_NAMES);
		params.addAll(EXPANDABLES_PARAM_NAMES);
		params.addAll(PAGINATION_PARAM_NAMES);
		params.addAll(COMMENT_PARAM_NAMES);
		params.addAll(unmodifiableSet(
			id,
			nid,

			assemblage,
			includeDescriptions,
			includeAttributes,

			query,
			treatAsString,
			descriptionType,
			extendedDescriptionTypeId,
			dynamicSememeColumns,
			sememeAssemblageId,

			// Taxonomy
			childDepth,
			parentHeight,
			countParents,

			countChildren,
			sememeMembership,
			
			// Concept
			includeParents,
			includeChildren,
			
			//IdAPIs
			inputType,
			outputType,
			
			state
			));
		ALL_VALID_PARAMETERS = params;
	}

	// Parameter default constants
	public final static String ISAAC_ROOT_UUID = "7c21b6c5-cf11-5af9-893b-743f004c97f5";

	/**
	 * This should only be modified for testing purposes.  Otherwise should always be IGNORE_CASE_VALIDATING_PARAM_NAMES_DEFAULT
	 * 
	 * Changing this to ignore case will probably break lots of things,
	 * as most comparisons do not ignore case, as currenlt coded
	 */
	public final static boolean IGNORE_CASE_VALIDATING_PARAM_NAMES_DEFAULT = false;
	public static boolean IGNORE_CASE_VALIDATING_PARAM_NAMES = IGNORE_CASE_VALIDATING_PARAM_NAMES_DEFAULT;

	/**
	 * @param parameters
	 * @param supportedParameterNames
	 * @throws RestException
	 * 
	 * This method validates the context request parameters against passed valid parameters
	 * It takes multiple parameter types in order to allow passing the constant parameter sets
	 * from RequestParameters as well as any individual parameters passed in specific methods
	 */
	public final static void validateParameterNamesAgainstSupportedNames(Map<String, List<String>> parameters, Object...supportedParameterNames) throws RestException {
		Set<String> supportedParameterNamesSet = new HashSet<>();
		if (supportedParameterNames != null && supportedParameterNames.length > 0) {
			for (Object parameter : supportedParameterNames) {
				if (parameter instanceof Iterable) {
					for (Object obj : (Iterable<?>)parameter) {
						supportedParameterNamesSet.add(obj.toString());
					}
				} else if (parameter.getClass().isArray()) {
					for (Object obj : (Object[])parameter) {
						supportedParameterNamesSet.add(obj.toString());
					}
				} else {
					supportedParameterNamesSet.add(parameter.toString());
				}
			}
		}
		for (String parameterName : parameters.keySet()) {
			String parameterNameToCompare = IGNORE_CASE_VALIDATING_PARAM_NAMES ? parameterName.toUpperCase() : parameterName;
			boolean foundMatch = false;
			for (String supportedParameterName : supportedParameterNamesSet) {
				String supportedParameterNameToCompare = IGNORE_CASE_VALIDATING_PARAM_NAMES ? supportedParameterName.toUpperCase() : supportedParameterName;
				if (supportedParameterNameToCompare.equals(parameterNameToCompare)) {
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch) {
				throw new RestException(parameterName, Arrays.toString(parameters.get(parameterName).toArray()), "Invalid or unsupported parameter name.  Must be one of " + Arrays.toString(supportedParameterNamesSet.toArray(new String[supportedParameterNamesSet.size()])));
			}
		}
	}
	private final static <T> Set<T> unmodifiableSet(@SuppressWarnings("unchecked") T...elements) {
		Set<T> list = new HashSet<>(elements.length);
		for (T element : elements) {
			list.add(element);
		}
		return Collections.unmodifiableSet(list);
	}
}
