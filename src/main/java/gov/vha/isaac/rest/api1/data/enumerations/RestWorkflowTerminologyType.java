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

import gov.vha.isaac.ochre.api.metacontent.workflow.StorableWorkflowContents.WorkflowTerminology;
import gov.vha.isaac.ochre.api.util.NumericUtils;

/**
 * {@link RestWorkflowTerminologyType}
 * A class that maps ISAAC {@link StorableWorkflowContents.WorkflowTerminology} values to REST.
 * 
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
public class RestWorkflowTerminologyType extends Enumeration
{
	/**
	 * Displayable text
	 */
	public String text;
	
	protected RestWorkflowTerminologyType()
	{
		//for jaxb
	}
	
	public RestWorkflowTerminologyType(WorkflowTerminology dt)
	{
		super(dt.name(), dt.ordinal());
		text = dt.toString();
	}
	
	public static RestWorkflowTerminologyType[] getAll()
	{
		RestWorkflowTerminologyType[] result = new RestWorkflowTerminologyType[WorkflowTerminology.values().length];
		for (int i = 0; i < WorkflowTerminology.values().length; i++)
		{
			result[i] = new RestWorkflowTerminologyType(WorkflowTerminology.values()[i]);
		}
		return result;
	}

	public static RestWorkflowTerminologyType valueOf(String str) {
		for (WorkflowTerminology enumValue : WorkflowTerminology.values()) {
			if (enumValue.name().equals(str.trim())
					|| enumValue.toString().equals(str.trim())) {
				return new RestWorkflowTerminologyType(enumValue);
			} else {
				Optional<Integer> intOptional = NumericUtils.getInt(str.trim());
				if (intOptional.isPresent() && intOptional.get() == enumValue.ordinal()) {
					return new RestWorkflowTerminologyType(enumValue);
				}
			}
		}
		throw new IllegalArgumentException("invalid RestWorkflowTerminologyType value \"" + str + "\"");
	}
}
