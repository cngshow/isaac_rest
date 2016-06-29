package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Designation;

import java.io.Serializable;

public class MapEntryDetailDTO implements Comparable<MapEntryDetailDTO>, Serializable
{
    public enum StatusType { ADDED, INACTIVATED, ACTIVATED, UPDATED };
    public StatusType status;

    private MapEntryDTO mapEntryDTO;
    private Designation sourceDesignation;
    private Designation targetDesignation;
    
    public MapEntryDetailDTO()
    {}
    
    public StatusType getStatus()
	{
		return status;
	}

	public void setStatus(StatusType status)
	{
		this.status = status;
	}


	public MapEntryDTO getMapEntryDTO()
	{
		return mapEntryDTO;
	}

	public void setMapEntryDTO(MapEntryDTO mapEntryDTO)
	{
		this.mapEntryDTO = mapEntryDTO;
	}

	public Designation getSourceDesignation()
	{
		return sourceDesignation;
	}

	public void setSourceDesignation(Designation sourceDesignation)
	{
		this.sourceDesignation = sourceDesignation;
	}

	public Designation getTargetDesignation()
	{
		return targetDesignation;
	}

	public void setTargetDesignation(Designation targetDesignation)
	{
		this.targetDesignation = targetDesignation;
	}

	public int compareTo(MapEntryDetailDTO anotherEntryDetail)
    {
        int anotherSequence = anotherEntryDetail.getMapEntryDTO().getMapSetRelationship().getSequence();  

        return this.getMapEntryDTO().getMapSetRelationship().getSequence() - anotherSequence;    
    }
}
