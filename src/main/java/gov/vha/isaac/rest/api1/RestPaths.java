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

	public static final String conceptPathComponent = apiVersionComponent + "concept/";
	public static final String conceptAppPathComponent = appPathComponent + conceptPathComponent;
	
	public static final String conceptChronologyComponent = "chronology/";
	public static final String conceptChronologyAppPathComponent = appPathComponent + conceptPathComponent + conceptChronologyComponent;
	
	public static final String conceptVersionsComponent = "versions/";
	public static final String conceptVersionsAppPathComponent = appPathComponent + conceptPathComponent + conceptVersionsComponent;
	
	public static final String conceptVersionComponent = "version/";
	public static final String conceptVersionAppPathComponent = appPathComponent + conceptPathComponent +conceptVersionComponent;
	
	public static final String searchPathComponent = apiVersionComponent + "search/";
	public static final String searchAppPathComponent = appPathComponent + searchPathComponent;
	
	public static final String taxonomyPathComponent = apiVersionComponent + "taxonomy/";
}
