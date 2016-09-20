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
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestCommentVersionBase}
 * This stub class is used for callers to edit {@link RestCommentVersion} objects.  It only contains the fields they may be edited after creation..
 * 
 * The API never returns this class.

 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestCommentVersionBase
{
	/**
	 * The comment
	 */
	@XmlElement
	String comment;

	/**
	 * An (optional) comment context to store with the comment.  Typically used for key words, etc. 
	 */
	@XmlElement
	String commentContext;

	protected RestCommentVersionBase()
	{
		//for Jaxb
	}
	
	public RestCommentVersionBase(String comment, String commentContext) {
		this.comment = comment;
		this.commentContext = commentContext;
	}

	/**
	 * @return the comment
	 */
	@XmlTransient
	public String getComment() {
		return comment;
	}

	/**
	 * @return the commentContext
	 */
	@XmlTransient
	public String getCommentContext() {
		return commentContext;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestCommentVersionBase [comment=" + comment + ", commentContext=" + commentContext + "]";
	}
}
