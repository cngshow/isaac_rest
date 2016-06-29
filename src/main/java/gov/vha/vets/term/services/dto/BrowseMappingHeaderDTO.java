package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.Version;

public class BrowseMappingHeaderDTO
{
	private MapSet mapSet;
	private Designation mapSetDesignation;
	private CodeSystem sourceCodeSystem;
	private CodeSystem targetCodeSystem;
	private Version sourceVersion;
	private Version targetVersion;
	private Version searchVersion;

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

	public CodeSystem getSourceCodeSystem()
	{
		return sourceCodeSystem;
	}

	public void setSourceCodeSystem(CodeSystem sourceCodeSystem)
	{
		this.sourceCodeSystem = sourceCodeSystem;
	}

	public CodeSystem getTargetCodeSystem()
	{
		return targetCodeSystem;
	}

	public void setTargetCodeSystem(CodeSystem targetCodeSystem)
	{
		this.targetCodeSystem = targetCodeSystem;
	}

	public Version getSourceVersion()
	{
		return sourceVersion;
	}

	public void setSourceVersion(Version sourceVersion)
	{
		this.sourceVersion = sourceVersion;
	}

	public Version getTargetVersion()
	{
		return targetVersion;
	}

	public void setTargetVersion(Version targetVersion)
	{
		this.targetVersion = targetVersion;
	}

	public Version getSearchVersion()
	{
		return searchVersion;
	}

	public void setSearchVersion(Version searchVersion)
	{
		this.searchVersion = searchVersion;
	}
}
