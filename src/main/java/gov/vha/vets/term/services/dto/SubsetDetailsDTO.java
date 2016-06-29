package gov.vha.vets.term.services.dto;

import java.util.List;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Subset;

public class SubsetDetailsDTO
{
	private Subset subset;
	private List<Designation> designations;
	private Long versionId;
	private String versionName;
	
	/**
	 * @return the subset
	 */
	public Subset getSubset()
	{
		return subset;
	}
	/**
	 * @param subset the subset to set
	 */
	public void setSubset(Subset subset)
	{
		this.subset = subset;
	}
	/**
	 * @return the designations
	 */
	public List<Designation> getDesignations()
	{
		return designations;
	}
	/**
	 * @param designations the designations to set
	 */
	public void setDesignations(List<Designation> designations)
	{
		this.designations = designations;
	}
	/**
	 * @return the versionId
	 */
	public Long getVersionId()
	{
		return versionId;
	}
	/**
	 * @param versionId the versionId to set
	 */
	public void setVersionId(Long versionId)
	{
		this.versionId = versionId;
	}
	/**
	 * @return the versionName
	 */
	public String getVersionName()
	{
		return versionName;
	}
	/**
	 * @param versionName the versionName to set
	 */
	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}
}
