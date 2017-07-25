package gov.vha.isaac.soap.model;

//import javax.persistence.DiscriminatorValue;
//import javax.persistence.Entity;
//
//
//@Entity
//@DiscriminatorValue("S")
public class Subset extends Concept
{
    public Subset()
    {
        
    }
    
    
    /**
     * @param name
     * @param vuid
     * @param version
     * @param active
     */
    public Subset(String name, long vuid, Version version, boolean active)
    {
        this.name = name;
        this.vuid = vuid;
        this.version = version;
        this.active = active;
    }

    /**
     * Override and not save 
     */
    public void setCodeSystem(CodeSystem theCodeSystem)
    {
        //don't allow the use of the codesystem per brian's request
    }
   
}
