package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.Version;

import java.util.List;

public class VHATQueryDTO
{
	private String searchString;
	private Version version;
	private List<Subset> subsets;
	
	private boolean isVuidSelected;
	private boolean isConceptNameSelected;
	private boolean isDesignationNameSelected;
	private boolean isPropertyNameSelected;
	private boolean isPropertyValueSelected;
	private boolean isRelationshipNameSelected;
	private boolean isRelationshipValueSelected;
	
	public boolean isVuidSelected()
	{
		return isVuidSelected;
	}

	public void setVuidSelected(boolean isVuidSelected)
	{
		this.isVuidSelected = isVuidSelected;
	}

	public boolean isConceptNameSelected()
	{
		return isConceptNameSelected;
	}

	public void setConceptNameSelected(boolean isConceptDescriptionSelected)
	{
		this.isConceptNameSelected = isConceptDescriptionSelected;
	}

	public boolean isDesignationNameSelected()
	{
		return isDesignationNameSelected;
	}

	public void setDesignationNameSelected(boolean isDesignationSelected)
	{
		this.isDesignationNameSelected = isDesignationSelected;
	}

	public boolean isPropertyNameSelected()
	{
		return isPropertyNameSelected;
	}

	public void setPropertyNameSelected(boolean isPropertyNameSelected)
	{
		this.isPropertyNameSelected = isPropertyNameSelected;
	}

	public boolean isPropertyValueSelected()
	{
		return isPropertyValueSelected;
	}

	public void setPropertyValueSelected(boolean isPropertyValueSelected)
	{
		this.isPropertyValueSelected = isPropertyValueSelected;
	}

	public boolean isRelationshipNameSelected()
	{
		return isRelationshipNameSelected;
	}

	public void setRelationshipNameSelected(boolean isRelationshipNameSelected)
	{
		this.isRelationshipNameSelected = isRelationshipNameSelected;
	}

	public boolean isRelationshipValueSelected()
	{
		return isRelationshipValueSelected;
	}

	public void setRelationshipValueSelected(boolean isRelationshipValueSelected)
	{
		this.isRelationshipValueSelected = isRelationshipValueSelected;
	}

	public String getSearchString()
	{
		return searchString;
	}
	
	public void setSearchString(String searchString)
	{
		this.searchString = searchString;
	}
	
	public Version getVersion()
	{
		return version;
	}
	
	public void setVersion(Version version)
	{
		this.version = version;
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
