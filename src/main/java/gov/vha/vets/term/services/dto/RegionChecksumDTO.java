/**
 * 
 */
package gov.vha.vets.term.services.dto;

/**
 * @author VHAISLMURDOH
 *
 */
public class RegionChecksumDTO
{
    private String regionName;
    private String checksum;
    
    public RegionChecksumDTO()
    {}
    
    public RegionChecksumDTO(String regionName, String checksum)
    {
        this.checksum = checksum;
        this.regionName = regionName;
    }
    
    public String getRegionChecksum()
    {
        return checksum;
    }
    
    public void setRegionChecksum(String checksum)
    {
        this.checksum = checksum;
    }
    
    public String getRegionName()
    {
        return regionName;
    }
    
    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }
    public String toString()
    {
        return "Region Name: "+regionName+" checksum: "+checksum;
    }
}   