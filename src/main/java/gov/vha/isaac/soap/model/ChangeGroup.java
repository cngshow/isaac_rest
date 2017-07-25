/*
 * Created on Oct 18, 2004
 */
package gov.vha.isaac.soap.model;

import java.util.Date;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;

//@Entity
//@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="CHANGEGROUP_SEQ", allocationSize=1)
//@Table(name="CHANGEGROUP")
public class ChangeGroup extends Base
{
//    private String source;
//    private Date changeDate;
//    
//	public enum SourceName
//	{
//		PEPS, TDE, SDO, TEd, Lab, TDS
//	}
//    
//    public ChangeGroup()
//    {
//    	
//    }
//
//    public ChangeGroup(String source)
//    {
//    	this.source = source;
//    	this.changeDate = new Date();
//    }
//    
    /**
     * @return Returns the id.
     */
    //@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }
//
//    /**
//     * @return Returns the name.
//     */
//    @Column (name="SOURCE", nullable=false, unique=false)
//    public String getSource()
//    {
//        return source;
//    }
//
//    @Column (name="CHANGEDATE", nullable=false, unique=false)
//    public Date getChangeDate()
//    {
//        return changeDate;
//    }
//
//    public void setChangeDate(Date changeDate)
//    {
//        this.changeDate = changeDate;
//    }
//
//    /**
//     * @param source The name to set.
//     */
//    public void setSource(String source)
//    {
//        this.source = source;
//    }
//
//    public String toString()
//    {
//        return this.getSource();
//    }
}
