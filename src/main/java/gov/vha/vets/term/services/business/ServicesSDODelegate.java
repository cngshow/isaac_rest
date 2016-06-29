package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dto.config.DomainConfig;
import gov.vha.vets.term.services.dto.importer.FileImportDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.importer.TerminologyDataImporter;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

/**
 * @author vhaislempeyd
 */
public class ServicesSDODelegate
{
    private static Logger log = Logger.getLogger(ServicesSDODelegate.class.getPackage().getName());

    // instance variables
    List<DomainConfig> publisherDomains = new ArrayList<DomainConfig>();

    // Default XML File and Schema
    private static String SCHEMA_FILENAME = "TerminologyData.xsd";
    
    /**
     * Gets the entire file
     * 
     * @return List List of FileImportDTO
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public synchronized static List<FileImportDTO> importTerminologyData(String fileName) throws STSException
    {
        List<FileImportDTO> sdoFileImportList = null;
        
        try
        {
            TerminologyDataImporter sdoImporter = new TerminologyDataImporter(fileName, SCHEMA_FILENAME);
        	sdoFileImportList = sdoImporter.process();
            
        }
        catch (NullPointerException ex)
        {
        	throw ex;
        }
        catch (Exception e)
        {
            throw new STSException(e.getMessage());
        }
        
        return sdoFileImportList;
    }

    /**
     * Get a list containing all non-VHAT versions
     * 
     * @return List<Version>
     */
    public static List<Version> getSDOVersions()
    {
        long vhatCodeSystemId = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME).getId();
        List<Version> sdoVersions = VersionDelegate.getSDOVersions(vhatCodeSystemId);
        return sdoVersions;
    }

    /**
     * Delete an SDO version from the database. Deletes all CodedConcepts,
     * Designations, DesignationRelationships and the Version by version ID.
     * 
     * @param versionId
     */
    public static void removeSDOVersion(long versionId) throws STSException
    {
        try
        {
            // remove the Version by ID
            VersionDelegate.removeSDOVersion(versionId);
        }
        catch (HibernateException e)
        {
            log.error("Failed to remove SDO with version id: " + versionId + ".", e);
            throw new STSException(e);
        }
    }

    /**
     * Verify that there is only one preferred designation type for each
     * coded concept of the given code system id.
     * 
     * @param codeSystemId
     * @throws STSException 
     */
	public static void verifyPreferredDesignationUnique(CodeSystem codeSystem) throws STSException
	{
		CodeSystemDelegate.verifyPreferredDesignationUnique(codeSystem);
	}
}
