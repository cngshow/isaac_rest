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

import java.util.Optional;

import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link LatestVersionUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class LatestVersionUtils {
	private LatestVersionUtils() {}

	public static <T extends SememeVersionImpl<T>> Optional<T> getLatestSememeVersion(SememeChronology<T> sememeChronology, Class<T> clazz, StampCoordinate sc) {
		Optional<LatestVersion<T>> latestVersionOptional = ((SememeChronology<T>)sememeChronology).getLatestVersion(clazz, sc);
		return latestVersionOptional.isPresent() ? Optional.of(latestVersionOptional.get().value()) : Optional.empty(); // TODO handle contradictions
	}

	public static <T extends SememeVersionImpl<T>> Optional<T> getLatestSememeVersion(SememeChronology<T> sememeChronology, Class<T> clazz) throws RestException {
		return getLatestSememeVersion(sememeChronology, clazz, Frills.makeStampCoordinateAnalogVaryingByModulesOnly(RequestInfo.get().getStampCoordinate(), RequestInfo.get().getEditCoordinate().getModuleSequence(), null));
	}
}
