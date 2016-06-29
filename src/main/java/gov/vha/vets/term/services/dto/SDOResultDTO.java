package gov.vha.vets.term.services.dto;

import java.io.Serializable;

public class SDOResultDTO implements Serializable, Comparable<SDOResultDTO>
{
	private static final long serialVersionUID = 1L;
	private Long conceptEntityId;
	private String conceptCode;
	private String designationName;
	private String designationTypeName;
	private Integer status;
	
	public SDOResultDTO(Long conceptEntityId, String conceptCode, String designationName, String designationTypeName, int status)
    {
		this.conceptEntityId = conceptEntityId;
		this.conceptCode = conceptCode;
		this.designationName = designationName;
		this.designationTypeName = designationTypeName;
		this.status = status;
    }

	public Long getConceptEntityId()
	{
		return conceptEntityId;
	}

	public void setConceptId(Long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}

	/**
	 * @return the conceptCode
	 */
	public String getConceptCode()
	{
		return conceptCode;
	}

	/**
	 * @param conceptCode the conceptCode to set
	 */
	public void setConceptCode(String conceptCode)
	{
		this.conceptCode = conceptCode;
	}

	public String getDesignationName()
	{
		return designationName;
	}
	
	public void setDesignationName(String designationName)
	{
		this.designationName = designationName;
	}
	
	
	public String getDesignationTypeName()
	{
		return designationTypeName;
	}

	public void setDesignationTypeName(String designationTypeName)
	{
		this.designationTypeName = designationTypeName;
	}

	public Integer getStatus()
	{
		return status;
	}
	
	public void setStatus(Integer status)
	{
		this.status = status;
	}

	public int compareTo(SDOResultDTO otherCodeSystemResult)
	{
		int compareTo = getDesignationName().compareTo(otherCodeSystemResult.getDesignationName());

		// sort in reverse order
		return -compareTo;
	}
}
