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
package gov.vha.isaac.rest.jaxbCrutch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.rest.api.data.wrappers.RestUUID;
import gov.vha.isaac.rest.api1.data.association.RestAssociationTypeVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingItemVersion;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowAvailableAction;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistoriesMapEntry;
import gov.vha.isaac.rest.api1.data.workflow.RestWorkflowProcessHistory;

/**
 * {@link ArrayUnwrappers}
 * 
 * Jersey already enhances JaxB to properly handle arrays and lists when is marshalls to XML.  However, we are using raw jaxb, not 
 * jersey enhanced jaxb to desserialize, so we need these silly stubs to deserialize arrays properly.
 * 
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a> 
 */
public class ArrayUnwrappers
{
	@XmlRootElement
	public static class RestCommentVersions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestCommentVersion[] restCommentVersion;
		
		public Object[] getValues()
		{
			return restCommentVersion;
		}
	}
	
	@XmlRootElement
	public static class RestSememeDescriptionVersions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestSememeDescriptionVersion[] restSememeDescriptionVersion;
		
		public Object[] getValues()
		{
			return restSememeDescriptionVersion;
		}
	}
	
	@XmlRootElement
	public static class RestMappingSetVersions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestMappingSetVersion[] restMappingSetVersion;
		
		public Object[] getValues()
		{
			return restMappingSetVersion;
		}
	}
	
	@XmlRootElement
	public static class RestMappingItemVersions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestMappingItemVersion[] restMappingItemVersion;
		
		public Object[] getValues()
		{
			return restMappingItemVersion;
		}
	}
	
	@XmlRootElement
	public static class RestWorkflowProcessHistories implements ArrayUnwrapper
	{
		@XmlElement
		protected RestWorkflowProcessHistory[] restWorkflowProcessHistory;
		
		public Object[] getValues()
		{
			return restWorkflowProcessHistory;
		}
	}
	

	@XmlRootElement
	public static class RestWorkflowProcessHistoriesMapEntries implements ArrayUnwrapper
	{
		@XmlElement
		protected RestWorkflowProcessHistoriesMapEntry[] restWorkflowProcessHistoriesMapEntry;
		
		public Object[] getValues()
		{
			return restWorkflowProcessHistoriesMapEntry;
		}
	}

	@XmlRootElement
	public static class RestUUIDs implements ArrayUnwrapper
	{
		@XmlElement
		protected RestUUID[] restUUID;
		
		public Object[] getValues()
		{
			return restUUID;
		}
	}
	
	@XmlRootElement
	public static class RestWorkflowAvailableActions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestWorkflowAvailableAction[] restWorkflowAvailableAction;
		
		public Object[] getValues()
		{
			return restWorkflowAvailableAction;
		}
	}
	
	
	@XmlRootElement
	public static class RestAssociationTypeVersions implements ArrayUnwrapper
	{
		@XmlElement
		protected RestAssociationTypeVersion[] restAssociationTypeVersion;
		
		public Object[] getValues()
		{
			return restAssociationTypeVersion;
		}
	}
}
