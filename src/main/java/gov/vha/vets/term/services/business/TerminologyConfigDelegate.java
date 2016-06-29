package gov.vha.vets.term.services.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.vha.vets.term.services.dao.TerminologyConfigDao;
import gov.vha.vets.term.services.dto.config.CodeSystemConfig;
import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.config.MapSetConfig;
import gov.vha.vets.term.services.dto.config.StateConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.exception.STSException;

public class TerminologyConfigDelegate
{
	/**
	 * Get a list of all states in the configuration file
	 * @return List<State>
	 * @throws Exception
	 */
	public static List<StateConfig> getStates() throws STSException
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		return config.getStates();
	}
	
	/**
	 * Get a list of domains in the configuration file.  If includeInactiveSubsets
	 * is false, only active subsets will be returned.
	 * @param includeInactiveSubsets
	 * @return List<PublisherDomain>
	 * @throws STSException
	 */
	public static List<DomainConfig> getDomains(boolean includeInactiveSubsets) throws STSException
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
        List<DomainConfig> results = null;
        try
        {
            results = config.getDomains(includeInactiveSubsets);
        }
        catch (Exception e)
        {
            throw new STSException(e);
        }
		return results;
	}
	
	/**
	 * Get a list of all domains in the configuration file
	 * @return List<PublisherDomain>
	 * @throws Exception
	 */
	public static List<DomainConfig> getDomains() throws STSException
	{
		return getDomains(true);
	}
	
	/**
	 * Get the subset configuration information for all subsets under the named domain
	 * @param domainName
	 * @return List<PublisherSubset>
	 * @throws Exception
	 */
	public static List<SubsetConfig> getSubsets(String domainName) throws Exception
	{
		List<SubsetConfig> subsets = new ArrayList<SubsetConfig>();
		
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		List<DomainConfig> domains = config.getDomains(true);
		for (DomainConfig domain : domains)
		{
			if(domain.getName().equals(domainName))
			{
				List<SubsetConfig> domainSubsets = domain.getSubsets();
				subsets.addAll(domainSubsets);
			}
		}
		return subsets;
	}
	
	/**
	 * Get all configuration information for a named Domain
	 * @param domainName
	 * @return PublisherDomain
	 * @throws Exception
	 */
	public static DomainConfig getDomain(String domainName) throws Exception
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		DomainConfig result = null;
		List<DomainConfig> list = config.getDomains(true);
		for (Iterator<DomainConfig> iter = list.iterator(); iter.hasNext();)
		{
			DomainConfig publisherDomain = (DomainConfig) iter.next();
			if (publisherDomain.getName().equals(domainName))
			{
				result = publisherDomain;
				break;
			}
		}
		if (result == null)
		{
			throw new Exception("Domain [" + domainName + "] does not exist.");
		}
		return result;
	}
	
	/**
	 * Get the codeSystem configuration using VUID
	 * @param vuid
	 * @return CodeSystemConfig
	 * @throws STSException 
	 */
	public static CodeSystemConfig getCodeSystem(Long vuid) throws STSException
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
    	CodeSystemConfig codeSystemConfig = null;

    	List<DomainConfig> domainConfigs = config.getDomains(true);
        for (DomainConfig domainConfig : domainConfigs)
		{
        	List<CodeSystemConfig> codeSystemConfigs = domainConfig.getCodeSystems();
        	for (CodeSystemConfig csConfig : codeSystemConfigs)
			{
				if (csConfig.getVuid() == vuid)
				{
					codeSystemConfig = csConfig;
					break;
				}
			}
        	if (codeSystemConfig != null)
        	{
        		break;
        	}
		}
        
        return codeSystemConfig;
	}
	
	/**
	 * Get the named subset configuration
	 * @param regionName
	 * @return PublisherSubset
	 * @throws Exception
	 */
	/*public static RegionConfig getRegion(String regionName) throws STSException
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		RegionConfig result = null;
		List<DomainConfig> list = config.getDomains(true);
		for(Iterator<DomainConfig> domainIter = list.iterator(); domainIter.hasNext(); )
		{
			DomainConfig domain = (DomainConfig) domainIter.next();
			List<SubsetConfig> subsets = domain.
			for(Iterator<SubsetConfig> subsetIter = subsets.iterator(); subsetIter.hasNext(); )
			{
				SubsetConfig subset = (SubsetConfig) subsetIter.next();
				if (subset.getName().equals(regionName))
				{
					result = subset;
					break;
				}
			}
			List<CodeSystemConfig> codeSystemConfigs = domain.getCodeSystems();
			for (CodeSystemConfig codeSystemConfig : codeSystemConfigs)
			{
				if (codeSystemConfig.getName().equals(regionName))
				{
					result = codeSystemConfig;
					break;
				}
			}
		}
		if (result == null)
		{
			throw new STSException("Region [" + regionName + "] does not exist.");
		}
		return result;
	}
	*/
	/**
	 * Get the mapSet configuration for a given vuid
	 * @param vuid
	 * @return
	 * @throws Exception
	 */
	public static MapSetConfig getMapSet(long vuid) throws STSException
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		return config.getMapSet(vuid);
	}
	
    public static List<Long> getMapSetsNotAccessibleVuidList() throws STSException
    {
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		return config.getMapSetsNotAccessibleVuidList();
    }
	
	/**
	 * Get the named subset configuration
	 * @param subsetName
	 * @return PublisherSubset
	 * @throws Exception
	 */
	public static SubsetConfig getSubset(String subsetName) throws Exception
	{
		TerminologyConfigDao config = new TerminologyConfigDao();
		
		SubsetConfig result = null;
		List<DomainConfig> list = config.getDomains(true);
		for(Iterator<DomainConfig> domainIter = list.iterator(); domainIter.hasNext(); )
		{
			DomainConfig domain = (DomainConfig) domainIter.next();
			List<SubsetConfig> subsets = domain.getSubsets();
			for(Iterator<SubsetConfig> subsetIter = subsets.iterator(); subsetIter.hasNext(); )
			{
				SubsetConfig subset = (SubsetConfig) subsetIter.next();
				if (subset.getName().equals(subsetName))
				{
					result = subset;
					break;
				}
			}
		}
		if (result == null)
		{
			throw new Exception("Subset [" + subsetName + "] does not exist.");
		}
		return result;
	}

	public static void main(String[] args)
	{
		try
		{
			MapSetConfig mapSetConfig = TerminologyConfigDelegate.getMapSet(123456);
			System.out.println("Name: " + mapSetConfig.getName());
			System.out.println("VUID: " + mapSetConfig.getVuid());
			System.out.println("GemContent: " + mapSetConfig.isGemContent());
			System.out.println("WebServiceAccessible: " + mapSetConfig.isWebServiceAccessible());
			System.out.println("SourceType: " + mapSetConfig.getSourceType());
			System.out.println("TargetType: " + mapSetConfig.getTargetType());
			System.out.println("Found: " + mapSetConfig.isFound());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
