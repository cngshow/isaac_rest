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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.spi.StandardLevel;
import org.codehaus.plexus.util.StringUtils;
import org.glassfish.hk2.runlevel.ChangeableRunLevelFuture;
import org.glassfish.hk2.runlevel.ErrorInformation;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.runlevel.RunLevelFuture;
import org.jvnet.hk2.annotations.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.identity.IdentifiedObject;
import gov.vha.isaac.rest.ApplicationConfig;
import gov.vha.isaac.rest.api1.data.mapping.RestMappingSetField;

/**
 * 
 * {@link MapSetFieldsService}
 * 
 * Return a new, uncached list of map set fields for use in ordering
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@RunLevel(LookupService.ISAAC_DEPENDENTS_RUNLEVEL)
@Service
@Singleton
public class MapSetFieldsService {
	public static class Field {
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
	}

	private Map<String, Field> fields_;
	
	MapSetFieldsService() {
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
