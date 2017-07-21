package gov.vha.isaac.soap.exception;

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