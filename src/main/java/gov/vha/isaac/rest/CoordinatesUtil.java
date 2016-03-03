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

package gov.vha.isaac.rest;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.bootstrap.TermAux;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPosition;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.session.RequestParameters;

/**
 * 
 * {@link CoordinatesUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class CoordinatesUtil {
	private static Logger log = LogManager.getLogger();
	
	private CoordinatesUtil() {}

	private static Map<String,String> getStampCoordinateParameters(Map<String, String> params) {
		Map<String,String> stampCoordinateParams = new HashMap<>();

		String stampCoordinateParamNames[] = new String[] {
				RequestParameters.stampCoordTime,
				RequestParameters.stampCoordPath,
				RequestParameters.stampCoordPrecedence,
				RequestParameters.stampCoordModules,
				RequestParameters.stampCoordStates
		};
		for (String paramName : stampCoordinateParamNames) {
			if (params.containsKey(paramName)) {
				stampCoordinateParams.put(paramName, params.get(paramName));
			}
		}
		
		return stampCoordinateParams;
	}
	public static StampCoordinate getStampCoordinateFromParameters(Map<String, String> params) throws RestException {
		StampPosition stampPosition = new StampPositionImpl(
				getStampCoordinateTimeFromParameter(params.get(RequestParameters.stampCoordTime)), 
				getStampCoordinatePathSequenceFromParameter(params.get(RequestParameters.stampCoordPath)));
		StampCoordinate stampCoordinate = new StampCoordinateImpl(
				getStampCoordinatePrecedenceFromParameter(params.get(RequestParameters.stampCoordPrecedence)),
				stampPosition, 
				getStampCoordinateModuleSequencesFromParameter(params.get(RequestParameters.stampCoordModules)),
				getStampCoordinateAllowedStatesFromParameter(params.get(RequestParameters.stampCoordStates)));

		log.debug("Created StampCoordinate from params: " + getStampCoordinateParameters(params) + ": " + stampCoordinate);

		return stampCoordinate;
	}
	public static StampPrecedence getStampCoordinatePrecedenceFromParameter(String precedenceStr) throws RestException {
		if (StringUtils.isBlank(precedenceStr)) {
			return StampPrecedence.PATH;
		}

		for (StampPrecedence value : StampPrecedence.values()) {
			if (value.name().equalsIgnoreCase(precedenceStr.trim())) {
				return value;
			}
		}

		Optional<Integer> stampPrecedenceOrdinalOptional = NumericUtils.getInt(precedenceStr.trim());
		if (stampPrecedenceOrdinalOptional.isPresent()) {
			for (StampPrecedence value : StampPrecedence.values()) {
				if (value.ordinal() == stampPrecedenceOrdinalOptional.get()) {
					return value;
				}
			}
		}

		throw new RestException("stampCoordPrecedence", "\"" + precedenceStr + "\"", "Invalid stamp coordinate precedence value");
	}

	public static EnumSet<State> getStampCoordinateAllowedStatesFromParameter(String statesStr) throws RestException {
		EnumSet<State> allowedStates = EnumSet.allOf(State.class);
		allowedStates.clear();

		if (StringUtils.isNotBlank(statesStr))
		{
			for (String state : statesStr.trim().split(","))
			{
				if (StringUtils.isNotBlank(state))
				{
					boolean foundMatch = false;
					for (State value : State.values()) {
						if (value.name().equalsIgnoreCase(state.trim())) {
							allowedStates.add(value);
							foundMatch = true;
							break;
						}
					}

					if (! foundMatch) {
						Optional<Integer> stateOrdinalOptional = NumericUtils.getInt(statesStr.trim());
						if (stateOrdinalOptional.isPresent()) {
							for (State value : State.values()) {
								if (value.ordinal() == stateOrdinalOptional.get()) {
									allowedStates.add(value);
									foundMatch = true;
									break;
								}
							}
						}
						
						if (! foundMatch) {
							throw new RestException("stampCoordStates", state, "Invalid stamp coordinate state value");
						}
					}
				}
			}

			if (allowedStates.isEmpty()) {
				return EnumSet.allOf(State.class);
			} else {
				return allowedStates;
			}
		} else {
			return EnumSet.allOf(State.class);
		}
	}

	public static ConceptSequenceSet getStampCoordinateModuleSequencesFromParameter(String modulesStr) throws RestException {
		ConceptSequenceSet returnValue = new ConceptSequenceSet();
		if (StringUtils.isNotBlank(modulesStr))
		{
			for (String moduleId : modulesStr.trim().split(","))
			{
				if (StringUtils.isNotBlank(moduleId))
				{
					Optional<Integer> moduleIdIntIdOptional = NumericUtils.getInt(moduleId.trim());
					if (moduleIdIntIdOptional.isPresent()) {
						returnValue.add(Get.identifierService().getConceptSequence(moduleIdIntIdOptional.get()));
						continue;
					}

					Optional<UUID> moduleUuidOptional = UUIDUtil.getUUID(moduleId.trim());
					if (moduleUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(moduleUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
						returnValue.add(Get.identifierService().getConceptSequenceForUuids(moduleUuidOptional.get()));
						continue;
					}

					throw new RestException("stampCoordModules", "\"" + moduleId + "\"", "Invalid stamp coordinate module value");
				}
			}
		}

		return returnValue;
	}

	public static int getStampCoordinatePathSequenceFromParameter(String pathStr) throws RestException {
		int development = TermAux.DEVELOPMENT_PATH.getConceptSequence();

		if (StringUtils.isBlank(pathStr)) {
			return development;
		}

		Optional<Integer> pathIntIdOptional = NumericUtils.getInt(pathStr.trim());
		if (pathIntIdOptional.isPresent()) {
			return Get.identifierService().getConceptSequence(pathIntIdOptional.get());
		}

		Optional<UUID> pathUuidOptional = UUIDUtil.getUUID(pathStr.trim());
		if (pathUuidOptional.isPresent() && Get.identifierService().getChronologyTypeForNid(Get.identifierService().getNidForUuids(pathUuidOptional.get())) == ObjectChronologyType.CONCEPT) {
			return Get.identifierService().getConceptSequenceForUuids(pathUuidOptional.get());
		}

		if (pathStr.trim().equalsIgnoreCase("development")) {
			return development;
		} else if (pathStr.trim().equalsIgnoreCase("master")) {
			return TermAux.MASTER_PATH.getConceptSequence();
		}

		throw new RestException("stampCoordPath", "\"" + pathStr + "\"", "Invalid stamp coordinate path value");
	}

	public static long getStampCoordinateTimeFromParameter(String timeStr) throws RestException {
		long latest = Long.MAX_VALUE;
		if (StringUtils.isBlank(timeStr)) {
			return latest;
		}

		Optional<Long> longTimeOptional = NumericUtils.getLong(timeStr.trim());
		if (longTimeOptional.isPresent()) {
			return longTimeOptional.get();
		}

		if (timeStr.trim().equalsIgnoreCase("latest")) {
			return latest;
		}

		throw new RestException("stampCoordTime", "\"" + timeStr + "\"", "Invalid stamp coordinate time value");
	}
}
