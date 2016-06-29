/**
 * 
 */
package gov.vha.vets.term.services.dto;

import java.io.Serializable;
import java.util.Collection;

@SuppressWarnings("serial")
public class RelatedConceptsDTO implements Serializable
{
    private String name;
    private Long vuid;
    private Collection<CodedConceptDesignationDTO> relatedConcepts;
    

    
    public RelatedConceptsDTO()
    {
    }

    public RelatedConceptsDTO(String name, Long vuid,
            Collection<CodedConceptDesignationDTO> relationshipList)
    {
        super();
        this.name = name;
        this.vuid = vuid;
        this.relatedConcepts = relationshipList;
    }


    public Collection<CodedConceptDesignationDTO> getRelatedConcepts()
    {
        return relatedConcepts;
    }

    public void setRelatedConcepts(
            Collection<CodedConceptDesignationDTO> relatedConcepts)
    {
        this.relatedConcepts = relatedConcepts;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getVuid()
    {
        return vuid;
    }

    public void setVuid(Long vuid)
    {
        this.vuid = vuid;
    }


}
