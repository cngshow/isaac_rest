/*
 * Created on Nov 5, 2006
 */
package gov.vha.vets.term.services.exception;

public class STSInvalidValueException extends STSException 
{
    public STSInvalidValueException()
    {
        super();
    }
    
    public STSInvalidValueException(String message)
    {
        super(message);
    }
    
    public STSInvalidValueException(Throwable cause)
    {
        super(cause);        
    }

    public STSInvalidValueException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
