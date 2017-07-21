/*
 * Created on Oct 18, 2004
 */
package gov.vha.isaac.soap.model;

//import javax.persistence.CascadeType;
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//import javax.persistence.ManyToOne;
//import javax.persistence.Table;
//
//import org.hibernate.annotations.ForeignKey;
//
//
//@Entity
//@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="CODESYSTEM_SEQ", allocationSize=1 )
//@Table(name="CODESYSTEM")
public class CodeSystem extends Base
{
    private String name;
    private Long vuid;
    private String description;
    private String copyright;
    private String copyrightURL;
    private DesignationType preferredDesignationType;

    public CodeSystem()
    {

    }

    public CodeSystem(String name, Long vuid, String description, String copyright, String copyrightURL, DesignationType preferredDesignationType)
    {
        this.name = name;
        this.vuid = vuid;
        this.description = description;
        this.copyright = copyright;
        this.copyrightURL = copyrightURL;
        this.preferredDesignationType = preferredDesignationType;
    }

    /**
     * @return Returns the id.
     */
    //@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }

    /**
     * @return Returns the copyright.
     */
    //@Column (name="COPYRIGHT", nullable=true)
    public String getCopyright()
    {
        return copyright;
    }

    /**
     * @param copyright
     *            The copyright to set.
     */
    public void setCopyright(String copyright)
    {
        this.copyright = copyright;
    }

    /**
	 * @return the copyrightURL
	 */
    //@Column (name="COPYRIGHTURL", nullable=true)
	public String getCopyrightURL()
	{
		return copyrightURL;
	}

	/**
	 * @param copyrightURL the copyrightURL to set
	 */
	public void setCopyrightURL(String copyrightURL)
	{
		this.copyrightURL = copyrightURL;
	}

	/**
     * @return Returns the description.
     */
    //@Column (name="DESCRIPTION", nullable=false)
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return Returns the name.
     */
    //@Column (name="NAME", nullable=false, unique=true)
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
    	return this.getName();
    }

    //@Column (name="VUID", nullable=true)
    public Long getVuid()
    {
        return vuid;
    }
    
    public void setVuid(Long vuid)
    {
        this.vuid = vuid;
    }
    
    /**
     * @return Returns the designationType
     */
    //@ManyToOne(cascade=CascadeType.REFRESH)
    //@JoinColumn(name="PREFERRED_DESIGNATION_TYPE_ID", nullable=false)
    //@ForeignKey(name="FK_DESIGNATION_TYPE")
    public DesignationType getPreferredDesignationType()
    {
        return preferredDesignationType;
    }

    public void setPreferredDesignationType(DesignationType preferredDesignationType)
    {
        this.preferredDesignationType = preferredDesignationType;
    }
}
