package gov.vha.isaac.rest.jerseyConfig;


import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.glassfish.grizzly.utils.Exceptions;
import org.slf4j.LoggerFactory;

@Provider
public class MyExceptionMapper implements ExceptionMapper<Exception>
{
	@Override
	public Response toResponse(Exception ex)
	{
		LoggerFactory.getLogger("web").error("oops", ex);
		return Response.status(500).entity(Exceptions.getStackTraceAsString(ex)).type("text/plain").build();
	}
}
