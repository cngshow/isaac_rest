package gov.vha.vets.term.services.dto.importer;

import gov.vha.vets.term.services.model.CodeSystem;

public class FileImportDTO
{
	protected CodeSystem codeSystem;
	protected String versionName;
	protected int conceptCount;
	
	public FileImportDTO(CodeSystem codeSystem, String versionName)
	{
		super();
		this.codeSystem = codeSystem;
		this.versionName = versionName;
	}

	/**
	 * @return the codeSystem
	 */
	public CodeSystem getCodeSystem()
	{
		return codeSystem;
	}

	/**
	 * @param codeSystem the codeSystem to set
	 */
	public void setCodeSystemName(CodeSystem codeSystem)
	{
		this.codeSystem = codeSystem;
	}

	/**
	 * @return the versionName
	 */
	public String getVersionName()
	{
		return versionName;
	}

	/**
	 * @param versionName the versionName to set
	 */
	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}

	/**
	 * @return the conceptCout
	 */
	public int getConceptCount()
	{
		return conceptCount;
	}

	/**
	 * @param conceptCount the conceptCount to set
	 */
	public void setConceptCount(int conceptCount)
	{
		this.conceptCount = conceptCount;
	}
}
