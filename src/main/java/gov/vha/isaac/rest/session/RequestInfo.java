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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.rest.ExpandUtil;
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
	private static Logger log = LogManager.getLogger();

	private String coordinatesToken_ = null;

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
	
	private static <E extends Enum<E>> byte[] byteArrayFromEnumSet(EnumSet<E> set) {
		byte[] returnValue = new byte[set.size()];
		int index = 0;
		for (Iterator<E> it = set.iterator(); it.hasNext();) {
			returnValue[index++] = (byte)it.next().ordinal();
		}
		
		return returnValue;
	}
	//public RequestInfo readCoordinatesToken()
	public RequestInfo readAll(Map<String, List<String>> parameters) throws RestException
	{
		readExpandables(parameters);

		// Set RequestInfo coordinatesToken string to parameter value if set, otherwise set to default
		Optional<String> tokenStringFromParameters = CoordinatesUtil.getCoordinatesTokenStringFromParameters(parameters);
		Optional<CoordinatesToken> token = Optional.empty();
		if (tokenStringFromParameters.isPresent()) {
			try {
				CoordinatesTokens.put(coordinatesToken_ = tokenStringFromParameters.get());
				token = Optional.of(CoordinatesTokens.get(coordinatesToken_));
			} catch (Exception e) {
				log.warn("Failed creating CoordinatesToken from parameters. caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
				e.printStackTrace();
				throw new RestException(RequestParameters.coordToken, tokenStringFromParameters.get(), "caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			}
		} else {
			coordinatesToken_ = CoordinatesTokens.getDefaultCoordinatesTokenString();
			token = Optional.of(CoordinatesTokens.getDefaultCoordinatesTokenObject());
		}

		// Determine if any relevant coordinate parameters set
		Map<String,List<String>> coordinateParameters = new HashMap<>();
		coordinateParameters.putAll(CoordinatesUtil.getTaxonomyCoordinateParameters(parameters));
		coordinateParameters.putAll(CoordinatesUtil.getStampCoordinateParameters(parameters));
		coordinateParameters.putAll(CoordinatesUtil.getLanguageCoordinateParameters(parameters));
		coordinateParameters.putAll(CoordinatesUtil.getLogicCoordinateParameters(parameters));
		
		// If ANY relevant coordinate parameter values set, then calculate new CoordinatesToken string 
		if (coordinateParameters.size() > 0) {
			// TaxonomyCoordinate components
			boolean stated = CoordinatesUtil.getStatedFromParameter(coordinateParameters.get(RequestParameters.stated), token);
			
			// LanguageCoordinate components
			int langCoordLangSeq = CoordinatesUtil.getLanguageCoordinateLanguageSequenceFromParameter(coordinateParameters.get(RequestParameters.langCoordLang), token); 
			int[] langCoordDialectPrefs = CoordinatesUtil.getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.langCoordDialectsPref), token);
			int[] langCoordDescTypePrefs = CoordinatesUtil.getLanguageCoordinateDescriptionTypePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.langCoordDescTypesPref), token);

			// StampCoordinate components
			long stampTime = CoordinatesUtil.getStampCoordinateTimeFromParameter(coordinateParameters.get(RequestParameters.stampCoordTime), token); 
			int stampPathSeq = CoordinatesUtil.getStampCoordinatePathSequenceFromParameter(coordinateParameters.get(RequestParameters.stampCoordPath), token);
			StampPrecedence stampPrecedence = CoordinatesUtil.getStampCoordinatePrecedenceFromParameter(coordinateParameters.get(RequestParameters.stampCoordPrecedence), token);
			ConceptSequenceSet stampModules = CoordinatesUtil.getStampCoordinateModuleSequencesFromParameter(coordinateParameters.get(RequestParameters.stampCoordModules), token);
			EnumSet<State> stampAllowedStates = CoordinatesUtil.getStampCoordinateAllowedStatesFromParameter(coordinateParameters.get(RequestParameters.stampCoordStates), token);
			
			// LogicCoordinate components
			int logicStatedSeq = CoordinatesUtil.getLogicCoordinateStatedAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicCoordStated), token);
			int logicInferredSeq = CoordinatesUtil.getLogicCoordinateInferredAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicCoordInferred), token);
			int logicDescProfileSeq = CoordinatesUtil.getLogicCoordinateDescProfileAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicCoordDesc), token);
			int logicClassifierSeq = CoordinatesUtil.getLogicCoordinateClassifierAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicCoordClassifier), token);
			
			try {
				requestInfo.get().coordinatesToken_ = CoordinatesToken.get(
							stampTime,
							stampPathSeq,
							(byte)stampPrecedence.ordinal(),
							stampModules.asArray(),
							byteArrayFromEnumSet(stampAllowedStates),
							langCoordLangSeq,
							langCoordDialectPrefs,
							langCoordDescTypePrefs,
							(byte)(stated ? PremiseType.STATED : PremiseType.INFERRED).ordinal(),
							logicStatedSeq,
							logicInferredSeq,
							logicDescProfileSeq,
							logicClassifierSeq).serialize();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RestException("Failed setting CoordinatesToken from parameters. Caught " + e.getClass().getName() + " " + e.getLocalizedMessage());
			}
		}
		
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
			try {
				return CoordinatesTokens.get(coordinatesToken_);
			} catch (Exception e) {
				// Should never fail because validated on readAll()
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			coordinatesToken_ = CoordinatesTokens.getDefaultCoordinatesTokenString();
			return CoordinatesTokens.getDefaultCoordinatesTokenObject();
		}
	}
}
