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

/**
 * 
 * {@link RestCommentVersionBaseCreate}
 * This stub class is used for callers to create {@link RestCommentVersion} objects.  This class, in combination with {@link RestCommentVersionBase} 
 * contains the fields that can be populated for creation.  
 * 
 * The API never returns this class.

 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestCommentVersionBaseCreate extends RestCommentVersionBase
{	
	/**
	 * The identifier of the object that is being commented on.  Could be a concept or a sememe
	 */
	@XmlElement
	public int commentedItem;

	protected RestCommentVersionBaseCreate()
	{
		//for Jaxb
		super();
	}
	
	public RestCommentVersionBaseCreate(int commentedItem, String comment, String commentContext) {
		super(comment, commentContext);
		
		this.commentedItem = commentedItem;
	}
}
