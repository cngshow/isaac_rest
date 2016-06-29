package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;

public class MapSetDesignationDTO
{
	private MapSet mapSet;
	private Designation designation;
	

	public MapSetDesignationDTO(MapSet mapSet, Designation designation)
	{
		super();
		
		this.mapSet = mapSet;
		this.designation = designation;
	}

	public MapSet getMapSet()
	{
		return mapSet;
	}
	
	public void setMapSet(MapSet mapSet)
	{
		this.mapSet = mapSet;
	}
	
	public Designation getDesignation()
	{
		return designation;
	}
	
	public void setDesignation(Designation designation)
	{
		this.designation = designation;
	}	
}
