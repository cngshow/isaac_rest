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
package gov.vha.isaac.rest.api1.data.comment;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;

/**
 * 
 * {@link RestCommentVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestCommentVersion
{
	/**
	 * The identifier data for the object
	 */
	@XmlElement
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this comment
	 */
	@XmlElement
	RestStampedVersion commentStamp;
	
	/**
	 * The identifier of the object that is being commented on.  Could be a concept or a sememe
	 */
	@XmlElement
	public int commentedItem;
	
	/**
	 * The comment
	 */
	@XmlElement
	public String comment;
	
	/**
	 * An (optional) comment context to store with the comment.  Typically used for key words, etc. 
	 */
	@XmlElement
	public String commentContex;
	
	public RestCommentVersion(DynamicSememe<?> comment)
	{
		identifiers = new RestIdentifiedObject(comment.getUuidList());
		commentStamp = new RestStampedVersion(comment);
		commentedItem = comment.getReferencedComponentNid();
		if (comment.getAssemblageSequence() != DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getConceptSequence())
		{
			throw new RuntimeException("The provided sememe isn't a comment!");
		}
			
		this.comment = comment.getData()[0].getDataObject().toString();
		if (comment.getData().length > 1 && comment.getData()[1] != null)
		{
			this.commentContex = comment.getData()[1].getDataObject().toString();
		}
	}
}
