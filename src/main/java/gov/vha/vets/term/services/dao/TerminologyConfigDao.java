package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.config.XSDLocator;
import gov.vha.vets.term.services.dto.config.CodeSystemConfig;
import gov.vha.vets.term.services.dto.config.DependentSubsetRule;
import gov.vha.vets.term.services.dto.config.DesignationConfig;
import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.config.MapSetConfig;
import gov.vha.vets.term.services.dto.config.PropertyConfig;
import gov.vha.vets.term.services.dto.config.RelationshipConfig;
import gov.vha.vets.term.services.dto.config.StateConfig;
import gov.vha.vets.term.services.dto.config.SubsetConfig;
import gov.vha.vets.term.services.exception.STSException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.msv.verifier.jarv.TheFactoryImpl;

public class TerminologyConfigDao
{
	// XML Tag Names
	private static final String DOMAINS = "Domains";
	private static final String SUBSET = "Subset";
	private static final String CODESYSTEM = "CodeSystem";
	private static final String STATES = "States";
	private static final String MAPSETS = "MapSets";
    private static final String DEPENDENCIES = "Dependencies";
    private static final String ALLOW_EMPTY = "AllowEmpty";
	private static final String PROPERTIES = "Properties";
	private static final String RELATIONSHIPS = "Relationships";
	private static final String DESIGNATIONS = "Designations";
	private static final String ACTIVE = "Active";
	private static final String NAME = "Name";
	private static final String TYPE = "Type";
	private static final String IS_LIST = "IsList";
	private static final String INVERSE = "Inverse";
	private static final String PROPERTY_TYPE = "PropertyType";
	private static final String PROPERTY_VALUE = "PropertyValue";
	private static final String INCLUDE_WITH_CHANGE = "IncludeWithChange";
    private static final String SUBSET_NAME = "SubsetName";
    private static final String RELATIONSHIP_NAME = "RelationshipName";
    private static final String VUID = "VUID";
    private static final String GEM_CONTENT = "GemContent";
    private static final String WEB_SERVICE_ACCESSIBLE = "WebServiceAccessible";
    private static final String SOURCE_TYPE = "SourceType";
    private static final String TARGET_TYPE = "TargetType";
    private static final String MAPSET_TYPE = "ConceptCode";

	// Default XML File and Schema
	private String configFileName = "TerminologyConfig.xml";
	private String testingConfigFileName = "TestingTerminologyConfig.xml";
	private String schemaFileName = "TerminologyConfig.xsd";

	// instance variables
	List<DomainConfig> publisherDomains = new ArrayList<DomainConfig>();

	private boolean validated = false;

