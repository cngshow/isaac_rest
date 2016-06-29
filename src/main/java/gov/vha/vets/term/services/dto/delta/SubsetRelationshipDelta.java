package gov.vha.vets.term.services.dto.delta;

public class SubsetRelationshipDelta
{
    protected String system;
    protected String subsetName;
    protected String conceptCode;
    protected String designationCode;
    
    /**
     * @param subsetName
     * @param designationName
     * @param conceptCode
     */
    public SubsetRelationshipDelta(String system, String subsetName, String designationCode)
    {
        this.system = system;
        this.subsetName = subsetName;
        this.designationCode = designationCode;
    }
    
    public String getSystem()
    {
        return system;
    }

    public void setSystem(String system)
    {
        this.system = system;
    }

    public String getDesignationCode()
    {
        return designationCode;
    }

    public void setDesignationCode(String designationCode)
    {
        this.designationCode = designationCode;
    }

    public String getSubsetName()
    {
        return subsetName;
    }

    public void setSubsetName(String subsetName)
    {
        this.subsetName = subsetName;
    }
}
