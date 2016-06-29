package gov.vha.vets.term.services.dto;

import java.util.List;

public class MapEntryCacheListDTO
{
	private Long totalNumberOfRecords;
	private List<MapEntryCacheDTO> mapEntryCaches;
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	public List<MapEntryCacheDTO> getMapEntryCaches()
	{
		return mapEntryCaches;
	}
	public void setMapEntryCaches(List<MapEntryCacheDTO> mapEntryCaches)
	{
		this.mapEntryCaches = mapEntryCaches;
	}
}