	/**
	 * Gets the entire state list
	 * @return List List of concept state names
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    public List<StateConfig> getStates() throws STSException
	{
		List<StateConfig> stateList = new ArrayList<StateConfig>();
		
		try
		{
			// Load the XML File and Walk Down the Tree.
			Element terminology = getTerminologyConfigRootElement();
			List<Element> configSectionList = terminology.getChildren();
			
			for(int i = 0; i < configSectionList.size(); i++)
			{
				Element section = configSectionList.get(i);
				String sectionName = section.getName();
				if(sectionName.equals(STATES))
				{
					List<Element> states = section.getChildren();
					for(int m = 0; m < states.size(); m++)
					{
						StateConfig newState = new StateConfig();
						Element state = states.get(m);
						List<Element> stateElements = state.getChildren();
						for(int n = 0; n < stateElements.size(); n++)
						{
							Element stateItem = stateElements.get(n);
							
							if(stateItem.getName().equals(NAME))
							{
//								stateValues.add(new StateFilter(stateItem.getValue()));
								newState.setName(stateItem.getValue());
							}
							if(stateItem.getName().equals(TYPE))
							{
								newState.setType(stateItem.getValue());
							}
							
						}
						stateList.add(newState);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new STSException(e);
		}
		
		return stateList;
	}
	
	/**
	 * Get a MapSetConfig for a given VUID
	 * @return MapSetConfig
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    public MapSetConfig getMapSet(long vuid) throws STSException
	{
		MapSetConfig mapSetConfig = new MapSetConfig(false, true, MAPSET_TYPE, MAPSET_TYPE);
		try
		{
			// Load the XML File and Walk Down the Tree.
			Element terminology = getTerminologyConfigRootElement();
			List<Element> configSectionList = terminology.getChildren();
			
			for (int i = 0; i < configSectionList.size(); i++)
			{
				Element section = configSectionList.get(i);
				String sectionName = section.getName();
				if (sectionName.equals(MAPSETS))
				{
					List<Element> mapSets = section.getChildren();
					for (int m = 0; m < mapSets.size(); m++)
					{
						MapSetConfig newMapSetConfig = new MapSetConfig(false, true, MAPSET_TYPE, MAPSET_TYPE);
						Element mapSet = mapSets.get(m);
						List<Element> mapSetElements = mapSet.getChildren();
						boolean foundMapSet = false;
						String name = null;
						for (int n = 0; n < mapSetElements.size(); n++)
						{
							Element mapSetItem = mapSetElements.get(n);
							
							if (mapSetItem.getName().equals(NAME))
							{
								name = mapSetItem.getValue();
							}
							else if (mapSetItem.getName().equals(VUID))
							{
								long vuidValue = Long.parseLong(mapSetItem.getValue());
								if (vuidValue != vuid)
								{
									break;
								}
								foundMapSet = true;
								newMapSetConfig.setVuid(vuidValue);
								newMapSetConfig.setName(name);
							}
							else if (mapSetItem.getName().equals(GEM_CONTENT))
							{
								newMapSetConfig.setGemContent(Boolean.parseBoolean(mapSetItem.getValue()));
							}
							else if (mapSetItem.getName().equals(WEB_SERVICE_ACCESSIBLE))
							{
								newMapSetConfig.setWebServiceAccessible(Boolean.parseBoolean(mapSetItem.getValue()));
							}
							else if (mapSetItem.getName().equals(SOURCE_TYPE))
							{
								newMapSetConfig.setSourceType(mapSetItem.getValue());
							}
							else if (mapSetItem.getName().equals(TARGET_TYPE))
							{
								newMapSetConfig.setTargetType(mapSetItem.getValue());
							}
						}
						mapSetConfig = newMapSetConfig;
						if (foundMapSet)
						{
							mapSetConfig.setFound(true);
							break;
						}
					}
					
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new STSException(e);
		}
		
		return mapSetConfig;
	}
	
	/**
	 * Get a list of map set VUIDs that are not to be accessible in web services
	 * @return List of Map Set VUIDs
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    public List<Long> getMapSetsNotAccessibleVuidList() throws STSException
	{
		List<Long> MapSetNotAccessibleVuidList = new ArrayList<Long>();
		try
		{
			// Load the XML File and Walk Down the Tree.
			Element terminology = getTerminologyConfigRootElement();
			List<Element> configSectionList = terminology.getChildren();
			
			for (int i = 0; i < configSectionList.size(); i++)
			{
				Element section = configSectionList.get(i);
				String sectionName = section.getName();
				if (sectionName.equals(MAPSETS))
				{
					List<Element> mapSets = section.getChildren();
					for (int m = 0; m < mapSets.size(); m++)
					{
						Element mapSet = mapSets.get(m);
						List<Element> mapSetElements = mapSet.getChildren();
						long vuid = 0L;
						for (int n = 0; n < mapSetElements.size(); n++)
						{
							Element mapSetItem = mapSetElements.get(n);
							
							if (mapSetItem.getName().equals(VUID))
							{
								vuid = Long.parseLong(mapSetItem.getValue());
							}
							else if (mapSetItem.getName().equals(WEB_SERVICE_ACCESSIBLE))
							{
								boolean isAccessable = Boolean.parseBoolean(mapSetItem.getValue());
								if (isAccessable == false)
								{
									MapSetNotAccessibleVuidList.add(vuid);
								}
							}
						}
					}
					
					break;
				}
			}
		}
		catch (Exception e)
		{
			throw new STSException(e);
		}
		
		return MapSetNotAccessibleVuidList;
	}
	
	/**
	 * Get all configuration information for all Domains
	 * @return List List of PublisherDomain objects
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    public List<DomainConfig> getDomains(boolean includeInactiveSubsets) throws STSException
	{
		try
		{
			// Load the XML File and Walk Down the Tree.
			Element terminology = getTerminologyConfigRootElement();
			List<Element> configSectionList = terminology.getChildren();
			
			SubsetConfig publisherSubset = null;
			CodeSystemConfig publisherCodeSystem = null;
			DomainConfig publisherDomain = null;
			
			String domainName = null;
			String subsetName = null;
			String codeSystemName = null;
			Long codeSystemVuid = null;
			boolean active = false;
			List<PropertyConfig> propertyFilterList = null;
			
			// Loop Through all Sections
			for (int i = 0; i < configSectionList.size(); i++)
			{
				Element section = configSectionList.get(i);
				String sectionName = section.getName();
				if (sectionName.equals(DOMAINS))
				{
					// get the Domains section list
					List<Element> domains = section.getChildren();
					for (int m = 0; m < domains.size(); m++)
					{
						//for each domain, create a new publisherSubsets List
						List<SubsetConfig> publisherSubsets = new ArrayList<SubsetConfig>();
						List<CodeSystemConfig> publisherCodeSystems = new ArrayList<CodeSystemConfig>();
						
						Element domainElement = domains.get(m);
						List domainParts = domainElement.getChildren();
						for (int n = 0; n < domainParts.size(); n++)
						{
							Element domainPart = (Element) domainParts.get(n);
							if (domainPart.getName().equals(NAME))
							{
								domainName = domainPart.getValue();
							}
							else if (domainPart.getName().equals(SUBSET))
							{
								Element subset = (Element) domainParts.get(n);
					            // must set this to empty array list because this is an optional attribute and might not be specified
								// therefore the rules will be null
					            List<DependentSubsetRule> dependentSubsetRules = new ArrayList<DependentSubsetRule>();
					            List<RelationshipConfig> relationshipFilterList = null;
					            List<DesignationConfig> designationFilterList = null;
								// Get All Children of Subset
								List<Element> subsetChildren = subset.getChildren();
								for (int r = 0; r < subsetChildren.size(); r++)
								{
									Element subsetChild = subsetChildren.get(r);
									if (subsetChild.getName().equals(NAME))
									{
										subsetName = subsetChild.getValue();
									}
									if (subsetChild.getName().equals(ACTIVE))
									{
										active = new Boolean(subsetChild.getValue()).booleanValue();
									}
                                    if (subsetChild.getName().equals(DEPENDENCIES))
                                    {
                                        dependentSubsetRules = processDependencies(subsetChild);
                                    }
									if (subsetChild.getName().equals(PROPERTIES))
									{
										propertyFilterList = processProperties(subsetChild);
									}
									if (subsetChild.getName().equals(RELATIONSHIPS))
									{
										relationshipFilterList = processRelationships(subsetChild);
									}
									if (subsetChild.getName().equals(DESIGNATIONS))
									{
										designationFilterList = processDesignations(subsetChild);
									}
								}
                                
								if(includeInactiveSubsets == true)
								{
									publisherSubset = new SubsetConfig(subsetName, active,
	                                        dependentSubsetRules, propertyFilterList, relationshipFilterList,
	                                        designationFilterList);
									publisherSubsets.add(publisherSubset);
								}
								else
								{
									if(active == true)
									{
										publisherSubset = new SubsetConfig(subsetName, active,
		                                        dependentSubsetRules, propertyFilterList, relationshipFilterList,
		                                        designationFilterList);
										publisherSubsets.add(publisherSubset);
									}
								}
							}
							else if (domainPart.getName().equals(CODESYSTEM))
							{
								Element subset = (Element) domainParts.get(n);
					            // must set this to empty array list because this is an optional attribute and might not be specified
								// therefore the rules will be null
					            List<DesignationConfig> designationFilterList = null;
					            List<RelationshipConfig> relationshipFilterList = new ArrayList<RelationshipConfig>();
								// Get All Children of Subset
								List<Element> codeSystemChildren = subset.getChildren();
								for (int r = 0; r < codeSystemChildren.size(); r++)
								{
									Element codeSystemChild = codeSystemChildren.get(r);
									if (codeSystemChild.getName().equals(NAME))
									{
										codeSystemName = codeSystemChild.getValue();
									}
									else if (codeSystemChild.getName().equals(VUID))
									{
										codeSystemVuid = new Long(codeSystemChild.getValue()).longValue();
									}
									else if (codeSystemChild.getName().equals(PROPERTIES))
									{
										propertyFilterList = processProperties(codeSystemChild);
									}
									else if (codeSystemChild.getName().equals(RELATIONSHIPS))
									{
										relationshipFilterList = processRelationships(codeSystemChild);
									}
									else if (codeSystemChild.getName().equals(DESIGNATIONS))
									{
										designationFilterList = processDesignations(codeSystemChild);
									}
								}
								publisherCodeSystem = new CodeSystemConfig(codeSystemName, codeSystemVuid, propertyFilterList, relationshipFilterList, designationFilterList);
								publisherCodeSystems.add(publisherCodeSystem);
							}
							publisherDomain = new DomainConfig(domainName, publisherSubsets, publisherCodeSystems);
						}
						// don't add the domain if there are no subsets or code systems
                        if (publisherSubsets.size() > 0 || publisherCodeSystems.size() > 0)
                        {
                            publisherDomains.add(publisherDomain);
                        }
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new STSException(e);
		}

		return publisherDomains;
	}

	private Element getTerminologyConfigRootElement() throws Exception
	{
		SAXBuilder builder = new SAXBuilder();
		validateXMLAgainstSchema();
		InputStream xmlIS = getClass().getClassLoader().getResourceAsStream(testingConfigFileName);
		if (xmlIS == null)
		{
			xmlIS = getClass().getClassLoader().getResourceAsStream(configFileName);
			if (xmlIS == null)
			{
				throw new FileNotFoundException("Unable to locate file: " + configFileName);
			}
		}
		else
		{
			System.out.println("***  WARNING: Using " + testingConfigFileName + " file!  ***");
		}
		Document document = builder.build(xmlIS);
		
		return document.getRootElement();
	}
	
	private void validateXMLAgainstSchema() throws Exception
	{
		if (!validated)
		{
			boolean isUsingTestingFile = false;
	        XSDLocator locator = new XSDLocator();
			InputStream xmlIS = getClass().getClassLoader().getResourceAsStream(testingConfigFileName);
			if (xmlIS == null)
			{
				xmlIS = getClass().getClassLoader().getResourceAsStream(configFileName);
				if (xmlIS == null)
				{
					throw new FileNotFoundException("Unable to locate file: " + configFileName + " to validate!");
				}
			}
			else
			{
				isUsingTestingFile = true;
				System.out.println("***  WARNING: Validating " + testingConfigFileName + " file!  ***");
			}
			InputStream schemaIS = locator.getClass().getResourceAsStream(schemaFileName);
			if (schemaIS == null)
			{
				throw new FileNotFoundException("The resource schema " + schemaFileName + " could not be found.");
			}
			try
			{
				VerifierFactory factory = new TheFactoryImpl();
				Schema schema = factory.compileSchema(schemaIS);
				Verifier verifier = schema.newVerifier();
				verifier.verify(new InputSource(xmlIS));
				validated = true;
			}
			catch (SAXException e)
			{
				if (xmlIS != null)
				{
					xmlIS.close();
				}
				String configFile = (isUsingTestingFile) ? testingConfigFileName : configFileName;
				throw new STSException("The " + configFile + " did not validate against the Schema file: "
						+ schemaFileName + ". The error is: " + e.getMessage(),	e);
			}
		}
	}

	@SuppressWarnings("unchecked")
    private List processDesignations(Element subsetChild)
	{
		List<DesignationConfig> designationsList = new ArrayList<DesignationConfig>();
		DesignationConfig designationFilter = null;

		List<Element> designationFilters = subsetChild.getChildren();
		// loop through all designations
		for (int i = 0; i < designationFilters.size(); i++)
		{
			Element designation = designationFilters.get(i);
			List<Element> designationChildList = designation.getChildren();
			String designationName = null;
			boolean allowEmpty = false;
			boolean isList = false;
			for (int j = 0; j < designationChildList.size(); j++)
			{
				Element designationChild = designationChildList
						.get(j);
				if (designationChild.getName().equals(NAME))
				{
					designationName = designationChild.getValue();
				}
				if (designationChild.getName().equals(ALLOW_EMPTY))
				{
					allowEmpty = new Boolean(designationChild.getValue())
							.booleanValue();
				}
				if (designationChild.getName().equals(IS_LIST))
				{
					isList = new Boolean(designationChild.getValue())
							.booleanValue();
				}

				designationFilter = new DesignationConfig(designationName,
						allowEmpty, isList);
			}
			designationsList.add(designationFilter);
		}

		return designationsList;
	}

	@SuppressWarnings("unchecked")
    private List processRelationships(Element subsetChild)
	{
		List<RelationshipConfig> relationshipsList = new ArrayList<RelationshipConfig>();
		String relationshipName = null;
		RelationshipConfig relationshipFilter = null;

		List<Element> relationshipFilters = subsetChild.getChildren();
		// loop through all Relationships
		for (int i = 0; i < relationshipFilters.size(); i++)
		{
			Element property = relationshipFilters.get(i);
			List<Element> relationshipChildList = property.getChildren();
			boolean inverse = false;
			String propertyType = null;
			String propertyValue = null;
			boolean isList = false;
			boolean allowEmpty = false;
			String includeWithChange = null;
			for (int j = 0; j < relationshipChildList.size(); j++)
			{
				Element relationshipChild = relationshipChildList.get(j);
				if (relationshipChild.getName().equals(NAME))
				{
					relationshipName = relationshipChild.getValue();
				}
				if (relationshipChild.getName().equals(ALLOW_EMPTY))
				{
					allowEmpty = new Boolean(relationshipChild.getValue())
							.booleanValue();
				}
				if (relationshipChild.getName().equals(IS_LIST))
				{
					isList = new Boolean(relationshipChild.getValue())
							.booleanValue();
				}
				if (relationshipChild.getName().equals(INVERSE))
				{
					inverse = new Boolean(relationshipChild.getValue())
							.booleanValue();
				}
				if (relationshipChild.getName().equals(PROPERTY_TYPE))
				{
					propertyType = relationshipChild.getValue();
				}
				if (relationshipChild.getName().equals(PROPERTY_VALUE))
				{
					propertyValue = relationshipChild.getValue();
				}
				if (relationshipChild.getName().equals(INCLUDE_WITH_CHANGE))
				{
					includeWithChange = relationshipChild.getValue();
				}
				relationshipFilter = new RelationshipConfig(relationshipName,
						allowEmpty, inverse, propertyType, propertyValue,
						isList, includeWithChange);
			}
			relationshipsList.add(relationshipFilter);
		}
		return relationshipsList;

	}

	@SuppressWarnings("unchecked")
    private List processProperties(Element subsetChild)
	{
		List<PropertyConfig> propertyFilters = new ArrayList<PropertyConfig>();
		PropertyConfig propertyFilter = null;

		List<Element> propertyElements = subsetChild.getChildren();
		// loop through all properties
		for (int i = 0; i < propertyElements.size(); i++)
		{
			Element property = propertyElements.get(i);
			List<Element> propertyChildList = property.getChildren();
			String propertyName = null;
			boolean allowEmpty = false;
			boolean isList = false;
			for (int j = 0; j < propertyChildList.size(); j++)
			{
				Element propertyChild = propertyChildList.get(j);
				if (propertyChild.getName().equals(NAME))
				{
					propertyName = propertyChild.getValue();
				}
				if (propertyChild.getName().equals(ALLOW_EMPTY))
				{
					allowEmpty = new Boolean(propertyChild.getValue())
							.booleanValue();
				}
				if (propertyChild.getName().equals(IS_LIST))
				{
					isList = new Boolean(propertyChild.getValue())
							.booleanValue();
				}

				propertyFilter = new PropertyConfig(propertyName, allowEmpty,
						isList);
			}
			propertyFilters.add(propertyFilter);
		}
		return propertyFilters;
	}
    
    @SuppressWarnings("unchecked")
    private List<DependentSubsetRule> processDependencies(Element subsetChild)
    {
        List<DependentSubsetRule> dependenciesList = new ArrayList<DependentSubsetRule>();
        DependentSubsetRule dependentSubsetRule = null;

        List<Element> dependentSubsets = subsetChild.getChildren();
        // loop through all dependent subsets
        for (int i = 0; i < dependentSubsets.size(); i++)
        {
            Element dependencySubset = dependentSubsets.get(i);
            List<Element> dependencySubsetChildList = dependencySubset.getChildren();
            String subsetName = null;
            String relationshipName = null;
            for (int j = 0; j < dependencySubsetChildList.size(); j++)
            {
                Element dependencySubsetChild = dependencySubsetChildList.get(j);
                if (dependencySubsetChild.getName().equals(SUBSET_NAME))
                {
                    subsetName = dependencySubsetChild.getValue();
                }
                if (dependencySubsetChild.getName().equals(RELATIONSHIP_NAME))
                {
                    relationshipName = dependencySubsetChild.getValue();
                }
            }
            dependentSubsetRule = new DependentSubsetRule(subsetName, relationshipName);
            dependenciesList.add(dependentSubsetRule);
        }
        
        return dependenciesList;
    }

	public void setConfigFileName(String fileName)
	{
		this.configFileName = fileName;
	}
}
