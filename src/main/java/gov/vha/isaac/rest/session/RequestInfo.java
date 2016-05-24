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
	private static Logger log = LogManager.getLogger();

	private String coordinatesToken_ = null;

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
	
	private static <E extends Enum<E>> byte[] byteArrayFromEnumSet(EnumSet<E> set) {
		byte[] returnValue = new byte[set.size()];
		int index = 0;
		for (Iterator<E> it = set.iterator(); it.hasNext();) {
			returnValue[index++] = (byte)it.next().ordinal();
		}
		
		return returnValue;
	}

	public RequestInfo readAll(Map<String, List<String>> parameters) throws Exception
	{
		readExpandables(parameters);

		String serializedTokenByParams = CoordinatesTokens.get(CoordinatesUtil.getCoordinateParameters(parameters));
		if (serializedTokenByParams != null) {
			log.debug("Using CoordinatesToken value cached by parameter");
			requestInfo.get().coordinatesToken_ = serializedTokenByParams;
		} else {
			log.debug("Constructing CoordinatesToken from parameters");
			
			// Set RequestInfo coordinatesToken string to parameter value if set, otherwise set to default
			Optional<CoordinatesToken> token = CoordinatesUtil.getCoordinatesTokenParameterTokenObjectValue(parameters);
			if (token.isPresent()) {
				log.debug("Applying CoordinatesToken " + RequestParameters.coordToken + " parameter \"" + token.get().getSerialized() + "\"");
				requestInfo.get().coordinatesToken_ = token.get().getSerialized();
			} else {
				log.debug("Applying default coordinates");

				requestInfo.get().coordinatesToken_ = CoordinatesTokens.getDefaultCoordinatesToken().getSerialized();
				token = Optional.of(CoordinatesTokens.getDefaultCoordinatesToken());
			}

			// Determine if any relevant coordinate parameters set
			Map<String,List<String>> coordinateParameters = new HashMap<>();
			coordinateParameters.putAll(CoordinatesUtil.getParametersSubset(parameters,
					RequestParameters.stated,
					RequestParameters.STAMP_COORDINATE_PARAM_NAMES,
					RequestParameters.LANGUAGE_COORDINATE_PARAM_NAMES,
					RequestParameters.LOGIC_COORDINATE_PARAM_NAMES));

			// If ANY relevant coordinate parameter values set, then calculate new CoordinatesToken string
			if (coordinateParameters.size() == 0) {
				log.debug("No individual coordinate parameters to apply to token \"" + requestInfo.get().coordinatesToken_ + "\"");

			} else { // if (coordinateParameters.size() > 0)
				log.debug("Applying {} individual parameters to coordinates token \"{}\": {}", requestInfo.get().coordinatesToken_, coordinateParameters.size(), coordinateParameters.toString());

				// TaxonomyCoordinate components
				boolean stated = CoordinatesUtil.getStatedFromParameter(coordinateParameters.get(RequestParameters.stated), token);

				// LanguageCoordinate components
				int langCoordLangSeq = CoordinatesUtil.getLanguageCoordinateLanguageSequenceFromParameter(coordinateParameters.get(RequestParameters.language), token); 
				int[] langCoordDialectPrefs = CoordinatesUtil.getLanguageCoordinateDialectAssemblagePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.dialectPrefs), token);
				int[] langCoordDescTypePrefs = CoordinatesUtil.getLanguageCoordinateDescriptionTypePreferenceSequencesFromParameter(coordinateParameters.get(RequestParameters.descriptionTypePrefs), token);

				// StampCoordinate components
				long stampTime = CoordinatesUtil.getStampCoordinateTimeFromParameter(coordinateParameters.get(RequestParameters.time), token); 
				int stampPathSeq = CoordinatesUtil.getStampCoordinatePathSequenceFromParameter(coordinateParameters.get(RequestParameters.path), token);
				StampPrecedence stampPrecedence = CoordinatesUtil.getStampCoordinatePrecedenceFromParameter(coordinateParameters.get(RequestParameters.precedence), token);
				ConceptSequenceSet stampModules = CoordinatesUtil.getStampCoordinateModuleSequencesFromParameter(coordinateParameters.get(RequestParameters.modules), token);
				EnumSet<State> stampAllowedStates = CoordinatesUtil.getStampCoordinateAllowedStatesFromParameter(coordinateParameters.get(RequestParameters.allowedStates), token);

				// LogicCoordinate components
				int logicStatedSeq = CoordinatesUtil.getLogicCoordinateStatedAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicStatedAssemblage), token);
				int logicInferredSeq = CoordinatesUtil.getLogicCoordinateInferredAssemblageFromParameter(coordinateParameters.get(RequestParameters.logicInferredAssemblage), token);
				int logicDescProfileSeq = CoordinatesUtil.getLogicCoordinateDescProfileAssemblageFromParameter(coordinateParameters.get(RequestParameters.descriptionLogicProfile), token);
				int logicClassifierSeq = CoordinatesUtil.getLogicCoordinateClassifierAssemblageFromParameter(coordinateParameters.get(RequestParameters.classifier), token);

				CoordinatesToken tokenObj = CoordinatesTokens.getOrCreate(
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
						logicClassifierSeq);

				requestInfo.get().coordinatesToken_ = tokenObj.getSerialized();

				CoordinatesTokens.put(CoordinatesUtil.getCoordinateParameters(parameters), tokenObj);
				
				log.debug("Created CoordinatesToken \"" + requestInfo.get().coordinatesToken_ + "\"");
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
				return CoordinatesTokens.getOrCreate(coordinatesToken_);
			} catch (Exception e) {
				// Should never fail because validated on readAll()
				log.error("Unexpected", e);
				throw new RuntimeException(e);
			}
		} else {
			coordinatesToken_ = CoordinatesTokens.getDefaultCoordinatesToken().getSerialized();
			return CoordinatesTokens.getDefaultCoordinatesToken();
		}
	}
}
