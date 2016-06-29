package gov.vha.vets.term.services.dto;

import java.util.List;

public class MapSetDetailListDTO
{
	private Long totalNumberOfRecords;
	private List<MapSetDetailDTO> mapSetDetails;
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	public List<MapSetDetailDTO> getMapSetDetails()
	{
		return mapSetDetails;
	}
	public void setMapSetDetails(List<MapSetDetailDTO> mapSetDetails)
	{
		this.mapSetDetails = mapSetDetails;
	}
}
