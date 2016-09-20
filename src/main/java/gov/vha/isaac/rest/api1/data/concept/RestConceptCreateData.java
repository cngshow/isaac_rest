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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
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
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptCreateData
{
	/**
	 * The sequences of the parent concepts of this concept. At least one is required.
	 */
	@XmlElement
	Set<Integer> parentConceptIds = new HashSet<>();

	/**
	 * The required Fully Specified Name description of this concept.  
	 */
	@XmlElement
	String fsn;
	
	/**
	 * The required Preferred Term description of this concept.
	 */
	@XmlElement
	String preferredTerm;

	/**
	 * The language concept associated with the required descriptions
	 */
	@XmlElement
	int requiredDescriptionsLanguageConceptId;
	
	/**
	 * The preferred dialects associated with the required descriptions
	 */
	@XmlElement
	Collection<Integer> requiredDescriptionsPreferredInDialectAssemblagesConceptIds = new HashSet<>();

	/**
	 * The acceptable dialects associated with the required descriptions
	 */
	@XmlElement
	Collection<Integer> requiredDescriptionsAcceptableInDialectAssemblagesConceptIds = new HashSet<>();
	
	/**
	 * An optional extended description type applying to required descriptions
	 */
	@XmlElement
	Integer requiredDescriptionsExtendedTypeConceptId = null;
	
	protected RestConceptCreateData()
	{
		//for Jaxb
	}

	/**
	 * @param parentConceptIds
	 * @param fsn
	 * @param preferredTerm
	 * @param requiredDescriptionsLanguageConceptId
	 * @param requiredDescriptionsPreferredDialectsConceptIds
	 * @param requiredDescriptionsAcceptableDialectsConceptIds
	 */
	public RestConceptCreateData(
			Collection<Integer> parentConceptIds,
			String fsn,
			String preferredTerm,
			int requiredDescriptionsLanguageConceptId,
			Integer requiredDescriptionsExtendedTypeId,
			Collection<Integer> requiredDescriptionsPreferredDialects,
			Collection<Integer> requiredDescriptionsAcceptableDialects) {
		super();
		if (parentConceptIds.size() < 1) {
			throw new IllegalArgumentException("At least one parent concept sequence is required");
		}
		this.parentConceptIds.addAll(parentConceptIds);
		this.fsn = fsn;
		this.preferredTerm = preferredTerm;
		this.requiredDescriptionsLanguageConceptId = requiredDescriptionsLanguageConceptId;
		
		this.requiredDescriptionsExtendedTypeConceptId = requiredDescriptionsExtendedTypeId;
		if (requiredDescriptionsPreferredDialects != null) {
			this.requiredDescriptionsPreferredInDialectAssemblagesConceptIds.addAll(requiredDescriptionsPreferredDialects);
		}
		if (requiredDescriptionsAcceptableDialects != null) {
			this.requiredDescriptionsAcceptableInDialectAssemblagesConceptIds.addAll(requiredDescriptionsAcceptableDialects);
		}
	}
	
	/**
	 * @param parentConceptIds
	 * @param fsn
	 * @param preferredTerm
	 * @param requiredDescriptionsLanguageConceptId
	 */
	public RestConceptCreateData(
			Collection<Integer> parentConceptIds,
			String fsn,
			String preferredTerm,
			int requiredDescriptionsLanguageConceptId) {
		this(
				parentConceptIds,
				fsn,
				preferredTerm,
				requiredDescriptionsLanguageConceptId,
				(Integer)null,
				(Collection<Integer>)null,
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
	 * @return the preferredTerm
	 */
	@XmlTransient
	public String getPreferredTerm() {
		return preferredTerm;
	}

	/**
	 * @return the requiredDescriptionsLanguageConceptId
	 */
	@XmlTransient
	public int getRequiredDescriptionsLanguageConceptId() {
		return requiredDescriptionsLanguageConceptId;
	}

	/**
	 * @return the requiredDescriptionsExtendedTypeConceptId
	 */
	@XmlTransient
	public Integer getRequiredDescriptionsExtendedTypeConceptId() {
		return requiredDescriptionsExtendedTypeConceptId;
	}

	/**
	 * @return the requiredDescriptionsPreferredInDialectAssemblagesConceptIds
	 */
	@XmlTransient
	public Collection<Integer> getRequiredDescriptionsPreferredInDialectAssemblagesConceptIds() {
		return Collections.unmodifiableCollection(requiredDescriptionsPreferredInDialectAssemblagesConceptIds);
	}

	/**
	 * @return the requiredDescriptionsAcceptableInDialectAssemblagesConceptIds
	 */
	@XmlTransient
	public Collection<Integer> getRequiredDescriptionsAcceptableInDialectAssemblagesConceptIds() {
		return Collections.unmodifiableCollection(requiredDescriptionsAcceptableInDialectAssemblagesConceptIds);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestConceptCreateData ["
				+ "fsn=" + fsn + ", preferredTerm=" + preferredTerm
				+ ", requiredDescriptionsLanguageSequence=" + requiredDescriptionsLanguageConceptId
				+ ", requiredDescriptionsExtendedTypeId=" + requiredDescriptionsExtendedTypeConceptId
				+ ", requiredDescriptionsPreferredInDialectAssemblagesConceptIds=" + requiredDescriptionsPreferredInDialectAssemblagesConceptIds
				+ ", requiredDescriptionsAcceptableInDialectAssemblagesConceptIds=" + requiredDescriptionsAcceptableInDialectAssemblagesConceptIds
				+ ", parentConceptIds=" + parentConceptIds + "]";
	}
}
