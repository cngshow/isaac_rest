package gov.vha.vets.term.services.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="VERSION_SEQ", allocationSize=1)
@Table(name="VERSION")
public class Version extends Base implements Comparable<Version>, Cloneable 
{
    protected String name;
    protected CodeSystem codeSystem;
    protected Date effectiveDate;
    protected Date releaseDate;
    protected Date deploymentDate;
    protected Date importDate;
    protected String description;
    protected String source;
    protected Integer conceptCount;

    public Version()
    {
    	
    }
    
    public Version(String name, Date effectiveDate, String description)
    {
        this.name = name;
    	this.effectiveDate = effectiveDate;
    	this.description = description;
    }
    
    /**
     * added this to allow setting the version on import from an authoring environment.
     * we use a large constant value to denote a version that is still in flux.
     */
    public void setId(long id)
    {
    	super.setId(id);
    }
    
    /**
     * @return Returns the id.
     */
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }
    
    /**
     * @return the name
     */
    @Column (name="NAME", nullable=false)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the codeSystem
     */
    @ManyToOne    
    @JoinColumn(name="CODESYSTEM_ID", nullable=false) 
    @ForeignKey(name="FK_VERSION_CS")    
    public CodeSystem getCodeSystem()
    {
        return codeSystem;
    }

    /**
     * @param codeSystem the codeSystem to set
     */
    public void setCodeSystem(CodeSystem codeSystem)
    {
        this.codeSystem = codeSystem;
    }

    /**
     * @return
     */
    @Column (name="DESCRIPTION", nullable=true)
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return
     */
    @Column (name="EFFECTIVEDATE", nullable=true)
    public Date getEffectiveDate()
    {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate)
    {
        this.effectiveDate = effectiveDate;
    }
    
    /**
     * @return the deploymentDate
     */
    @Column (name="DEPLOYMENTDATE", nullable=true)
    public Date getDeploymentDate()
    {
        return deploymentDate;
    }

    /**
     * @param deploymentDate the deploymentDate to set
     */
    public void setDeploymentDate(Date deploymentDate)
    {
        this.deploymentDate = deploymentDate;
    }

    /**
     * @return the releaseDate
     */
    @Column (name="RELEASEDATE", nullable=true)
    public Date getReleaseDate()
    {
        return releaseDate;
    }

    /**
     * @param releaseDate the releaseDate to set
     */
    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }
    
	/**
	 * @return the importDate
	 */
    @Column (name="IMPORTDATE", nullable=true)
    public Date getImportDate()
	{
		return this.importDate;
	}

	/**
	 * @param importDate the importDate to set
	 */
	public void setImportDate(Date importDate)
	{
		this.importDate = importDate;
	}
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    /**
	 * @return the source
	 */
    @Column (name="SOURCE", nullable=true)
	public String getSource()
    {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source)
	{
		this.source = source;
	}

	/**
	 * @return the conceptCount
	 */
    @Column (name="CONCEPTCOUNT", nullable=true)
	public Integer getConceptCount()
	{
		return conceptCount;
	}

	/**
	 * @param conceptCount the conceptCount to set
	 */
	public void setConceptCount(Integer conceptCount)
	{
		this.conceptCount = conceptCount;
	}

	public String toString()
    {
        return this.id + ", " + this.name + ", " + this.codeSystem.getName();
    }

    public int compareTo(Version version)
    {
        // TODO Auto-generated method stub
        return (int) -(this.getId()-version.getId());
    }

}
