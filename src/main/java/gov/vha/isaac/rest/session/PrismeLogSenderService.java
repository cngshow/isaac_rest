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

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;
import org.codehaus.plexus.util.StringUtils;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.vha.isaac.rest.ApplicationConfig;

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
	private static class BadResponseException extends RuntimeException {
		private static final long serialVersionUID = 1482716963119375807L;

		/**
		 * @param message
		 * @param cause
		 */
		public BadResponseException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * @param message
		 */
		public BadResponseException(String message) {
			super(message);
		}
	}
	
	private final static LogEvent POISON_PILL_SHUTDOWN_MARKER = new LogEvent() {
		private static final long serialVersionUID = 1L;
		@Override public Map<String, String> getContextMap() { return null; }
		@Override public ContextStack getContextStack() { return null; }
		@Override public String getLoggerFqcn() { return null; }
		@Override public Level getLevel() { return null; }
		@Override public String getLoggerName() { return null; }
		@Override public Marker getMarker() { return null; }
		@Override public Message getMessage() { return null; }
		@Override public long getTimeMillis() { return 0; }
		@Override public StackTraceElement getSource() { return null; }
		@Override public String getThreadName() { return null; }
		@Override public Throwable getThrown() { return null; }
		@Override public ThrowableProxy getThrownProxy() {return null;}
		@Override public boolean isEndOfBatch() { return false; }
		@Override public boolean isIncludeLocation() { return false; }
		@Override public void setEndOfBatch(boolean endOfBatch) {}
		@Override public void setIncludeLocation(boolean locationRequired) {}
		@Override public long getNanoTime() { return 0;}
	};
	
	private static Logger LOGGER = LogManager.getLogger(PrismeLogSenderService.class);

	final static String PRISME_NOTIFY_URL = PrismeServiceUtils.getPrismeProperties(false).getProperty("prisme_notify_url");

	static Client CLIENT = null;

	static BlockingQueue<LogEvent> EVENT_QUEUE = null;

	PrismeLogSenderService() {
		// For HK2
	}
	
	public void disable() {
		if (EVENT_QUEUE != null) {
			EVENT_QUEUE.add(POISON_PILL_SHUTDOWN_MARKER);
			EVENT_QUEUE = null;
		}
	}
	public boolean isEnabled() { return EVENT_QUEUE != null; }

	@PostConstruct
	public void startupPrismeLogSenderService() {
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// disable() if not configured
				if (StringUtils.isBlank(PRISME_NOTIFY_URL)) {
					LOGGER.error("CANNOT LOG EVENT TO PRISME LOGGER API BECAUSE prisme_notify_url NOT CONFIGURED IN prisme.properties");
					disable();
					return;
				}

				// enable if not yet enabled
				if (EVENT_QUEUE == null) {
					EVENT_QUEUE = new LinkedBlockingQueue<>();
				}

				final int maxAttemptsForMessage = 10;
				int attemptsForMessage = 0;
				
				final int initialWait = 0;
				int wait = initialWait;

				final int initialIncrement = 100;
				int increment = initialIncrement;
				
				final int maxWait = 1000 * 60 * 2; // 2 minute maximum wait
				final int maxQueueSize = 200;
				LogEvent eventToSend = null;
				while (isEnabled()) {
					try {
						// Read log event, blocking on empty queue
						if (eventToSend != null && attemptsForMessage < maxAttemptsForMessage) {
							attemptsForMessage++;
							LOGGER.info("PrismeLogSenderService: retrying send of log event for attempt " + attemptsForMessage + " of " + maxAttemptsForMessage + ": " + eventToSend);
						} else {
							attemptsForMessage = 0;
							eventToSend = EVENT_QUEUE.take();
							
							// If read POISON_PILL_SHUTDOWN_MARKER from queue then shutdown
							if (eventToSend == POISON_PILL_SHUTDOWN_MARKER) {
								// Shutdown
								disable();
								break;
							}
						}

						// Send log event
						sendEvent(eventToSend);
						
						// If sendEvent succeeded then set eventToSend = null so it won't attempt to resend
						eventToSend = null;
						wait = initialWait;
						increment = initialIncrement;
					} catch (Exception re) {
						LOGGER.error("FAILED SENDING LOG EVENT TO PRISME: " + eventToSend, re);
						
						// Wait (Thread.sleep()), if wait > 0
						if (wait > 0) {
							try {
								LOGGER.debug("PrismeLogSenderService: WAITING " + wait + " SECONDS");
								Thread.sleep(wait);
							} catch (InterruptedException e) {
								// ignore
							}
						}
						
						// If less than maxWait, then increment wait and increment
						if (wait < maxWait) {
							wait = wait + increment;
							increment = wait;
						}
						
						// Enforce cap on wait
						if (wait >= maxWait) {
							wait = maxWait;
							increment = 0;
						}
					}
					
					// Remove and discard excess events,
					// calling disable() and break if encountering POISON_PILL_SHUTDOWN_MARKER
					while (EVENT_QUEUE.size() > maxQueueSize) {
						LogEvent eventToDiscard = EVENT_QUEUE.remove();
						if (eventToDiscard == POISON_PILL_SHUTDOWN_MARKER) {
							// Shutdown
							disable();
							break;
						}
					}
				} // End run() outer while loop
			} // End run()
		}; // End Runnable declaration

		new Thread(runnable).start();
	}

	@PreDestroy
	public void shutdownPrismeLogSenderService() {
		// disable() on shutdown
		disable();

		if (CLIENT != null) {
			CLIENT.close();
			CLIENT = null;
		}
	}

	private void sendEvent(LogEvent event) {
		/*
		 * property name:
		 * 		prisme_notify_url
		 * 
		 * example property:
		 * 		prisme_notify_url=http://localhost:3000/log_event?security_token=%5B%22u%5Cf%5Cx92%5CxBC%5Cx17%7D%5CxD1%5CxE4%5CxFB%5CxE5%5Cx99%5CxA3%5C%22%5CxE8%5C%5CK%22%2C+%22%3E%5Cx16%5CxDE%5CxA8v%5Cx14%5CxFF%5CxD2%5CxC6%5CxDD%5CxAD%5Cx9F%5Cx1D%5CxD1cF%22%5D
		 * 
		 * example target with path:
		 * 		http://localhost:3000/log_event
		 * 
		 * example parameters and values:
		 * 		security_token=%5B%22u%5Cf%5Cx92%5CxBC%5Cx17%7D%5CxD1%5CxE4%5CxFB%5CxE5%5Cx99%5CxA3%5C%22%5CxE8%5C%5CK%22%2C+%22%3E%5Cx16%5CxDE%5CxA8v%5Cx14%5CxFF%5CxD2%5CxC6%5CxDD%5CxAD%5Cx9F%5Cx1D%5CxD1cF%22%5D
		 * 		application_name=isaac
		 * 		level=1
		 * 		tag=SOME_TAG
		 * 		message=broken
		 */
		if (event == POISON_PILL_SHUTDOWN_MARKER) {
			// Shutting down. Shouldn't even get here.
			return;
		}
		if (event.getLoggerName().equalsIgnoreCase(LOGGER.getName())) {
			// Ignore log messages queued from this logger to avoid recursion
			return;
		}

		final String event_logged_key = "event_logged";
		final String validation_errors_key = "validation_errors";
		final String level_key = "level";
		final String application_name_key = "application_name";
		final String application_name_value = ApplicationConfig.getInstance().getContextPath();
		final String tag_key = "tag";
		final String message_key = "message";
		final String security_token_key = "security_token";
		final String token_error_key = "token_error";

		Map<String, Object> dto = new HashMap<>();

		int prismeLevel = 1; // ALWAYS

		// LEVELS = {ALWAYS: 1, WARN: 2, ERROR: 3, FATAL: 4}
		switch(event.getLevel().getStandardLevel()) {
		case FATAL:
			prismeLevel = 4;
			break;
		case ERROR:
			prismeLevel = 3;
			break;
		case WARN:
			prismeLevel = 2;
			break;
		case ALL:
		case INFO:
		case OFF:
		case TRACE:
		case DEBUG:
			prismeLevel = 1;
			break;
		default:
			LOGGER.warn("ENCOUNTERED UNEXPECTED/UNSUPPORTED LogEvent StandardLevel VALUE: {}", event.getLevel().getStandardLevel());
			break;
		}

		String tag = null;
		try {
			// If logger name is a class then make tag the class simple name
			tag = Class.forName(event.getLoggerName()).getSimpleName();
		} catch (Exception e) {
			tag = event.getLoggerName();
		}
		dto.put(level_key, prismeLevel);
		dto.put(application_name_key, application_name_value);
		dto.put(tag_key, tag);
		dto.put(message_key, event.getMessage().getFormattedMessage()); // TODO Joel should use this or msgFromLayout?

		String eventInputJson = null;
		try {
			eventInputJson = PrismeLogSenderService.jsonIze(dto);
		} catch (IOException e) {
			LOGGER.error("FAILED GENERATING LOG EVENT JSON FROM MAP OF PRISME LOGGER API PARAMETERS: " + dto.toString(), e);

			eventInputJson = "{ \""+ level_key + "\":\"3\", \"" + tag_key + "\":\"LOGGING ERROR\", \"" + message_key + "\":\"FAILED TO LOG EVENT TO PRISME. CHECK ISAAC LOGS.\", \"" + application_name_key + "\":\"" + application_name_value + "\" }";
		}

		if (StringUtils.isBlank(PRISME_NOTIFY_URL)) {
			disable();
			LOGGER.error("CANNOT LOG EVENT TO PRISME LOGGER API BECAUSE prisme_notify_url NOT CONFIGURED IN prisme.properties: {}", dto.toString());
			return;
		}

		String targetWithPath = PRISME_NOTIFY_URL.replaceAll("\\?.*", "");
		String securityToken = PRISME_NOTIFY_URL.replaceFirst(".*\\?" + security_token_key + "=", "");

		WebTarget webTargetWithPath = (CLIENT != null ? CLIENT : (CLIENT = ClientBuilder.newClient())).target(targetWithPath);

		Map<String, String> params = new HashMap<>();
		params.put(security_token_key, securityToken);

		String responseJson = PrismeServiceUtils.postJsonToPrisme(webTargetWithPath, eventInputJson, params);

		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> map = null;
		try {
			map = mapper.readValue(responseJson, Map.class);
		} catch (IOException e) {
			//LOGGER.error("FAILED TO READ RESPONSE TO SUBMISSION OF LOG EVENT TO PRISME: " + eventInputJson, e);
			throw new BadResponseException("FAILED TO READ RESPONSE TO SUBMISSION OF LOG EVENT TO PRISME: " + eventInputJson, e);
		}

		if (map == null || map.get(event_logged_key) == null || !map.get(event_logged_key).toString().equalsIgnoreCase("true")) {
			if (map != null && map.containsKey(validation_errors_key) && map.get(validation_errors_key) != null && ((Map<?,?>)(map.get(validation_errors_key))).size() > 0) {
				LOGGER.error("FAILED PUBLISHING LOG EVENT TO PRISME WITH {} VALIDATION ERRORS: {}, EVENT: {}", ((Map<?,?>)(map.get(validation_errors_key))).size(), ((Map<?,?>)map.get(validation_errors_key)).toString(), eventInputJson);
			} else if (map != null && map.containsKey(token_error_key) && map.get(token_error_key) != null && StringUtils.isNotBlank(map.get(token_error_key).toString())) {
				LOGGER.error("FAILED PUBLISHING LOG EVENT TO PRISME WITH TOKEN ERROR: {}, EVENT: {}", map.toString(), eventInputJson);
			} else if (map == null || (map != null && (map.get(event_logged_key) == null || !map.get(event_logged_key).toString().equalsIgnoreCase("false")))) {
				LOGGER.error("SENDING LOG EVENT {} RETURNED INVALID RESPONSE MESSAGE FROM PRISME: {}", eventInputJson, map);
				throw new BadResponseException("Invalid response received from PRISME");
			} else {
				LOGGER.error("FAILED PUBLISHING LOG EVENT {} TO PRISME WITH NO VALIDATION ERRORS IN RESPONSE: {}", eventInputJson, map);
			}
		} else {
			LOGGER.debug("SUCCEEDED publishing log event to PRISME: {}", eventInputJson);
		}
	}

	private static String toJson(ObjectNode root) throws JsonProcessingException, IOException
	{
		StringWriter ws = new StringWriter();
		new ObjectMapper().writeTree(new JsonFactory().createGenerator(ws).setPrettyPrinter(new DefaultPrettyPrinter()), root);
		return ws.toString();
	}

	private static String jsonIze(Map<String, Object> map) throws JsonProcessingException, IOException
	{
		ObjectNode root = JsonNodeFactory.instance.objectNode();
		for (Map.Entry<String, Object> entry : map.entrySet())
		{
			root.put(entry.getKey(), entry.getValue() != null ? (entry.getValue() + "") : null);
		}
		return PrismeLogSenderService.toJson(root);
	}
}
