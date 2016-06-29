package gov.vha.vets.term.services.dto;

import java.io.Serializable;

public class RegionEntityDTO implements Comparable<RegionEntityDTO>, Serializable
{
	private long conceptEntityId;
	private long designationEntityId;
    private String designationName;
    private long vuid;
    
	public RegionEntityDTO()
	{
		super();
	}

    public long getConceptEntityId()
    {
        return conceptEntityId;
    }

    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }

    public long getDesignationEntityId()
    {
        return designationEntityId;
    }

    public void setDesignationEntityId(long designationEntityId)
    {
        this.designationEntityId = designationEntityId;
    }

    public String getDesignationName()
    {
        return designationName;
    }

    public void setDesignationName(String designationName)
    {
        this.designationName = designationName;
    }

    public long getVuid()
    {
        return vuid;
    }

    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }

    public String toString()
    {
        return "ConceptEntityId: "+conceptEntityId+" DesignationEntityId: "+designationEntityId+" DesignationName: "+designationName+" VUID: "+vuid;
        
    }

	public int compareTo(RegionEntityDTO regionEntityDTO)
	{
		if (regionEntityDTO.getVuid() == vuid)
		{
			return 0;
		}
		else if (regionEntityDTO.getVuid() < vuid)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
}
