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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;

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
			PREFERRED_TERM("Preferred Term");
			
			private String description;
			
			private NonConceptFieldName(String description) {
				this.description = description;
			}
			
			public String getDescription() {
				return description;
			}
		}
		
		private final String name;
		private final boolean computed;
		private final IdentifiedObject object;

		private Field(IdentifiedObject object, boolean computed) {
			this(object.getPrimordialUuid().toString(), computed, object);
		}
		
		private Field(String name, boolean computed) {
			this(name, computed, null);
		}

		/**
		 * @param name
		 * @param computed
		 * @param object
		 */
		private Field(String name, boolean computed, IdentifiedObject object) {
			super();
			this.name = name;
			this.computed = computed;
			this.object = object;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return the computed
		 */
		public boolean isComputed() {
			return computed;
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
			return "Field [name=" + name + ", computed=" + computed + ", object=" + object + "]";
		}
	}

	private Map<String, Field> fields_;
	
	MapSetDisplayFieldsService() {
		// For HK2
	}

	public Collection<Field> getAllFields() {
		return Collections.unmodifiableCollection(fields_.values());
	}
	public Set<String> getAllFieldNames() {
		return Collections.unmodifiableSet(fields_.keySet());
	}
	public Field getFieldByIdOrNameIfNotId(String nameOrId) {
		if (fields_.get(nameOrId) != null) {
			return fields_.get(nameOrId);
		}

		try {
			int intId = Integer.parseInt(nameOrId.trim());
			Optional<UUID> uuid = Optional.empty();

			uuid = Get.identifierService().getUuidPrimordialFromConceptId(intId);
			if (uuid.isPresent()) {
				return fields_.get(uuid.get().toString());
			}
			
			uuid = Get.identifierService().getUuidPrimordialFromSememeId(intId);
			if (uuid.isPresent()) {
				return fields_.get(uuid.get().toString());
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private void add(IdentifiedObject object, boolean computed) {
		Field field = new Field(object, computed);
		fields_.put(field.name, field);
	}
	private void add(String name, boolean computed) {
		Field field = new Field(name, computed, null);
		fields_.put(field.name, field);
	}
	
	@PostConstruct
	public void construct() {
		fields_ = new HashMap<>();

		add(MetaData.FULLY_SPECIFIED_NAME, true);
		add(MetaData.SCTID, true);
		add(MetaData.LOINC_NUM, true);
		add(MetaData.RXCUI, true);
		add(MetaData.VUID, true);
		add(MetaData.CODE, true);

		add("PREFERRED_TERM", true);
	}
	@PreDestroy
	public void destroy() {
		fields_.clear();
	}
}
