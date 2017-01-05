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

import javax.inject.Singleton;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.spi.StandardLevel;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

/**
 * 
 * {@link PrismeLogSenderService}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@RunLevel(1)
@Service
public class PrismeLogSenderService {
	/**
	 * 
	 */
	PrismeLogSenderService() {
//		while (true) { // TODO Joel should there be a shutdown hook?
//			try {
//				LogEvent nextEvent = PrismeLogAppender.EVENT_QUEUE.take();
//				
//				if (nextEvent.getLoggerName().equals("gov.vha.isaac.ochre.api.LookupService")
//						&& nextEvent.getLevel().getStandardLevel().intLevel() >= StandardLevel.WARN.intLevel()) {
//					// Avoid infinite recursion by not sending messages about log events caused by sending messages
//					//System.out.println("Not sending log event: " + nextEvent.getLoggerName() + ": " + nextEvent.getMessage());
//					continue;
//				}
//				
//				PrismeLogAppender.sendEvent(nextEvent);
//
//			} catch (InterruptedException e) {
//				// TODO Joel how to log without recursion?
//			}
//		}
	}
}
