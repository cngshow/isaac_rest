package gov.vha.vets.term.services.exception;

import java.util.Date;

public class STSProcessLockedException extends STSException
{
    protected Date date;
    
    public STSProcessLockedException(String message, Date date)
    {
        super(message);
        this.date = date;
    }
    
    public Date getDate()
    {
        return date;
    }
}
