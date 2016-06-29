package gov.vha.vets.term.services.dto.history;

import java.util.ArrayList;
import java.util.List;

public class DesignationEntityHistoryDTO extends EntityHistoryDTO
{
    protected List<EntityHistoryDTO> properties;
    protected List<EntityHistoryDTO> subsets;

    public DesignationEntityHistoryDTO()
    {
        super();
        properties = new ArrayList<EntityHistoryDTO>();
        subsets = new ArrayList<EntityHistoryDTO>();
    }
    /**
     * @return the properties
     */
    public List<EntityHistoryDTO> getProperties()
    {
        return properties;
    }
    /**
     * @param properties the properties to set
     */
    public void setProperties(List<EntityHistoryDTO> properties)
    {
        this.properties = properties;
    }
    
    public void addProperty(EntityHistoryDTO property)
    {
        properties.add(property);
    }
    /**
     * @return the subsets
     */
    public List<EntityHistoryDTO> getSubsets()
    {
        return subsets;
    }
    /**
     * @param subsets the subsets to set
     */
    public void setSubsets(List<EntityHistoryDTO> subsets)
    {
        this.subsets = subsets;
    }

    public void addSubset(EntityHistoryDTO subset)
    {
        subsets.add(subset);
    }
}
