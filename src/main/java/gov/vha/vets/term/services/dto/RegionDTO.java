/**
 * 
 */
package gov.vha.vets.term.services.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VHAISAOSTRAR
 *
 */
public class RegionDTO implements Serializable, Cloneable, Comparable<RegionDTO>
{
	public static final String NON_SUBSET = "Non-Subset";
	private String name;
	private boolean active;
	private List<ConceptEntityDTO> conceptEntityDTOList;
	
    public RegionDTO()
    {
    }

    public RegionDTO(String name, List<ConceptEntityDTO> conceptEntityDTOList, boolean active)
    {
        this.name = name;
        this.active = active;
        this.conceptEntityDTOList = conceptEntityDTOList;
    }
	/**
	 * @return the conceptEntityDTOList
	 */
	public List<ConceptEntityDTO> getConceptEntityDTOList()
	{
		return conceptEntityDTOList;
	}
	/**
	 * @param conceptEntityDTOList the conceptEntityDTOList to set
	 */
	public void setConceptEntityDTOList(List<ConceptEntityDTO> conceptEntityDTOList)
	{
		this.conceptEntityDTOList = conceptEntityDTOList;
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	public boolean contains(long conceptEntityId)
	{
		for (ConceptEntityDTO conceptEntityDTO : conceptEntityDTOList)
		{
			if (conceptEntityDTO.entityId == conceptEntityId)
			{
				return true;
			}
		}
		return false; // not found
	}
    public Object clone() throws CloneNotSupportedException
    {
        // First make exact bitwise copy
        RegionDTO copy = (RegionDTO) super.clone();
        List<ConceptEntityDTO> conceptEntityDTOListClone = new ArrayList<ConceptEntityDTO>();
        for (ConceptEntityDTO conceptEntityDTO : copy.conceptEntityDTOList)
        {
            conceptEntityDTOListClone.add((ConceptEntityDTO) conceptEntityDTO.clone());
        }
        copy.setConceptEntityDTOList(conceptEntityDTOListClone);
        return copy;
    } // clone

	public int compareTo(RegionDTO regionDTO)
	{
		return name.compareTo(regionDTO.name);
	}

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }
	
}
