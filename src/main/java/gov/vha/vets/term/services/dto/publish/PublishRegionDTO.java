/**
 * 
 */
package gov.vha.vets.term.services.dto.publish;

import java.io.Serializable;
import java.util.List;

/**
 * @author VHAISAOSTRAR
 *
 */
public class PublishRegionDTO implements Serializable 
{
	private String regionName;
	private List<PublishConceptDTO> publishConceptDTOList;
	

    public PublishRegionDTO(String regionName, List<PublishConceptDTO> publishConceptDTOList)
    {
        super();
        this.regionName = regionName;
        this.publishConceptDTOList = publishConceptDTOList;
    }
    /**
	 * @return the publishConceptDTOList
	 */
	public List<PublishConceptDTO> getPublishConceptDTOList()
	{
		return publishConceptDTOList;
	}
	/**
	 * @param publishConceptDTOList the publishConceptDTOList to set
	 */
	public void setPublishConceptDTOList(
			List<PublishConceptDTO> publishConceptDTOList)
	{
		this.publishConceptDTOList = publishConceptDTOList;
	}
	/**
	 * @return the regionName
	 */
	public String getRegionName()
	{
		return regionName;
	}
	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName)
	{
		this.regionName = regionName;
	}

	
}
