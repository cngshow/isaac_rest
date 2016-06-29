/*
 * Created on Nov 5, 2006
 */
package gov.vha.vets.term.services.exception;

public class STSNotFoundException extends STSException 
{
    public STSNotFoundException()
    {
        super();
    }
    
    public STSNotFoundException(String message)
    {
        super(message);
    }
    
    public STSNotFoundException(Throwable cause)
    {
        super(cause);        
    }

    public STSNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
