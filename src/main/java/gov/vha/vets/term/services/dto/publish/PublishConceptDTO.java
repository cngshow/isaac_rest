/**
 * 
 */
package gov.vha.vets.term.services.dto.publish;

import gov.vha.vets.term.services.business.ServicesDeploymentDelegate.DataChangeType;

import java.io.Serializable;
import java.util.List;

/**
 * @author VHAISAOSTRAR
 *
 */
public class PublishConceptDTO implements Serializable 
{
	private String publishName;
	private Long vuid;
	private boolean active;
	private DataChangeType changeType;
	private List<NameValueDTO> propertyList;
	private List<NameValueDTO> relationshipList;
	private List<NameValueDTO> designationList;
	
	/**
	 * @return the active
	 */
	public boolean isActive()
	{
		return active;
	}
	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active)
	{
		this.active = active;
	}
	/**
	 * @return the propertyList
	 */
	public List<NameValueDTO> getPropertyList()
	{
		return propertyList;
	}
	/**
	 * @param propertyList the propertyList to set
	 */
	public void setPropertyList(List<NameValueDTO> propertyList)
	{
		this.propertyList = propertyList;
	}
	/**
	 * @return the publishName
	 */
	public String getPublishName()
	{
		return publishName;
	}
	/**
	 * @param publishName the publishName to set
	 */
	public void setPublishName(String publishName)
	{
		this.publishName = publishName;
	}
	/**
	 * @return the relationshipList
	 */
	public List<NameValueDTO> getRelationshipList()
	{
		return relationshipList;
	}
	/**
	 * @param relationshipList the relationshipList to set
	 */
	public void setRelationshipList(List<NameValueDTO> relationshipList)
	{
		this.relationshipList = relationshipList;
	}
	/**
	 * Get the designation list
	 * @return the designation list
	 */
	public List<NameValueDTO> getDesignationList()
	{
		return designationList;
	}
	/**
	 * Set the designation list
	 * @param designationList to set
	 */
	public void setDesignationList(List<NameValueDTO> designationList)
	{
		this.designationList = designationList;
	}
	/**
	 * @return the vuid
	 */
	public Long getVuid()
	{
		return vuid;
	}
	/**
	 * @param vuid the vuid to set
	 */
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}
	public DataChangeType getChangeType()
	{
		return changeType;
	}
	public void setChangeType(DataChangeType changeType)
	{
		this.changeType = changeType;
	}
}
