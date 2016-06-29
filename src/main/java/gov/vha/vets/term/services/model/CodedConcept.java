package gov.vha.vets.term.services.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;



@Entity
@DiscriminatorValue("C")
public class CodedConcept extends Concept
{
    
    /**
     * @param name
     * @param code
     * @param vuid
     * @param
     * @param type
     * @param code
     * @param codeSystem
     */
    public CodedConcept(String name, String code, long vuid, CodeSystem codeSystem, Version version, boolean active)
    {
        this.name = name;
        this.code = code;
        this.vuid = vuid;
        this.codeSystem = codeSystem;
        this.version = version;
        this.active = active;
    }

    public CodedConcept()
    {
    }


    public Object clone () throws CloneNotSupportedException
    {
        CodedConcept newCodedConcept = (CodedConcept)super.clone();

        if (this.codeSystem != null)
            newCodedConcept.codeSystem = (CodeSystem)this.codeSystem.clone();
        
        return newCodedConcept;
    }
}
