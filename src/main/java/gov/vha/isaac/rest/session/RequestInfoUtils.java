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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RequestInfoUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class RequestInfoUtils {
	private RequestInfoUtils() {}

	public static boolean parseBooleanParameter(String parameterName, String str) throws RestException {
		if (str == null || (! str.equalsIgnoreCase("false") && ! str.equalsIgnoreCase("true"))) {
			throw new RestException(parameterName, str, "invalid boolean parameter value");
		} else {
			return Boolean.parseBoolean(str);
		}
	}

	public static boolean getBooleanFromParameters(String parameterName, Map<String, List<String>> parameters) throws RestException {
		try {
			if (parameters.get(parameterName).size() != 1) {
				throw new RestException(parameterName, null, "invalid boolean parameter value");
			}
			return parseBooleanParameter(parameterName, parameters.get(parameterName).get(0));
		} catch (Exception e) {
			throw new RestException(parameterName, null, e.getLocalizedMessage());
		}
	}

	public static List<String> expandCommaDelimitedElements(String list) {
		List<String> expandedList = new ArrayList<>();
		
		if (list == null) {
			return null;
		}
		for (String s : list.trim().split(","))
		{
			if (StringUtils.isNotBlank(s))
			{
				expandedList.add(s.trim());
			}
		}
		
		return expandedList;
	}

	public static List<String> expandCommaDelimitedElements(List<String> list) {
		List<String> expandedList = new ArrayList<>();
	
		if (list == null) {
			return null;
		}
		for (String element : list) {
			if (element != null && element.contains(",")) {
				for (String s : element.trim().split(","))
				{
					if (StringUtils.isNotBlank(s))
					{
						expandedList.add(s.trim());
					}
				}
			} else {
				expandedList.add(element.trim());
			}
		}
		
		return expandedList;
	}
}
