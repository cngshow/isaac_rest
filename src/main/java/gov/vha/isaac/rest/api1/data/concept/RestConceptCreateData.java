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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link RestConceptCreateData}
 * This class is used for callers to add {@link RestConceptCreateData} objects.  It only contains the fields required or allowed for creation
 * 
 * The API never returns this class.
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptCreateData
{
	/**
	 * The sequences of the parent concepts of this concept. At least one is required.
	 */
	@XmlElement
	@JsonInclude
	Set<Integer> parentConceptIds = new HashSet<>();

	/**
	 * The required Fully Specified Name description of this concept.  
	 */
	@XmlElement
	@JsonInclude
	String fsn;

	/**
	 * The required language concept associated with the required descriptions
	 */
	@XmlElement
	@JsonInclude
	int descriptionLanguageConceptId;
	
	/**
	 * The optional preferred dialects associated with the required description.
	 * A default will be assigned if not set.
	 */
	@XmlElement
	@JsonInclude
	Collection<Integer> descriptionPreferredInDialectAssemblagesConceptIds = new HashSet<>();
	
	/**
	 * An optional extended description type applying to required descriptions
	 */
	@XmlElement
	@JsonInclude
	Integer descriptionExtendedTypeConceptId = null;
	
	protected RestConceptCreateData()
	{
		//for Jaxb
	}

	/**
	 * @param parentConceptIds
	 * @param fsn
	 * @param descriptionLanguageConceptId
	 * @param descriptionExtendedTypeId (optional)
	 * @param descriptionPreferredDialectsConceptIds (optional)
	 */
	public RestConceptCreateData(
			Collection<Integer> parentConceptIds,
			String fsn,
			int descriptionsLanguageConceptId,
			Integer descriptionsExtendedTypeId,
			Collection<Integer> descriptionsPreferredDialects) {
		super();
		if (parentConceptIds.size() < 1) {
			throw new IllegalArgumentException("At least one parent concept sequence is required");
		}
		this.parentConceptIds.addAll(parentConceptIds);
		this.fsn = fsn;
		this.descriptionLanguageConceptId = descriptionsLanguageConceptId;
		
		this.descriptionExtendedTypeConceptId = descriptionsExtendedTypeId;
		if (descriptionsPreferredDialects != null) {
			this.descriptionPreferredInDialectAssemblagesConceptIds.addAll(descriptionsPreferredDialects);
		}
	}
	
	/**
	 * @param parentConceptIds
	 * @param fsn
	 * @param descriptionLanguageConceptId
	 */
	public RestConceptCreateData(
			Collection<Integer> parentConceptIds,
			String fsn,
			int descriptionLanguageConceptId) {
		this(
				parentConceptIds,
				fsn,
				descriptionLanguageConceptId,
				(Integer)null,
				(Collection<Integer>)null);
	}

	/**
	 * @return the parentConceptIds
	 */
	@XmlTransient
	public Set<Integer> getParentConceptIds() {
		return Collections.unmodifiableSet(parentConceptIds);
	}

	/**
	 * @return the fsn
	 */
	@XmlTransient
	public String getFsn() {
		return fsn;
	}

	/**
	 * @return the descriptionLanguageConceptId
	 */
	@XmlTransient
	public int getDescriptionLanguageConceptId() {
		return descriptionLanguageConceptId;
	}

	/**
	 * @return the descriptionExtendedTypeConceptId
	 */
	@XmlTransient
	public Integer getDescriptionExtendedTypeConceptId() {
		return descriptionExtendedTypeConceptId;
	}

	/**
	 * @return the descriptionPreferredInDialectAssemblagesConceptIds
	 */
	@XmlTransient
	public Collection<Integer> getDescriptionPreferredInDialectAssemblagesConceptIds() {
		return Collections.unmodifiableCollection(descriptionPreferredInDialectAssemblagesConceptIds);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestConceptCreateData ["
				+ "fsn=" + fsn
				+ ", descriptionLanguageSequence=" + descriptionLanguageConceptId
				+ ", descriptionExtendedTypeId=" + descriptionExtendedTypeConceptId
				+ ", descriptionPreferredInDialectAssemblagesConceptIds=" + descriptionPreferredInDialectAssemblagesConceptIds
				+ ", parentConceptIds=" + parentConceptIds + "]";
	}
}
