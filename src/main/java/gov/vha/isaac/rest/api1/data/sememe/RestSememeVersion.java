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
import java.util.function.Function;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs;
import gov.vha.isaac.rest.api1.sememe.SememeAPIs.SememeVersions;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;

/**
 * 
 * {@link RestSememeVersion}
 * 
 * Note that this is an abstract base class.  The actual returned type will be one of the
 * concrete subtype classes, such as {@link RestSememeDescriptionVersion} or {@link RestDynamicSememeVersion}
 *
 * @see RestSememeDescriptionVersion
 * @see RestDynamicSememeVersion
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestSememeDescriptionVersion.class, RestDynamicSememeVersion.class, RestSememeLogicGraphVersion.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestSememeVersion 
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The sememe chronology for this concept.  Depending on the expand parameter, may be empty.
	 */
	@XmlElement
	RestSememeChronology sememeChronology;
	
	
	/**
	 * The StampedVersion details for this version of this sememe.
	 */
	@XmlElement
	RestStampedVersion sememeVersion;
	
	/**
	 * The nested sememes attached to this sememe.  Not populated by default, include expand=nested to expand these.
	 */
	@XmlElement
	List<RestDynamicSememeVersion> nestedSememes;

	protected RestSememeVersion()
	{
		//For jaxb
	}
	public RestSememeVersion(SememeVersion<?> sv, boolean includeChronology, boolean expandNested, boolean expandReferenced, 
			Function<RestSememeVersion, Boolean> includeInNested) throws RestException
	{
		setup(sv, includeChronology, expandNested, expandReferenced, includeInNested);
	}

	private static String getRequestPathForExpandable(SememeVersion<?> sv) {
		switch (sv.getChronology().getSememeType()) {
		case LOGIC_GRAPH:
			return RestPaths.logicGraphVersionAppPathComponent + sv.getSememeSequence();
		case MEMBER:
		case COMPONENT_NID:
		case LONG:
		case STRING:
		case DYNAMIC:
		case DESCRIPTION:
		case RELATIONSHIP_ADAPTOR:
		case UNKNOWN:
			default:
				return RestPaths.sememeVersionAppPathComponent + sv.getSememeSequence();
		}
	}
	
	protected void setup(SememeVersion<?> sv, boolean includeChronology, boolean expandNested, boolean expandReferenced, Function<RestSememeVersion, Boolean> includeInNested) 
			throws RestException
	{
		sememeVersion = new RestStampedVersion(sv);
		expandables = new Expandables();
		if (includeChronology)
		{
			sememeChronology = new RestSememeChronology(sv.getChronology(), false, false, false, expandReferenced);
		}
		else
		{
			sememeChronology = null;
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables.add(new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
		}
		
		if (!expandReferenced && RequestInfo.get().returnExpandableLinks())
		{
			//No details on this one to follow, there is no clear URL that would fetch all of the details that this convenience adds
			expandables.add(new Expandable(ExpandUtil.referencedDetails, ""));
		}
		
		if (expandNested)
		{
			nestedSememes = new ArrayList<>();
			//Always include the chronology for nested sememes... otherwise, the user would always have to make a return trip to find out what the 
			//nested thing is
			SememeVersions temp = SememeAPIs.get(sv.getNid() + "", null, 1, Integer.MAX_VALUE, true);
			for (SememeVersion<?> nestedSv : temp.getValues())
			{
				RestSememeVersion rsv = RestSememeVersion.buildRestSememeVersion(nestedSv, true, true, expandReferenced);
				if (includeInNested == null || includeInNested.apply(rsv))
				{
					//This cast is expected to be safe - we should never nest a DescriptionSememe under another type of Sememe.
					//In the case where we do have descriptions, the includeInNested function should handle it.
					//Everything else is being treated as a DynamicSememe
					nestedSememes.add((RestDynamicSememeVersion) rsv);
				}
			}
		}
		else
		{
			nestedSememes = null;
			if (RequestInfo.get().returnExpandableLinks() && sv.getChronology().getSememeType() != SememeType.LOGIC_GRAPH)
			{
				expandables.add(new Expandable(ExpandUtil.nestedSememesExpandable, getRequestPathForExpandable(sv) + "?" 
						+ RequestParameters.expand + "=" + ExpandUtil.nestedSememesExpandable + (includeChronology ? "," + ExpandUtil.chronologyExpandable : "")));
			}
		}
		
		if (expandables.size() == 0)
		{
			expandables = null;
		}
	}
	
	public static RestSememeVersion buildRestSememeVersion(SememeVersion<?> sv, boolean includeChronology, boolean expandNested, boolean expandReferenced) throws RestException
	{
		switch(sv.getChronology().getSememeType())
		{
			case COMPONENT_NID:
				return new RestDynamicSememeVersion((ComponentNidSememe<?>) sv, includeChronology, expandNested, expandReferenced);
			case DESCRIPTION:
				return new RestSememeDescriptionVersion((DescriptionSememe<?>) sv, includeChronology, expandNested, expandReferenced);
			case DYNAMIC:
				return new RestDynamicSememeVersion((DynamicSememe<?>) sv, includeChronology, expandNested, expandReferenced);
			case LONG:
				return new RestDynamicSememeVersion((LongSememe<?>) sv, includeChronology, expandNested, expandReferenced);
			case MEMBER:
				return new RestDynamicSememeVersion((SememeVersion<?>) sv, includeChronology, expandNested, expandReferenced);
			case STRING:
				return new RestDynamicSememeVersion((StringSememe<?>) sv, includeChronology, expandNested, expandReferenced);
			case LOGIC_GRAPH:
				return new RestSememeLogicGraphVersion((LogicGraphSememe<?>) sv, includeChronology);
			case RELATIONSHIP_ADAPTOR:
			case UNKNOWN:
			default :
				throw new RestException("Sememe Type " + sv.getChronology().getSememeType() + " not currently supported");
			
		}
	}
}
