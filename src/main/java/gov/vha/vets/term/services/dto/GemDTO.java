package gov.vha.vets.term.services.dto;

import java.util.List;

public class GemDTO
{
	private String mapSetName;
	private String sourceCodeSystemName;
	private String targetCodeSystemName;
	private String sourceCode;
	private String sourceDescription;
	private List<GemVersionDTO> gemVersions;
	
	
	public String getSourceCode()
	{
		return sourceCode;
	}
	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}
	public String getSourceDescription()
	{
		return sourceDescription;
	}
	public void setSourceDescription(String sourceDescription)
	{
		this.sourceDescription = sourceDescription;
	}
	public String getSourceCodeSystemName()
	{
		return sourceCodeSystemName;
	}
	public void setSourceCodeSystemName(String sourceCodeSystemName)
	{
		this.sourceCodeSystemName = sourceCodeSystemName;
	}
	public String getTargetCodeSystemName()
	{
		return targetCodeSystemName;
	}
	public void setTargetCodeSystemName(String targetCodeSystemName)
	{
		this.targetCodeSystemName = targetCodeSystemName;
	}
	public List<GemVersionDTO> getGemVersions()
	{
		return gemVersions;
	}
	public void setGemVersions(List<GemVersionDTO> gemVersions)
	{
		this.gemVersions = gemVersions;
	}
	public String getMapSetName()
	{
		return mapSetName;
	}
	public void setMapSetName(String mapSetName)
	{
		this.mapSetName = mapSetName;
	}
}
