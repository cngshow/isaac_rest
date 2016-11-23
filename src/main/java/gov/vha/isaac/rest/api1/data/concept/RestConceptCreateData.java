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
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestConceptCreateData.class)
public class RestConceptCreateData
{
	/**
	 * The concept identifiers (UUID, nid or sequence) of the parent concepts of this concept. At least one is required.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Set<String> parentConceptIds = new HashSet<>();

	/**
	 * The required Fully Specified Name description of this concept.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String fsn;
	
	/**
	 * If set to true, and if the provided parentConceptIds all represent concepts that have a common semantic tag, then a semantic tag will be appended to 
	 * the FSN which matches the semantic tag of the parent concept(s).  Additionally, a preferred term description will be created with the exact value provided
	 * by the FSN.  If the parent concepts have multiple semantic tags, an error will be thrown.
	 * If not set, or set to false, then:
	 *   - if the fsn value contains a semantic tag: the FSN will be created exactly as specified, and a preferred term will be created by stripping the semantic tag
	 *     from the provided value.
	 *   - if the fsn value does not contain a semantic tag: FSN description will be created and it will carry the exact value of the FSN.  No preferred term will be created 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean calculateSemanticTag;

	/**
	 * The optional language concept (uuid, nid or sequence) associated with the required descriptions.  Will be set to 
	 * ENGLISH if not specified
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String descriptionLanguageConceptId;
	
	/**
	 * The optional concept identifiers (uuid, nid or sequence) of the preferred dialects associated with the required description.
	 * A default of US ENGLISH will be assigned if not set.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Collection<String> descriptionPreferredInDialectAssemblagesConceptIds = new HashSet<>();
	
	/**
	 * An optional concept identifier (nid, sequence or UUID) of a concept that represents an extended type of the description.  
	 * This will be applied to the preferred description created on the concept if {@link #calculateSemanticTag} is true.  Will be applied to 
	 * the FSN description created onthe concept if  {@link #calculateSemanticTag} is false or absent.
	 * This may be a concept like Abbreviation or Vista Name
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String extendedDescriptionTypeConcept = null;
	
	/**
	 * True to indicate the concept should be set as active, false for inactive.  
	 * This field is optional, if not provided, it will be assumed to be active.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Boolean active;
	
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
			Collection<String> parentConceptIds,
			String fsn,
			boolean createSemanticTag,
			String descriptionsLanguageConceptId,
			String descriptionsExtendedTypeId,
			Collection<String> descriptionsPreferredDialects) {
		super();
		if (parentConceptIds.size() < 1) {
			throw new IllegalArgumentException("At least one parent concept sequence is required");
		}
		this.parentConceptIds.addAll(parentConceptIds);
		this.fsn = fsn;
		this.calculateSemanticTag = createSemanticTag;
		this.descriptionLanguageConceptId = descriptionsLanguageConceptId;
		
		this.extendedDescriptionTypeConcept = descriptionsExtendedTypeId;
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
			Collection<String> parentConceptIds,
			String fsn,
			boolean createSemanticTag,
			String descriptionLanguageConceptId) {
		this(
				parentConceptIds,
				fsn,
				createSemanticTag,
				descriptionLanguageConceptId,
				(String)null,
				(Collection<String>)null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestConceptCreateData ["
				+ "fsn=" + fsn
				+ ", descriptionLanguageSequence=" + descriptionLanguageConceptId
				+ ", extendedDescriptionTypeConcept=" + extendedDescriptionTypeConcept
				+ ", descriptionPreferredInDialectAssemblagesConceptIds=" + descriptionPreferredInDialectAssemblagesConceptIds
				+ ", parentConceptIds=" + parentConceptIds + "]";
	}
}
