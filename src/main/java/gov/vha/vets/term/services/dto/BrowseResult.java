package gov.vha.vets.term.services.dto;

public interface BrowseResult {
    public long getMapEntryEntityId();
    
    public void setMapEntryEntityId(long mapEntryEntityId);

    public String getSourceCode();
    
    public void setSourceCode(String sourceCode);

    public String getSourceCodeDescription();
  
    public void setSourceCodeDescription(String sourceCodeDescription);

    public String getTargetCode();

    public void setTargetCode(String targetCode);

    public String getTargetCodeDescription();

    public void setTargetCodeDescription(String targetCodeDescription);

    public int getSequence();

    public void setSequence(int sequence);

    public String getActive();

    public void setActive(String active);
}
