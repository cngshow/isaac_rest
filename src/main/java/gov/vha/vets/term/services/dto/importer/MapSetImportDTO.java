package gov.vha.vets.term.services.dto.importer;

import java.util.Date;

public class MapSetImportDTO extends ConceptImportDTO
{
	String sourceCodeSystemName;
	String sourceVersionName;
	String targetCodeSystemName;
	String targetVersionName;
	protected Date effectiveDate;
	
	public MapSetImportDTO(String action, String name, String code, Long vuid,
			boolean active, String sourceCodeSystemName, String sourceVersionName, 
			String targetCodeSystemName, String targetVersionName, Date effectiveDate)
	{
		super(action, name, code, vuid, active);
		this.sourceCodeSystemName = sourceCodeSystemName;
		this.sourceVersionName = sourceVersionName;
		this.targetCodeSystemName = targetCodeSystemName;
		this.targetVersionName = targetVersionName;
		this.effectiveDate = effectiveDate;
	}

	public String getSourceCodeSystemName()
	{
		return sourceCodeSystemName;
	}

	public void setSourceCodeSystemName(String sourceCodeSystemName)
	{
		this.sourceCodeSystemName = sourceCodeSystemName;
	}

	public String getSourceVersionName()
	{
		return sourceVersionName;
	}

	public void setSourceVersionName(String sourceVersionName)
	{
		this.sourceVersionName = sourceVersionName;
	}

	public String getTargetCodeSystemName()
	{
		return targetCodeSystemName;
	}

	public void setTargetCodeSystemName(String targetCodeSystemName)
	{
		this.targetCodeSystemName = targetCodeSystemName;
	}

	public String getTargetVersionName()
	{
		return targetVersionName;
	}

	public void setTargetVersionName(String targetVersionName)
	{
		this.targetVersionName = targetVersionName;
	}

	public Date getEffectiveDate()
	{
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
    
}
