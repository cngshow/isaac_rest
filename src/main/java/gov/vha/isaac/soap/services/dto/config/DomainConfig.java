package gov.vha.isaac.soap.services.dto.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class DomainConfig implements Comparable<DomainConfig>, Serializable
{
	private String name;
	private List<SubsetConfig> publisherSubsets;
	private List<CodeSystemConfig> codeSystems;
	
	public DomainConfig(String name, List<SubsetConfig> publisherSubsets, List<CodeSystemConfig> publisherCodeSystems)
	{
		super();
		this.name = name;
		this.publisherSubsets = publisherSubsets;
		this.codeSystems = publisherCodeSystems;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public List<SubsetConfig> getSortedSubsets()
	{
        List<SubsetConfig> sortedList = new ArrayList<SubsetConfig>();
        sortedList.addAll(publisherSubsets);
        Collections.sort(sortedList);
		return sortedList;
	}
	
    public List<SubsetConfig> getSubsets()
    {
        return publisherSubsets;
    }
    
	public void setSubsets(List<SubsetConfig> publisherSubsets)
	{
		this.publisherSubsets = publisherSubsets;
	}

	public List<CodeSystemConfig> getCodeSystems()
	{
		return codeSystems;
	}

	public void setCodeSystems(List<CodeSystemConfig> codeSystems)
	{
		this.codeSystems = codeSystems;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DomainConfig domainConfig)
	{
		return getName().compareTo(domainConfig.getName());
	}
}
