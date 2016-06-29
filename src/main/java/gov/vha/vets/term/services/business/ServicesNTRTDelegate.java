package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ServicesNTRTDao;
import gov.vha.vets.term.services.dto.PublishedRequestDTO;

import java.util.List;
import java.util.Map;


public class ServicesNTRTDelegate
{
    /**
     * Get a list of designation names for the given subset name on which the subset relationships are active
     *   Question: Can the designation be inactive or must it be active?
     *   Rename method to getDesignationNamesBySubset()
     * @param designationName
     * @param subsetNames
     * @return
     */
	public static List<String> getDesignationNamesBySubset(String subsetName)
	{
		return ServicesNTRTDao.getDesignationNamesBySubset(subsetName);
	}
	
	/**
     * Get a list of designation names for the given subset name, designation name and relationshipTypeName
     * @param designationName
     * @param relationshipTypeName
     * @param subsetName
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getRelationshipDesignationNames(String designationName, String relationshipTypeName)
	{
		return ServicesNTRTDao.getRelationshipDesignationNames(designationName, relationshipTypeName);
	}
	
	/**
     * Get a list of designation names for the given subset name, designation name and relationshipTypeName using a inverse relationship
     * @param designationName
     * @param relationshipTypeName
     * @param subsetName
     * @return
     */
	@SuppressWarnings("unchecked")
	public static List<String> getInverseRelationshipDesignationNames(String designationName, String relationshipTypeName, String subsetName)
	{
		return ServicesNTRTDao.getInverseRelationshipDesignationNames(designationName, relationshipTypeName, subsetName);
	}
	
    /**
     * Determine whether the designation name is in a list of subsets on which the subset relationships are active
     *   Question: Can the designation be inactive or must it be active?
     * @param designationName
     * @param subsetNames
     * @return
     */
	public static boolean isDesignationInSubsets(String designationName, List<String> subsetNames)
    {
    	return ServicesNTRTDao.isDesignationInSubsets(designationName, subsetNames);
    }
	
//	public static List<String> getPublishedRequestList(String name, String subset, String domainName)
//    {
//        return ServicesNTRTDao.getPublishedRequestList(name, subset, domainName);
//    }
	
	public static Map<String, String> getPublishedRequestsVitals(List<String> qualifierNames, String type, String subset, boolean allQualifiers)
    {
        return ServicesNTRTDao.getPublishedRequestsVitals(qualifierNames, type, subset, allQualifiers);
    }
	
	public static List<PublishedRequestDTO> getPublishedRequestList(String name, String subset, String domainName)
	{
		return ServicesNTRTDao.getPublishedRequestList(name, subset, domainName);
	}
	
	// checks if the source code is in the specified mapset
	public static boolean isValidMapSetSourceCode(Long mapSetVuid, String sourceCode)
	{
		return ServicesNTRTDao.isValidMapSetSourceCode(mapSetVuid, sourceCode);
	}
	
	// Checks if the code exists in the code system
	public static boolean isValidCode(String code, String codeSystemName)
	{
		return ServicesNTRTDao.isValidCode(code, codeSystemName);
	}
	
	public static boolean isExistingMapping(String sourceCode, String targetCode, Long mapSetVuid)
	{
		return ServicesNTRTDao.isExistingMapping(sourceCode, targetCode, mapSetVuid);
	}
	
	public static boolean isMapSetSourceTerm(Long mapSetVuid, String sourceTerm)
	{
		return ServicesNTRTDao.isMapSetSourceTerm(mapSetVuid, sourceTerm);
	}

	
//	public static Map<String, String> getPublishedRequests(String name, List<String> qualifierNames, String subset)
//    {
//        return ServicesNTRTDao.getPublishedRequests(name, qualifierNames, subset);
//    }
	
//	public static List<PublishedRequestDTO> getPublishedRequestWithQualifierRelationships(String name, String subset)
//    {
//        return ServicesNTRTDao.getPublishedRequestWithQualifierRelationships(name, subset);
//    }
	
	//get all published requests by code
//	public static List<PublishedRequestDTO> getPublishedRequestList(String code, String subset)
//	{
//	    return ServicesNTRTDao.getPublishedRequestsByCode(code, subset);
//	}
	
	public static List<String> getAllVitalCategories(String subsetName)
	{
	    return ServicesNTRTDao.getAllDesignationsInSubset(subsetName);
	}
}
