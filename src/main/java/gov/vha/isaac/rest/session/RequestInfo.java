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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * {@link RequestInfo}
 * This class is intended to hold a cache of global request info that we tie to the request / session being processed.
 * Things like the STAMP that applies, the expandable parameters, etc.
 * We will (likely) set this up on the thread local with a request filter that looks at every request before it arrives 
 * at the implementing method.
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RequestInfo
{
	private boolean stated_ = true;
	private StampCoordinate stampCoordinate_ = null;
	private LanguageCoordinate languageCoordinate_ = null;

	private Set<String> expandablesForDirectExpansion_ = new HashSet<>(0);
	private boolean returnExpandableLinks_ = true;  //implementations that know the API don't need to have these links returned to them - they can 
	//request these to be skipped in the replies, which will give them a performance boost.
	
	private static final ThreadLocal<RequestInfo> requestInfo = new ThreadLocal<RequestInfo>()
	{
		@Override
		protected RequestInfo initialValue()
		{
			return new RequestInfo();
		}
	};

	public static RequestInfo get()
	{
		return requestInfo.get();
	}

	private RequestInfo()
	{
	}
	
	public static void remove() {
		requestInfo.remove();
	}

	public RequestInfo readExpandables(String expandableString)
	{
		requestInfo.get().expandablesForDirectExpansion_ = ExpandUtil.read(expandableString);
		return get();
	}
	public RequestInfo readExpandables(Map<String, List<String>> parameters) throws RestException
	{
		requestInfo.get().expandablesForDirectExpansion_ = new HashSet<>();
		if (parameters.containsKey(RequestParameters.expand)) {
			for (String expandable : RequestInfoUtils.expandCommaDelimitedElements(parameters.get(RequestParameters.expand))) {
				if (expandable != null) {
					requestInfo.get().expandablesForDirectExpansion_.add(expandable.trim());
				}
			}
		}
		return get();
	}
	public RequestInfo readStampCoordinate(Map<String, List<String>> parameters) throws RestException
	{
		requestInfo.get().stampCoordinate_ = CoordinatesUtil.getStampCoordinateFromParameters(parameters);
		return get();
	}
	public RequestInfo readLanguageCoordinate(Map<String, List<String>> parameters) throws RestException
	{
		requestInfo.get().languageCoordinate_ = CoordinatesUtil.getLanguageCoordinateFromParameters(parameters);
		return get();
	}
	
	public RequestInfo readStated(String statedParameter) throws RestException {
		if (StringUtils.isNotBlank(statedParameter)) {
			requestInfo.get().stated_ = RequestInfoUtils.parseBooleanParameter(RequestParameters.stated, statedParameter);
		} else {
			requestInfo.get().stated_ = RequestInfoUtils.parseBooleanParameter(RequestParameters.stated, RequestParameters.statedDefault);
		}
		return get();
	}
	public RequestInfo readStated(Map<String, List<String>> parameters) throws RestException
	{
		if (parameters != null && parameters.get(RequestParameters.stated) != null && parameters.get(RequestParameters.stated).size() > 0) {
			requestInfo.get().stated_ = RequestInfoUtils.getBooleanFromParameters(RequestParameters.stated, parameters);
		} else {
			requestInfo.get().stated_ = RequestInfoUtils.parseBooleanParameter(RequestParameters.stated, RequestParameters.statedDefault);
		}
		return get();
	}
	public RequestInfo readAll(Map<String, List<String>> parameters) throws RestException
	{
		readExpandables(parameters);
		readStampCoordinate(parameters);
		readLanguageCoordinate(parameters);
		readStated(parameters);
		
		return requestInfo.get();
	}
	
	public boolean shouldExpand(String expandable)
	{
		return expandablesForDirectExpansion_.contains(expandable);
	}
	
	public boolean returnExpandableLinks()
	{
		return returnExpandableLinks_;
	}

	/**
	 * @return
	 */
	public StampCoordinate getStampCoordinate()
	{
		if (stampCoordinate_ != null) {
			return stampCoordinate_;
		} else {
			return stampCoordinate_ = StampCoordinates.getDevelopmentLatest();
		}
	}

	/**
	 * @return
	 */
	public LanguageCoordinate getLanguageCoordinate()
	{
		if (languageCoordinate_ != null) {
			return languageCoordinate_;
		} else {
			return languageCoordinate_ = LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
		}
	}

	/**
	 * @return
	 */
	public TaxonomyCoordinate getTaxonomyCoordinate(boolean stated)
	{
		if (stated)
		{
			return TaxonomyCoordinates.getStatedTaxonomyCoordinate(getStampCoordinate(), getLanguageCoordinate());
		}
		else
		{
			return TaxonomyCoordinates.getInferredTaxonomyCoordinate(getStampCoordinate(), getLanguageCoordinate());
		}
	}

	/**
	 * @return
	 */
	public boolean useFSN()
	{
		//TODO Joel, this needs to be implemented....
		return true;
	}
	
	/**
	 * @return
	 */
	public boolean getStated() {
		return stated_;
	}
}
