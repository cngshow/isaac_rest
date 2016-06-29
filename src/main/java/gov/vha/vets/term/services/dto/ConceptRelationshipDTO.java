/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.ConceptRelationship;

import java.util.Comparator;

/**
 * @author
 *
 */
@SuppressWarnings("serial")
public class ConceptRelationshipDTO extends RelationshipDTO implements Comparator<ConceptRelationshipDTO>
{
    protected String conceptCode;

    
    public ConceptRelationshipDTO()
    {
        super();
    }

    public ConceptRelationshipDTO(String conceptCode, ConceptRelationship relationship, String name)
    {
        this.conceptCode = conceptCode;
        this.relationship = relationship;
        this.name = name;
    }

    public String getConceptCode()
    {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode)
    {
        this.conceptCode = conceptCode;
    }

    @Override
    public int compare(ConceptRelationshipDTO o1, ConceptRelationshipDTO o2)
    {
        return o1.getConceptCode().compareTo(o2.getConceptCode());
    }
}
