package gov.vha.isaac.rest.api1.system;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.PrismeRoleConstants;
import gov.vha.isaac.rest.api.data.wrappers.RestWriteResponse;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;
import gov.vha.isaac.rest.utils.SecurityUtils;
import javafx.concurrent.Task;

/**
 * Various admin-only hack methods for particular use cases.
 * Not intended to be part of the end-user API.
 * @author darmbrust
 */
@Path(RestPaths.writePathComponent + RestPaths.systemAPIsPathComponent)
@RolesAllowed({PrismeRoleConstants.SUPER_USER, PrismeRoleConstants.ADMINISTRATOR})
public class SystemWriteAPIs {
	private static Logger log = LogManager.getLogger(SystemWriteAPIs.class);

	@Context
	private SecurityContext securityContext;


	/**
	 * 
	 * @param editToken - 
	 *            EditToken string returned by previous call to 1/coordinate/editToken
	 *            or as renewed EditToken returned by previous write API call in a RestWriteResponse
	 * @return a renewed edit token.  The index operation will be running in the background - see the logs for an indication of status.  During the reindex operation, 
	 *     queries that you would expect to return results may not return results until the reindex completes.  The time to reindex depends on the size of the DB - reindexing
	 *     the VHAT database, for example, takes about 1 minute on reasonable hardware.
	 * @throws RestException
	 */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.systemAPIsRebuildIndexComponent)
	public RestWriteResponse triggerReindex(@QueryParam(RequestParameters.editToken) String editToken) throws RestException
	{
		log.info("Reindex of datastore requested");
		SecurityUtils.validateRole(securityContext, getClass());
		Task<Void> t = Get.startIndexTask(null);
		log.info("Reindex of datastore started");
		return new RestWriteResponse(RequestInfo.get().getEditToken());
	}
}
