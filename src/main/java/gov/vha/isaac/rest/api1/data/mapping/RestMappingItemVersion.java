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
import java.util.Optional;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.comment.CommentAPIs;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.session.MapSetDisplayFieldsService;
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
public class RestMappingItemVersion extends RestMappingItemVersionBase
{
	private static Logger log = LogManager.getLogger(RestMappingItemVersion.class);
	
	/**
	 * The data that was not expanded as part of this call (but can be)
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Expandables expandables;
	
	/**
	 * The concept that identifies the map set that this entry belongs to.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject mapSetConcept;
	
	/**
	 * The source concept mapped by this map item.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject sourceConcept;
	
	/**
	 * The (optional) target concept being mapped by this map item.  This field is optional, and may be blank, if no target mapping
	 * is available.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject targetConcept;

	/**
	 * An (optional) concept used to qualify this mapping entry.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject qualifierConcept;
	
	/**
	 * The identifier data for the sememe that represents this mapping item
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this mapping entry
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestStampedVersion mappingItemStamp;
	
	/**
	 * The (optionally) populated comments attached to this map set.  This field is only populated when requested via an 'expand' parameter.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestCommentVersion> comments;
	
	/**
	 * If any computed columns were specified to be included in the definition of this mapset, 
	 * they are returned here.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public List<RestMappingItemComputedDisplayField> computedDisplayFields;
	
	protected RestMappingItemVersion()
	{
		//for Jaxb
		super();
	}

	public RestMappingItemVersion(DynamicSememe<?> sememe, int targetColPosition, int qualifierColPosition, 
			boolean expandDescriptions, boolean expandComments, UUID processId, List<RestMappingSetDisplayField> displayFieldsFromMapSet)
	{
		final StampCoordinate stampCoordinate = RequestInfo.get().getStampCoordinate();
		identifiers = new RestIdentifiedObject(sememe.getChronology());
		mappingItemStamp = new RestStampedVersion(sememe);
		mapSetConcept = new RestIdentifiedObject(sememe.getAssemblageSequence(), ObjectChronologyType.CONCEPT);
		if (Get.identifierService().getChronologyTypeForNid(sememe.getReferencedComponentNid()) != ObjectChronologyType.CONCEPT)
		{
			throw new RuntimeException("Source of the map is not a concept");
		}
		else
		{
			sourceConcept = new RestIdentifiedObject(sememe.getReferencedComponentNid(), ObjectChronologyType.CONCEPT);
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
						new RestIdentifiedObject(((DynamicSememeUUID) data[i]).getDataUUID())
						: null);
					offset++;
				}
				else if (i == qualifierColPosition)
				{
					qualifierConcept = ((data[i] != null) ? 
							new RestIdentifiedObject(((DynamicSememeUUID) data[i]).getDataUUID()) 
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

		if (displayFieldsFromMapSet != null && displayFieldsFromMapSet.size() > 0) {
			for (RestMappingSetDisplayField fieldFromMapSet : displayFieldsFromMapSet) {
				if (computedDisplayFields == null) {
					computedDisplayFields = new ArrayList<>();
				}
				//Only need to return these for computed fields...
				if (fieldFromMapSet.componentType.enumId != MapSetItemComponent.ITEM_EXTENDED.ordinal())
				{
					computedDisplayFields.add(constructDisplayField(mapSetConcept.nid, sourceConcept.nid, targetConcept.nid, qualifierConcept.nid, 
						fieldFromMapSet.id, MapSetItemComponent.valueOf(fieldFromMapSet.componentType.enumName)));
				}
			}
		}

		expandables = new Expandables();
		if (expandDescriptions)
		{
//			if (qualifierConcept != null)
//			{
//				qualifierDescription = Util.readBestDescription(qualifierConcept.sequence, stampCoordinate);
//			}
//			sourceDescription = Util.readBestDescription(sourceConcept.sequence, stampCoordinate);
//			if (targetConcept != null)
//			{
//				targetDescription = Util.readBestDescription(targetConcept.sequence, stampCoordinate);
//			}
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
				comments = CommentAPIs.readComments(sememe.getNid() + "", processId, stampCoordinate);
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

	private static RestMappingItemComputedDisplayField constructDisplayField(int mapSetNid, int sourceConceptNid, int targetConceptNid, int qualifierConceptNid, 
			String fieldId, MapSetItemComponent componentType) {
		Integer componentNid = null;
		String value = null;
		switch(componentType) {
		case SOURCE:
			componentNid = sourceConceptNid;
			break;
		case TARGET:
			componentNid = targetConceptNid;
			break;
		case QUALIFIER:
			componentNid = qualifierConceptNid;
			break;
		default:
			String msg = "Invalid/unsupported MapSetItemComponent value \"" + componentType + "\".  Should be one of " + MapSetItemComponent.values();
			log.error(msg);
			throw new RuntimeException(msg);
		}
		/*
		 * Fields must correspond to entries returned by MapSetDisplayFieldsService.getAllFields()
		 */
		UUID annotationConceptUuid = null;
		try {
			annotationConceptUuid = UUID.fromString(fieldId);
		} catch (Exception e) {
			// ignore
		}
		if (annotationConceptUuid != null) {
			Optional<String> valueOptional = Frills.getAnnotationStringValue(componentNid, Get.conceptService().getConcept(annotationConceptUuid).getNid(), 
					RequestInfo.get().getStampCoordinate());
			// TODO handle missing values and contradictions
			value = valueOptional.isPresent() ? valueOptional.get() : null;
		}
		else if (fieldId.equals(MapSetDisplayFieldsService.Field.NonConceptFieldName.DESCRIPTION.name())) {
			Optional<String> descLatestVersion = Frills.getDescription(componentNid, RequestInfo.get().getStampCoordinate(), RequestInfo.get().getLanguageCoordinate());
			// TODO handle missing values and contradictions
			value = descLatestVersion.isPresent() ? descLatestVersion.get() : null;
		} else {
			throw new RuntimeException("Unsupported/unexpected map set field \"" + fieldId + "\"");
		}
		
		try {
			return new RestMappingItemComputedDisplayField(fieldId, componentType, value);
		} catch (RestException e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestMappingItemVersion [expandables=" + expandables + ", identifiers=" + identifiers
				+ ", mappingItemStamp=" + mappingItemStamp
//				+ ", sourceDescription=" + sourceDescription
//				+ ", targetDescription=" + targetDescription
//				+ ", qualifierDescription=" + qualifierDescription
				+ ", mapSetConcept=" + mapSetConcept
				+ ", sourceConcept=" + sourceConcept + ", targetConcept=" + targetConcept + ", qualifierConcept="
				+ qualifierConcept + ", mapItemExtendedFields=" + mapItemExtendedFields + "]";
	}
}
