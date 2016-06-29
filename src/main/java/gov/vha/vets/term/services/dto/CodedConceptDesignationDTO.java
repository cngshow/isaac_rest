package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;

public class CodedConceptDesignationDTO
{
	private CodedConcept concept;
	private Designation designation;
	
	public CodedConceptDesignationDTO(CodedConcept concept, Designation designation)
	{
		super();
		this.concept = concept;
		this.designation = designation;
	}
	
	public CodedConcept getConcept()
	{
		return concept;
	}
	
	public void setConcept(CodedConcept concept)
	{
		this.concept = concept;
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
