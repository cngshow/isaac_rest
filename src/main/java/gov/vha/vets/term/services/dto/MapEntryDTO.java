package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSetRelationship;

public class MapEntryDTO
{
	private MapEntry mapEntry;
	private MapSetRelationship mapSetRelationship;
	
	public MapEntryDTO()
	{
		super();
	}
	
	public MapEntryDTO(MapEntry mapEntry, MapSetRelationship mapSetRelationship)
	{
		super();
		this.mapEntry = mapEntry;
		this.mapSetRelationship = mapSetRelationship;
	}

	public MapEntry getMapEntry()
	{
		return mapEntry;
	}

	public void setMapEntry(MapEntry mapEntry)
	{
		this.mapEntry = mapEntry;
	}

	public MapSetRelationship getMapSetRelationship()
	{
		return mapSetRelationship;
	}

	public void setMapSetRelationship(MapSetRelationship mapSetRelationship)
	{
		this.mapSetRelationship = mapSetRelationship;
	}
	
}
