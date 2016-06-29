/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.Version;

import java.io.Serializable;
import java.util.List;

/**
 * @author vhaislmurdoh
 *
 */
public class ConceptDetailDTO implements Serializable
{
    private List<CodedConcept> codedConceptList;
    private List<Property> propertyList;
    private List<Designation> designationList;
    private List<Version> versionList;
    private List<ConceptRelationshipDTO> relationshipList;
    private List<SubsetDetailDTO> subsetList;
    
    public ConceptDetailDTO(List<CodedConcept> codedConcepts, 
                            List<Property> properties, 
                            List<Designation> designations, 
                            List<Version> versions,
                            List<ConceptRelationshipDTO> relationships,
                            List<SubsetDetailDTO> subsets)
    {
        this.codedConceptList = codedConcepts;
        this.propertyList = properties;
        this.designationList = designations;
        this.versionList = versions;
        this.relationshipList = relationships;
        this.subsetList = subsets;
    }
    
    /**
     * @return the codedConceptList
     */
    public List<CodedConcept> getCodedConceptList()
    {
        return codedConceptList;
    }
    /**
     * @param codedConceptList the codedConceptList to set
     */
    public void setCodedConcept(List<CodedConcept> codedConcept)
    {
        this.codedConceptList = codedConcept;
    }
    /**
     * @return the designationList
     */
    public List<Designation> getDesignationList()
    {
        return designationList;
    }
    /**
     * @param designationList the designationList to set
     */
    public void setDesignations(List<Designation> designations)
    {
        this.designationList = designations;
    }
    /**
     * @return the propertyList
     */
    public List<Property> getPropertyList()
    {
        return propertyList;
    }
    /**
     * @param propertyList the propertyList to set
     */
    public void setProperties(List<Property> properties)
    {
        this.propertyList = properties;
    }

    /**
     * @return the versionList
     */
    public List<Version> getVersionList()
    {
        return versionList;
    }

    /**
     * @param versionList the versionList to set
     */
    public void setVersionList(List<Version> versionList)
    {
        this.versionList = versionList;
    }

    /**
     * @return the relationshipList
     */
    public List<ConceptRelationshipDTO> getRelationshipList()
    {
        return relationshipList;
    }

    /**
     * @param relationshipList the relationshipList to set
     */
    public void setRelationshipList(List<ConceptRelationshipDTO> relationshipList)
    {
        this.relationshipList = relationshipList;
    }

    /**
     * @return the subsetList
     */
    public List<SubsetDetailDTO> getSubsetList()
    {
        return subsetList;
    }

    /**
     * @param subsetList the subsetList to set
     */
    public void setSubsetList(List<SubsetDetailDTO> subsetList)
    {
        this.subsetList = subsetList;
    }

}
