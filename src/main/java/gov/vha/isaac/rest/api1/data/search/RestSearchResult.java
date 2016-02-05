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
package gov.vha.isaac.rest.api1.data.search;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * {@link RestSearchResult}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestSearchResult
{
	/**
	 * The internal identifier of the sememe that matched the query
	 */
	@XmlElement Integer matchNid;
	/**
	 * The text of the description that matched the query (may be blank, if the description is not available/active on the path used to populate this)
	 */
	@XmlElement String matchText;
	/**
	 * The Lucene Score for this result.  This value is only useful for ranking search results relative to other search results within the SAME QUERY 
	 * execution.  It may not be used to rank one query against another.
	 */
	@XmlElement float score;

	protected RestSearchResult()
	{
		//for Jaxb
	}

	public RestSearchResult(int matchNid, String matchText, float score)
	{
		this.matchNid = matchNid;
		this.matchText = matchText;
		this.score = score;
	}
}
