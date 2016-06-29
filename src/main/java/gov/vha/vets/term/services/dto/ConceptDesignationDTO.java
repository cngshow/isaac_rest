package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Designation;

public class ConceptDesignationDTO 
{
	protected long conceptEntityId;
	protected Designation designation;

	
	public ConceptDesignationDTO(long conceptEntityId, Designation designation)
    {
        super();
        this.conceptEntityId = conceptEntityId;
        this.designation = designation;
    }
	
    public long getConceptEntityId()
	{
	    return conceptEntityId;
	}
	public void setConceptEntityId(long conceptEntityId)
	{
	    this.conceptEntityId = conceptEntityId;
	}
	public Designation getDesignation()
	{
	    return designation;
	}
	public void setDesignation(Designation designation)
	{
	    this.designation = designation;
	}
}
