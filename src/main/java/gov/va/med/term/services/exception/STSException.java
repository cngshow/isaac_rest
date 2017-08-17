package gov.va.med.term.services.exception;

//moved to this package since SOAP will show the package name in the error response.
public class STSException extends Exception
{

    public STSException()
    {
        super();
    }

    public STSException(String message)
    {
        super(message);
    }

    public STSException(Throwable cause)
    {
        super(cause);
    }

    public STSException(String message, Throwable cause)
    {
        super(message, cause);
    }

}