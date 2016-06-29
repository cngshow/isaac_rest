package gov.vha.vets.term.services.importer;

import gov.vha.vets.term.services.business.CodeSystemDelegate;
import gov.vha.vets.term.services.business.CodedConceptDelegate;
import gov.vha.vets.term.services.business.ConceptDelegate;
import gov.vha.vets.term.services.business.ConceptRelationshipDelegate;
import gov.vha.vets.term.services.business.ConceptStateDelegate;
import gov.vha.vets.term.services.business.DesignationDelegate;
import gov.vha.vets.term.services.business.DesignationRelationshipDelegate;
import gov.vha.vets.term.services.business.MapEntryDelegate;
import gov.vha.vets.term.services.business.MapSetDelegate;
import gov.vha.vets.term.services.business.MapSetRelationshipDelegate;
import gov.vha.vets.term.services.business.PropertyDelegate;
import gov.vha.vets.term.services.business.StateDelegate;
import gov.vha.vets.term.services.business.SubsetDelegate;
import gov.vha.vets.term.services.business.SubsetRelationshipDelegate;
import gov.vha.vets.term.services.business.VersionDelegate;
import gov.vha.vets.term.services.business.VuidDelegate;
import gov.vha.vets.term.services.config.XSDLocator;
import gov.vha.vets.term.services.dao.CodeSystemDao;
import gov.vha.vets.term.services.dto.ConceptDesignationDTO;
import gov.vha.vets.term.services.dto.MapSetDesignationDTO;
import gov.vha.vets.term.services.dto.importer.ConceptImportDTO;
import gov.vha.vets.term.services.dto.importer.DesignationImportDTO;
import gov.vha.vets.term.services.dto.importer.EntityImportDTO;
import gov.vha.vets.term.services.dto.importer.FileImportDTO;
import gov.vha.vets.term.services.dto.importer.MapEntryImportDTO;
import gov.vha.vets.term.services.dto.importer.MapSetImportDTO;
import gov.vha.vets.term.services.dto.importer.PropertyImportDTO;
import gov.vha.vets.term.services.dto.importer.RelationshipImportDTO;
import gov.vha.vets.term.services.dto.importer.SubsetImportDTO;
import gov.vha.vets.term.services.dto.importer.SubsetMembershipImportDTO;
import gov.vha.vets.term.services.dto.importer.TypeImportDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSet;
import gov.vha.vets.term.services.model.MapSetRelationship;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.PropertyType;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.model.Vuid;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class TerminologyDataImporter extends DefaultHandler
{
	private static Logger log = Logger.getLogger(TerminologyDataImporter.class.getPackage().getName());

    protected static final int MAX_BATCH_SIZE = 1000;
    protected static final int FLUSH_SIZE = 30;
    protected static final int CONCEPTS_PROCESSED_TO_LOG = 5000;

    protected static final String ROOT_ELEMENT = "Terminology";
    protected static final String CODESYSTEM_ELEMENT = "CodeSystem";
    protected static final String VERSION_ELEMENT = "Version";
    protected static final String CODEDCONCEPT_ELEMENT = "CodedConcept";
    protected static final String CODEDCONCEPTS_ELEMENT = "CodedConcepts";
    protected static final String MAPSET_ELEMENT = "MapSet";
    protected static final String MAPSETS_ELEMENT = "MapSets";
    protected static final String MAPENTRY_ELEMENT = "MapEntry";
    protected static final String MAPENTRIES_ELEMENT = "MapEntries";
    
    protected static final String DESIGNATION_ELEMENT = "Designation";
    protected static final String PROPERTY_ELEMENT = "Property";
    protected static final String RELATIONSHIP_ELEMENT = "Relationship";
    protected static final String TYPE_ELEMENT = "Type";
    protected static final String TYPES_ELEMENT = "Types";
    protected static final String SUBSET_MEMBERSHIP_ELEMENT = "SubsetMembership";
    protected static final String SUBSET_MEMBERSHIPS_ELEMENT = "SubsetMemberships";
    protected static final String MOVE_FROM_CONCEPT_CODE_ELEMENT = "MoveFromConceptCode";

    protected static final String DESIGNATIONS_ELEMENT = "Designations";
    protected static final String PROPERTIES_ELEMENT = "Properties";
    protected static final String RELATIONSHIPS_ELEMENT = "Relationships";
    
    protected static final String SUBSET_ELEMENT = "Subset";
    protected static final String SUBSETS_ELEMENT = "Subsets";

    protected static final String CODE_ELEMENT = "Code";
    protected static final String NAME_ELEMENT = "Name";
    protected static final String VUID_ELEMENT = "VUID";
    
    protected static final String DESCRIPTION_ELEMENT = "Description";
    protected static final String COPYRIGHT_ELEMENT = "Copyright";
    protected static final String COPYRIGHT_URL_ELEMENT = "CopyrightURL";
    
    protected static final String EFFECTIVE_DATE_ELEMENT = "EffectiveDate";
    protected static final String RELEASE_DATE_ELEMENT = "ReleaseDate";
    protected static final String SOURCE_ELEMENT = "Source";
    protected static final String APPEND_ELEMENT = "Append";    
    protected static final String AUTO_ASSIGN_VUIDS = "AutoAssignVuids";    
    protected static final String ACTIVE_ELEMENT = "Active";
    protected static final String ALLOW_DUPLICATES = "AllowDuplicates";

    protected static final String TYPE_NAME_ELEMENT = "TypeName";
    protected static final String VALUE_NEW_ELEMENT = "ValueNew";
    protected static final String VALUE_OLD_ELEMENT = "ValueOld";

    protected static final String SOURCE_CODE_ELEMENT = "SourceCode";
    protected static final String TARGET_CODE_ELEMENT = "TargetCode";
    protected static final String SEQUENCE_ELEMENT = "Sequence";
    protected static final String GROUPING_ELEMENT = "Grouping";
    protected static final String MUID_ELEMENT = "MUID";

    protected static final String NEW_TARGETCODE_ELEMENT = "NewTargetCode";
    protected static final String OLD_TARGETCODE_ELEMENT = "OldTargetCode";

    protected static final String SOURCE_CODE_SYSTEM = "SourceCodeSystem";
    protected static final String SOURCE_VERSION_NAME = "SourceVersionName";
    protected static final String TARGET_CODE_SYSTEM = "TargetCodeSystem";
    protected static final String TARGET_VERSION_NAME = "TargetVersionName";
    
    protected static final String SUBSET_NAME_ELEMENT = "SubsetName";
    
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    
    protected static final String ACTION_ELEMENT = "Action";
    protected static final String ACTION_ADD = "add";
    protected static final String ACTION_UPDATE = "update";
    protected static final String ACTION_NONE = "none";
    protected static final String ACTION_REMOVE = "remove";

    protected static final String PREFERRED_DESIGNATION_TYPE_ELEMENT = "PreferredDesignationType";

    private static final Object KIND_ELEMENT = "Kind";

    private static final String KIND_PROPERTY_TYPE = "PropertyType";
    private static final String KIND_DESIGNATION_TYPE = "DesignationType";
    private static final String KIND_RELATIONSHIP_TYPE = "RelationshipType";


    protected Stack<String> elementStack = new Stack<String>();
    
    protected String currentElement;
    protected String parentElement;
    protected String elementData;
    protected StringBuffer elementDataBuffer;
    
    protected CodeSystem currentCodeSystem;
    protected Version currentVersion;
    protected String currentConceptCode;
    protected List<ConceptImportDTO> importConcepts = new ArrayList<ConceptImportDTO>();
    protected List<SubsetImportDTO> importSubsets = new ArrayList<SubsetImportDTO>();
    protected List<TypeImportDTO> importTypes = new ArrayList<TypeImportDTO>();
    protected List<RelationshipImportDTO> importRelationships = new ArrayList<RelationshipImportDTO>();
    protected List<SubsetMembershipImportDTO> importSubsetMemberships = null;
    protected List<DesignationImportDTO> importDesignations = null;
    protected List<PropertyImportDTO> importProperties = null;
    protected boolean codeSystemProcessed = false;
    protected boolean versionProcessed = false;
    protected boolean codedConceptProcessed = false;
    protected boolean conceptDesignationProcessed = true;
    protected boolean designationNameProcessed = false;
    protected boolean subsetMembershipProcessed = false;
    protected boolean conceptPropertyProcessed = false;
    protected boolean conceptRelationshipProcessed = false;
    protected ConceptImportDTO importConcept = null;
    protected DesignationImportDTO importDesignation = null;
    protected VuidDelegate vuidDelegate = new VuidDelegate();
    protected MapSet mapSet = null;
    protected Set<Long> mapSetEntityIds = new HashSet<Long>();
    protected Map<RelationshipImportDTO, ConceptImportDTO> conceptToRelationshipMap = new HashMap<RelationshipImportDTO, ConceptImportDTO>();
    protected boolean autoAssign=false;
    protected boolean allowDuplicateMapEntries=false;
    
    
    protected String conceptType = null; 
    protected State initialState = null;
    
    protected String inputFile;
    protected String schemaName;
    protected int numberOfConceptsProcessed = 0;
    protected List<FileImportDTO> sdoFileImportList;
    protected State state = null;
    protected HashMap<String, String> elementMap = new HashMap<String, String>();
    protected Set<String> processedMapEntries = new HashSet<String>();

    private Set<Long> preferredDesignationMap = null;

    public TerminologyDataImporter(String inputFile, String schemaName)
    {
        this.inputFile = inputFile;
        this.schemaName = schemaName;
    }

    public List<FileImportDTO> process() throws STSException 
    {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        sdoFileImportList = new ArrayList<FileImportDTO>();
        XSDLocator builder = new XSDLocator();

        initialState = StateDelegate.getByType(State.INITIAL);
        
        try
        {
            if (schemaName != null)
        	{
                URL url = builder.getClass().getResource(schemaName);
    			if (url == null)
    			{
    				throw new FileNotFoundException("Unable to locate file: " + schemaName);
    			}

    			XMLReader reader = XMLReaderFactory.createXMLReader();
    			Source source = new SAXSource(reader, new InputSource(new FileInputStream(inputFile)));
    			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);      
    			Schema schema = factory.newSchema(url);      
    			Validator validator = schema.newValidator();      
    			validator.validate(source);
/*            	log.debug("Loading propertyNameMapForGEM Map: ");
                for (Property property : PropertyDelegate.getPropertiesByTypeName("GEM_Flags")){
                    Concept concept = ConceptDelegate.get(property.getConceptEntityId());
                    String conceptSourceCode = concept.getName();
                	String propertyValue = (property.getValue() != null) ? property.getValue() : "";
                	propertyNameMapForGEM.put(conceptSourceCode+":"+property.getPropertyType().getName()+":"+propertyValue, property);
                }
            	log.debug("Loaded propertyNameMapForGEM Map Count: "+propertyNameMapForGEM.size());
*/    			SAXParser parser = parserFactory.newSAXParser();
                parser.parse(inputFile, this);
        	}
        }
		catch (SAXParseException e)
		{
			throw new STSException("The import file did not validate against the Schema file: "
					+ schemaName + " at line " +  e.getLineNumber() + ", column " + e.getColumnNumber() + ". The error is: " + e.getMessage(), e);
		}
		catch (NullPointerException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new STSException(e.getMessage(), e);
		}

		return sdoFileImportList;
    }

    public void startElement(String namespaceUri, String localName,
            String qualifiedName, Attributes attributes) throws SAXException
    {
        parentElement = currentElement;
        currentElement = qualifiedName;
        elementStack.push(qualifiedName);
        elementDataBuffer = new StringBuffer();

        
        try
        {
            // we are starting CodedConcepts so let's finish up with codeSystem
            if (!processElements(qualifiedName, true))
            {
                if (currentElement.equals(VERSION_ELEMENT))
                {
                	processCodeSystem();
                }
                else if (currentElement.equals(CODEDCONCEPTS_ELEMENT))
                {
                	conceptType = CODEDCONCEPT_ELEMENT;
                    processVersion();
                }
                else if (currentElement.equals(DESIGNATIONS_ELEMENT) || 
                        currentElement.equals(RELATIONSHIPS_ELEMENT) )
                {
                    // we need to process the concept information
                    checkConceptProccessed();
                }
                else if (currentElement.equalsIgnoreCase(PROPERTIES_ELEMENT))
                {
                    checkConceptProccessed();
                    checkDesignationProcessed();
                }
                else if (currentElement.equals(MAPENTRIES_ELEMENT))
                {
                    conceptElement(currentElement, false);
                    // we are at the end of map sets
                    if (importConcepts.size() > 0)
                    {
                        processConceptData(importConcepts);
                        importConcepts.clear();
                    }
                	conceptType = MAPENTRY_ELEMENT;
                }
                else if (currentElement.equals(SUBSET_MEMBERSHIPS_ELEMENT))
                {
                    checkDesignationProcessed();
                }
                else if (currentElement.equals(MAPSETS_ELEMENT))
                {
                	conceptType = MAPSET_ELEMENT;
                	if (currentVersion == null)
                	{
                	    processVersion();
                	}
                	// we only use the stat for MAP Sets so get it only under that condition
                }
            }
        }
        catch(NullPointerException e)
        {
        	throw e;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }

    public void endElement(String namespaceUri, String localName,
            String qualifiedName) throws SAXException
    {
        elementData = elementDataBuffer.toString();
        
        currentElement = (String) elementStack.pop();
        if (elementStack.size() > 1)
        {
            parentElement = (String) elementStack.peek();
        }

        try
        {
            if (!processElements(qualifiedName, false))
            {
                if (currentElement.equals(CODESYSTEM_ELEMENT))
                {
                	if (currentCodeSystem == null)
                	{
                        processCodeSystem();
                	}
                }
                if (currentElement.equals(VERSION_ELEMENT))
                {
                    // we need to load the version because the current version object is not associated with the session
                    currentVersion = VersionDelegate.getByVersionId(currentVersion.getId());
                	currentVersion.setConceptCount(numberOfConceptsProcessed);
                	VersionDelegate.update(currentVersion);
                    FileImportDTO sdoFileImportDTO = new FileImportDTO(currentCodeSystem, currentVersion.getName());
                    sdoFileImportDTO.setConceptCount(numberOfConceptsProcessed);
                    sdoFileImportList.add(sdoFileImportDTO);
                }
                if (qualifiedName.equals(TYPE_ELEMENT))
                {
                    // the the type and add it to the list
                    TypeImportDTO typeDTO = getTypeData();
                    importTypes.add(typeDTO);
                }
                else if (qualifiedName.equals(RELATIONSHIP_ELEMENT))
                {
                    // add the relationship
                    importRelationships.add(getRelationshipData());
                }
                else if (qualifiedName.equals(PROPERTY_ELEMENT))
                {
                    // add the property
                    importProperties.add(getPropertyData());
                }
                else if (qualifiedName.equals(PROPERTIES_ELEMENT) && parentElement.equals(DESIGNATION_ELEMENT))
                {
                    importDesignation.addProperties(importProperties);
                    importProperties.clear();
                }
                else if (qualifiedName.equals(TYPES_ELEMENT))
                {
                    // process all the types
                    processTypes();
                }
                else if (qualifiedName.equals(SUBSET_ELEMENT))
                {
                    SubsetImportDTO subsetDTO = getSubsetData();
                    importSubsets.add(subsetDTO);
                }
                else if (qualifiedName.equals(SUBSETS_ELEMENT))
                {
                    // process all the Subsets
                    processSubsets();
                }
                else if (qualifiedName.equals(CODEDCONCEPTS_ELEMENT) || qualifiedName.equals(MAPSETS_ELEMENT) ||
                		qualifiedName.equals(MAPENTRIES_ELEMENT))
                {
                    // we are at the end of the concepts
                    if (importConcepts.size() > 0)
                    {
                        processConceptData(importConcepts);
                        importConcepts.clear();
                    }
                    if (importRelationships.size() > 0)
                    {
                        processRelationshipData(importRelationships);
                    }
                    if (qualifiedName.equals(MAPENTRIES_ELEMENT))
                    {
                    	// finished processing map entries - go back to processing map set elements
                        conceptType = MAPSET_ELEMENT;
                    }
                }
                else if (qualifiedName.equals(SUBSET_MEMBERSHIP_ELEMENT))
                {
                    importSubsetMemberships.add(getSubsetMembershipData());
                }
                else if (qualifiedName.equals(ROOT_ELEMENT))
                {
                	processConceptStates();
                }
                else
                {
                    elementMap.put(currentElement, elementData);
                }
            }
        }
        catch (NullPointerException ex)
        {
        	throw ex;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new SAXException(e);
        }
    }



    private void processConceptStates() throws STSException
	{
    	for (Long mapSetEntityId : mapSetEntityIds)
		{
        	MapEntryDelegate.verifySequence(mapSetEntityId);
		}
    	// process only if we have something in Authoring - it would not be added to this set unless it was
        if (currentVersion != null && currentVersion.getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            HibernateSessionFactory.currentSession().flush();
            HibernateSessionFactory.currentSession().clear();
            int count = ConceptStateDelegate.removeInconsistentConceptStates();
        	log.debug("Number of inconsistent concept states removed: "+count);
        }
	}

	public void characters(char[] chars, int startIndex, int endIndex)
    {
        String s = new String(chars, startIndex, endIndex);
        
        if (elementDataBuffer != null)
        {
            elementDataBuffer.append(s);
        }            
    }

    protected boolean processElements(String elementName, boolean isStartElement) throws Exception
    {
        boolean processed = true;
        if (elementName.equals(DESIGNATION_ELEMENT))
        {
            conceptDesignationElement(isStartElement);
        }
        else if (elementName.equals(CODEDCONCEPT_ELEMENT) || elementName.equals(MAPSET_ELEMENT) || elementName.equals(MAPENTRY_ELEMENT))
        {
            conceptElement(elementName, isStartElement);
        }
        else
        {
            processed = false;
        }
        return processed;
    }

    
    protected void conceptElement(String elementName, boolean isStartElement) throws Exception
    {
        if (isStartElement)
        {
            importDesignations = new ArrayList<DesignationImportDTO>();
            importProperties = new ArrayList<PropertyImportDTO>();
            codedConceptProcessed = false;
        }
        else
        {
            checkConceptProccessed();
            importConcept.setDesignations(importDesignations);
            importConcept.setProperties(importProperties);
            if (importConcepts.size() >= MAX_BATCH_SIZE)
            {
                processConceptData(importConcepts);
                importConcepts.clear();
            }
            if (numberOfConceptsProcessed % CONCEPTS_PROCESSED_TO_LOG == 0)
            {
            	log.debug("Imported SDO '"+currentCodeSystem.getName()+"' entry: "+numberOfConceptsProcessed);
            }
        }
    }
    
    protected void conceptDesignationElement(boolean isStartElement) throws Exception
    {
        if (isStartElement)
        {
            importSubsetMemberships = new ArrayList<SubsetMembershipImportDTO>();
            conceptDesignationProcessed = false;
        }
        else
        {
            checkDesignationProcessed();
            importDesignation.setSubsets(importSubsetMemberships);
        }
    }

    private void checkDesignationProcessed() throws STSException
    {
        if (conceptDesignationProcessed == false)
        {
            conceptDesignationProcessed = true;
            importDesignation = getDesignationData();
            importDesignations.add(importDesignation);
        }
    }
    /**
     * @throws Exception 
     */
    private void checkConceptProccessed() throws Exception
    {
        // process codedConcept (Designation start element)
        if (codedConceptProcessed == false)
        {
            codedConceptProcessed = true;
            importConcept = getConceptData();
            importConcepts.add(importConcept);
            numberOfConceptsProcessed++;
        }
    }

    /**
     * Process all the types that have been specified in the import file
     * @throws STSNotFoundException
     */
    protected void processTypes() throws STSNotFoundException
    {
        for (TypeImportDTO typeDTO : importTypes)
        {
            if (typeDTO.getKind().equals(KIND_PROPERTY_TYPE))
            {
                PropertyType type = PropertyDelegate.getType(typeDTO.getName());
                if (type == null)
                {
                    PropertyDelegate.createType(typeDTO.getName());
                }
            }
            else if (typeDTO.getKind().equals(KIND_DESIGNATION_TYPE))
            {
                DesignationType type = DesignationDelegate.getType(typeDTO.getName());
                if (type == null)
                {
                    DesignationDelegate.createType(typeDTO.getName());
                }
            }
            else if (typeDTO.getKind().equals(KIND_RELATIONSHIP_TYPE))
            {
                RelationshipType type = ConceptRelationshipDelegate.getType(typeDTO.getName());
                if (type == null)
                {
                    ConceptRelationshipDelegate.createType(typeDTO.getName());
                }
            }
        }
    }
    
    protected void processSubsets() throws Exception
    {
        // auto assign vuid and codes
        processVuids(importSubsets);
        for (SubsetImportDTO subsetDTO : importSubsets)
        {
        	Subset subset = SubsetDelegate.getByName(subsetDTO.getSubsetName());
            if (ACTION_ADD.equals(subsetDTO.getAction()))
            {
            	if (subset == null)
                {
                    SubsetDelegate.create(subsetDTO.getCode(), subsetDTO.getSubsetName(), subsetDTO.getVuid(), true);
                }
            }
            else if (ACTION_UPDATE.equals(subsetDTO.getAction()))
            {
                SubsetDelegate.update(subsetDTO.getVuid(), subsetDTO.isActive());
            }
        }
    }
    protected void processCodeSystem() throws Exception
    {
    	String codeSystemName = elementMap.get(NAME_ELEMENT);
        String vuidString = elementMap.get(VUID_ELEMENT);
    	String action = elementMap.get(ACTION_ELEMENT);
    	String description = elementMap.get(DESCRIPTION_ELEMENT);
    	String copyright = elementMap.get(COPYRIGHT_ELEMENT);
    	String copyrightURL = elementMap.get(COPYRIGHT_URL_ELEMENT);
    	String preferredDesignationType = elementMap.get(PREFERRED_DESIGNATION_TYPE_ELEMENT);
    	elementMap.clear();
    	
        Long vuid = null;
        if (vuidString != null)
        {
            vuid = Long.valueOf(vuidString);
        }

        CodeSystem codeSystem = CodeSystemDelegate.get(codeSystemName);
        log.info("Importing code system: "+codeSystemName);
        if (ACTION_ADD.equals(action))
        {
			if (codeSystem != null)
			{
	            throw new STSException("CodeSystem: "+codeSystem.getName()+" cannot be added because it already exists!");
			}
            DesignationType designationType = null;
            if (preferredDesignationType != null)
            {
                designationType = DesignationDelegate.getType(preferredDesignationType);
                if (description == null)
                {
                    throw new STSException("The description must be specified for CodeSystem: "+codeSystemName);
                }
                if (designationType == null)
                {
                    throw new STSException("The Preferred Designation Type: "+preferredDesignationType+" is not found for CodeSystem: "+codeSystemName);
                }
            }
            else
            {
                throw new STSException("A Preferred Designation Type is required for CodeSystem: "+codeSystemName);
            }
            codeSystem = CodeSystemDelegate.create(codeSystemName, vuid, description, copyright, copyrightURL, designationType);
        }
        else if (ACTION_NONE.equals(action))
        {
            if (codeSystem == null)
            {
                throw new STSException("CodeSystem: " + codeSystemName + " is not found!");
            }
        }
		else if (ACTION_UPDATE.equals(action))
		{
            if (codeSystem == null)
            {
                throw new STSException("CodeSystem: " + codeSystemName + " is not found!");
            }
            
            if (vuid != null && vuid != codeSystem.getVuid())
            {
            	codeSystem.setVuid(vuid);
            }
			
			if (preferredDesignationType != null && preferredDesignationType.equals(codeSystem.getPreferredDesignationType().getName()) != true)
			{
                DesignationType designationType = DesignationDelegate.getType(preferredDesignationType);
                if (designationType == null)
                {
                    throw new STSException("Preferred designation type: "+preferredDesignationType+" is not found.");
                }
			    codeSystem.setPreferredDesignationType(designationType);
			}
			
			if (copyright != null && copyright.equals(codeSystem.getCopyright()) == false)
			{
				codeSystem.setCopyright(copyright);
			}
			if (copyrightURL != null && copyrightURL.equals(codeSystem.getCopyrightURL()) == false)
			{
				codeSystem.setCopyrightURL(copyrightURL);
			}
			if (description != null && description.equals(codeSystem.getDescription()) == false)
			{
				codeSystem.setDescription(description);
			}
		}
        
        HibernateSessionFactory.currentSession().flush();
        HibernateSessionFactory.currentSession().clear();

        currentCodeSystem = codeSystem;
    }
    
    protected void processVersion() throws Exception
    {
    	
        String name = elementMap.get(NAME_ELEMENT);
        String effectiveDateString = elementMap.get(EFFECTIVE_DATE_ELEMENT);
        String releaseDateString = elementMap.get(RELEASE_DATE_ELEMENT);
        String description = elementMap.get(DESCRIPTION_ELEMENT);
        String source = elementMap.get(SOURCE_ELEMENT);
        String append = elementMap.get(APPEND_ELEMENT);
        String autoAssignString = elementMap.get(AUTO_ASSIGN_VUIDS);
        elementMap.clear();

        if (autoAssignString != null && parseBoolean(autoAssignString) == true)
        {
        	autoAssign = true;
        }
        
        Version version = VersionDelegate.get(currentCodeSystem.getName(), name);
        if (append != null && parseBoolean(append) == true)
        {
            if (version == null)
            {
                throw new STSException("Version append flag was specified but the version: "+name+" does not exist");
            }
            List<Version> versions = VersionDelegate.getVersions(currentCodeSystem);
            if (versions == null || versions.size() == 0)
            {
                throw new STSException("Version append flag was specified but no versions currently exist");
            }
            Version checkVersion = versions.get(0);
            if (checkVersion != null && checkVersion.getId() == version.getId())
            {
                currentVersion = version; 
            }
            else
            {
                throw new STSException("Version: "+name+" does not match the current version of "+currentCodeSystem.getName()+" which is: "+checkVersion.getName());
            }
        }
        else
        {
            if (version != null)
            {
                throw new STSException("Version "+name+" for Code System "+currentCodeSystem.getName()+" already exists!");
            }
            else
            {
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                Date effectiveDate;
                try
                {
                    effectiveDate = sdf.parse(effectiveDateString);
                } 
                catch (Exception ex)
                {
                    throw new STSException("Effective Date was not valid. reason: "+ex.getMessage());
                }
                Date releaseDate;
                try
                {
                    releaseDate = sdf.parse(releaseDateString);
                } 
                catch (Exception ex)
                {
                    throw new STSException("Release Date was not valid. reason: "+ex.getMessage());
                }
                currentVersion = VersionDelegate.createSDO(name, effectiveDate, releaseDate, new Date(), description, currentCodeSystem, source);
            }
        }
    }
    
    /**
     * Process given list of concepts and delegate designations to called method
     * @param codeSystem
     * @param version
     * @param conceptDTOs
     * @throws Exception 
     */
    private void processConceptData(List<ConceptImportDTO> conceptDTOs) throws Exception
    {
        Map<Object, Concept> conceptMap = buildConceptMap(conceptDTOs);

        Concept concept = null;

        // Make a map that will link the designation with it's concept model object
        Map<DesignationImportDTO, Long> designationMap = new HashMap<DesignationImportDTO, Long>();
        Set<String> designationTypes = new HashSet<String>();
        // Make a map that will link the Property with it's concept model object
        Map<PropertyImportDTO, Long> propertyMap = new HashMap<PropertyImportDTO, Long>();
        Set<String> propertyTypes = new HashSet<String>();
        // auto assign vuid and codes
        if (currentCodeSystem.getName().equals(HibernateSessionFactory.VHAT_NAME) || autoAssign)
        {
            processVuids(conceptDTOs);
        }
        
        int flushCount = 0;
        // loop over all the import concepts
        for (ConceptImportDTO importDTO : conceptDTOs)
        {
        	if (importDTO instanceof MapSetImportDTO)
        	{
        		concept = processMapSet((MapSetImportDTO)importDTO, conceptMap);
        		mapSet = (MapSet) concept;
        	}
        	else if (importDTO instanceof MapEntryImportDTO)
        	{
        		concept = processMapEntry(mapSet, (MapEntryImportDTO)importDTO, conceptMap);
        	}
        	else
        	{
        		concept = processCodedConcept(importDTO, conceptMap);
        	}

            // process all designations, properties and relationships
            List<DesignationImportDTO> designationDTOs = importDTO.getDesignations();
            if (designationDTOs != null)
            {
	            for (DesignationImportDTO designationDTO : designationDTOs)
	            {
	                designationTypes.add(designationDTO.getTypeName());
	                designationMap.put(designationDTO, concept.getEntityId());
	            }
            }
            List<PropertyImportDTO> propertyDTOs = importDTO.getProperties();
            if (propertyDTOs != null)
            {
	            for (PropertyImportDTO propertyDTO : propertyDTOs)
	            {
	                propertyTypes.add(propertyDTO.getTypeName());
	                propertyMap.put(propertyDTO, concept.getEntityId());
	            }
            }
            if (++flushCount % FLUSH_SIZE == 0)
            {
                HibernateSessionFactory.currentSession().flush();
                HibernateSessionFactory.currentSession().clear();
            }
        }
        if (!designationMap.isEmpty())
        {
        	processDesignationData(designationMap, designationTypes);
        }
        if (!propertyMap.isEmpty())
        {
        	processPropertyData(propertyMap, propertyTypes);
        }
    }


	/**
     * 
     * @param conceptDTOs
     * @return
	 * @throws STSException 
     */
    private Map<Object, Concept> buildConceptMap(List<ConceptImportDTO> conceptDTOs) throws STSException
    {
    	HashMap<Object, Concept> conceptMap = new HashMap<Object, Concept>();
    	if (conceptDTOs.get(0) instanceof MapSetImportDTO )
    	{
	        List<Long> vuids = new ArrayList<Long>();
	        for (EntityImportDTO importDTO : conceptDTOs)
	        {
	            vuids.add(importDTO.getVuid());
	        }
	        List<MapSetDesignationDTO> mapSets = MapSetDelegate.getByVuids(vuids);
	        for (MapSetDesignationDTO mapSet : mapSets)
	        {
	            conceptMap.put(mapSet.getMapSet().getVuid(), mapSet.getMapSet());
	        }
    	}
    	else if (conceptDTOs.get(0) instanceof MapEntryImportDTO)
    	{
    		Set<String> sourceCodes = new HashSet<String>();
    		Set<String> targetCodes = new HashSet<String>();
	        List<Long> vuids = new ArrayList<Long>();
	        for (EntityImportDTO importDTO : conceptDTOs)
	        {
	        	MapEntryImportDTO mapEntryImportDTO = (MapEntryImportDTO)importDTO; 
	        	if (importDTO.getAction().equals(ACTION_ADD))
	        	{
		        	sourceCodes.add(mapEntryImportDTO.getSourceConceptCode());
		        	targetCodes.add(mapEntryImportDTO.getTargetConceptCode());
	        	}
	        	else if (importDTO.getAction().equals(ACTION_UPDATE) || importDTO.getAction().equals(ACTION_NONE))
	        	{
	        		if (importDTO.getVuid() == null)
	        		{
	        			throw new STSException("Must specify the VUID of the mapEntry! source code: "+mapEntryImportDTO.getSourceConceptCode()+" target code: "+mapEntryImportDTO.getTargetConceptCode());
	        		}
		            vuids.add(importDTO.getVuid());
	        	}
	        }

	        // build a set so we can query if the source or target code is valid
	        Set<String> sourceSet = new HashSet<String>();
	        if (!sourceCodes.isEmpty())
	        {
		        List<Concept> sourceConcepts = ConceptDelegate.get(VersionDelegate.getByVersionId(mapSet.getSourceVersionId()).getCodeSystem(), sourceCodes);
		        for (Concept concept : sourceConcepts)
	            {
		            if (concept instanceof CodedConcept || concept instanceof Designation)
		            {
		                sourceSet.add(concept.getCode());
		            }
	            }
	        }
            Set<String> targetSet = new HashSet<String>();
	        if (!targetCodes.isEmpty())
	        {
	            List<Concept> targetConcepts = ConceptDelegate.get(VersionDelegate.getByVersionId(mapSet.getTargetVersionId()).getCodeSystem(), targetCodes);
	            for (Concept concept : targetConcepts)
	            {
	                if (concept instanceof CodedConcept || concept instanceof Designation)
	                {
	                    targetSet.add(concept.getCode());
	                }
	            }
	        }
            for (EntityImportDTO importDTO : conceptDTOs)
            {
	        	if (importDTO.getAction().equals(ACTION_ADD))
	        	{
	                MapEntryImportDTO mapEntryImportDTO = (MapEntryImportDTO)importDTO;
	                if (!sourceSet.contains(mapEntryImportDTO.getSourceConceptCode()))
	                {
	                    throw new STSException("Cannot find source code: "+mapEntryImportDTO.getSourceConceptCode());
	                }
	                if (!targetSet.contains(mapEntryImportDTO.getTargetConceptCode()))
	                {
	                    throw new STSException("Cannot find target code: "+mapEntryImportDTO.getTargetConceptCode());
	                }
	        	}
            }
	        
	        List<MapEntry> mapEntries = null;
	        if (!sourceCodes.isEmpty() || !targetCodes.isEmpty())
	        {
	        	mapEntries = MapEntryDelegate.get(sourceCodes, targetCodes);
	        	for (MapEntry mapEntry : mapEntries)
				{
					conceptMap.put(mapEntry.getSourceCode()+"-"+mapEntry.getTargetCode(), mapEntry);
				}
	        }
	        if (!vuids.isEmpty())
	        {
	        	mapEntries = MapEntryDelegate.get(vuids);
	        	for (MapEntry mapEntry : mapEntries)
				{
					conceptMap.put(mapEntry.getVuid(), mapEntry);
				}
	        }
    	}
    	else
    	{
	        List<String> codes = new ArrayList<String>();
	        for (ConceptImportDTO importDTO : conceptDTOs)
	        {
	            codes.add(importDTO.getCode());
	        }
	        List<Concept> concepts = ConceptDelegate.get(currentCodeSystem, codes);
	        for (Concept concept : concepts)
	        {
	            conceptMap.put(concept.getCode(), concept);
	        }
    	}
    	return conceptMap;
    }
    
    private void processVuids(List<? extends EntityImportDTO> conceptDTOs) throws Exception
    {
    	int vuidCount = 0;
    	for (EntityImportDTO entityImportDTO : conceptDTOs)
		{
            if (entityImportDTO.getAction().equals(ACTION_ADD)  && entityImportDTO.getVuid() == null)
            {
                vuidCount++;
            }
		}
    	long startingVuid = -1;
    	if (vuidCount > 0)
    	{
	        Vuid vuid = getVuid("Terminology Data Importer", vuidCount);
	        startingVuid = vuid.getStartVuid();
        }
	        
    	for (EntityImportDTO conceptImportDTO : conceptDTOs)
		{
            if (conceptImportDTO.getAction().equals(ACTION_ADD))
            {
                if (conceptImportDTO.getVuid() == null && vuidCount > 0)
                {
                    conceptImportDTO.setVuid(startingVuid++);
                }
                if (conceptImportDTO instanceof EntityImportDTO)
                {
                    EntityImportDTO entityImportDTO = (EntityImportDTO)conceptImportDTO;
                    if (entityImportDTO.getCode() == null && entityImportDTO.getVuid() != null && entityImportDTO.getVuid() > 0)
                    {
                        entityImportDTO.setCode(""+entityImportDTO.getVuid());
                    }
                }
            }
		}
    }

    /**
     * Process the CodedConcept
     * @param conceptImportDTO
     * @param conceptMap
     * @return
     * @throws STSException
     */
    private Concept processCodedConcept(ConceptImportDTO conceptImportDTO, Map<Object, Concept> conceptMap) throws STSException
    {
    	Concept concept = null;
        // check the action
        if (ACTION_ADD.equals(conceptImportDTO.getAction()))
        {
            if (conceptImportDTO.getCode() == null)
            {
                throw new STSException("Concept code cannot be empty");
            }
            if (conceptMap.containsKey(conceptImportDTO.getCode()))
            {
                throw new STSException("Concept code: " + conceptImportDTO.getCode() + " is already loaded");
            }
            concept = CodedConceptDelegate.createSDO(currentVersion, conceptImportDTO.getCode(), conceptImportDTO.getName(), conceptImportDTO.getVuid(), conceptImportDTO.isActive());
            conceptMap.put(conceptImportDTO.getCode(), concept);
        }
        else if (ACTION_UPDATE.equals(conceptImportDTO.getAction()))
        {
            concept = conceptMap.get(conceptImportDTO.getCode());
            
            if (concept != null)
            {
                String conceptName = (conceptImportDTO.getName() == null) ? concept.getName() : conceptImportDTO.getName();
                if (((conceptName == null && concept.getName() == null) || conceptName.equals(concept.getName())) && conceptImportDTO.isActive() == concept.getActive())
                {
                    throw new STSException("Concept code: " + conceptImportDTO.getCode() + " <Action> is 'update' but no changes where found: "+currentCodeSystem.getName());
                }
                CodedConceptDelegate.updateSDO(currentVersion, (CodedConcept)concept, conceptName, conceptImportDTO.getCode(), conceptImportDTO.isActive());
            }
        }
        else if (ACTION_REMOVE.equals(conceptImportDTO.getAction()))
        {
            if (currentVersion.getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
            {
                throw new STSException("Action type Remove is only allowed for VHAT codesystems!");
            }
            concept = conceptMap.get(conceptImportDTO.getCode());
            if (concept == null)
            {
                throw new STSException("Concept code: "+conceptImportDTO.getCode()+" not found!");
            }
            concept = (Concept) CodedConceptDelegate.remove(currentCodeSystem, conceptImportDTO.getCode());
        }
        else if (ACTION_NONE.equals(conceptImportDTO.getAction()))
        {
            concept = conceptMap.get(conceptImportDTO.getCode());
        }

        if (concept == null)
        {
            throw new STSException("Concept code: " + conceptImportDTO.getCode() + " is not found in Code System: "+currentCodeSystem.getName());
        }

        // save off a set of conceptEntityIds that need the concept state set
        if (currentVersion.getId() == HibernateSessionFactory.AUTHORING_VERSION_ID)
        {
            setConceptState(concept, initialState);
        }
        return concept;
    }

    /**
     * Process the MapSet concept
     * @param mapSetImportDTO
     * @param conceptMap
     * @return
     * @throws STSException
     */
    private Concept processMapSet(MapSetImportDTO mapSetImportDTO, Map<Object, Concept> conceptMap) throws STSException
    {
    	Concept concept = null;
        // check the action
        if (ACTION_ADD.equals(mapSetImportDTO.getAction()))
        {
            if (conceptMap.containsKey(mapSetImportDTO.getVuid()))
            {
                throw new STSException("MapSet vuid: " + mapSetImportDTO.getVuid() + " is already loaded");
            }
        	concept = MapSetDelegate.createVHAT(mapSetImportDTO.getName(), mapSetImportDTO.getCode(), mapSetImportDTO.getVuid(), mapSetImportDTO.isActive(), 
        			mapSetImportDTO.getSourceCodeSystemName(), mapSetImportDTO.getSourceVersionName(), 
        			mapSetImportDTO.getTargetCodeSystemName(), mapSetImportDTO.getTargetVersionName(), mapSetImportDTO.getEffectiveDate()); // call to create map set
        	mapSetEntityIds.add(concept.getEntityId());
        	conceptMap.put(mapSetImportDTO.getVuid(), concept);
        }
        else if (ACTION_UPDATE.equals(mapSetImportDTO.getAction()))
        {
            concept = conceptMap.get(mapSetImportDTO.getVuid());
            
            if (concept != null)
            {
                if (concept instanceof MapSet)
                {
                    MapSet mapSet = (MapSet)concept;
                    MapSetDelegate.updateVHAT(mapSet, mapSetImportDTO.getSourceCodeSystemName(), mapSetImportDTO.getSourceVersionName(), 
                        mapSetImportDTO.getTargetCodeSystemName(), mapSetImportDTO.getTargetVersionName(), mapSetImportDTO.getEffectiveDate());
                }
                else
                {
                    throw new STSException("Could not find a MapSet for VUID: "+mapSetImportDTO.getVuid());
                }
            }
        }
        else if (ACTION_NONE.equals(mapSetImportDTO.getAction()))
        {
            concept = conceptMap.get(mapSetImportDTO.getVuid());
        }
        
        // make sure we have a concept
        if (concept == null)
        {
            throw new STSException("MapSet vuid: " + mapSetImportDTO.getVuid() + " is not found in Code System: "+currentCodeSystem.getName());
        }
        else
        {
        	// set the concept state - check to see if we should allow an update
        	setConceptState(concept, initialState);
        }
        return concept;
    }

    /**
     * 
     * @param mapEntryImportDTO
     * @param conceptMap
     * @return
     * @throws STSException 
     */
    private Concept processMapEntry(MapSet mapSet, MapEntryImportDTO mapEntryImportDTO,
			Map<Object, Concept> conceptMap) throws STSException
	{
    	Concept concept = null;
        // check the action
        if (ACTION_ADD.equals(mapEntryImportDTO.getAction()))
        {
        	// (if duplicate and allow duplicates) or it does not exist
            if ((conceptMap.containsKey(getMapEntryKey(mapEntryImportDTO)) && allowDuplicateMapEntries) ||
            		!conceptMap.containsKey(getMapEntryKey(mapEntryImportDTO)))
            {
	        	concept = MapEntryDelegate.createVHAT(currentVersion, mapSet.getEntityId(), mapEntryImportDTO.getCode(), 
	        			mapEntryImportDTO.getVuid(), mapEntryImportDTO.isActive(), 
	        			mapEntryImportDTO.getSourceConceptCode(), mapEntryImportDTO.getTargetConceptCode(), 
	        			mapEntryImportDTO.getSequence(), mapEntryImportDTO.getGrouping(), mapEntryImportDTO.getEffectiveDate()); 
	        	conceptMap.put(getMapEntryKey(mapEntryImportDTO), concept);
            }
            else
            {
            	// just create a relationship to the existing map entry
            	concept = conceptMap.get(getMapEntryKey(mapEntryImportDTO));
            	if (concept instanceof MapEntry)
            	{
            		MapSetRelationship mapSetRelationship = MapSetRelationshipDelegate.get(mapSet.getEntityId(), concept.getEntityId());
            		if ( mapSetRelationship != null) // if you don't allow duplicates and you found one
            		{
                		throw new STSException("MapEntry: SourceCode:"+
                				mapEntryImportDTO.getSourceConceptCode()+" TargetCode:"+mapEntryImportDTO.getTargetConceptCode()+" already exists for this mapset.");
            		}
        			MapSetRelationshipDelegate.create(currentVersion, mapSet.getEntityId(), concept.getEntityId(), mapEntryImportDTO.isActive(), mapEntryImportDTO.getSequence(), mapEntryImportDTO.getGrouping());
            	}
            	else
            	{
            		throw new STSException("Cannot process MapEntry: SourceCode:"+
            				mapEntryImportDTO.getSourceConceptCode()+" TargetCode:"+mapEntryImportDTO.getTargetConceptCode());
            	}
            }
        	mapSetEntityIds.add(mapSet.getEntityId());
        }
        else if (ACTION_UPDATE.equals(mapEntryImportDTO.getAction()))
        {
            concept = conceptMap.get(mapEntryImportDTO.getVuid());
            
            if (concept != null)
            {
            	MapEntryDelegate.updateVHAT(mapSet.getEntityId(), (MapEntry)concept, mapEntryImportDTO.getTargetConceptCode(), 
            			mapEntryImportDTO.isActive(), mapEntryImportDTO.getSequence(), mapEntryImportDTO.getGrouping(), mapEntryImportDTO.getEffectiveDate());
            	mapSetEntityIds.add(mapSet.getEntityId());
            }
        }
        else if (ACTION_NONE.equals(mapEntryImportDTO.getAction()))
        {
            concept = conceptMap.get(mapEntryImportDTO.getVuid());
        }
        if (concept == null)
        {
            throw new STSException("MapSet SourceCode: " + mapEntryImportDTO.getSourceConceptCode()+
            		" TargetCode: "+mapEntryImportDTO.getTargetConceptCode()+ " is not found in Code System: "+currentCodeSystem.getName());
        }
        return concept;
	}
    private String getMapEntryKey(MapEntryImportDTO mapEntryImportDTO)
    {
    	return mapEntryImportDTO.getSourceConceptCode()+"-"+mapEntryImportDTO.getTargetConceptCode(); 
    }
    
    private void processSubsetMembershipData(
            List<SubsetMembershipImportDTO> subsetMemberships) throws STSException
    {
        // build a list of all the unique subsets
        HashSet<Long> subsetVuids = new HashSet<Long>();
        HashSet<Long> designationEntityIds = new HashSet<Long>();
        HashSet<Long> subsetEntityIds = new HashSet<Long>();
        for (SubsetMembershipImportDTO subsetMembership : subsetMemberships)
        {
            subsetVuids.add(subsetMembership.getVuid());
            designationEntityIds.add(subsetMembership.getEntityId());
        }
        Map<Long, Subset> subsetMap = new HashMap<Long, Subset>();
        List<Subset> subsets = SubsetDelegate.getSubsets(subsetVuids);
        for (Subset subset : subsets)
        {
            subsetMap.put(subset.getVuid(), subset);
            subsetEntityIds.add(subset.getEntityId());
        }
        
        Map<String, SubsetRelationship> subsetRelationshipMap = new HashMap<String, SubsetRelationship>();
        List<SubsetRelationship> subsetRelationships = new ArrayList<SubsetRelationship>();
        if (!subsetEntityIds.isEmpty() && !designationEntityIds.isEmpty())
        {
            subsetRelationships = SubsetRelationshipDelegate.get(subsetEntityIds, designationEntityIds);
        }
        for (SubsetRelationship subsetRelationship : subsetRelationships)
        {
            subsetRelationshipMap.put(subsetRelationship.getSourceEntityId()+":"+subsetRelationship.getTargetEntityId(), subsetRelationship);
        }
        
        for (SubsetMembershipImportDTO subsetMembership : subsetMemberships)
        {
            Subset subset = subsetMap.get(subsetMembership.getVuid());
            if (subset == null)
            {
                throw new STSException("Subset with VUID: "+subsetMembership.getVuid()+" is not found!");
            }
            SubsetRelationship relationship = subsetRelationshipMap.get(subset.getEntityId()+":"+subsetMembership.getEntityId());
            if (subsetMembership.getAction().equals(ACTION_ADD))
            {
                if (relationship != null)
                {
                    throw new STSException("Subset Relationship already exists for Designation entity: "+subsetMembership.getEntityId());
                }
                SubsetRelationshipDelegate.createSDO(currentVersion, subset.getEntityId(), subsetMembership.getEntityId(), subsetMembership.isActive());
            }
            else if (subsetMembership.getAction().equals(ACTION_UPDATE))
            {
                if (relationship == null)
                {
                    throw new STSException("Subset Relationship does not exist for Designation entity: "+subsetMembership.getEntityId());
                }
                SubsetRelationshipDelegate.updateSDO(currentVersion, relationship.getEntityId(), relationship.getSourceEntityId(), relationship.getTargetEntityId(), subsetMembership.isActive());
            }
            else if (subsetMembership.getAction().equals(ACTION_REMOVE))
            {
                if (currentVersion.getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
                {
                    throw new STSException("Action type Remove is only allowed for VHAT codesystems!");
                }
                SubsetRelationshipDelegate.remove(relationship);
            }
        }
        subsetMemberships.clear();
    }

    private void processRelationshipData(List<RelationshipImportDTO> relationships ) throws STSException
    {
        List<RelationshipImportDTO> relationshipBatch = new ArrayList<RelationshipImportDTO>();
        for (Iterator<RelationshipImportDTO> iterator = relationships.iterator(); iterator.hasNext();)
        {
            RelationshipImportDTO relationshipSDOImportDTO = iterator.next();
            relationshipBatch.add(relationshipSDOImportDTO);
            if (relationshipBatch.size() >= MAX_BATCH_SIZE)
            {
                processRelationshipDataBatch(relationshipBatch);
                relationshipBatch.clear();
            }
            iterator.remove();
        }
        if (relationshipBatch.size() > 0)
        {
            processRelationshipDataBatch(relationshipBatch);
        }
    }
    
    private void processRelationshipDataBatch(List<RelationshipImportDTO> relationships ) throws STSException
    {
        Set<String> relationshipTypes = new HashSet<String>();
        List<String> conceptCodeLookup = new ArrayList<String>();
        for (RelationshipImportDTO relationship : relationships)
        {
            relationshipTypes.add(relationship.getTypeName());
            if (relationship.getNewTargetCode() != null && relationship.getNewTargetCode().length() > 0)
            {
                conceptCodeLookup.add(relationship.getNewTargetCode());
            }
            if (relationship.getOldTargetCode() != null && relationship.getOldTargetCode().length() > 0)
            {
                conceptCodeLookup.add(relationship.getOldTargetCode());
            }
            if (relationship.getSourceCode() == null)
            {
                ConceptImportDTO conceptImportDTO = conceptToRelationshipMap.get(relationship);
                relationship.setSourceCode(conceptImportDTO.getCode());
            }
            conceptCodeLookup.add(relationship.getSourceCode());
        }
        
        Map<String, RelationshipType> relationshipTypeMap = new HashMap<String, RelationshipType>();
        for (String relationshipTypeName : relationshipTypes)
        {
            RelationshipType relationshipType = ConceptRelationshipDelegate.getType(relationshipTypeName);
            if (relationshipType == null)
            {
                throw new STSException("Concept Relationship type: "+relationshipTypeName+" does not exist!");
            }
            relationshipTypeMap.put(relationshipTypeName, relationshipType);
        }
        
        // use a hashset to avoid duplicates
        Set<Long> sourceConceptEntitySetIds = new HashSet<Long>();
        Set<Long> targetConceptEntitySetIds = new HashSet<Long>();
        Map<String, Concept> conceptMap = new HashMap<String, Concept>();
        
        HibernateSessionFactory.currentSession().flush();
        HibernateSessionFactory.currentSession().clear();
        List<Concept> concepts = ConceptDelegate.get(currentCodeSystem, conceptCodeLookup);
        for (Concept concept : concepts)
        {
            conceptMap.put(concept.getCode(), concept);
        }
        
        // get the source and target codes
        for (RelationshipImportDTO relationship : relationships)
        {
            Concept sourceConcept = conceptMap.get(relationship.getSourceCode());
            if (sourceConcept == null)
            {
                throw new STSException("Cannot find source concept with code: "+relationship.getSourceCode()+" CodeSystem: "+currentCodeSystem.getName());
            }
            if ((relationship.getAction().equals(ACTION_UPDATE) || relationship.getAction().equals(ACTION_REMOVE)) && relationship.getOldTargetCode() == null)
            {
            	throw new STSException("Missing element <OldTargetCode> on relationship update for source concept code: "
            			+relationship.getSourceCode()+" in CodeSystem: "+currentCodeSystem.getName());
            }
            String targetCode = (relationship.getAction().equals(ACTION_UPDATE) || relationship.getAction().equals(ACTION_REMOVE)) ? relationship.getOldTargetCode() : relationship.getNewTargetCode();
            if (targetCode == null)
            {
            	String missingElementName = (relationship.getAction().equals(ACTION_UPDATE) || relationship.getAction().equals(ACTION_REMOVE)) ? "OldTargetCode" : "NewTargetCode";
            	throw new STSException("Missing or null relationship element: " + missingElementName + " for concept code: " + currentConceptCode);
            }
            Concept targetConcept = conceptMap.get(targetCode);
            if (targetConcept == null)
            {
                throw new STSException("Cannot find target concept with code: "+targetCode+" CodeSystem: "+currentCodeSystem.getName());
            }
            targetConceptEntitySetIds.add(targetConcept.getEntityId());
            sourceConceptEntitySetIds.add(sourceConcept.getEntityId());
        }
        
        Map<String, ConceptRelationship> relationshipTargetMap = new HashMap<String, ConceptRelationship>();
        List<ConceptRelationship> relationshipLookups = ConceptRelationshipDelegate.getRelationships(new ArrayList<Long>(sourceConceptEntitySetIds), new ArrayList<String>(relationshipTypes), new ArrayList<Long>(targetConceptEntitySetIds), false, true);
        for (ConceptRelationship relationship : relationshipLookups)
        {
            relationshipTargetMap.put(relationship.getSourceEntityId()+":"+relationship.getRelationshipType().getName()+":"+relationship.getTargetEntityId(), relationship);
        }
        
        int flushCount = 0;
        for (RelationshipImportDTO relationshipDTO : relationships)
        {
            Concept sourceConcept = conceptMap.get(relationshipDTO.getSourceCode());
            if (ACTION_ADD.equals(relationshipDTO.getAction()))
            {
                Concept targetConcept = conceptMap.get(relationshipDTO.getNewTargetCode());                
                ConceptRelationship relationship = relationshipTargetMap.get(sourceConcept.getEntityId()+":"+relationshipDTO.getTypeName()+":"+targetConcept.getEntityId());
                if (relationship != null)
                {
                    throw new STSException("Relationship type: "+relationship.getRelationshipType().getName()+", source concept code: "+relationshipDTO.getSourceCode()
                    		+" and target concept code: "+relationshipDTO.getNewTargetCode()+" already exists");
                }
                ConceptRelationshipDelegate.createSDO(currentVersion, sourceConcept.getEntityId(), relationshipTypeMap.get(relationshipDTO.getTypeName()), targetConcept.getEntityId());
            }
            else if (ACTION_UPDATE.equals(relationshipDTO.getAction()))
            {
                Concept targetConcept = conceptMap.get(relationshipDTO.getOldTargetCode());                
                if (targetConcept == null)
                {
                    throw new STSException("Cannot find target concept with code: "+relationshipDTO.getOldTargetCode());
                }
                ConceptRelationship relationship = relationshipTargetMap.get(sourceConcept.getEntityId()+":"+relationshipDTO.getTypeName()+":"+targetConcept.getEntityId());
                if (relationship == null)
                {
                    throw new STSException("Cannot find relationship with type: "+relationshipDTO.getTypeName()+", source concept code: "
                    		+relationshipDTO.getSourceCode()+" and target concept code: "+relationshipDTO.getOldTargetCode());
                }
                
                // now find the new target
                Concept newTargetConcept = null;
                if (relationshipDTO.getNewTargetCode() != null && relationshipDTO.getNewTargetCode().length() > 0)
                {
                    newTargetConcept = conceptMap.get(relationshipDTO.getNewTargetCode());
                    if (newTargetConcept == null)
                    {
                        throw new STSException("Cannot find target concept with code: "+getRelationshipTargetValue(relationshipDTO));
                    }
                }
                if (newTargetConcept == null && relationship.getActive() == relationshipDTO.isActive())
                {
                    throw new STSException("No change detected with type: "+relationshipDTO.getTypeName()+" and target concept code:"+relationshipDTO.getOldTargetCode());
                }
                long targetEntityId = (newTargetConcept == null) ? targetConcept.getEntityId() : newTargetConcept.getEntityId();
                
                ConceptRelationshipDelegate.updateSDO(currentVersion, relationship, targetEntityId, relationshipDTO.isActive());
            }
            else if (ACTION_REMOVE.equals(relationshipDTO.getAction()))
            {
                if (currentVersion.getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
                {
                    throw new STSException("Action type Remove is only allowed for VHAT codesystems!");
                }
                ConceptRelationshipDelegate.removeConceptRelationshipsInAuthoring(sourceConcept.getEntityId());
            }
            
            if (++flushCount % FLUSH_SIZE == 0)
            {
                HibernateSessionFactory.currentSession().flush();
                HibernateSessionFactory.currentSession().clear();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void processDesignationData(Map<DesignationImportDTO, Long> designationMap,
            Set<String> designationTypes) throws Exception
    {
        Map<String, DesignationType> designationTypeMap = new HashMap<String, DesignationType>();
        for (String designationTypeName : designationTypes)
        {
            DesignationType designationType = DesignationDelegate.getType(designationTypeName);
            if (designationType == null)
            {
                throw new STSException("Designation type: "+designationTypeName+" does not exist!");
            }
            designationTypeMap.put(designationTypeName, designationType);
        }
        List<DesignationImportDTO> bulkUpdates = new ArrayList();
        for (Iterator iter = designationMap.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry<DesignationImportDTO, CodedConcept> entry = (Map.Entry<DesignationImportDTO, CodedConcept>) iter.next();
            DesignationImportDTO designationDTO = entry.getKey();
            bulkUpdates.add(designationDTO);
            if (bulkUpdates.size() >= MAX_BATCH_SIZE)
            {
                processDesignationDataBatch(currentCodeSystem, currentVersion, bulkUpdates, designationMap, designationTypeMap);
                bulkUpdates.clear();
            }
        }
        if (bulkUpdates.size() > 0)
        {
            processDesignationDataBatch(currentCodeSystem, currentVersion, bulkUpdates, designationMap, designationTypeMap);
        }
    }

    @SuppressWarnings("unchecked")
    private void processPropertyData(Map<PropertyImportDTO, Long> propertyMap,
            Set<String> propertyTypes) throws STSException
    {
        Map<String, PropertyType> propertyTypeMap = new HashMap<String, PropertyType>();
        for (String propertyTypeName : propertyTypes)
        {
            PropertyType propertyType = PropertyDelegate.getType(propertyTypeName);
            if (propertyType == null)
            {
                throw new STSException("Property type: "+propertyTypeName+" does not exist!");
            }
            propertyTypeMap.put(propertyTypeName, propertyType);
        }
        List<PropertyImportDTO> bulkUpdates = new ArrayList();
        for (Iterator iter = propertyMap.entrySet().iterator(); iter.hasNext();)
        {
            Map.Entry<PropertyImportDTO, Long> entry = (Map.Entry<PropertyImportDTO, Long>) iter.next();
            PropertyImportDTO propertyDTO = entry.getKey();
            bulkUpdates.add(propertyDTO);
            if (bulkUpdates.size() >= MAX_BATCH_SIZE)
            {
                processPropertyDataBatch(currentCodeSystem, currentVersion, bulkUpdates, propertyMap, propertyTypeMap);
                bulkUpdates.clear();
            }
        }
        if (bulkUpdates.size() > 0)
        {
            processPropertyDataBatch(currentCodeSystem, currentVersion, bulkUpdates, propertyMap, propertyTypeMap);
        }
    }
    
    /** Process the designation in batch fashion
     * @param codeSystem
     * @param version
     * @param bulkUpdates
     * @throws Exception 
     */
    private void processDesignationDataBatch(CodeSystem codeSystem, Version version, List<DesignationImportDTO> bulkUpdates, 
            Map<DesignationImportDTO, Long> designationMap, Map<String, DesignationType> designationTypeMap) throws Exception
    {
    	List<String> designationLookupCodes = new ArrayList<String>();
    	List<String> designationLookupNames = new ArrayList<String>();
    	Set<Long> designationConceptIds = new HashSet<Long>();
    	Map<String, ConceptDesignationDTO> designationNameMap = new HashMap<String, ConceptDesignationDTO>();

    	Map<PropertyImportDTO, Long> propertyMap = new HashMap<PropertyImportDTO, Long>();
        Set<String> propertyTypes = new HashSet<String>();

    	// check to see if we have designation codes
        for (DesignationImportDTO designationDTO : bulkUpdates)
        {
        	if (designationDTO.getCode() != null && designationDTO.getCode().length() > 0)
        	{
            	designationLookupCodes.add(designationDTO.getCode());
        	}
        	else
        	{
        	    designationLookupNames.add(getDesignationName(designationDTO));
        		designationConceptIds.add(designationMap.get(designationDTO));
        	}
        }
        
        Map<String, Designation> designationCodeLookupMap = new HashMap<String, Designation>();

        // if we do have designation codes do a lookup
        if (designationLookupCodes.size() > 0)
        {
	        List<Designation> designationLookups = DesignationDelegate.get(codeSystem, designationLookupCodes);
	        for (Designation designation : designationLookups)
	        {
	            designationCodeLookupMap.put(designation.getCode(), designation);
	        }
        }
        if (designationLookupNames.size() > 0)
        {
        	List<ConceptDesignationDTO> designationLookups = DesignationDelegate.get(designationConceptIds, designationLookupNames);
        	for (ConceptDesignationDTO conceptDesignationDTO : designationLookups)
            {
        	    designationNameMap.put(conceptDesignationDTO.getConceptEntityId()+":"+conceptDesignationDTO.getDesignation().getType().getName()
        	    		+":"+conceptDesignationDTO.getDesignation().getName(), conceptDesignationDTO);
            }
        }

        // auto assign vuid and codes
        if (currentCodeSystem.getName().equals(HibernateSessionFactory.VHAT_NAME) || autoAssign)
        {
            processVuids(bulkUpdates);
        }
        
        List<SubsetMembershipImportDTO> subsetMembershipBulk = new ArrayList<SubsetMembershipImportDTO>();
        int flushCount = 0;
        for (DesignationImportDTO designationDTO : bulkUpdates)
        {
            Designation designation = null;
            if (ACTION_ADD.equals(designationDTO.getAction()))
            {
                // VALIDATE to prevent duplicates
                if (designationDTO.getCode() == null || designationDTO.getCode().length() < 1)
                {
                    Long conceptEntityId = designationMap.get(designationDTO);
                    ConceptDesignationDTO conceptDesignation = designationNameMap.get(conceptEntityId+":"+designationDTO.getTypeName()+":"+designationDTO.getValueNew());
                    if (conceptDesignation != null)
                    {
                        throw new STSException("Designation: "+designationDTO.getValueNew()+" already exists");
                    }
                }
                else
                {
                    designation = designationCodeLookupMap.get(designationDTO.getCode());
                    if (designation != null)
                    {
                        throw new STSException("Designation code: "+designationDTO.getCode()+" already exists");
                    }
                }
                Long conceptEntityId = designationMap.get(designationDTO);
                if (designationDTO.getTypeName().equals(currentCodeSystem.getPreferredDesignationType().getName()))
                {
                    if (hasPreferredDesignation(conceptEntityId, designationDTO.isActive()))
                    {
                        throw new STSException("Preferred designation is not unique for designation name: "+ designationDTO.getValueNew() +" type: "+ designationDTO.getTypeName());
                    }
                }
                designation = DesignationDelegate.createSDO(version, conceptEntityId, designationTypeMap.get(designationDTO.getTypeName()), designationDTO.getValueNew(),
                        designationDTO.getCode(), designationDTO.getVuid(), designationDTO.isActive());
            }
            else if (ACTION_UPDATE.equals(designationDTO.getAction()) || ACTION_NONE.equals(designationDTO.getAction()) || ACTION_REMOVE.equals(designationDTO.getAction()))
            {
                boolean useCode = false;
                if (designationDTO.getCode() == null || designationDTO.getCode().length() < 1)
                {
                    String designationName = getDesignationName(designationDTO);
                    Long conceptEntityId = designationMap.get(designationDTO);
                    ConceptDesignationDTO conceptDesignation = designationNameMap.get(conceptEntityId+":"+designationDTO.getTypeName()+":"+designationName);
                    if (conceptDesignation == null)
                    {
                    	throw new STSException("Designation name: " + designationName + " does not exist!");
                    }
                    designation = conceptDesignation.getDesignation();
                    useCode = true;
                }
                else
                {
                    designation = designationCodeLookupMap.get(designationDTO.getCode());
                    if (designation == null)
                    {
                        throw new STSException("Designation code: " + designationDTO.getCode() + " does not exist!");
                    }
                }
                if (ACTION_UPDATE.equals(designationDTO.getAction()))
                {
                    String value =  (designationDTO.getValueNew() == null) ? designation.getName() : designationDTO.getValueNew();
                    if (designation.getName().equals(value) && designationDTO.isActive() == designation.getActive()
                    		&& designationDTO.getTypeName() == designation.getType().getName())
                    {
                        String message = (useCode) ? "Designation Code: "+designation.getCode() : "Designation Name: "+designation.getName();
                        throw new STSException(message+" <Action> is 'update' but no changes where found. ["+currentCodeSystem.getName()+"]");
                    }
                    DesignationType type = designationTypeMap.get(designationDTO.getTypeName());
                    if (!designationDTO.getTypeName().equals(designation.getType().getName()) && designationDTO.getTypeName().equals(currentCodeSystem.getPreferredDesignationType().getName()))
                    {
                        Long conceptEntityId = designationMap.get(designationDTO);
                        if (hasPreferredDesignation(conceptEntityId, designation.getActive()))
                        {
                            throw new STSException("Preferred designation is not unique for designation name: "+ designationDTO.getValueNew() +" type: "+ designationDTO.getTypeName());
                        }
                    }
                    DesignationDelegate.updateSDO(version, designation, value, type, designationDTO.isActive());
                    if (designationDTO.getMoveFromConceptCode() != null && designationDTO.getMoveFromConceptCode().length() > 0)
                    {
                        CodedConcept moveFromConcept = CodedConceptDelegate.get(codeSystem, designationDTO.getMoveFromConceptCode());
                        if (moveFromConcept == null)
                        {
                            throw new STSException("Move to concept code: "+ designationDTO.getMoveFromConceptCode()+ " does not exist!");
                        }
                        Long conceptEntityId = designationMap.get(designationDTO);
                        CodedConcept moveToConcept = (CodedConcept) ConceptDelegate.get(conceptEntityId);
                        
                        DesignationRelationshipDelegate.update(codeSystem, designation.getCode(), moveFromConcept.getCode(), moveToConcept.getCode(), initialState);
                    }
                }
                else if (ACTION_REMOVE.equals(designationDTO.getAction()))
                {
                    Long conceptEntityId = designationMap.get(designationDTO);
                    if (currentVersion.getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
                    {
                        throw new STSException("Action type Remove is only allowed for VHAT codesystems!");
                    }
                    DesignationDelegate.delete(conceptEntityId, designation);
                }
            }
            // we need to associate the designation with the subset memberships entries
            List<PropertyImportDTO> propertyDTOs = designationDTO.getProperties();
            if (propertyDTOs != null)
            {
                for (PropertyImportDTO propertyDTO : propertyDTOs)
                {
                    propertyTypes.add(propertyDTO.getTypeName());
                    propertyMap.put(propertyDTO, designation.getEntityId());
                }
            }
            
            List<SubsetMembershipImportDTO> subsetMemberships = designationDTO.getSubsets();
            for (SubsetMembershipImportDTO subsetMembershipDTO : subsetMemberships)
            {
                subsetMembershipDTO.setEntityId(designation.getEntityId());
            }
            if ((subsetMemberships.size()+subsetMembershipBulk.size()) > MAX_BATCH_SIZE)
            {
                processSubsetMembershipData(subsetMembershipBulk);
            }
            subsetMembershipBulk.addAll(subsetMemberships);
            if (subsetMembershipBulk.size() >= MAX_BATCH_SIZE)
            {
                processSubsetMembershipData(subsetMembershipBulk);
            }
            
            if (++flushCount % FLUSH_SIZE == 0)
            {
                HibernateSessionFactory.currentSession().flush();
                HibernateSessionFactory.currentSession().clear();
            }
        }

        if (!propertyMap.isEmpty())
        {
            processPropertyData(propertyMap, propertyTypes);
        }

        if (subsetMembershipBulk.size() > 0)
        {
            processSubsetMembershipData(subsetMembershipBulk);
        }
    }
    
    /**
     *  make sure there is only one designation per coded concept 
     *  of type: preferred designation for the code system     
     * @throws STSException 
     **/
    private boolean hasPreferredDesignation(Long conceptEntityId, boolean active) throws STSException
    {
        boolean found = false;
        if (active)
        {
            if (preferredDesignationMap  == null)
            {
                preferredDesignationMap = CodeSystemDao.getPreferredForConcepts(currentCodeSystem);
            }
            found = preferredDesignationMap.contains(conceptEntityId);
            if (!found)
            {
                preferredDesignationMap.add(conceptEntityId);
            }
        }
        return found;
    }

    private void processPropertyDataBatch(CodeSystem codeSystem, Version version, List<PropertyImportDTO> bulkUpdates, 
            Map<PropertyImportDTO, Long> propertyMap, Map<String, PropertyType> propertyTypeMap) throws STSException
    {
        Set<Long> propertyConceptIds = new HashSet<Long>();
        Map<String, Property> propertyNameMap = new HashMap<String, Property>();
        Map<String, Property> propertyNameMapForGEM = new HashMap<String, Property>();

        for (PropertyImportDTO propertyDTO : bulkUpdates)
        {
            propertyConceptIds.add(propertyMap.get(propertyDTO));
        }
        
        if (propertyConceptIds.size() > 0)
        {
            List<String> propertyTypes = new ArrayList<String>();
            propertyTypes.addAll(propertyTypeMap.keySet());
            List<Property> propertyLookups = PropertyDelegate.getByConceptEntityIdsAndTypes(propertyConceptIds, propertyTypes);
            for (Property property : propertyLookups)
            {
            	String propertyValue = (property.getValue() != null) ? property.getValue() : "";
                propertyNameMap.put(property.getConceptEntityId()+":"+property.getPropertyType().getName()+":"+propertyValue, property);
            }
        }
        
        int flushCount = 0;
        for (PropertyImportDTO propertyDTO : bulkUpdates)
        {
            Long conceptEntityId = propertyMap.get(propertyDTO);
            Concept concept = ConceptDelegate.get(conceptEntityId, version.getId());
            if (ACTION_ADD.equals(propertyDTO.getAction()))
            {
                Property property = propertyNameMap.get(conceptEntityId+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueNew());
                if (property != null)
                {
                    throw new STSException("Property type: "+property.getPropertyType().getName()+" value: "+property.getValue()+", concept code: "+concept.getCode()+" already exists");
                }
                if(propertyDTO.getTypeName().equalsIgnoreCase("GEM_Flags")){
                    String conceptName = concept.getName();
                    property = propertyNameMapForGEM.get(conceptName+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueNew());
                    if (property != null)
                    {
                        throw new STSException("Property type: "+property.getPropertyType().getName()+" value: "+property.getValue()+", concept code: "+concept.getCode()+" already exists");
                    }
                }
                Property newProp = PropertyDelegate.create(version, conceptEntityId, propertyTypeMap.get(propertyDTO.getTypeName()), propertyDTO.getValueNew());
                propertyNameMapForGEM.put(concept.getName()+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueNew(), newProp);
            }
            else if (ACTION_UPDATE.equals(propertyDTO.getAction()))
            {
                // if the new value is null then they are probable just inactivating
                Property property = propertyNameMap.get(conceptEntityId+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueOld());
                if (property == null)
                {
                    throw new STSException("Property type: "+propertyDTO.getTypeName()+", value: "+propertyDTO.getValueOld()+", concept code: "+concept.getCode()+" does not exist.");
                }
                String value = (propertyDTO.getValueOld() != null && propertyDTO.getValueOld().length() > 0) ? propertyDTO.getValueOld() : null;
                if (propertyDTO.getValueNew() != null)
                {
                	value = propertyDTO.getValueNew();
                	if (value.length() == 0)
                	{
                		value = null;
                	}
                }
                if (((property.getValue() != null && property.getValue().equals(value)) || property.getValue() == value) && property.getActive() == propertyDTO.isActive())
                {
                    throw new STSException("No property change detected with type: "+property.getPropertyType().getName()+", concept code: "+concept.getCode());
                }
                Property newProp = PropertyDelegate.updateSDO(version, property, value, propertyDTO.isActive());
                if(propertyDTO.getTypeName().equalsIgnoreCase("GEM_Flags")){
                    String conceptName = concept.getName();
                    property = propertyNameMapForGEM.get(conceptName+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueOld());
                    if (property != null)
                    {
                    	propertyNameMapForGEM.remove(property);
                        propertyNameMapForGEM.put(concept.getName()+":"+propertyDTO.getTypeName()+":"+value, newProp);

                    }
                }
            }
            else if (ACTION_REMOVE.equals(propertyDTO.getAction()))
            {
                if (propertyDTO.getValueOld() == null || propertyDTO.getValueOld().length() == 0)
                {
                    throw new STSException("Must specify the value for the property (OldValue) for concept code: "+concept.getCode());
                }
                Property property = propertyNameMap.get(conceptEntityId+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueOld());
                if (currentVersion.getId() != HibernateSessionFactory.AUTHORING_VERSION_ID)
                {
                    throw new STSException("Action type Remove is only allowed for VHAT codesystems!");
                }
                PropertyDelegate.remove(property);
                if(propertyDTO.getTypeName().equalsIgnoreCase("GEM_Flags")){
                    String conceptName = concept.getName();
                    property = propertyNameMapForGEM.get(conceptName+":"+propertyDTO.getTypeName()+":"+propertyDTO.getValueOld());
                    if (property != null)
                    {
                    	propertyNameMapForGEM.remove(property);

                    }
                }
            }
            if (++flushCount % FLUSH_SIZE == 0)
            {
                HibernateSessionFactory.currentSession().flush();
                HibernateSessionFactory.currentSession().clear();
            }
        }
    }
    
    
    // Get the designation name based on action
    private String getDesignationName(DesignationImportDTO designationDTO)
    {
        String result = null;
        if (ACTION_UPDATE.equals(designationDTO.getAction()) && (designationDTO.getValueOld() != null && designationDTO.getValueOld().length() > 0))
        {
            result = designationDTO.getValueOld();
        }
        else
        {
            result = designationDTO.getValueNew();
        }
        
        return result;
    }

    private void setConceptState(Concept concept, State state) throws STSException
    {
    	// if code is in set then we allow the concept to be changed
    
       	ConceptState conceptState = ConceptStateDelegate.get(concept.getEntityId());
    	if (conceptState != null)
    	{
    		if (!conceptState.getState().getType().equals(State.INITIAL))
    		{
    			// concept is not allowed to be changed - throw an error
    			throw new STSUpdateException("Cannot change concept with code: " + concept.getCode() + " that is in state: " + conceptState.getState().getName());
    		}
    	}
    	else
    	{
    	    ConceptStateDelegate.createOrUpdate(concept.getEntityId(), state);
    	}
    }

    // Get the designation name based on action
    private String getPropertyValue(PropertyImportDTO propertyDTO)
    {
        String result = null;
        if (ACTION_UPDATE.equals(propertyDTO.getAction()) || ACTION_REMOVE.equals(propertyDTO.getAction()))
        {
            result = propertyDTO.getValueOld();
        }
        else
        {
            result = propertyDTO.getValueNew();
        }
        
        return result;
    }
    
    // Get the designation name based on action
    private  String getRelationshipTargetValue(RelationshipImportDTO relationshipDTO)
    {
        String result = null;
        if ((ACTION_UPDATE.equals(relationshipDTO.getAction()) || ACTION_REMOVE.equals(relationshipDTO.getAction())) && (relationshipDTO.getOldTargetCode() != null && relationshipDTO.getOldTargetCode().length() > 0))
        {
            result = relationshipDTO.getOldTargetCode();
        }
        else
        {
            result = relationshipDTO.getNewTargetCode();
        }
        
        return result;
    }
    /**
     * @param versionElement
     * @param codeSystem
     * @param version
     * @throws Exception 
     */
    protected ConceptImportDTO getConceptData() throws Exception
    {
    	String action = elementMap.get(ACTION_ELEMENT);
    	String name = elementMap.get(NAME_ELEMENT);
        String vuidString = elementMap.get(VUID_ELEMENT);
    	String code = elementMap.get(CODE_ELEMENT);
    	String active = elementMap.get(ACTIVE_ELEMENT);
        Long vuid = null;
        
    	if (active == null && (ACTION_ADD.equals(action) || ACTION_UPDATE.equals(action)) )
    	{
    	    throw new STSException("Active element must be specified for concept code: "+code);
    	}
    	else if (active == null)
    	{
            active = Boolean.TRUE.toString();
    	}
        if (vuidString != null)
        {
            vuid = Long.valueOf(vuidString);
        }
    	ConceptImportDTO conceptDTO = null;
    	
    	if (conceptType.equals(MAPENTRY_ELEMENT))
    	{
    		String sourceCode = elementMap.get(SOURCE_CODE_ELEMENT);
    		String targetCode = elementMap.get(TARGET_CODE_ELEMENT);
    		String sequenceString = elementMap.get(SEQUENCE_ELEMENT);
    		String groupingString = elementMap.get(GROUPING_ELEMENT);
    		int sequence = 0;
    		Long grouping = null;
            if (sequenceString != null)
            {
            	sequence = Integer.valueOf(sequenceString);
            }
            if (groupingString != null)
            {
            	grouping = Long.valueOf(groupingString);
            }
            String effectiveDateString = elementMap.get(EFFECTIVE_DATE_ELEMENT);
            Date effectiveDate = convertToDate(effectiveDateString);

            conceptDTO = new MapEntryImportDTO(action, name, code, vuid, parseBoolean(active),sourceCode, targetCode, sequence, grouping, effectiveDate);
    	}
    	else if (conceptType.equals(MAPSET_ELEMENT))
    	{
    		String sourceCodeSystem = elementMap.get(SOURCE_CODE_SYSTEM);
    		String sourceVersionName = elementMap.get(SOURCE_VERSION_NAME);
    		String targetCodeSystem = elementMap.get(TARGET_CODE_SYSTEM);
    		String targetVersionName = elementMap.get(TARGET_VERSION_NAME);
            String effectiveDateString = elementMap.get(EFFECTIVE_DATE_ELEMENT);
    		String allowDuplicates = elementMap.get(ALLOW_DUPLICATES);
            elementMap.clear();
            Date effectiveDate = convertToDate(effectiveDateString);
            if (allowDuplicates != null && parseBoolean(allowDuplicates) == true)
            {
            	allowDuplicateMapEntries = true;
            }
            else
            {
            	allowDuplicateMapEntries = false;
            }
    		
    		conceptDTO = new MapSetImportDTO(action, name, code, vuid, parseBoolean(active), sourceCodeSystem, sourceVersionName, targetCodeSystem, targetVersionName, effectiveDate);
    	}
    	else if (conceptType.equals(CODEDCONCEPT_ELEMENT))
    	{
        	conceptDTO = new ConceptImportDTO(action, name, code, vuid, parseBoolean(active));        
    	}
        currentConceptCode = code;
        elementMap.clear();

        return conceptDTO;
    }

	private Date convertToDate(String dateString) throws STSException
	{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

		Date thisDate = null;
		
		if (dateString != null)
		{
		    try
		    {
		        thisDate = sdf.parse(dateString);
		    } 
		    catch (Exception ex)
		    {
		        throw new STSException("The Date 'dateString' was not valid it must be formatted as '"+DATE_FORMAT+"'. reason: "+ex.getMessage());
		    }
		}
		return thisDate;
	}
    
    protected TypeImportDTO getTypeData() throws STSException
    {
        String name = elementMap.get(NAME_ELEMENT);
        String kind = elementMap.get(KIND_ELEMENT);
        elementMap.clear();
        
        TypeImportDTO typeDTO = new TypeImportDTO(kind, name);
        
        return typeDTO;
    }
    
    protected SubsetImportDTO getSubsetData() throws STSException
    {
        String action = elementMap.get(ACTION_ELEMENT);
        String subsetName = elementMap.get(NAME_ELEMENT);
        String vuidString = elementMap.get(VUID_ELEMENT);
        String active = elementMap.get(ACTIVE_ELEMENT);
        Long vuid = null;
        elementMap.clear();
        
        if (active == null)
        {
            // default of 'Active' element is true
            active = "true";
        }
        if (vuidString != null)
        {
            vuid = Long.valueOf(vuidString);
        }
        SubsetImportDTO subsetDTO = new SubsetImportDTO(action, subsetName, vuid, parseBoolean(active));
        
        return subsetDTO;
    }

    protected DesignationImportDTO getDesignationData() throws STSException
    {
    	String action = elementMap.get(ACTION_ELEMENT);
    	String typeName = elementMap.get(TYPE_NAME_ELEMENT);
    	String code = elementMap.get(CODE_ELEMENT);
        String vuidString = elementMap.get(VUID_ELEMENT);
    	String valueOld = elementMap.get(VALUE_OLD_ELEMENT);
    	String valueNew = elementMap.get(VALUE_NEW_ELEMENT);
    	String active = elementMap.get(ACTIVE_ELEMENT);
    	String moveFromConceptCode = elementMap.get(MOVE_FROM_CONCEPT_CODE_ELEMENT);
        elementMap.clear();

        Long vuid = null;
    	if (active == null)
    	{
    		// default of 'Active' element is true
    		active = "true";
    	}
    	
        if (vuidString != null)
        {
            vuid = Long.valueOf(vuidString);
        }
        
    	DesignationImportDTO designationDTO = new DesignationImportDTO(action, typeName, code, valueOld, valueNew, vuid, parseBoolean(active));
    	designationDTO.setMoveFromConceptCode(moveFromConceptCode);
        return designationDTO;
    }
    
    protected PropertyImportDTO getPropertyData() throws STSException
    {
        String action = elementMap.get(ACTION_ELEMENT);
        String typeName = elementMap.get(TYPE_NAME_ELEMENT);
        String valueOld = elementMap.get(VALUE_OLD_ELEMENT);
        String valueNew = elementMap.get(VALUE_NEW_ELEMENT);
        String active = elementMap.get(ACTIVE_ELEMENT);
        elementMap.clear();

        if (active == null)
        {
            // default of 'Active' element is true
            active = "true";
        }
        if (ACTION_UPDATE.equals(action))
        {
        	if (valueOld == null)
        	{
                throw new STSException("Property old value cannot be null on an action 'update' for concept code: "+currentConceptCode+" and code system: "+currentCodeSystem.getName());
        	}
        }
        if (valueOld != null && valueOld.equals(valueNew) == true)
        {
            throw new STSException("Property old and new values ("+valueOld+") cannot be the same for property type: "+typeName+", concept code: "+currentConceptCode+" and code system: "+currentCodeSystem.getName());
        }

        PropertyImportDTO propertyDTO = new PropertyImportDTO(action, typeName, valueOld, valueNew, parseBoolean(active));

        return propertyDTO;
    }

    protected RelationshipImportDTO getRelationshipData() throws STSException
    {
        String action = elementMap.get(ACTION_ELEMENT);
        String typeName = elementMap.get(TYPE_NAME_ELEMENT);
        String newTargetCode = elementMap.get(NEW_TARGETCODE_ELEMENT);
        String oldTargetCode = elementMap.get(OLD_TARGETCODE_ELEMENT);
        String active = elementMap.get(ACTIVE_ELEMENT);
        elementMap.clear();

        if (active == null)
        {
            // default of 'Active' element is true
            active = "true";
        }
        if (oldTargetCode != null && oldTargetCode.equals(newTargetCode) == true)
        {
            throw new STSException("Old target code and new target code ("+oldTargetCode+") cannot be the same for Relationship type: "+typeName+", Concept code: "+currentConceptCode+" and CodeSystem: "+currentCodeSystem.getName());
        }

        
        RelationshipImportDTO relationshipDTO = new RelationshipImportDTO(action, typeName, importConcept.getCode(), oldTargetCode, newTargetCode, parseBoolean(active));

        if (importConcept.getCode() == null)
        {
            conceptToRelationshipMap.put(relationshipDTO, importConcept);
        }
        
        return relationshipDTO;
    }

    private SubsetMembershipImportDTO getSubsetMembershipData()
    {
        String action = elementMap.get(ACTION_ELEMENT);
        String vuidString = elementMap.get(VUID_ELEMENT);
        String active = elementMap.get(ACTIVE_ELEMENT);
        elementMap.clear();

        SubsetMembershipImportDTO subsetDTO = new SubsetMembershipImportDTO(action, Long.valueOf(vuidString), parseBoolean(active));
        return subsetDTO;
    }
    
    private boolean parseBoolean(String value)
    {
        boolean result = false;
        if (value == null || value.equalsIgnoreCase("true") || value.equals("1"))
        {
            result = true;
        }
        
        return result;
    }
    private Vuid getVuid(String reason, int count) throws Exception
    {
        return vuidDelegate.createVuidRange(count, "Terminology Import", reason);
    }

    public static void main(String[] args) throws STSException
    {
        Transaction tx = HibernateSessionFactory.currentSession().beginTransaction();
        TerminologyDataImporter importer = new TerminologyDataImporter("C:\\Users\\vhaisfsharmv\\Documents\\GEMSTFDupeTest.xml", "TerminologyData.xsd");
        importer.process();
    	log.debug("Finished Importing");
    }
    
}
