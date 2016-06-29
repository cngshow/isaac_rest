package gov.vha.vets.term.services.dto.api;

public class RelationshipDetailViewDTO
{
	private Long entityId;
	private String sourceConceptCode;
	private String targetConceptCode;
	private String sourceDesignationName;
	private String targetDesignationName;
	private String relationshipTypeName;
	private boolean relationshipStatus;
	private Long totalNumberOfRecords;
	
	public Long getEntityId() 
	{
		return entityId;
	}
	public void setEntityId(Long entityId)
	{
		this.entityId = entityId;
	}
	public String getSourceConceptCode()
	{
		return sourceConceptCode;
	}
	public void setSourceConceptCode(String sourceConceptCode)
	{
		this.sourceConceptCode = sourceConceptCode;
	}
	public String getTargetConceptCode()
	{
		return targetConceptCode;
	}
	public void setTargetConceptCode(String targetConceptCode)
	{
		this.targetConceptCode = targetConceptCode;
	}
	public String getSourceDesignationName()
	{
		return sourceDesignationName;
	}
	public void setSourceDesignationName(String sourceDesignationName)
	{
		this.sourceDesignationName = sourceDesignationName;
	}
	public String getTargetDesignationName()
	{
		return targetDesignationName;
	}
	public void setTargetDesignationName(String targetDesignationName)
	{
		this.targetDesignationName = targetDesignationName;
	}
	public String getRelationshipTypeName()
	{
		return relationshipTypeName;
	}
	public void setRelationshipTypeName(String relationshipTypeName)
	{
		this.relationshipTypeName = relationshipTypeName;
	}
	public boolean isRelationshipStatus()
	{
		return relationshipStatus;
	}
	public void setRelationshipStatus(boolean relationshipStatus)
	{
		this.relationshipStatus = relationshipStatus;
	}
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords) 
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
}
