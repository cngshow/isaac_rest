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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.session.interceptors;

import java.io.IOException;
import java.io.InputStreamReader;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.apache.commons.io.input.ReaderInputStream;
import com.github.rwitzel.streamflyer.core.ModifyingReader;
import com.github.rwitzel.streamflyer.regex.RegexModifier;

/**
 * {@link XMLCorrectionReaderInterceptor}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
@Provider
public class XMLCorrectionReaderInterceptor implements ReaderInterceptor
{

	private final String charsetName = "ISO-8859-1";
	private final RegexModifier modifier = new RegexModifier("_class=\\\"", 0, "@class=\"");
	
	/**
	 * @see javax.ws.rs.ext.ReaderInterceptor#aroundReadFrom(javax.ws.rs.ext.ReaderInterceptorContext)
	 */
	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException
	{
		if (context.getMediaType().toString().equals(MediaType.APPLICATION_XML))
		{
			context.setInputStream(new ReaderInputStream(new ModifyingReader(new InputStreamReader(context.getInputStream(), charsetName),
				modifier), charsetName));
		}
		return context.proceed();
	}
}
