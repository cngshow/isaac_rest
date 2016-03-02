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
package gov.vha.isaac.rest;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * 
 * {@link ExpandUtil}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class ExpandUtil
{
	public static final String versionsAllExpandable = "versionsAll";
	public static final String versionsLatestOnlyExpandable = "versionsLatestOnly";
	public static final String versionExpandable = "version";
	public static final String chronologyExpandable = "chronology";
	public static final String parentsExpandable = "parents";
	public static final String childrenExpandable = "children";
	public static final String nestedSememesExpandable = "nestedSememes";
	public static final String logicNodeUuidsExpandable = "logicNodeUuids";
	public static final String logicNodeConceptVersionsExpandable = "logicNodeConceptVersions";
	
	public static Set<String> read(String expandableString)
	{
		if (StringUtils.isNotBlank(expandableString))
		{
			HashSet<String> expandables = new HashSet<>(expandableString.length() / 10);
			for (String s : expandableString.trim().split(","))
			{
				if (StringUtils.isNotBlank(s))
				{
					expandables.add(s.trim());
				}
				
			}
			return expandables;
		}
		return new HashSet<>(0);
	}
}
