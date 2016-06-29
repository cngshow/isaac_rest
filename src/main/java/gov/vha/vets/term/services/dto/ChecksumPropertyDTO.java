package gov.vha.vets.term.services.dto;

public class ChecksumPropertyDTO
{
    private long conceptEntityId;
    private long designationEntityId;
    private String propertyType;
    private String propertyValue;
    
	/**
	 * @return the conceptEntityId
	 */
	public long getConceptEntityId()
	{
		return conceptEntityId;
	}
	/**
	 * @param conceptEntityId the conceptEntityId to set
	 */
	public void setConceptEntityId(long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}
	/**
	 * @return the designationEntityId
	 */
	public long getDesignationEntityId() {
		return designationEntityId;
	}
	/**
	 * @param designationEntityId the designationEntityId to set
	 */
	public void setDesignationEntityId(long designationEntityId)
	{
		this.designationEntityId = designationEntityId;
	}
	/**
	 * @return the propertyType
	 */
	public String getPropertyType()
	{
		return propertyType;
	}
	/**
	 * @param propertyType the propertyType to set
	 */
	public void setPropertyType(String propertyType)
	{
		this.propertyType = propertyType;
	}
	/**
	 * @return the propertyValue
	 */
	public String getPropertyValue()
	{
		return propertyValue;
	}
	/**
	 * @param propertyValue the propertyValue to set
	 */
	public void setPropertyValue(String propertyValue)
	{
		this.propertyValue = propertyValue;
	}
}
