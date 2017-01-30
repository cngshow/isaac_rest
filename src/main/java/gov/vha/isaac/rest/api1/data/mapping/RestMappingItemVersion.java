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
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api.data.Expandable;
import gov.vha.isaac.rest.api.data.Expandables;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.comment.CommentAPIs;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.RestStampedVersion;
import gov.vha.isaac.rest.api1.data.comment.RestCommentVersion;
import gov.vha.isaac.rest.api1.data.enumerations.MapSetItemComponent;
import gov.vha.isaac.rest.api1.data.enumerations.RestMapSetItemComponentType;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeData;
import gov.vha.isaac.rest.api1.mapping.MappingAPIs;
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
	public RestIdentifiedObject identifiers;
	
	/**
	 * The StampedVersion details for this mapping entry
	 */
	@XmlElement
	public RestStampedVersion mappingItemStamp;
	
	
	/**
	 * An (optional) description of the {@link #sourceConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String sourceDescription;
	
	/**
	 * An (optional) description of the {@link #targetConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String targetDescription;
	
	/**
	 * An (optional) description of the {@link #qualifierConcept} - only populated when requested via the expandable 'referencedDetails'
	 */
	@XmlElement
	public String qualifierDescription;
	
	/**
	 * The (optionally) populated comments attached to this map set.  This field is only populated when requested via an 'expand' parameter.
	 */
	@XmlElement
	public List<RestCommentVersion> comments;
	
	/**
	 * Optional list of fields available for ordering and display.
	 * Populated and ordered based on respective fields, if any, in corresponding map set.
	 */
	@XmlElement
	public List<RestMappingSetDisplayField> displayFields;
	
	protected RestMappingItemVersion()
	{
		//for Jaxb
		super();
	}

	public RestMappingItemVersion(DynamicSememe<?> sememe, int targetColPosition, int qualifierColPosition, 
			boolean expandDescriptions, boolean expandComments, UUID processId)
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

		List<RestMappingSetDisplayField> displayFieldsFromMapSet = MappingAPIs.getMappingSetDisplayFieldsFromMappingSet(mapSetConcept.nid, RequestInfo.get().getStampCoordinate());
		if (displayFieldsFromMapSet != null && displayFieldsFromMapSet.size() > 0) {
			for (RestMappingSetDisplayField fieldFromMapSet : displayFieldsFromMapSet) {
				if (displayFields == null) {
					displayFields = new ArrayList<>();
				}
				displayFields.add(constructDisplayField(sourceConcept.nid, targetConcept.nid, fieldFromMapSet.name, MapSetItemComponent.valueOf(fieldFromMapSet.componentType.enumName)));
			}
		}

		expandables = new Expandables();
		if (expandDescriptions)
		{
			if (qualifierConcept != null)
			{
				qualifierDescription = Util.readBestDescription(qualifierConcept.sequence, stampCoordinate);
			}
			sourceDescription = Util.readBestDescription(sourceConcept.sequence, stampCoordinate);
			if (targetConcept != null)
			{
				targetDescription = Util.readBestDescription(targetConcept.sequence, stampCoordinate);
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

	private static RestMappingSetDisplayField constructDisplayField(int sourceConceptNid, int targetConceptNid, String fieldName, MapSetItemComponent componentType) {
		Integer componentNid = null;
		String value = null;
		switch(componentType) {
		case SOURCE:
			componentNid = sourceConceptNid;
			break;
		case TARGET:
			componentNid = targetConceptNid;
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
			annotationConceptUuid = UUID.fromString(fieldName);
		} catch (Exception e) {
			// ignore
		}
		if (annotationConceptUuid != null) {
			Optional<String> valueOptional = Frills.getAnnotationStringValue(componentNid, Get.conceptService().getConcept(annotationConceptUuid).getNid(), RequestInfo.get().getStampCoordinate());
			// TODO handle missing values and contradictions
			value = valueOptional.isPresent() ? valueOptional.get() : null;
		}
		else if (fieldName.equals(MapSetDisplayFieldsService.Field.NonConceptFieldName.FULLY_SPECIFIED_NAME.name())) {
			ConceptChronology<?> cc = Get.conceptService().getConcept(componentNid);
			Optional<LatestVersion<DescriptionSememe<?>>> descLatestVersion = cc.getFullySpecifiedDescription(RequestInfo.get().getLanguageCoordinate(), RequestInfo.get().getStampCoordinate());
			// TODO handle missing values and contradictions
			value = descLatestVersion.isPresent() ? descLatestVersion.get().value().getText() : null;
		} else if (fieldName.equals(MapSetDisplayFieldsService.Field.NonConceptFieldName.PREFERRED_TERM.name())) {
			ConceptChronology<?> cc = Get.conceptService().getConcept(componentNid);
			Optional<LatestVersion<DescriptionSememe<?>>> descLatestVersion = cc.getPreferredDescription(RequestInfo.get().getLanguageCoordinate(), RequestInfo.get().getStampCoordinate());
			 // TODO handle missing values and contradictions
			value = descLatestVersion.isPresent() ? descLatestVersion.get().value().getText() : null;
		} else {
			throw new RuntimeException("Unsupported/unexpected map set field \"" + fieldName + "\"");
		}
		
		try {
			return new RestMappingSetDisplayField(fieldName, value, new RestMapSetItemComponentType(componentType));
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
				+ ", sourceDescription=" + sourceDescription + ", targetDescription=" + targetDescription
				+ ", qualifierDescription=" + qualifierDescription + ", mapSetConcept=" + mapSetConcept
				+ ", sourceConcept=" + sourceConcept + ", targetConcept=" + targetConcept + ", qualifierConcept="
				+ qualifierConcept + ", mapItemExtendedFields=" + mapItemExtendedFields + "]";
	}
}
