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
package gov.vha.isaac.rest.api1.sememe;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.ochre.model.configuration.StampCoordinates;
import gov.vha.isaac.ochre.model.sememe.version.SememeVersionImpl;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeVersion;
import gov.vha.isaac.rest.api1.session.RequestInfo;


/**
 * {@link SememeAPIs}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.sememePathComponent)
public class SememeAPIs
{

	/**
	 * Returns the chronology of a sememe.  
	 * @param id - A UUID, nid or sememe sequence
	 * @param expand - A comma separated list of fields to expand.  Supports 'versionsAll', 'versionsLatestOnly'
	 * If latest only is specified in combination with versionsAll, it is ignored (all versions are returned)
	 * @return the sememe chronology object
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.chronologyComponent + "{id}")
	public RestSememeChronology getSememeChronology(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		RequestInfo ri = RequestInfo.init(expand);
		return new RestSememeChronology(findSememeChronology(id), ri.shouldExpand(ExpandUtil.versionsAllExpandable), ri.shouldExpand(ExpandUtil.versionsLatestOnlyExpandable));
	}
	
	/**
	 * Returns a single version of a sememe.
	 * TODO still need to define how to pass in a version parameter
	 * If no version parameter is specified, returns the latest version.
	 * @param id - A UUID, nid, or concept sequence
	 * @param expand - comma separated list of fields to expand.  Supports 'chronology'
	 * @return the sememe version object
	 * @throws RestException 
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.versionComponent + "{id}")
	public RestSememeVersion getSememeVersion(@PathParam("id") String id, @QueryParam("expand") String expand) throws RestException
	{
		RequestInfo ri = RequestInfo.init(expand);
		@SuppressWarnings("rawtypes")
		SememeChronology sc = findSememeChronology(id);
		@SuppressWarnings("unchecked")
		Optional<LatestVersion<SememeVersion<?>>> sv = sc.getLatestVersion(SememeVersionImpl.class, StampCoordinates.getDevelopmentLatest());
		if (sv.isPresent())
		{
			return RestSememeVersion.buildRestSememeVersion(sv.get().value(), ri.shouldExpand(ExpandUtil.chronologyExpandable));
		}
		else
		{
			throw new RestException("id", id, "No sememe was found");
		}
	}
	
	public static SememeChronology<? extends SememeVersion<?>> findSememeChronology(String id) throws RestException
	{
		SememeService sememeService = Get.sememeService();
		
		Optional<UUID> uuidId = UUIDUtil.getUUID(id);
		Optional<Integer> intId = Optional.empty();
		if (uuidId.isPresent())
		{
			if (Get.identifierService().hasUuid(uuidId.get()))
			{
				intId = Optional.of(Get.identifierService().getNidForUuids(uuidId.get()));
			}
			else
			{
				throw new RestException("id", id, "Is not known by the system");
			}
		}
		else
		{
			intId = NumericUtils.getInt(id);
		}
		
		if (intId.isPresent())
		{
			Optional<? extends SememeChronology<? extends SememeVersion<?>>> sc = sememeService.getOptionalSememe(intId.get());
			if (sc.isPresent())
			{
				return sc.get();
			}
			else
			{
				throw new RestException("id", id, "No Sememe was located with the given identifier");
			}
		}
		throw new RestException("id", id, "Is not a sememe identifier.  Must be a UUID or an integer");
	}
}
