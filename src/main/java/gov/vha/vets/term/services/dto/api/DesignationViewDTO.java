package gov.vha.vets.term.services.dto.api;

import java.util.List;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.Subset;

public class DesignationViewDTO
{
	private Designation designation;
	private List<Property> properties;
	private List<Subset> subsets;
	
	public Designation getDesignation()
	{
		return designation;
	}
	
	public void setDesignation(Designation designation)
	{
		this.designation = designation;
	}
	
	public List<Property> getProperties()
	{
		return properties;
	}
	
	public void setProperties(List<Property> properties)
	{
		this.properties = properties;
	}
	
	public List<Subset> getSubsets()
	{
		return subsets;
	}
	
	public void setSubsets(List<Subset> subsets)
	{
		this.subsets = subsets;
	}
}
