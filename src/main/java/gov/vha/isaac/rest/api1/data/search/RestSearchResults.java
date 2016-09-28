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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
@XmlRootElement(name = "restSearchResults")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
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
	List<RestSearchResult> results = new ArrayList<>();

	protected RestSearchResults()
	{
		//For jaxb
	}

	/**
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param approximateTotal approximate size of full matching set of which this paginated result is a subset
	 * @param baseUrl url used to construct example previous and next urls
	 * @param results list of RestSearchResult
	 * @throws RestException 
	 */
	public RestSearchResults(int pageNum, int maxPageSize, int approximateTotal, String baseUrl, List<RestSearchResult> results) throws RestException {
		this.results.addAll(results);
		this.paginationData = new Pagination(pageNum, maxPageSize, approximateTotal, baseUrl);
	}

	/**
	 * @return the results
	 */
	@XmlTransient
	public List<RestSearchResult> getResults() {
		return Collections.unmodifiableList(results);
	}
}