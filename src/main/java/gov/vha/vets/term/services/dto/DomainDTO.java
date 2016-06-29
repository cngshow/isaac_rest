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
public class DomainDTO implements Comparable<DomainDTO>, Serializable, Cloneable
{
    public static final String NON_DOMAIN = "Non-Domain";
    public static final String NON_DOMAIN_CONCEPTS = "Non-Domain Concepts";
    public static final String MAP_SETS = "Map Sets";
    
    private String name;
	private List<RegionDTO> regionDTOList;
	
    public DomainDTO()
    {
    }

    public DomainDTO(String name, List<RegionDTO> regionDTOList)
    {
        this.name = name;
        this.regionDTOList = regionDTOList;
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
	/**
	 * @return the regionDTOList
	 */
	public List<RegionDTO> getRegionDTOList()
	{
		return regionDTOList;
	}
	/**
	 * @param regionDTOList the regionDTOList to set
	 */
	public void setRegionDTOList(List<RegionDTO> regionDTOList)
	{
		this.regionDTOList = regionDTOList;
	}
	
	public boolean contains(long conceptEntityId)
	{
		for (RegionDTO regionDTO : regionDTOList)
		{
			if (regionDTO.contains(conceptEntityId))
			{
				return true;
			}
		}
		return false; // not found
	}
	
	
	public int compareTo(DomainDTO domainDTO)
	{
		if (domainDTO.getName().equals(NON_DOMAIN) && getName().equals(MAP_SETS))
		{
			return 1; // MapSet to positioned first in the list
		}
		else if (getName().equals(NON_DOMAIN) && domainDTO.getName().equals(MAP_SETS))
		{
			return -1; // NON_DOMAIN to positioned second in the list
		}
		else if (domainDTO.getName().equals(NON_DOMAIN) || domainDTO.getName().equals(MAP_SETS))
		{
			return 1; // NON_DOMAIN and Map Set to positioned first in the list
		}
		else if (getName().equals(NON_DOMAIN) || getName().equals(MAP_SETS))
		{
			return -1; // NON_DOMAIN and Mapset to positioned first in the list
		}
		
		return getName().compareTo(domainDTO.getName());
	}

	public Object clone() throws CloneNotSupportedException
    {
        // First make exact bitwise copy
        DomainDTO copy = (DomainDTO) super.clone();

        List<RegionDTO> regionDTOListClone = new ArrayList<RegionDTO>();
        for (RegionDTO regionDTO : copy.regionDTOList)
        {
            regionDTOListClone.add((RegionDTO) regionDTO.clone());
        }
        copy.setRegionDTOList(regionDTOListClone);
        return copy;
    } // clone
	
}
