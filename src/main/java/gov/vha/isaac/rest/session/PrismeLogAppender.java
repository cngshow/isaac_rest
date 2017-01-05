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
import java.io.Serializable;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.codehaus.plexus.util.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * {@link PrismeLogAppender}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Plugin(name="PrismeLogAppender", category="Core", elementType="appender", printObject=true)
public class PrismeLogAppender extends AbstractAppender {
	private static final long serialVersionUID = -228479087489358210L;

	//private static Logger log = LogManager.getLogger(PrismeLogAppender.class);
	
	private final static String PRISME_NOTIFY_URL = PrismeServiceUtils.getPrismeProperties().getProperty("prisme_notify_url");

	private static boolean LOGGED_PRISME_CONFIGURATION_ERROR = false;
	
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock readLock = rwLock.readLock();

	private Client client_ = null;
	private WebTarget webTargetWithPath_ = null;

	/**
	 * @param name
	 * @param filter
	 * @param layout
	 */
	public PrismeLogAppender(
			String name,
			Filter filter,
			Layout<? extends Serializable> layout) {
		super(name, filter, layout);
	}

	/**
	 * @param name
	 * @param filter
	 * @param layout
	 * @param ignoreExceptions
	 */
	public PrismeLogAppender(
			String name,
			Filter filter,
			Layout<? extends Serializable> layout,
			boolean ignoreExceptions) {
		super(name, filter, layout, ignoreExceptions);
	}

	/* (non-Javadoc)
	 * @see org.apache.logging.log4j.core.Appender#append(org.apache.logging.log4j.core.LogEvent)
	 */
	@Override
	public void append(LogEvent event) {
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

		final String event_logged_key = "event_logged";
		final String validation_errors_key = "validation_errors";
		final String level_key = "level";
		final String application_name_key = "application_name";
		final String application_name_value = "ISAAC";
		final String tag_key = "tag";
		final String message_key = "message";
		final String security_token_key = "security_token";

		/*
		 * If PRISME not configured it should log the error once and never retry
		 */
		if (LOGGED_PRISME_CONFIGURATION_ERROR) {
			return;
		}

		readLock.lock();
		try {

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
				LOGGER.error("ENCOUNTERED UNEXPECTED/UNSUPPORTED LogEvent StandardLevel VALUE: {}", event.getLevel().getStandardLevel());
				break;
			}

			// TODO Joel figure out what to do with Layout, if anything
			Layout<? extends Serializable> layout = getLayout() != null ? getLayout() : PatternLayout.createDefaultLayout();
			final byte[] bytes = getLayout().toByteArray(event);
			String msgFromLayout = new String(bytes);

			dto.put(level_key, prismeLevel);
			dto.put(application_name_key, application_name_value);
			dto.put(tag_key, event.getLoggerName());
			dto.put(message_key, event.getMessage().getFormattedMessage()); // TODO Joel should use this or msgFromLayout?

			String eventInputJson = null;
			try {
				eventInputJson = jsonIze(dto);
			} catch (IOException e) {
				LOGGER.error("FAILED GENERATING LOG EVENT JSON FROM MAP OF PRISME LOGGER API PARAMETERS: {}", dto.toString());

				eventInputJson = "{ \""+ level_key + "\":\"3\", \"" + tag_key + "\":\"LOGGING ERROR\", \"" + message_key + "\":\"FAILED TO LOG EVENT TO PRISME. CHECK ISAAC LOGS.\", \"" + application_name_key + "\":\"" + application_name_value + "\" }";
			}

			if (StringUtils.isBlank(PRISME_NOTIFY_URL)) {
				LOGGER.error("CANNOT LOG EVENT TO PRISME LOGGER API BECAUSE prisme_notify_url NOT CONFIGURED IN prisme.properties: {}", dto.toString());
				LOGGED_PRISME_CONFIGURATION_ERROR = true; //If PRISME not configured it should log the error once and never retry
				return;
			}
			
			String targetWithPath = PRISME_NOTIFY_URL.replaceAll("\\?.*", "");
			String securityToken = PRISME_NOTIFY_URL.replaceFirst(".*\\?" + security_token_key + "=", "");

			boolean forceCreateNewWebTarget = false;
			if (client_ == null) {
				client_ = ClientBuilder.newClient();
				forceCreateNewWebTarget = true;;
			}
			if (webTargetWithPath_ == null || forceCreateNewWebTarget) {
				webTargetWithPath_ = client_.target(targetWithPath);
			}

			Map<String, String> params = new HashMap<>();
			params.put(security_token_key, securityToken);

			String responseJson = PrismeServiceUtils.postJsonToPrisme(webTargetWithPath_, eventInputJson, params);

			ObjectMapper mapper = new ObjectMapper();
			Map<?, ?> map = null;
			try {
				map = mapper.readValue(responseJson, Map.class);
			} catch (IOException e) {
				LOGGER.error("FAILED TO READ RESPONSE TO SUBMISSION OF LOG EVENT TO PRISME: {}", eventInputJson);
				return;
			}

			if (map == null || map.get(event_logged_key) == null || !map.get(event_logged_key).toString().equalsIgnoreCase("true")) {
				if (map != null && map.containsKey(validation_errors_key) && map.get(validation_errors_key) != null) {
					LOGGER.error("FAILED PUBLISHING LOG EVENT TO PRISME WITH VALIDATION ERRORS: {}, EVENT: {}", map.get(validation_errors_key).toString(), eventInputJson);
				} else {
					LOGGER.error("FAILED PUBLISHING LOG EVENT TO PRISME WITH NO KNOWN VALIDATION ERRORS: {}", eventInputJson);
				}
			} else {
				LOGGER.debug("SUCCEEDED publishing log event to PRISME: {}", eventInputJson);
			}
		} finally {
//			if (client_ != null) {
//				// This code only used if each append() should have its own Client
//				client_.close();
//				client_ = null;
//				webTargetWithPath_ = null;
//			}
			readLock.unlock();
		}
	}
	
	// Custom appenders need to declare a factory method
	// annotated with `@PluginFactory`. Log4j will parse the configuration
	// and call this factory method to construct an appender instance with
	// the configured attributes.
	@PluginFactory
	public static PrismeLogAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginElement("Filter") final Filter filter,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginAttribute("ignoreExceptions") Boolean ignoreExceptions) {
		if (name == null) {
			LOGGER.error("No name provided for PrismeLogAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new PrismeLogAppender(name, filter, layout, (ignoreExceptions != null && ignoreExceptions) ? ignoreExceptions : false);
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
		return toJson(root);
	}
}
