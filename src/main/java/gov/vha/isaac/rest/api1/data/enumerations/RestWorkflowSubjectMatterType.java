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
package gov.vha.isaac.rest.api1.data.enumerations;

import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.metacontent.workflow.StorableWorkflowContents.SubjectMatter;
import gov.vha.isaac.ochre.api.util.NumericUtils;

/**
 * {@link RestWorkflowSubjectMatterType}
 * A class that maps ISAAC {@link ProcessDetail.SubjectMatter} values to REST.
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
public class RestWorkflowSubjectMatterType extends Enumeration
{
	/**
	 * Displayable text
	 */
	public String text;
	
	protected RestWorkflowSubjectMatterType()
	{
		//for jaxb
	}
	
	public RestWorkflowSubjectMatterType(SubjectMatter dt)
	{
		super(dt.name(), dt.ordinal());
		text = dt.toString();
	}
	
	public static RestWorkflowSubjectMatterType[] getAll()
	{
		RestWorkflowSubjectMatterType[] result = new RestWorkflowSubjectMatterType[SubjectMatter.values().length];
		for (int i = 0; i < SubjectMatter.values().length; i++)
		{
			result[i] = new RestWorkflowSubjectMatterType(SubjectMatter.values()[i]);
		}
		return result;
	}

	public static RestWorkflowSubjectMatterType valueOf(String str) {
		for (SubjectMatter enumValue : SubjectMatter.values()) {
			if (enumValue.name().equals(str.trim())
					|| enumValue.toString().equals(str.trim())) {
				return new RestWorkflowSubjectMatterType(enumValue);
			} else {
				Optional<Integer> intOptional = NumericUtils.getInt(str.trim());
				if (intOptional.isPresent() && intOptional.get() == enumValue.ordinal()) {
					return new RestWorkflowSubjectMatterType(enumValue);
				}
			}
		}
		throw new IllegalArgumentException("invalid RestWorkflowSubjectMatterType value \"" + str + "\"");
	}
}
