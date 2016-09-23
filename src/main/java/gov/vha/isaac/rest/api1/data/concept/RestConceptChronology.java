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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.util.AlphanumComparator;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestConceptChronology}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestConceptChronology implements Comparable<RestConceptChronology>
{
	/**
	 * The concept sequence identifier of this concept
	 */
	@XmlElement
	int conceptSequence;
	
	/**
	 * The "best" description for this concept.  This is selected based on the attributes within the session for 
	 * stamp and language coordinates - or - if none present - the server default.
	 */
	@XmlElement
	String description;
	
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The identifier data for the object
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	/**
	 * The list of concept versions.  Depending on the expand parameter, may be empty, the latest only, or all versions.
	 */
	@XmlElement
	List<RestConceptVersion> versions = new ArrayList<>();

	protected RestConceptChronology()
	{
		//for JaxB
	}
	
	@SuppressWarnings("rawtypes") 
	public RestConceptChronology(ConceptChronology<? extends ConceptVersion> cc, boolean includeAllVersions, boolean includeLatestVersion)
	{
		conceptSequence = cc.getConceptSequence();
		identifiers = new RestIdentifiedObject(cc.getUuidList());
		
		description = Util.readBestDescription(cc.getNid());
		
		if (includeAllVersions || includeLatestVersion)
		{
			expandables = null;
			if (includeAllVersions)
			{
				for (ConceptVersion cv : cc.getVersionList())
				{
					versions.add(new RestConceptVersion(cv, false, false, false, false, false, false, false));
				}
			}
			else // if (includeLatestVersion)
			{
				@SuppressWarnings("unchecked")
				Optional<LatestVersion<ConceptVersion>> latest = 
						((ConceptChronology)cc).getLatestVersion(ConceptVersion.class, RequestInfo.get().getStampCoordinate());
				if (latest.isPresent())
				{
					versions.add(new RestConceptVersion(latest.get().value(), false, false, false, false, false, false, false));
				}
			}
		}
		else
		{
			versions.clear();
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables = new Expandables(
					new Expandable(ExpandUtil.versionsAllExpandable,
						RestPaths.conceptVersionsAppPathComponent + cc.getConceptSequence() + "/"),
					new Expandable(ExpandUtil.versionsLatestOnlyExpandable,
						RestPaths.conceptVersionAppPathComponent + cc.getConceptSequence() + "/"
						+ "?" + RequestParameters.coordToken + "=" + RequestInfo.get().getCoordinatesToken().getSerialized()));
			}
			else
			{
				expandables = null;
			}
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RestConceptChronology o)
	{
		return AlphanumComparator.compare(description,  o.description,  true);
	}

	/**
	 * @return conceptSequence
	 */
	@XmlTransient
	public int getConceptSequence() {
		return conceptSequence;
	}
	
	/**
	 * @return description
	 */
	@XmlTransient
	public String getDescription() {
		return description;
	}

	/**
	 * @return identifiers
	 */
	@XmlTransient
	public RestIdentifiedObject getIdentifiers() {
		return identifiers;
	}

	/**
	 * @return the versions
	 */
	public List<RestConceptVersion> getVersions() {
		return Collections.unmodifiableList(versions);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestConceptChronology [conceptSequence=" + conceptSequence + ", description=" + description
				+ ", identifiers=" + identifiers + ", versions=" + versions + "]";
	}
}
