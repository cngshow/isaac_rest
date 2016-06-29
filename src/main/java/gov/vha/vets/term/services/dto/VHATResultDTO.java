package gov.vha.vets.term.services.dto;

import java.io.Serializable;

public class VHATResultDTO implements Serializable, Comparable<VHATResultDTO>
{
	private Long conceptEntityId;
	private Long conceptVuid;
	private Long designationVuid;
	private String designationName;
	private String designationType;
	private Integer designationStatus;
	
	public VHATResultDTO(Long conceptEntityId, Long conceptVuid, Long designationVuid, String designationName, String designationType, int designationStatus)
    {
		this.conceptEntityId = conceptEntityId;
		this.conceptVuid = conceptVuid;
		this.designationVuid = designationVuid;
		this.designationName = designationName;
		this.designationType = designationType;
		this.designationStatus = designationStatus;
    }


	/**
	 * @return the conceptEntityId
	 */
	public Long getConceptEntityId()
	{
		return conceptEntityId;
	}


	/**
	 * @param conceptEntityId the conceptEntityId to set
	 */
	public void setConceptEntityId(Long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}


	/**
	 * @return the conceptVuid
	 */
	public Long getConceptVuid()
	{
		return conceptVuid;
	}


	/**
	 * @param conceptVuid the conceptVuid to set
	 */
	public void setConceptVuid(Long conceptVuid)
	{
		this.conceptVuid = conceptVuid;
	}


	/**
	 * @return the designationVuid
	 */
	public Long getDesignationVuid()
	{
		return designationVuid;
	}


	/**
	 * @param designationVuid the designationVuid to set
	 */
	public void setDesignationVuid(Long designationVuid)
	{
		this.designationVuid = designationVuid;
	}


	/**
	 * @return the designationName
	 */
	public String getDesignationName()
	{
		return designationName;
	}


	/**
	 * @param designationName the designationName to set
	 */
	public void setDesignationName(String designationName)
	{
		this.designationName = designationName;
	}


	/**
	 * @return the designationType
	 */
	public String getDesignationType()
	{
		return designationType;
	}


	/**
	 * @param designationType the designationType to set
	 */
	public void setDesignationType(String designationType)
	{
		this.designationType = designationType;
	}


	/**
	 * @return the designationStatus
	 */
	public Integer getDesignationStatus()
	{
		return designationStatus;
	}


	/**
	 * @param designationStatus the designationStatus to set
	 */
	public void setDesignationStatus(Integer designationStatus)
	{
		this.designationStatus = designationStatus;
	}


	public int compareTo(VHATResultDTO otherCodeSystemResult)
	{
		int compareTo = getDesignationName().compareTo(otherCodeSystemResult.getDesignationName());

		// sort in reverse order
		return -compareTo;
	}
}
