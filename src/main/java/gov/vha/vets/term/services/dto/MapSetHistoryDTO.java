package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Version;

import java.util.List;
import java.util.Map;

public class MapSetHistoryDTO
{
	private MapSet mapSet;
	private Designation mapSetDesignation;
	private List<Version> mapSetVersions;
	private Map<String, List<MapEntryVersionDetailDTO>> mapEntryVersionMap;
	private MapEntryVersionDetailDTO selectedMapEntryDetail;
	private int maxSequenceNumber = 0;
	private Version mostCurrentVersion;
	
	public List<Version> getMapSetVersions() {
		return mapSetVersions;
	}

	public MapSet getMapSet()
	{
		return mapSet;
	}

	public void setMapSet(MapSet mapSet)
	{
		this.mapSet = mapSet;
	}

	public Designation getMapSetDesignation()
	{
		return mapSetDesignation;
	}

	public void setMapSetDesignation(Designation mapSetDesignation)
	{
		this.mapSetDesignation = mapSetDesignation;
	}

	/**
	 * @return the mapSets
	 */
	public List<Version> getMapEntryVersions()
	{
		return mapSetVersions;
	}
	
	/**
	 * @param mapSets the mapSets to set
	 */
	public void setMapSetVersions(List<Version> mapSetVersions)
	{
		this.mapSetVersions = mapSetVersions;
	}
	
	/**
	 * @return the mapEntryVersionMap
	 */
	public Map<String, List<MapEntryVersionDetailDTO>> getMapEntryVersionMap()
	{
		return mapEntryVersionMap;
	}
	
	/**
	 * @param mapEntryVersionMap the mapEntryVersionMap to set
	 */
	public void setMapEntryVersionMap(Map<String, List<MapEntryVersionDetailDTO>> mapEntryVersionMap)
	{
		this.mapEntryVersionMap = mapEntryVersionMap;
	}

	public MapEntryVersionDetailDTO getSelectedMapEntryDetail()
	{
		return selectedMapEntryDetail;
	}

	public void setSelectedMapEntryDetail(MapEntryVersionDetailDTO selectedMapEntryDetail)
	{
		this.selectedMapEntryDetail = selectedMapEntryDetail;
	}

	/**
	 * @return the maxSequenceNumber
	 */
	public int getMaxSequenceNumber()
	{
		return maxSequenceNumber;
	}

	/**
	 * @param maxSequenceNumber the maxSequenceNumber to set
	 */
	public void setMaxSequenceNumber(int maxSequenceNumber)
	{
		this.maxSequenceNumber = maxSequenceNumber;
	}
	
	public Version getMostCurrentVersion() {
		return mostCurrentVersion;
	}

	public void setMostCurrentVersion(Version mostCurrentVersion) {
		this.mostCurrentVersion = mostCurrentVersion;
	}


}
