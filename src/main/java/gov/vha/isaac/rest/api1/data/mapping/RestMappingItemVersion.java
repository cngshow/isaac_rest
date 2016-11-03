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
package gov.vha.isaac.rest.api1.data.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.logging.log4j.LogManager;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.comment.CommentAPIs;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * 
 * {@link RestMappingItemVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestMappingItemVersion extends RestMappingItemVersionBaseCreate implements Comparable<RestMappingItemVersion>
{
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	Expandables expandables;
	
	/**
	 * The sememe sequence of the sememe that represents this mapping item
	 */
	@XmlElement
	public int sememeSequence;
	
	/**
	 * The identifier data for the object
	 */
	@XmlElement
	RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this mapping entry
	 */
	@XmlElement
	RestStampedVersion mappingItemStamp;
	
	
	/**
	 * An (optional) description of the {@link #sourceConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	String sourceDescription;
	
	/**
	 * An (optional) description of the {@link #targetConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	String targetDescription;
	
	/**
	 * An (optional) description of the {@link #qualifierConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	String qualifierDescription;
	
	/**
	 * The (optionally) populated comments attached to this map set.  This field is only populated when requested via an 'expand' parameter.
	 */
	@XmlElement
	List<RestCommentVersion> comments;
	
		
	protected RestMappingItemVersion()
	{
		//for Jaxb
		super();
	}

	public RestMappingItemVersion(DynamicSememe<?> sememe, StampCoordinate stampCoord, int targetColPosition, int qualifierColPosition, 
			boolean expandDescriptions, boolean expandComments, UUID processId)
	{
		sememeSequence = sememe.getSememeSequence();
		identifiers = new RestIdentifiedObject(sememe.getUuidList());
		mappingItemStamp = new RestStampedVersion(sememe);
		active = sememe.getState() == State.ACTIVE;
		mapSetConcept = sememe.getAssemblageSequence();
		if (Get.identifierService().getChronologyTypeForNid(sememe.getReferencedComponentNid()) != ObjectChronologyType.CONCEPT)
		{
			throw new RuntimeException("Source of the map is not a concept");
		}
		else
		{
			sourceConcept = Get.identifierService().getConceptSequence(sememe.getReferencedComponentNid());
		}
		
		DynamicSememeData[] data = sememe.getData();
		int offset = 0;
		
		if (data != null)
		{
			mapItemExtendedFields = new ArrayList<>();
			for (int i = 0; i < data.length; i++)
			{
				if (i == targetColPosition)
				{
					targetConcept = ((data[i] != null) ? 
						Get.identifierService().getConceptSequenceForUuids(((DynamicSememeUUID) data[i]).getDataUUID())
						: null);
					offset++;
				}
				else if (i == qualifierColPosition)
				{
					qualifierConcept = ((data[i] != null) ? 
							Get.identifierService().getConceptSequenceForUuids(((DynamicSememeUUID) data[i]).getDataUUID()) 
							: null);
					offset++;
				}
				else
				{
					RestDynamicSememeData rdsd = RestDynamicSememeData.translate(i, data[i]);
					if (rdsd != null)
					{
						rdsd.columnNumber = i - offset;  //renumber, to match with the numbers we are removing.
					}
					mapItemExtendedFields.add(rdsd);
				}
			}
		}

		expandables = new Expandables();
		if (expandDescriptions)
		{
			if (qualifierConcept != null)
			{
				qualifierDescription = Util.readBestDescription(qualifierConcept);
			}
			sourceDescription = Util.readBestDescription(sourceConcept);
			if (targetConcept != null)
			{
				targetDescription = Util.readBestDescription(targetConcept);
			}
		}
		else
		{
			if (RequestInfo.get().returnExpandableLinks())
			{
				//TODO fix this expandable link
				expandables.add(new Expandable(ExpandUtil.referencedDetails, ""));
			}
		}
		if (expandComments)
		{
			try
			{
				comments = CommentAPIs.readComments(sememe.getNid() + "", processId);
			}
			catch (RestException e)
			{
				LogManager.getLogger().error("Unexpected", e);
				throw new RuntimeException(e);
			}
		}
		else
		{
			if (RequestInfo.get().returnExpandableLinks())
			{
				//TODO fix this expandable link
				expandables.add(new Expandable(ExpandUtil.comments, ""));
			}
		}
		if (expandables.size() == 0)
		{
			expandables = null;
		}
	}

	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RestMappingItemVersion o)
	{
		// TODO implement sorting
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingItemVersion [expandables=" + expandables + ", identifiers=" + identifiers
				+ ", mappingItemStamp=" + mappingItemStamp
				+ ", sourceDescription=" + sourceDescription + ", targetDescription=" + targetDescription
				+ ", qualifierDescription=" + qualifierDescription + ", mapSetConcept=" + mapSetConcept
				+ ", sourceConcept=" + sourceConcept + ", targetConcept=" + targetConcept + ", qualifierConcept="
				+ qualifierConcept + ", mapItemExtendedFields=" + mapItemExtendedFields + "]";
	}

	/**
	 * @return the identifiers
	 */
	@XmlTransient
	public RestIdentifiedObject getIdentifiers() {
		return identifiers;
	}

	/**
	 * @return the mappingItemStamp
	 */
	@XmlTransient
	public RestStampedVersion getMappingItemStamp() {
		return mappingItemStamp;
	}

	/**
	 * @return the sourceDescription
	 */
	public String getSourceDescription() {
		return sourceDescription;
	}

	/**
	 * @return the targetDescription
	 */
	@XmlTransient
	public String getTargetDescription() {
		return targetDescription;
	}

	/**
	 * @return the qualifierDescription
	 */
	@XmlTransient
	public String getQualifierDescription() {
		return qualifierDescription;
	}
}
