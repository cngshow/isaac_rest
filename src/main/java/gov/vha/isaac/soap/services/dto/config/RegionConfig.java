package gov.vha.isaac.soap.services.dto.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("serial")
public class RegionConfig implements Comparable<RegionConfig>, Serializable
{
    protected String name;
    protected List<PropertyConfig> propertyFilters;
    private List<RelationshipConfig> relationshipFilters;
    protected List<DesignationConfig> designationFilters;

    
    public RegionConfig(String name, List<PropertyConfig> propertyFilters, List<RelationshipConfig> relationshipFilters,
			List<DesignationConfig> designationFilters)
	{
		super();
		this.name = name;
		this.propertyFilters = propertyFilters;
		this.relationshipFilters = relationshipFilters;
		this.designationFilters = designationFilters;
	}

	public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public List<PropertyConfig> getPropertyFilters()
    {
        return propertyFilters;
    }

    @SuppressWarnings("unchecked")
	public List<String> getPropertyNameList()
    {
        List<String> propertyNames = new ArrayList<String>();
        // build the property list
        for (Iterator iter = propertyFilters.iterator(); iter.hasNext();)
        {
            PropertyConfig filter = (PropertyConfig) iter.next();
            propertyNames.add(filter.getName());
        }
        Collections.sort(propertyNames);
        return propertyNames;
    }

    public List<DesignationConfig> getDesignationFilters()
    {
        return designationFilters;
    }

    @SuppressWarnings("unchecked")
	public List<String> getDesignationNameList()
    {
        List<String> names = new ArrayList<String>();
        // build the property list
        for (Iterator iter = designationFilters.iterator(); iter.hasNext();)
        {
            DesignationConfig filter = (DesignationConfig) iter.next();
            names.add(filter.getName());
        }
        Collections.sort(names);
        return names;
    }
    
    public List<RelationshipConfig> getRelationshipsFilters()
    {
        return relationshipFilters;
    }

    public List<String> getRelationshipNameList()
    {
        List<String> relationshipNames = new ArrayList<String>();
        // build the property list
        for (Iterator iter = relationshipFilters.iterator(); iter.hasNext();)
        {
            RelationshipConfig filter = (RelationshipConfig) iter.next();
            relationshipNames.add(filter.getName());
        }
        Collections.sort(relationshipNames);
        return relationshipNames;
    }

    public List<String> getRegularRelationshipNameList()
    {
        List<String> relationshipNames = new ArrayList<String>();
        // build the property list
        for (Iterator iter = relationshipFilters.iterator(); iter.hasNext();)
        {
            RelationshipConfig filter = (RelationshipConfig) iter.next();
            if (!filter.isInverse())
            {
                relationshipNames.add(filter.getName());
            }
        }
        Collections.sort(relationshipNames);
        return relationshipNames;
    }

    public List<String> getInverseRelationshipNameList()
    {
        List<String> relationshipNames = new ArrayList<String>();
        // build the property list
        for (Iterator iter = relationshipFilters.iterator(); iter.hasNext();)
        {
            RelationshipConfig filter = (RelationshipConfig) iter.next();
            if (filter.isInverse())
            {
                relationshipNames.add(filter.getName());
            }
        }
        Collections.sort(relationshipNames);
        return relationshipNames;
    }
    
	public int compareTo(RegionConfig config)
	{
		return name.compareTo(config.name);
	}

}
