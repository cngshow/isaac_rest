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
package gov.vha.isaac.rest.api1.data.sememe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestSememeChronology}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeChronology
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	public Expandables expandables;
	
	/**
	 * The concept sequence identifier of the concept that represents the type of this sememe
	 */
	@XmlElement
	public RestIdentifiedObject assemblage;
	
	/**
	 * The identifier of the object that is referenced by this sememe instance.  This could represent a concept or a sememe.
	 */
	@XmlElement
	public RestIdentifiedObject referencedComponent;
	
	
	/**
	 * If the referencedComponentNid represents a concept, then this carries the "best" description for that concept.  This is selected based on the 
	 * attributes within the session for  stamp and language coordinates - or - if none present - the server default.  This is not populated if the 
	 * referencedComponentNid is a sememe type.
	 * Only populated when the expand parameter 'referencedDetails' is passed.
	 */
	@XmlElement
	public String referencedComponentNidDescription;
	
	/**
	 * The permanent identifiers attached to this sememe instance
	 */
	@XmlElement
	public RestIdentifiedObject identifiers;
	
	/**
	 * The list of sememe versions.  Depending on the expand parameter, may be empty, the latest only, or all versions.
	 */
	@XmlElement
	public List<RestSememeVersion> versions;

	protected RestSememeChronology()
	{
		//For Jaxb
	}

	public RestSememeChronology(SememeChronology<? extends SememeVersion<?>> sc, boolean includeAllVersions, boolean includeLatestVersion, boolean includeNested,
			boolean populateReferencedDetails, UUID processId) throws RestException
	{
		identifiers = new RestIdentifiedObject(sc);
		assemblage= new RestIdentifiedObject(sc.getAssemblageSequence(), ObjectChronologyType.CONCEPT);
		referencedComponent = new RestIdentifiedObject(sc.getReferencedComponentNid());
		if (populateReferencedDetails)
		{
			if (referencedComponent.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
			{
				referencedComponentNidDescription = Util.readBestDescription(referencedComponent.nid);
			}
			else if (referencedComponent.type.enumId == ObjectChronologyType.SEMEME.ordinal())
			{
				@SuppressWarnings("rawtypes")
				SememeChronology<? extends SememeVersion> referencedComponentSememe = Get.sememeService().getSememe(referencedComponent.nid);
				if (SememeType.DESCRIPTION == referencedComponentSememe.getSememeType())
				{
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Optional<LatestVersion<DescriptionSememe>> ds = ((SememeChronology)referencedComponentSememe)
							.getLatestVersion(DescriptionSememe.class, Util.getPreWorkflowStampCoordinate(processId, referencedComponentSememe.getNid()));
					if (ds.isPresent())
					{
						//TODO handle contradictions
						referencedComponentNidDescription = ds.get().value().getText();
					}
				}
				
				if (referencedComponentNidDescription == null)
				{
					referencedComponentNidDescription = "[" + referencedComponentSememe.getSememeType().name() + "]";
				}
			}
		}
		
		if (includeAllVersions || includeLatestVersion)
		{
			expandables = null;
			versions = new ArrayList<>();
			if (includeAllVersions)
			{
				for (SememeVersion<?> sv : sc.getVersionList())
				{
					versions.add(RestSememeVersion.buildRestSememeVersion(sv, false, includeNested, populateReferencedDetails, processId));
				}
			}
			else if (includeLatestVersion)
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<SememeVersion>> latest = 
						((SememeChronology)sc).getLatestVersion(SememeVersion.class, 
								Util.getPreWorkflowStampCoordinate(processId, sc.getNid()));
				if (latest.isPresent())
				{
					versions.add(RestSememeVersion.buildRestSememeVersion(latest.get().value(), false, includeNested, populateReferencedDetails, processId));
				}
			}
		}
		else
		{
			versions = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables = new Expandables(
						new Expandable(ExpandUtil.versionsAllExpandable, 
								RestPaths.sememeVersionsAppPathComponent + sc.getSememeSequence() + "?" + RequestParameters.expand + "=" + ExpandUtil.versionsAllExpandable), 
						new Expandable(
								ExpandUtil.versionsLatestOnlyExpandable, 
								RestPaths.sememeVersionAppPathComponent + sc.getSememeSequence() + "?" + RequestParameters.expand + "=" + ExpandUtil.versionsLatestOnlyExpandable
								+ "&" + RequestParameters.coordToken + "=" + RequestInfo.get().getCoordinatesToken().getSerialized()));
			}
			else
			{
				expandables = null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestSememeChronology [assemblageSequence=" + assemblage
				+ ", referencedComponentNid=" + referencedComponent + ", referencedComponentNidDescription="
				+ referencedComponentNidDescription + ", identifiers=" + identifiers + ", versions=" + versions + "]";
	}
}
