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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.LogicCoordinate;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.ochre.model.coordinate.EditCoordinateImpl;
import gov.vha.isaac.ochre.workflow.provider.WorkflowProvider;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.tokens.CoordinatesToken;
import gov.vha.isaac.rest.tokens.CoordinatesTokens;
import gov.vha.isaac.rest.tokens.EditToken;
import gov.vha.isaac.rest.tokens.EditTokens;

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

	private Map<String, List<String>> parameters_ = new HashMap<>();

	private String coordinatesToken_ = null;

	private EditToken editToken_ = null;
	//private Integer authorSequence;
	private EditCoordinate editCoordinate_ = null;

	//just a cache
	private static WorkflowProvider wfp_;

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
		if (parameters.containsKey(RequestParameters.returnExpandableLinks))
		{
			List<String> temp = parameters.get(RequestParameters.returnExpandableLinks);
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
		parameters_.clear();
		for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
			parameters_.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
		}

		readExpandables(parameters);

		String serializedCoordinatesTokenByParams = CoordinatesTokens.get(CoordinatesUtil.getCoordinateParameters(parameters));
		if (serializedCoordinatesTokenByParams != null) {
			log.debug("Using CoordinatesToken value cached by parameter");
			requestInfo.get().coordinatesToken_ = serializedCoordinatesTokenByParams;
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
			coordinateParameters.putAll(RequestInfoUtils.getParametersSubset(parameters,
					RequestParameters.COORDINATE_PARAM_NAMES));

			// If no coordinate parameter or only coordToken value set, then use
			if (coordinateParameters.size() == 0 || (coordinateParameters.size() == 1 && coordinateParameters.containsKey(RequestParameters.coordToken))) {
				log.debug("No individual coordinate parameters to apply to token \"" + requestInfo.get().coordinatesToken_ + "\"");

			} else { // If ANY coordinate parameter other than coordToken value set, then calculate new CoordinatesToken string
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
	 * @return parameters Map
	 */
	public Map<String, List<String>> getParameters() {
		return Collections.unmodifiableMap(parameters_);
	}

	/**
	 * @return the stamp coordinate as requested by the user.
	 */
	//TODO nearly every usage of this call, needs to be redone to utilize the Util.getPreWorkflowStampCoordinate call.
	//Anything else, leads to an incomplete return based on the requested stamps.
	//Until we actually put workflow in, however, it won't matter..
	public StampCoordinate getStampCoordinate()
	{
		return getCoordinatesToken().getTaxonomyCoordinate().getStampCoordinate();
	}

	/**
	 * Lazily create, cache and return an EditCoordinate
	 *
	 * @return EditToken
	 */
	public EditToken getEditToken() {
		if (editToken_ == null) {
			try {
				EditToken editToken = null;

				Integer module = null;
				Integer path = null;
				UUID workflowProcessid = null;

				EditCoordinate defaultEditCoordinate = EditCoordinates.getDefaultUserSolorOverlay();
				
				// Set default EditToken parameters to values in passedEditToken if set, otherwise set to default
				Optional<EditToken> passedEditToken = EditTokens.getEditTokenParameterTokenObjectValue(parameters_);
				if (passedEditToken.isPresent()) {
					// Found valid EditToken passed as parameter
					log.debug("Applying EditToken " + RequestParameters.editToken + " parameter \"" + passedEditToken.get().getSerialized() + "\"");

					// Set local values to values from passed EditToken
					module = passedEditToken.get().getModuleSequence();
					path = passedEditToken.get().getPathSequence();
					workflowProcessid = passedEditToken.get().getActiveWorkflowProcessId();

					// Override values from passed EditToken with values from parameters
					if (parameters_.containsKey(RequestParameters.processId)) {
						RequestInfoUtils.validateSingleParameterValue(parameters_, RequestParameters.processId);
						workflowProcessid = RequestInfoUtils.parseUuidParameter(RequestParameters.processId, parameters_.get(RequestParameters.processId).iterator().next());
					}
					if (parameters_.containsKey(RequestParameters.editModule)) {
						module = RequestInfoUtils.getConceptSequenceFromParameter(parameters_, RequestParameters.editModule);
					}
					if (parameters_.containsKey(RequestParameters.editPath)) {
						path = RequestInfoUtils.getConceptSequenceFromParameter(parameters_, RequestParameters.editPath);
					}
					
					// Create new EditToken based on any passed parameters // TODO joel only recreate if necessary
					editToken = EditTokens.getOrCreate(
							passedEditToken.get().getAuthorSequence(),
							module,
							path,
							workflowProcessid,
							passedEditToken.get().getRoles()
							);
				} else {
					// No valid EditToken passed as parameter
					log.debug("Retrieving new EditToken with SSO token " + parameters_.get(RequestParameters.ssoToken));

					if (parameters_.containsKey(RequestParameters.processId)) {
						RequestInfoUtils.validateSingleParameterValue(parameters_, RequestParameters.processId);
						workflowProcessid = RequestInfoUtils.parseUuidParameter(RequestParameters.processId, parameters_.get(RequestParameters.processId).iterator().next());
					}
					if (parameters_.containsKey(RequestParameters.editModule)) {
						module = RequestInfoUtils.getConceptSequenceFromParameter(parameters_, RequestParameters.editModule);
					}
					if (parameters_.containsKey(RequestParameters.editPath)) {
						path = RequestInfoUtils.getConceptSequenceFromParameter(parameters_, RequestParameters.editPath);
					}
					
					// Must have either EditToken or SSO token in order to get author
					RequestInfoUtils.validateSingleParameterValue(parameters_, RequestParameters.ssoToken);
					PrismeIntegratedUserService userService = LookupService.getService(PrismeIntegratedUserService.class);
					User user = userService.getUser(parameters_.get(RequestParameters.ssoToken).iterator().next()).get();
					editToken = EditTokenUtil.getUserToken(
							user,
							module != null ? module : defaultEditCoordinate.getModuleSequence(),
							path != null ? path : defaultEditCoordinate.getPathSequence(),
							workflowProcessid);
				}

				editToken_ = editToken;

				log.debug("Created EditToken \"" + requestInfo.get().editToken_ + "\"");
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return editToken_;
	}

	/**
	 * Lazily create, cache and return an EditCoordinate
	 *
	 * @return
	 */
	public EditCoordinate getEditCoordinate()
	{
		//TODO implement this properly - find the right author/module/path
		if (editCoordinate_ == null) {
			editCoordinate_ = new EditCoordinateImpl(
					getEditToken().getAuthorSequence(),
					getEditToken().getModuleSequence(),
					getEditToken().getPathSequence());
		}

		return editCoordinate_;
	}

	/**
	 * @return
	 */
	public UUID getActiveWorkflowProcessId()
	{
		//TODO implement this properly - need the active workflow in this session
		return getEditToken().getActiveWorkflowProcessId();

		//		try
		//		{
		//			if (workflowProcessId == null)
		//			{
		//				workflowProcessId =  LookupService.getService(WorkflowProvider.class).getWorkflowProcessInitializerConcluder()
		//						.createWorkflowProcess(LookupService.getService(WorkflowProvider.class).getBPMNInfo().getDefinitionId(), -99, "Rest Test Name", "Rest Test Description");
		//			}
		//		}
		//		catch (Exception e)
		//		{
		//			log.error("Unexpected", e);
		//		}
		//		return workflowProcessId;
		//		return null;
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

	public WorkflowProvider getWorkflow()
	{
		if (wfp_ == null)
		{
			wfp_ = LookupService.getService(WorkflowProvider.class);
			if (wfp_ == null)
			{
				throw new RuntimeException("Workflow service not available!");
			}
		}
		return wfp_;
	}
}
