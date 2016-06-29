package gov.vha.vets.term.services.model;

import java.io.Serializable;
import java.util.Date;

/**
 * @author VHAISLMURDOH
 *
 */
public class VuidDetail implements Serializable
{
    protected long beginingVuid;
    protected long endingVuid;
    protected String userInitials;
    protected Date requestDate;
    protected String requestReason;
    protected int vuidCount;
    
    public VuidDetail()
    {
    }
    
    public VuidDetail(long beginingVuid,long endingVuid, String userInitials, Date requestDate, String requestReason, int vuidCount)
    {
        this.beginingVuid = beginingVuid;
        this.endingVuid = endingVuid;
        this.userInitials = userInitials;
        this.requestDate = requestDate;
        this.requestReason = requestReason;
        this.vuidCount = vuidCount;
    }
    
    public long getBeginingVuid()
    {
        return beginingVuid;
    }
    
    public void setBeginingVuid(long beginingVuid)
    {
        this.beginingVuid = beginingVuid;
    }
    
    public long getEndingVuid()
    {
        return endingVuid;
    }
    
    public void setEndingVuid(long endingVuid)
    {
        this.endingVuid = endingVuid;
    }
    
    public Date getRequestDate()
    {
        return requestDate;
    }
    
    public void setRequestDate(Date requestDate)
    {
        this.requestDate = requestDate;
    }
    
    public String getRequestReason()
    {
        return requestReason;
    }
    
    public void setRequestReason(String requestReason)
    {
        this.requestReason = requestReason;
    }

    public String getUserInitials()
    {
        return userInitials;
    }

    public void setUserInitials(String userInitials)
    {
        this.userInitials = userInitials;
    }

    public int getVuidCount()
    {
        return vuidCount;
    }

    public void setVuidCount(int vuidCount)
    {
        this.vuidCount = vuidCount;
    }
}