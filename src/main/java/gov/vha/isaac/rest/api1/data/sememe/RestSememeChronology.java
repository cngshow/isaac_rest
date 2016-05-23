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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestSememeChronology}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestSememeChronology
{
	
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The sememe sequence identifier of this sememe instance
	 */
	@XmlElement
	int sememeSequence;
	
	/**
	 * The concept sequence identifier of the concept that represents the type of this sememe
	 */
	@XmlElement
	int assemblageSequence;
	
	/**
	 * The NID identifier of the object that is referenced by this sememe instance.  This could represent a concept or a sememe.
	 */
	@XmlElement
	int referencedComponentNid;
	
	/**
	 * The type of the object that is referenced by the referencedComponentNid value.  This would tell you if the nid represents a concept or a sememe.
	 * Only populated when the expand parameter 'referencedDetails' is passed.
	 */
	@XmlElement
	RestObjectChronologyType referencedComponentNidObjectType;
	
	/**
	 * If the referencedComponentNid represents a concept, then this carries the "best" description for that concept.  This is selected based on the 
	 * attributes within the session for  stamp and language coordinates - or - if none present - the server default.  This is not populated if the 
	 * referencedComponentNid is a sememe type.
	 * Only populated when the expand parameter 'referencedDetails' is passed.
	 */
	@XmlElement
	String referencedComponentNidDescription;
	
	/**
	 * The permanent identifier object(s) attached to this sememe instance
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	/**
	 * The list of sememe versions.  Depending on the expand parameter, may be empty, the latest only, or all versions.
	 */
	@XmlElement
	List<RestSememeVersion> versions;

	protected RestSememeChronology()
	{
		//For Jaxb
	}

	public RestSememeChronology(SememeChronology<? extends SememeVersion<?>> sc, boolean includeAllVersions, boolean includeLatestVersion, boolean includeNested,
			boolean populateReferencedDetails) throws RestException
	{
		identifiers = new RestIdentifiedObject(sc.getUuidList());
		sememeSequence = sc.getSememeSequence();
		assemblageSequence = sc.getAssemblageSequence();
		referencedComponentNid = sc.getReferencedComponentNid();
		if (populateReferencedDetails)
		{
			ObjectChronologyType cronType = Get.identifierService().getChronologyTypeForNid(referencedComponentNid);
			referencedComponentNidObjectType = new RestObjectChronologyType(cronType);
			if (cronType == ObjectChronologyType.CONCEPT)
			{
				referencedComponentNidDescription = RestConceptChronology.readBestDescription(referencedComponentNid);
			}
			else if (cronType == ObjectChronologyType.SEMEME)
			{
				@SuppressWarnings("rawtypes")
				SememeChronology<? extends SememeVersion> referencedComponentSememe = Get.sememeService().getSememe(referencedComponentNid);
				if (SememeType.DESCRIPTION == referencedComponentSememe.getSememeType())
				{
					@SuppressWarnings({ "rawtypes", "unchecked" })
					Optional<LatestVersion<DescriptionSememe>> ds = ((SememeChronology)referencedComponentSememe)
							.getLatestVersion(DescriptionSememe.class, RequestInfo.get().getStampCoordinate());
					if (ds.isPresent())
					{
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
					versions.add(RestSememeVersion.buildRestSememeVersion(sv, false, includeNested, populateReferencedDetails));
				}
			}
			else if (includeLatestVersion)
			{
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<SememeVersion>> latest = 
						((SememeChronology)sc).getLatestVersion(SememeVersion.class, RequestInfo.get().getStampCoordinate());
				if (latest.isPresent())
				{
					versions.add(RestSememeVersion.buildRestSememeVersion(latest.get().value(), false, includeNested, populateReferencedDetails));
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
						new Expandable(ExpandUtil.versionsLatestOnlyExpandable, 
								RestPaths.sememeVersionAppPathComponent + sc.getSememeSequence() + "?" + RequestParameters.expand + "=" + ExpandUtil.versionsLatestOnlyExpandable));
			}
			else
			{
				expandables = null;
			}
		}
	}
}
