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
package gov.vha.isaac.rest.api1.session;

import java.util.HashSet;
import java.util.Set;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.configuration.TaxonomyCoordinates;
import gov.vha.isaac.rest.ExpandUtil;

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
	private Set<String> expandablesForDirectExpansion_;
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
	
	public static RequestInfo init(String expandables)
	{
		RequestInfo ri = new RequestInfo(expandables);
		requestInfo.set(ri);
		return get();
	}
	
	
	private RequestInfo()
	{
		expandablesForDirectExpansion_ = new HashSet<>(0);
	}
	
	private RequestInfo(String expandableString)
	{
		expandablesForDirectExpansion_ = ExpandUtil.read(expandableString);
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
		//TODO implement
		return StampCoordinates.getDevelopmentLatest();
	}

	/**
	 * @return
	 */
	public LanguageCoordinate getLanguageCoordinate()
	{
		//TODO implement
		return LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate();
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
		return true;
	}
}
