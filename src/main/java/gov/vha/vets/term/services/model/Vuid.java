/**
 * 
 */
package gov.vha.vets.term.services.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Entity;

/**
 *@hibernate.class table="VUID"
 * @author VHAISLMURDOH
 *
 */
@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="VUID_SEQ", allocationSize=1 )
@Table(name="VUID")
public class Vuid implements Serializable
{
    protected long id;
    protected long startVuid;
    protected long endVuid;
    protected String userInitials;
    protected Date requestDate;
    protected String requestReason;
    
    public Vuid()
    {
    }
    
    public Vuid(long startVuid,long endVuid, String userInitials, Date requestDate, String requestReason)
    {
        this.startVuid = startVuid;
        this.endVuid = endVuid;
        this.userInitials = userInitials;
        this.requestDate = requestDate;
        this.requestReason = requestReason;
    }
    
    /**
     * @hibernate.id generator-class="gov.vha.vets.term.services.util.TableNameSequenceGenerator"
     */
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }
    
    /**
     *@hibernate.property column="STARTVUID"
     * @return the startVuid
     */
    @Column(name="STARTVUID", nullable=false)
    public long getStartVuid()
    {
        return startVuid;
    }
    
    /**
     * @param startVuid the startVuid to set
     */
    public void setStartVuid(long startVuid)
    {
        this.startVuid = startVuid;
    }
    
    /**
     *@hibernate.property column="ENDVUID"
     * @return the endingVuid
     */
    @Column(name="ENDVUID", nullable=false)
    public long getEndVuid()
    {
        return endVuid;
    }
    
    /**
     * @param endingVuid the endingVuid to set
     */
    public void setEndVuid(long endingVuid)
    {
        this.endVuid = endingVuid;
    }
    
    /**
     *@hibernate.property column="REQUESTDATE"
     * @return the requestDate
     */
    @Column(name="REQUESTDATE", nullable=false)
    public Date getRequestDate()
    {
        return requestDate;
    }
    
    /**
     * @param requestDate the requestDate to set
     */
    public void setRequestDate(Date requestDate)
    {
        this.requestDate = requestDate;
    }
    
    /**
     *@hibernate.property column="REQUESTREASON"
     * @return the requestReason
     */
    @Column(name="REQUESTREASON", nullable=false)
    public String getRequestReason()
    {
        return requestReason;
    }
    
    /**
     * @param requestReason the requestReason to set
     */
    public void setRequestReason(String requestReason)
    {
        this.requestReason = requestReason;
    }

    /**
     * @hibernate.property column="USERINITIALS"
     * @return the userinitials
     */
    @Column(name="USERINITIALS", nullable=false)
    public String getUserInitials()
    {
        return userInitials;
    }

    /**
     * @param userInitials the userinitials to set
     */
    public void setUserInitials(String userInitials)
    {
        this.userInitials = userInitials;
    }
}
