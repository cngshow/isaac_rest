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

package gov.vha.isaac.rest.tokens;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.model.configuration.EditCoordinates;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link EditTokens}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class EditTokens {
	private final static Object OBJECT_BY_TOKEN_CACHE_LOCK = new Object();
	
	private static final int DEFAULT_MAX_SIZE = 1024;
	private static EditToken defaultEditToken = null;
	private static Map<String, EditToken> OBJECT_BY_TOKEN_CACHE = null;

	private static void init(final int maxEntries) {
		synchronized(OBJECT_BY_TOKEN_CACHE_LOCK) {
			if (OBJECT_BY_TOKEN_CACHE == null) {
				OBJECT_BY_TOKEN_CACHE = new LinkedHashMap<String, EditToken>(maxEntries, 0.75F, true) {
					private static final long serialVersionUID = -1236481390177598762L;
					@Override
					protected boolean removeEldestEntry(Map.Entry<String, EditToken> eldest){
						return size() > maxEntries;
					}
				};

				EditCoordinate defaultEditCoordinate = EditCoordinates.getDefaultUserSolorOverlay();
				defaultEditToken = EditTokens.getOrCreate(
						defaultEditCoordinate.getAuthorSequence(),
						defaultEditCoordinate.getModuleSequence(),
						defaultEditCoordinate.getPathSequence(),
						null
						);
			}
		}
	}

	public static EditToken getDefaultEditToken() {
		init(DEFAULT_MAX_SIZE);
		return defaultEditToken;
	}

	/**
	 * 
	 * This method caches a EditToken object,
	 * automatically serializing itself to generate its key
	 * 
	 * @param value EditToken object
	 * @throws Exception
	 */
	private static void put(EditToken value) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}

		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			OBJECT_BY_TOKEN_CACHE.put(value.getSerialized(), value);
		}
	}

	/**
	 * 
	 * This method attempts to retrieve the EditToken object
	 * corresponding to the passed serialized EditToken string key.
	 * 
	 * @param key serialized EditToken string
	 * @return EditToken object
	 * @throws Exception
	 */
	private static EditToken get(String key) {
		if (OBJECT_BY_TOKEN_CACHE == null) {
			init(DEFAULT_MAX_SIZE);
		}
		synchronized (OBJECT_BY_TOKEN_CACHE_LOCK) {
			return OBJECT_BY_TOKEN_CACHE.get(key);
		}
	}

	public static EditToken getOrCreate(String key) throws RestException {
		EditToken token = get(key);
		
		if (token == null) {
			try {
				token = new EditToken(key);
			} catch (Exception e) {
				throw new RestException("Failed creating EditToken from \"" + key + "\".  Caught " + e.getClass().getName() + " " + e.getLocalizedMessage(), e);
			}
			put(token);
		}
		
		return get(key);
	}

	public static EditToken getOrCreate(
			int authorSequence,
			int moduleSequence,
			int pathSequence,
			UUID workflowProcessId) {
		EditToken constructedToken =
				new EditToken(
						authorSequence,
						moduleSequence,
						pathSequence,
						workflowProcessId);
		
		EditToken cachedToken = get(constructedToken.getSerialized());
		
		if (cachedToken == null) {
			cachedToken = constructedToken;
			put(cachedToken);
		}
		
		return get(cachedToken.getSerialized());
	}

	/**
	 * 
	 * This method returns an Optional containing an EditToken object if its parameter exists in the parameters map.
	 * If the parameter exists, it automatically attempts to construct and cache the EditToken object before returning it
	 *
	 * @param allParams parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return an Optional containing a EditToken string if it exists in the parameters map
	 * @throws Exception 
	 */
	public static Optional<EditToken> getEditTokenParameterTokenObjectValue(Map<String, List<String>> allParams) throws RestException {
		Optional<String> tokenStringOptional = getEditTokenParameterStringValue(allParams);

		if (! tokenStringOptional.isPresent()) {
			return Optional.empty();
		} else {
			//return Optional.of(EditTokens.getOrCreate(tokenStringOptional.get()));
			try {
				return Optional.of(new EditToken(tokenStringOptional.get()));
			} catch (Exception e) {
				throw new RestException("Failed creating EditToken from string \"" + tokenStringOptional.get() + "\"", e);
			}
		}
	}
	/**
	 * 
	 * This method returns an Optional containing a EditToken string if it exists in the parameters map.
	 *
	 * @param allParams parameter name to value-list map provided in UriInfo by ContainerRequestContext
	 * @return an Optional containing a EditToken string if it exists in the parameters map
	 * @throws RestException
	 */
	public static Optional<String> getEditTokenParameterStringValue(Map<String, List<String>> allParams) throws RestException {
		List<String> editTokenParameterValues = allParams.get(RequestParameters.editToken);
		
		if (editTokenParameterValues == null || editTokenParameterValues.size() == 0 || StringUtils.isBlank(editTokenParameterValues.get(0))) {
			return Optional.empty();
		} else if (editTokenParameterValues.size() > 1) {
			throw new RestException(RequestParameters.editToken, "\"" + editTokenParameterValues + "\"", "too many (" + editTokenParameterValues.size() 
			+ " values - should only be passed with one value");
		}		
		return Optional.of(editTokenParameterValues.get(0));
	}
}
