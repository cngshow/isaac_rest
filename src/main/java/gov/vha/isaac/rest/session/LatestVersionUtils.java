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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.externalizable.OchreExternalizableObjectType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link LatestVersionUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class LatestVersionUtils {
	private static Logger log = LogManager.getLogger(LatestVersionUtils.class);
	
	private LatestVersionUtils() {}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends ConceptVersion<T>> Optional<T> getLatestVersion(ConceptChronology<T> conceptChronology, StampCoordinate sc) {
		return getLatestVersion((ConceptChronology)conceptChronology, ConceptVersion.class, sc);
	}

	public static <T extends StampedVersion> Optional<T> getLatestVersion(ObjectChronology<T> objectChronology, Class<T> clazz, StampCoordinate sc) {
		Optional<LatestVersion<T>> latestVersionOptional = objectChronology.getLatestVersion(clazz, sc);

		if (latestVersionOptional.isPresent()) {
			if (latestVersionOptional.get().contradictions().isPresent()) {
				// TODO properly handle contradictions
				final OchreExternalizableObjectType objectType = objectChronology.getOchreObjectType();
				String detail = "object";
				switch (objectType) {
				case SEMEME:
					detail = objectType + " UUID=" + objectChronology.getPrimordialUuid() + ", SEMEME SEQ=" + ((SememeChronology<?>)objectChronology).getSememeSequence() + ", REF COMP NID=" + ((SememeChronology<?>)objectChronology).getReferencedComponentNid();
					break;
				case CONCEPT:
				case STAMP_ALIAS:
				case STAMP_COMMENT:
					detail = objectType + " UUID=" + objectChronology.getPrimordialUuid();
					break;
				default:
					throw new RuntimeException("Unsupported OchreExternalizableObjectType for passed ObjectChronology UUID=" + objectChronology.getPrimordialUuid());
				}
				log.warn("Getting latest version of " + detail + " with " + latestVersionOptional.get().contradictions().get().size() 
						+ " version contradictions");
			}
			
			return Optional.of(latestVersionOptional.get().value());
		}

		return Optional.empty();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends ConceptVersion<T>> Optional<T> getLatestVersionForUpdate(ConceptChronology<T> conceptChronology) throws RestException {
		return getLatestVersionForUpdate((ConceptChronology)conceptChronology, ConceptVersion.class);
	}

	public static <T extends StampedVersion> Optional<T> getLatestVersionForUpdate(ObjectChronology<T> objectChronology, Class<T> clazz) throws RestException {
		StampCoordinate sc = Frills.makeStampCoordinateAnalogVaryingByModulesOnly(
				RequestInfo.get().getStampCoordinate(),
				RequestInfo.get().getEditCoordinate().getModuleSequence(),
				null).makeAnalog(State.values()).makeAnalog(Long.MAX_VALUE);
		
		Optional<T> latestVersion =  getLatestVersion(objectChronology, clazz, sc);
		if (!latestVersion.isPresent()) {
			sc = RequestInfo.get().getStampCoordinate().makeAnalog(State.values()).makeAnalog(Long.MAX_VALUE);
			latestVersion = getLatestVersion(objectChronology, clazz, sc);
		}
		
		return latestVersion;
	}
}
