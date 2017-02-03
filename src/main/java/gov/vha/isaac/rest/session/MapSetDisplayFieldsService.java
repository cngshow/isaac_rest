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

package gov.vha.isaac.rest.session;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;

/**
 * 
 * {@link MapSetDisplayFieldsService}
 * 
 * Return available immutable map set display fields for use in ordering and displaying map set data
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@RunLevel(LookupService.ISAAC_DEPENDENTS_RUNLEVEL)
@Service
public class MapSetDisplayFieldsService {
public static class Field {
		public static enum NonConceptFieldName {
			DESCRIPTION("Description");
			
			private String description;
			
			private NonConceptFieldName(String description) {
				this.description = description;
			}
			
			public String getDescription() {
				return description;
			}
		}
		
		private final String id;
		private final IdentifiedObject object;

		private Field(IdentifiedObject object) {
			this(object.getPrimordialUuid().toString(), object);
		}
		
		private Field(String name) {
			this(name, null);
		}

		/**
		 * @param name
		 * @param computed
		 * @param object
		 */
		private Field(String id, IdentifiedObject object) {
			super();
			this.id = id;
			this.object = object;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the object, if any
		 */
		public IdentifiedObject getObject() {
			return object;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Field [id=" + id + ", object=" + object + "]";
		}
	}
	
	private static Logger log = LogManager.getLogger(MapSetDisplayFieldsService.class);
	
	private Map<String, Field> fields_;
	
	MapSetDisplayFieldsService() {
		// For HK2
	}

	private Map<String, Field> getFields() {
		synchronized (fields_) {
			if (fields_.size() == 0) {
				/*
				 * All added fields must be handlable by code in RestMappingItemVersion constructor
				 */

				// Non-concept fields
				add(MapSetDisplayFieldsService.Field.NonConceptFieldName.DESCRIPTION.name());

				for (ConceptChronology<?> cc : getAnnotationConcepts(StampCoordinates.getDevelopmentLatest())) {
					add(cc);
				}

				// TODO Find a way to find and add these assemblages automatically
				add(MetaData.SCTID);
				add(MetaData.LOINC_NUM);
				add(MetaData.RXCUI);
				add(MetaData.VUID);
				add(MetaData.CODE);
			}

			return fields_;
		}
	}
	
	public void invalidateCache() {
		getFields().clear();
	}

	public Collection<Field> getAllFields() {
		return Collections.unmodifiableCollection(getFields().values());
	}
	public Set<String> getAllGlobalFieldIds() {
		return Collections.unmodifiableSet(getFields().keySet());
	}
	public Field getFieldByConceptIdOrStringIdIfNotConceptId(String stringOrConceptId) {
		if (getFields().get(stringOrConceptId) != null) {
			return getFields().get(stringOrConceptId);
		}

		try {
			int intId = Integer.parseInt(stringOrConceptId.trim());
			Optional<UUID> uuid = Optional.empty();

			uuid = Get.identifierService().getUuidPrimordialFromConceptId(intId);
			if (uuid.isPresent()) {
				return getFields().get(uuid.get().toString());
			}
			
			uuid = Get.identifierService().getUuidPrimordialFromSememeId(intId);
			if (uuid.isPresent()) {
				return getFields().get(uuid.get().toString());
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}

	synchronized private void add(IdentifiedObject object) {
		Field field = new Field(object);
		fields_.put(field.id, field);
	}
	synchronized private void add(String id) {
		Field field = new Field(id, null);
		fields_.put(field.id, field);
	}

	private static Set<ConceptChronology<?>> getAnnotationConcepts(StampCoordinate sc) {
		Map<Integer, Set<Integer>> extensionDefinitionsByAssemblageNid = new HashMap<>();
		Set<ConceptChronology<?>> annotationConcepts = new HashSet<>();
		
		//Stream<SememeChronology<? extends SememeVersion<?>>> extensionDefinitionChronologyStream = Get.sememeService().getSememesFromAssemblage(DynamicSememeConstants.get().DYNAMIC_SEMEME_DEFINITION_DESCRIPTION.getConceptSequence());
		Stream<SememeChronology<? extends SememeVersion<?>>> extensionDefinitionChronologyStream = Get.sememeService().getSememesFromAssemblage(DynamicSememeConstants.get().DYNAMIC_SEMEME_EXTENSION_DEFINITION.getConceptSequence());
		extensionDefinitionChronologyStream.forEach(extensionDefinitionChronology -> {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<SememeVersionImpl>> extensionDefinitionLatestOptional = ((SememeChronology)extensionDefinitionChronology).getLatestVersion(SememeVersionImpl.class, sc);
			if (extensionDefinitionLatestOptional.isPresent()) {
				// TODO handle contradictions
				@SuppressWarnings("rawtypes")
				SememeVersionImpl extensionDefinition = extensionDefinitionLatestOptional.get().value();
				Set<Integer> extensionDefinitionsForAssemblage = extensionDefinitionsByAssemblageNid.get(extensionDefinition.getReferencedComponentNid());
				if (extensionDefinitionsForAssemblage == null) {
					extensionDefinitionsForAssemblage = new HashSet<>();
					extensionDefinitionsByAssemblageNid.put(extensionDefinition.getReferencedComponentNid(), extensionDefinitionsForAssemblage);
				}
				extensionDefinitionsForAssemblage.add(extensionDefinition.getNid());
			}
		});

		for (Map.Entry<Integer, Set<Integer>> entry : extensionDefinitionsByAssemblageNid.entrySet()) {
			ConceptChronology<? extends ConceptVersion<?>> assemblageConcept = Get.conceptService().getConcept(entry.getKey());
//			try {
//				String msg = "Concept " + getUuidsWithDescriptions(entry.getKey()) + " has " + entry.getValue().size() + " extension defs: " + getUuidsWithDescriptions(entry.getValue().toArray(new Integer[entry.getValue().size()]));
//				log.debug(msg);
//			} catch (Exception e) {
//				log.error(e);
//			}
			if (entry.getValue().size() == 1) {
				log.debug("Registering annotation concept as map item display field: " + getUuidsWithDescriptions(assemblageConcept.getNid()));
				annotationConcepts.add(assemblageConcept);
			} else {
				log.debug("NOT registering annotation concept with " + entry.getValue().size() + " extension definitions as map item display field: " + getUuidsWithDescriptions(assemblageConcept.getNid()));
			}
		}
		
		return annotationConcepts;
	}

	@PostConstruct
	public void construct() {
		 fields_ = new HashMap<>();
	}

	@PreDestroy
	public void destroy() {
		fields_.clear();
	}

	private static Map<Object, String> getUuidsWithDescriptions(Integer...ids) {
		Map<Object, String> descriptionsByUuid = new HashMap<>();
		
		if (ids != null) {
			for (int id : ids) {
				ConceptChronology<? extends ConceptVersion<?>> concept = Get.conceptService().getConcept(id);

				descriptionsByUuid.put(concept.getPrimordialUuid(), Get.conceptDescriptionText(id));
			}
		}
		
		return descriptionsByUuid;
	}
}
