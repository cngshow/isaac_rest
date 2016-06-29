package gov.vha.vets.term.services.dto.publish;

import gov.vha.vets.term.services.dto.MapEntryDTO;

import java.util.Date;
import java.util.List;

public class PublishMapSetDTO
{
    private Long mapSetEntityId;
	private String name;
	private Long vuid;
	private Date effectiveDate;
	private List<MapEntryDTO> mapEntries;
	
	public PublishMapSetDTO()
	{
		super();
	}
	
	public PublishMapSetDTO(Long mapSetEntityId, Long vuid, String name, Date effectiveDate, List<MapEntryDTO> mapEntries)
	{
		super();
		this.mapSetEntityId = mapSetEntityId; 
		this.vuid = vuid;
		this.name = name;
		this.effectiveDate = effectiveDate;
		this.mapEntries = mapEntries;
	}
	
	/**
	 * the EffectiveDate to get
	 * @return Date
	 */
	public Date getEffectiveDate()
	{
		return this.effectiveDate;
	}
	
	/**
	 * The EffectiveDate to set
	 * @param effectiveDate
	 */
	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
	
	/**
	 * The MapEntries to get
	 * @return List<MapEntryDTO>
	 */
	public List<MapEntryDTO> getMapEntries()
	{
		return this.mapEntries;
	}
	
	/**
	 * The MapEntries to set
	 * @param mapEntries
	 */
	public void setMapEntries(List<MapEntryDTO> mapEntries)
	{
		this.mapEntries = mapEntries;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getVuid()
	{
		return vuid;
	}

	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}

    public long getMapSetEntityId()
    {
        return mapSetEntityId;
    }

    public void setMapSetEntityId(long mapSetEntityId)
    {
        this.mapSetEntityId = mapSetEntityId;
    }
	
	
}
