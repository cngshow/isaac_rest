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
package gov.vha.isaac.rest.api1.data.concept;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestConceptCreateData}
 * This stub class is used for callers to add {@link RestConceptCreateData} objects.  It only contains the fields required or allowed for creation
 * 
 * The API never returns this class.
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptCreateData
{
	
	/**
	 * The primary name of this map set.  
	 */
	@XmlElement
	public String fsn;
	
	/**
	 * The (optional) inverse name of this map set.  Used when a map set is of the pattern:
	 * ingredient-of <--> has-ingredient 
	 */
	@XmlElement
	public String preferredTerm;
	
	/**
	 * The sequences of the parent concepts of this concept. At least one is required.
	 */
	@XmlElement
	public List<Integer> parentIds = new ArrayList<>();

	protected RestConceptCreateData()
	{
		//for Jaxb
	}

	/**
	 * @param fsn
	 * @param preferredTerm
	 * @param parentIds
	 */
	public RestConceptCreateData(String fsn, String preferredTerm, Collection<Integer> parentIds) {
		super();
		this.fsn = fsn;
		this.preferredTerm = preferredTerm;
		this.parentIds.addAll(parentIds);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestNewConceptData [fsn=" + fsn + ", preferredTerm=" + preferredTerm + ", parentIds=" + parentIds + "]";
	}
}
