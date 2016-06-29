package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.Concept;
import gov.vha.vets.term.services.model.ConceptState;

public class ConceptChangeDTO extends BaseChangeDTO
{
    String preferredName;
    boolean entityOfConceptChanged = false;
    ConceptState conceptState;
    
    
    public String getPreferredName()
    {
        return preferredName;
    }
    public void setPreferredName(String preferredName)
    {
        this.preferredName = preferredName;
    }
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null)
        {
            Concept previousConcept = (Concept)previous;
            Concept recentConcept = (Concept)recent;
            
            String recentConceptName = (recentConcept.getName() != null) ? recentConcept.getName() : ""; 
            String previousConceptName = (previousConcept.getName() != null) ? previousConcept.getName() : "";
            
            if (!recentConceptName.equals(previousConceptName)
            		|| (recentConcept.getVuid() == null && previousConcept.getVuid() != null)
            		|| (recentConcept.getVuid() != null && previousConcept.getVuid() == null)
                    || (recentConcept.getVuid() != null && previousConcept.getVuid() != null && 
                    		!recentConcept.getVuid().equals(previousConcept.getVuid())))
            {
                result = true;
            }
        }
        else
        {
            result = false;
        }
        return result;
    }
    public ConceptState getConceptState()
    {
        return conceptState;
    }
    public void setConceptState(ConceptState conceptState)
    {
        this.conceptState = conceptState;
    }
    public boolean isEntityOfConceptChanged()
    {
        return entityOfConceptChanged;
    }
    public void setEntityOfConceptChanged(boolean entityOfConceptChanged)
    {
        this.entityOfConceptChanged = entityOfConceptChanged;
    }
}
