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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api.data.Pagination;
import gov.vha.isaac.rest.api.exceptions.RestException;


/**
 * {@link RestSearchResults}
 * 
 * This class carries back result sets in a way that allows pagination
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSearchResults
{
	
	/**
	 * Link to retrieve current page
	 */
	@XmlElement
	Pagination paginationData;

	/**
	 * The contained results
	 */
	@XmlElement
	List<RestSearchResult> results = null;

	protected RestSearchResults()
	{
		//For jaxb
	}

	/**
	 * @param pageNum
	 * @param maxPageSize
	 * @param total
	 * @param baseUrl
	 * @param results
	 * @throws RestException 
	 */
	public RestSearchResults(int pageNum, int maxPageSize, int total, String baseUrl, List<RestSearchResult> results) throws RestException {
		this.results = results;
		this.paginationData = new Pagination(pageNum, maxPageSize, total, results.size(), baseUrl);
	}
}