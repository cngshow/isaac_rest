package gov.vha.vets.term.services.exception;

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