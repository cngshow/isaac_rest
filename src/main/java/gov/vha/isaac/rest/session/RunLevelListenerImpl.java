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

import org.glassfish.hk2.runlevel.RunLevelListener;
import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.ochre.api.LookupService;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevelFuture;

/**
 * 
 * {@link RunLevelListenerImpl}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Service(name="IsaacShutdownRunLevelListener")
@Singleton
public class RunLevelListenerImpl implements RunLevelListener {
	private static Logger log = LogManager.getLogger(RunLevelListenerImpl.class);
	
	/* (non-Javadoc)
	 * @see org.glassfish.hk2.runlevel.RunLevelListener#onProgress(org.glassfish.hk2.runlevel.ChangeableRunLevelFuture, int)
	 */
	@Override
	public void onProgress(ChangeableRunLevelFuture currentJob, int levelAchieved) {
		log.info("RunLevel " + (currentJob.isDown() ? "Coming down from " : "Going up from ") + levelAchieved + " to " + currentJob.getProposedLevel());
		if (levelAchieved == LookupService.ISAAC_DEPENDENTS_RUNLEVEL && currentJob.isDown()) {
			LookupService.getService(PrismeLogSenderService.class).disable();
		}
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.runlevel.RunLevelListener#onCancelled(org.glassfish.hk2.runlevel.RunLevelFuture, int)
	 */
	@Override
	public void onCancelled(RunLevelFuture currentJob, int levelAchieved) {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.glassfish.hk2.runlevel.RunLevelListener#onError(org.glassfish.hk2.runlevel.RunLevelFuture, org.glassfish.hk2.runlevel.ErrorInformation)
	 */
	@Override
	public void onError(RunLevelFuture currentJob, ErrorInformation errorInformation) {
		// noop
	}
}
