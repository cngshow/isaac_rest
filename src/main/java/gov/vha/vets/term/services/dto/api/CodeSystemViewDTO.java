package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Version;

import java.util.List;

public class CodeSystemViewDTO
{
	private CodeSystem codeSystem;
	private List<Version> versions;
	
	public CodeSystemViewDTO(CodeSystem codeSystem, List<Version> versions)
	{
		super();
		this.codeSystem = codeSystem;
		this.versions = versions;
	}
	
	public CodeSystemViewDTO()
	{
		super();
	}

	public CodeSystem getCodeSystem()
	{
		return codeSystem;
	}

	public void setCodeSystem(CodeSystem codeSystem)
	{
		this.codeSystem = codeSystem;
	}

	public List<Version> getVersions()
	{
		return versions;
	}

	public void setVersions(List<Version> versions)
	{
		this.versions = versions;
	}
}
