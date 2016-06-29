package gov.vha.vets.term.services.dto.config;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class CodeSystemConfig extends RegionConfig 
{
    private long vuid;

    public CodeSystemConfig(String name, long vuid, List<PropertyConfig> propertyFilters, List<RelationshipConfig> relationshipFilters, List<DesignationConfig> designationFilters)
    {
    	super(name, propertyFilters, relationshipFilters, designationFilters);
        this.vuid = vuid;
    }


    public long getVuid()
    {
        return vuid;
    }

    public void setVuid(long vuid)
    {
        this.vuid = vuid;
    }
}
