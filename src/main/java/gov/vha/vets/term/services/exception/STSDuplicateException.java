/*
 * Created on Nov 5, 2006
 */
package gov.vha.vets.term.services.exception;

public class STSDuplicateException extends STSException 
{
    public STSDuplicateException()
    {
        super();
    }
    
    public STSDuplicateException(String message)
    {
        super(message);
    }
    
    public STSDuplicateException(Throwable cause)
    {
        super(cause);        
    }

    public STSDuplicateException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
