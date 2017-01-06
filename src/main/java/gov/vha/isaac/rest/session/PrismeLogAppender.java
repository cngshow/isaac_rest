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

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

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
		if (PrismeLogSenderService.EVENT_QUEUE != null) {
			PrismeLogSenderService.EVENT_QUEUE.add(event);
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
}
