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
package gov.vha.isaac.rest.api1.data.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RestSememeDescriptionVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestSememeDescriptionVersion extends RestSememeVersion
{
	/**
	 * The concept sequence of the concept that represents the case significance flag on the description .
	 * This should be description case sensitive, description not case sensitive or description initial character sensitive
	 */
	@XmlElement
	int caseSignificanceConceptSequence;
	
	/**
	 * The concept sequence of the concept that represents the language of the description (note, this is NOT 
	 * the dialect)
	 */
	@XmlElement
	int languageConceptSequence;
	
	/**
	 * The text of the description
	 */
	@XmlElement
	String text;
	
	/**
	 * The concept sequence of the concept that represents the type of the description.  
	 * This should be FSN, Synonym, or Definition.
	 */
	@XmlElement
	int descriptionTypeConceptSequence;

	protected RestSememeDescriptionVersion()
	{
		//for Jaxb
	}
	
	public RestSememeDescriptionVersion(DescriptionSememe<?> dsv, boolean includeChronology) throws RestException
	{
		super(dsv, includeChronology);
		caseSignificanceConceptSequence = dsv.getCaseSignificanceConceptSequence();
		languageConceptSequence = dsv.getLanguageConceptSequence();
		text = dsv.getText();
		descriptionTypeConceptSequence = dsv.getDescriptionTypeConceptSequence();
	}
}
