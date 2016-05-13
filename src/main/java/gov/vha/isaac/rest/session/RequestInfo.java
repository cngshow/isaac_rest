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

import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.tokens.CoordinatesToken;
import gov.vha.isaac.rest.tokens.CoordinatesTokens;

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
	//private static Logger log = LogManager.getLogger();

	private CoordinatesToken coordinatesToken_ = null;

	private Set<String> expandablesForDirectExpansion_ = new HashSet<>(0);
	//Default to this, users may override by specifying expandables=true
	private boolean returnExpandableLinks_ = ApplicationConfig.getInstance().isDebugDeploy();
	
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

	public RequestInfo readExpandables(Map<String, List<String>> parameters) throws RestException
	{
		requestInfo.get().expandablesForDirectExpansion_ = new HashSet<>(10);
		if (parameters.containsKey(RequestParameters.expand)) {
			for (String expandable : RequestInfoUtils.expandCommaDelimitedElements(parameters.get(RequestParameters.expand))) {
				if (expandable != null) {
					requestInfo.get().expandablesForDirectExpansion_.add(expandable.trim());
				}
			}
		}
		if (parameters.containsKey(RequestParameters.expandables))
		{
			List<String> temp = parameters.get(RequestParameters.expandables);
			if (temp.size() > 0)
			{
				returnExpandableLinks_ = Boolean.parseBoolean(temp.get(0).trim());
			}
		}
		return get();
	}

	public RequestInfo readAll(Map<String, List<String>> parameters) throws Exception
	{
		readExpandables(parameters);

		requestInfo.get().coordinatesToken_ = CoordinatesTokens.getOrCreate(parameters);

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
		return getCoordinatesToken().getTaxonomyCoordinate().getStampCoordinate();
	}

	/**
	 * @return
	 */
	public LanguageCoordinate getLanguageCoordinate()
	{
		return getCoordinatesToken().getTaxonomyCoordinate().getLanguageCoordinate();
	}
	
	/**
	 * @return
	 */
	public LogicCoordinate getLogicCoordinate()
	{
		return getCoordinatesToken().getTaxonomyCoordinate().getLogicCoordinate();
	}

	/**
	 * @return
	 */
	public TaxonomyCoordinate getTaxonomyCoordinate()
	{
		return getCoordinatesToken().getTaxonomyCoordinate();
	}
	/**
	 * @return
	 */
	public TaxonomyCoordinate getTaxonomyCoordinate(boolean stated)
	{
		if (stated)
		{
			return getTaxonomyCoordinate().getTaxonomyType() == PremiseType.STATED ? getTaxonomyCoordinate() : getTaxonomyCoordinate().makeAnalog(PremiseType.STATED);
		}
		else // (! stated)
		{
			return getTaxonomyCoordinate().getTaxonomyType() == PremiseType.INFERRED ? getTaxonomyCoordinate() : getTaxonomyCoordinate().makeAnalog(PremiseType.INFERRED);
		}
	}

	/**
	 * @return
	 */
	public boolean useFsn()
	{
		return getLanguageCoordinate().isFSNPreferred();
	}
	
	/**
	 * @return
	 */
	public boolean getStated() {
		return getTaxonomyCoordinate().getTaxonomyType() == PremiseType.STATED;
	}
	
	/**
	 * @return CoordinatesToken created from existing coordinates
	 */
	public CoordinatesToken getCoordinatesToken() {
		if (coordinatesToken_ != null) {
			return coordinatesToken_;
		} else {
			return coordinatesToken_ = CoordinatesTokens.getDefaultCoordinatesToken();
		}
	}
}
