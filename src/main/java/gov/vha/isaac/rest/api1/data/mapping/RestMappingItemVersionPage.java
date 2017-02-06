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
package gov.vha.isaac.rest.api1.data.mapping;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api.data.Pagination;
import gov.vha.isaac.rest.api.exceptions.RestException;


/**
 * {@link RestMappingItemVersionPage}
 * 
 * This class carries back result sets in a way that allows pagination
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingItemVersionPage
{
	
	/**
	 * Link to retrieve current page
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Pagination paginationData;

	/**
	 * The contained results
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestMappingItemVersion[] results;

	protected RestMappingItemVersionPage()
	{
		//For jaxb
	}

	/**
	 * @param pageNum The pagination page number >= 1 to return
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param approximateTotal approximate size of full matching set of which this paginated result is a subset
	 * @param baseUrl url used to construct example previous and next urls
	 * @param results
	 * @throws RestException 
	 */
	public RestMappingItemVersionPage(
			int pageNum,
			int maxPageSize,
			int approximateTotal,
			boolean hasMoreData,
			boolean totalIsExact, String baseUrl, 
			RestMappingItemVersion[] results) throws RestException {
		this.results = results;
		this.paginationData = new Pagination(pageNum, maxPageSize, approximateTotal, totalIsExact, hasMoreData, baseUrl);
	}
	/**
	 * @param results
	 * @throws RestException 
	 */
	public RestMappingItemVersionPage(RestMappingItemVersion[] results) throws RestException {
		this.results = results;
		this.paginationData = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingItemVersionPage [results=" + results + "]";
	}
}