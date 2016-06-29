/**
 * 
 */
package gov.vha.vets.term.services.dto.delta;

/**
 * @author vhaislmurdoh
 *
 */
public class RelationshipDelta
{

    protected String sourceCode;
    protected String targetCode;
    protected String relationshipType;
    protected String system;
    
    public RelationshipDelta()
    {
    	
    }
    
    public RelationshipDelta(String system, String sourceCode, String targetCode, String relationshipType)
    {
    	this.sourceCode = sourceCode;
    	this.targetCode = targetCode;
    	this.relationshipType = relationshipType;
    	this.system = system;
    }
    
    /**
     * @return Returns the system.
     */
    public String getSystem()
    {
        return system;
    }

    /**
     * @param system The system to set.
     */
    public void setSystem(String system)
    {
        this.system = system;
    }


    public String getRelationshipType()
    {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType)
    {
        this.relationshipType = relationshipType;
    }

    public String getSourceCode()
    {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode)
    {
        this.sourceCode = sourceCode;
    }

    public String getTargetCode()
    {
        return targetCode;
    }

    public void setTargetCode(String targetCode)
    {
        this.targetCode = targetCode;
    }
    
}
