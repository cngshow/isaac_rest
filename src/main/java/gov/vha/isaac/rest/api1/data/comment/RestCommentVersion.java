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
public class RestCommentVersion extends RestCommentVersionBaseCreate
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
	
	RestCommentVersion() {
		// For JAXB
		super();
	}
	
	public RestCommentVersion(DynamicSememe<?> commentSememe)
	{
		super(commentSememe.getReferencedComponentNid(),
				commentSememe.getData()[0].getDataObject().toString(),
				(commentSememe.getData().length > 1 && commentSememe.getData()[1] != null) ? commentSememe.getData()[1].getDataObject().toString() : null);
		identifiers = new RestIdentifiedObject(commentSememe.getUuidList());
		commentStamp = new RestStampedVersion(commentSememe);
		if (commentSememe.getAssemblageSequence() != DynamicSememeConstants.get().DYNAMIC_SEMEME_COMMENT_ATTRIBUTE.getConceptSequence())
		{
			throw new RuntimeException("The provided sememe isn't a comment!");
		}
	}
}
