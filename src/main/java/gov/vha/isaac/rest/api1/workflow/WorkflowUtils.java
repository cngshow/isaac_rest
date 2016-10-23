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

package gov.vha.isaac.rest.api1.workflow;

import java.util.Optional;
import java.util.UUID;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronology;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowAccessor;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link WorkflowUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class WorkflowUtils {
	private WorkflowUtils() {}

	public static <T extends StampedVersion> Optional<T> getStampedVersion(Class<T> clazz, Optional<UUID> processId, int componentNid) throws Exception {
		return getStampedVersion(clazz, processId.isPresent() ? processId.get() : null, componentNid);
	}
	public static <T extends StampedVersion> Optional<T> getStampedVersion(Class<T> clazz, UUID processId, int componentNid) throws Exception {
		final WorkflowAccessor wfAccessor = LookupService.get().getService(WorkflowAccessor.class);

		T version = null;
		if (processId != null && wfAccessor.isComponentInProcess(processId, componentNid)) {
			try {
				version = wfAccessor.getVersionPriorToWorkflow(clazz, processId, componentNid);
			} catch (Exception e) {
				throw new Exception("Failed retrieving component NID=" + componentNid + " version existing prior to commencement of workflow process " + processId);
			}
			
			if (version == null) {
				throw new Exception("Component NID=" + componentNid + " version did not exist prior to commencement of workflow process " + processId);
			}
		} else {
			ObjectChronology<?>  objChron;
			if (Get.identifierService().getChronologyTypeForNid(componentNid) == ObjectChronologyType.CONCEPT) {
				objChron = Get.conceptService().getConcept(componentNid);
			} else if (Get.identifierService().getChronologyTypeForNid(componentNid) == ObjectChronologyType.SEMEME) {
				objChron = Get.sememeService().getSememe(componentNid);
			} else {
				throw new Exception("Cannot reconcile NID with Identifier Service for nid: " + componentNid);
			}
			@SuppressWarnings("unchecked")
			Optional<LatestVersion<T>> cv = ((ObjectChronology<T>)(objChron)).getLatestVersion(clazz, RequestInfo.get().getStampCoordinate());
			if (cv.isPresent()) {
				// TODO handle contradictions
				version = cv.get().value();
			}
		}
		
		return version != null ? Optional.of(version) : Optional.empty();
	}
}
