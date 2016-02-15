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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;

/**
 * 
 * {@link RestSememeVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestSememeDescriptionVersion.class, RestDynamicSememeVersion.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY)
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

	protected RestSememeVersion()
	{
		//For jaxb
	}
	
	public RestSememeVersion(SememeVersion<?> sv, boolean includeChronology) throws RestException
	{
		sememeVersion = new RestStampedVersion(sv);
		if (includeChronology)
		{
			sememeChronology = new RestSememeChronology(sv.getChronology(), false, false);
		}
		else
		{
			if (RequestInfo.get().returnExpandableLinks())
			{
				expandables = new Expandables(
					new Expandable(ExpandUtil.chronologyExpandable,  RestPaths.sememeChronologyAppPathComponent + sv.getChronology().getSememeSequence()));
			}
			else
			{
				expandables = null;
			}
			sememeChronology = null;
		}
	}
	
	public static RestSememeVersion buildRestSememeVersion(SememeVersion<?> sv, boolean includeChronology) throws RestException
	{
		switch(sv.getChronology().getSememeType())
		{
			case COMPONENT_NID:
				return new RestDynamicSememeVersion((ComponentNidSememe<?>) sv, includeChronology);
			case DESCRIPTION:
				return new RestSememeDescriptionVersion((DescriptionSememe<?>) sv, includeChronology);
			case DYNAMIC:
				return new RestDynamicSememeVersion((DynamicSememe<?>) sv, includeChronology);
			case LONG:
				return new RestDynamicSememeVersion((LongSememe<?>) sv, includeChronology);
			case MEMBER:
				return new RestDynamicSememeVersion((SememeVersion<?>) sv, includeChronology);
			case STRING:
				return new RestDynamicSememeVersion((StringSememe<?>) sv, false);
			case LOGIC_GRAPH:
			case RELATIONSHIP_ADAPTOR:
			case UNKNOWN:
			default :
				throw new RestException("Sememe Type " + sv.getChronology().getSememeType() + " not currently supported");
			
		}
	}
}
