package gov.vha.isaac.soap.model;

//import javax.persistence.Column;
//import javax.persistence.DiscriminatorColumn;
//import javax.persistence.DiscriminatorType;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
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
//@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="CONCEPT_SEQ", allocationSize=10 )
//@Table(name="CONCEPT")
//@DiscriminatorColumn(name="KIND",discriminatorType=DiscriminatorType.STRING, length=1)
public class Concept extends BaseVersion
{
    protected String name;
    protected Long vuid;
    protected String code;
    protected Type type;
    protected CodeSystem codeSystem;
    
    //@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }
    
    //@Column (name="ENTITY_ID", nullable=false)
    public long getEntityId()
    {
        return entityId;
    }

    public void setEntityId(long entityId)
    {
        this.entityId = entityId;
    }

    //@Column (name="CODE", nullable=true)
    public String getCode()
    {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code)
    {
        this.code = code;
    }
    
    //@ManyToOne    
    //@JoinColumn(name="VERSION_ID", nullable=false) 
    //@ForeignKey(name="FK_CONCEPT_VERSION")    
    public Version getVersion()
    {
        return version;
    }
    
    //@Column (name="Active", nullable=false)
    public boolean getActive()
    {
        return active;
        
    }

    //@ManyToOne   
    //@JoinColumn(name="TYPE_ID", nullable=true) 
    //@ForeignKey(name="FK_CONCEPT_TYPE")    
    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    //@ManyToOne   
    //@JoinColumn(name="CHANGEGROUP_ID", nullable=true) 
    //@ForeignKey(name="FK_CONCEPT_CHANGEGROUP")    
    public ChangeGroup getChangeGroup()
    {
        return changeGroup;
    }

    public void setChangeGroup(ChangeGroup changeGroup)
    {
        this.changeGroup = changeGroup;
    }
    
    //@Column (name="NAME", nullable=true)
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
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
    
    public String toString()
    {
        return this.name;
    }

//    @ManyToOne (fetch=FetchType.LAZY)    
//    @JoinColumn(name="CODESYSTEM_ID", nullable=true) 
//    @ForeignKey(name="FK_CONCEPT_CS")    
    public CodeSystem getCodeSystem()
    {
        return codeSystem;
    }
    
    /**
     * @param theCodeSystem The theCodeSystem to set.
     */
    public void setCodeSystem(CodeSystem theCodeSystem)
    {
        this.codeSystem = theCodeSystem;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        Concept newConcept = (Concept)super.clone();
        newConcept.setCodeSystem(codeSystem);
        newConcept.setVersion(version);
        newConcept.setType(type);
        newConcept.setEntityId(entityId);
        
        return newConcept;
    }
}
