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
package gov.vha.isaac.rest.api.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link Pagination}
 * 
 * Carries data for paginating result sets and calculates and creates example previous and next page URLs
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class Pagination
{
	/**
	 * Link to retrieve previous result set page
	 */
	@XmlElement
	String previousUrl;
	/**
	 * Link to retrieve next result set page
	 */
	@XmlElement
	String nextUrl;
	
	/**
	 * The page (of maximum size pageSize) number from beginning of dataset starting at 1
	 */
	@XmlElement
	int pageNum;
	/**
	 * The page max size.  Must be an integer >= 0
	 */
	@XmlElement
	int maxPageSize;

	/**
	 * Estimated size of set of all matching values of which the current page is a subset. Value is negative if and only if unknown. May be affected by filtering.
	 */
	@XmlElement
	int total;
	
	/**
	 * Size of returned set
	 */
	@XmlElement
	int size;

	protected Pagination()
	{
		//For jaxb
	}

	/**
	 * @param pageNum page number index > 0
	 * @param maxPageSize The maximum number of results to return per page, must be greater than 0
	 * @param estimated total size of set of which this page is a subset. May be affected by filtering.
	 * @param size actual size of this page
	 * @param baseUrl base URL used to construct and return example previous and next URLs
	 * @throws RestException 
	 */
	public Pagination(int pageNum, int maxPageSize, int total, int size, String baseUrl) throws RestException {
		PaginationUtils.validateParameters(pageNum, maxPageSize);

		this.maxPageSize = maxPageSize;
		this.pageNum = pageNum;
		this.size = size;

		boolean baseUrlHasParams = baseUrl.contains("?");
		
		int previousPageNum = 0;
		int previousPageSize = 0;
		if (this.pageNum == 1) {
			// At beginning
			previousPageNum = 1;
			previousPageSize = 0;
		} else if ((this.pageNum - 1) * this.maxPageSize <= total || total < 0) {
			// Within first chunk
			previousPageNum = this.pageNum - 1;
			previousPageSize = this.maxPageSize;
		} else {
			// Somewhere in the middle
			previousPageNum = this.pageNum - 1;
			previousPageSize = this.maxPageSize;
		}
		this.previousUrl = baseUrl + (baseUrlHasParams ? "&" : "?") + RequestParameters.pageNum + "=" + previousPageNum + "&" + RequestParameters.maxPageSize + "=" + previousPageSize;
		
		int nextPageNum = 0;
		int nextPageSize = 0;
		if (total < 0) {
			// If total < 0 then no known limit
			nextPageNum = this.pageNum + 1;
			nextPageSize = this.maxPageSize;
			this.total = -1; // total unknown
		} else {
			this.total = total; // total unknown
			if ((this.pageNum * this.maxPageSize) >= total) {
				// Current result contains or is past end of results
				nextPageNum = this.pageNum;
				nextPageSize = 0;
			} else if (((this.pageNum + 1) * this.maxPageSize) >= total) {
				// Next result contains end of results
				nextPageNum = this.pageNum + 1;
				nextPageSize = total - (this.pageNum * this.maxPageSize);
			} else {
				// Somewhere near beginning or middle
				nextPageNum = this.pageNum + 1;
				nextPageSize = this.maxPageSize;
			}
		}
		this.nextUrl = baseUrl + (baseUrlHasParams ? "&" : "?") + RequestParameters.pageNum + "=" + nextPageNum + "&" + RequestParameters.maxPageSize + "=" + nextPageSize;
	}
}