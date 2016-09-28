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
package gov.vha.isaac.rest.jerseyConfig;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * 
 * {@link MyJacksonXMLMapperConfig}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Provider
@Produces({ MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_XML })
public class MyJacksonXMLMapperConfig implements ContextResolver<XmlMapper>
{
	final XmlMapper defaultObjectMapper;

	public MyJacksonXMLMapperConfig()
	{
		defaultObjectMapper = createDefaultMapper();
	}

	@Override
	public XmlMapper getContext(final Class<?> type)
	{
		return defaultObjectMapper;
	}

	private static XmlMapper createDefaultMapper()
	{
		JacksonXmlModule module = new JacksonXmlModule();
		module.setDefaultUseWrapper(false);
		final XmlMapper result= new XmlMapper(module);
		result.enable(SerializationFeature.INDENT_OUTPUT);
		result.setAnnotationIntrospector(createJaxbJacksonAnnotationIntrospector());
		result.setPropertyNamingStrategy(new PropertyNamingStrategyBase()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String translate(String propertyName)
			{
				return propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1, propertyName.length());
			}
		});

		return result;
	}

	private static AnnotationIntrospector createJaxbJacksonAnnotationIntrospector()
	{

		final AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
		final AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();

		return AnnotationIntrospector.pair(jacksonIntrospector, jaxbIntrospector);
	}
}
