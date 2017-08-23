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

package gov.vha.isaac.rest.session.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mahout.math.Arrays;

/**
 * 
 * {@link RestWriteRequestSynchronizingFilter}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@WebFilter(filterName="restWriteRequestSynchronizingFilter", urlPatterns="/*") // TODO test this
public class RestWriteRequestSynchronizingFilter implements Filter {
	private final static Logger LOG = LogManager.getLogger(RestWriteRequestSynchronizingFilter.class);
	
	private final static Object OBJECT = new Object();
	/**
	 * 
	 */
	public RestWriteRequestSynchronizingFilter() {
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		LOG.debug("{} initialized", getClass().getSimpleName());
	}

	private static String toString(Map<String, String[]> parameterMap) {
		Map<String, String> map = new HashMap<>();
		for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
			map.put(entry.getKey(), Arrays.toString(entry.getValue()));
		}
		
		return map.toString();
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String parameters = toString(request.getParameterMap());
		if (request instanceof HttpServletRequest) {
			String uri = ((HttpServletRequest)request).getRequestURI().toString();

			if (uri.toLowerCase().contains("/write/")) {
				LOG.debug("{} handling write request for {} with params {}", getClass().getSimpleName(), uri, parameters);
				
				// This is a write API, so synchronize
				synchronized(OBJECT) {
					chain.doFilter(request, response);
				}
			} else {
				LOG.debug("{} handling read request for {} with params {}", getClass().getSimpleName(), uri, parameters);
				
				// This is a read API, so do not synchronize
				chain.doFilter(request, response);
			}
		} else {
			LOG.error("{}.doFilter() passed a {} not a HttpServletRequest, so cannot determine whether read or write request. Synchronizing as if write request. (params={})", this.getClass().getName(), request.getClass().getSimpleName(), parameters);

			// Don't assume this is a read API, so synchronize
			synchronized(OBJECT) {
				chain.doFilter(request, response);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		LOG.debug("{} destroyed", getClass().getSimpleName());
	}
}
