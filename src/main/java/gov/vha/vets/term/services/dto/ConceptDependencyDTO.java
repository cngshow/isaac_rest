package gov.vha.vets.term.services.dto;

public class ConceptDependencyDTO
{
    String sourceName;
    Long sourceVuid;
    String sourceDeploymentName;
    String targetName;
    Long targetVuid;
    String targetDeploymentName;
    
    public ConceptDependencyDTO(String sourceName, Long sourceVuid, String sourceDeploymentName, String targetName, Long targetVuid, String targetDeploymentName)
    {
        super();
        this.sourceName = sourceName;
        this.sourceVuid = sourceVuid;
        this.targetName = targetName;
        this.targetVuid = targetVuid;
        this.sourceDeploymentName = sourceDeploymentName;
        this.targetDeploymentName = targetDeploymentName;
    }
    
    public Long getSourceVuid()
    {
        return sourceVuid;
    }

    public void setSourceVuid(Long sourceVuid)
    {
        this.sourceVuid = sourceVuid;
    }

    public Long getTargetVuid()
    {
        return targetVuid;
    }

    public void setTargetVuid(Long targetVuid)
    {
        this.targetVuid = targetVuid;
    }

    public String getSourceName()
    {
        return sourceName;
    }
    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }
    public String getTargetName()
    {
        return targetName;
    }
    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public String getSourceDeploymentName()
    {
        return sourceDeploymentName;
    }

    public void setSourceDeploymentName(String sourceDeploymentName)
    {
        this.sourceDeploymentName = sourceDeploymentName;
    }

    public String getTargetDeploymentName()
    {
        return targetDeploymentName;
    }

    public void setTargetDeploymentName(String targetDeploymentName)
    {
        this.targetDeploymentName = targetDeploymentName;
    }

    
}
