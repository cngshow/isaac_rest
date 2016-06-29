package gov.vha.vets.term.services.dto;

public class ConceptHierarchyDTO
{
	private long conceptEntityId;
	private String designationName;
	private boolean hasChildren;
	
	public ConceptHierarchyDTO(long conceptEntityId, String designationName, boolean hasChildren)
	{
		super();
		this.conceptEntityId = conceptEntityId;
		this.designationName = designationName;
		this.hasChildren = hasChildren;
	}
	
	public long getConceptEntityId()
	{
		return conceptEntityId;
	}
	public void setConceptEntityId(long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}
	public String getDesignationName()
	{
		return designationName;
	}
	public void setDesignationName(String designationName)
	{
		this.designationName = designationName;
	}
	public boolean isHasChildren()
	{
		return hasChildren;
	}
	public void setHasChildren(boolean hasChildren)
	{
		this.hasChildren = hasChildren;
	}
}
