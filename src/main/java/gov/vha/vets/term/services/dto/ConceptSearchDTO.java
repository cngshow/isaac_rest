package gov.vha.vets.term.services.dto;

import java.io.Serializable;

/**
 * 
 * @author vhaislnobleb
 *
 */
public class ConceptSearchDTO implements Serializable, Comparable
{
    private long conceptEntityId;
    private String conceptName;
    private String preferredName;
    boolean active;

    /**
     * @param conceptEntityId
     * @param conceptName
     * @param preferredName
     * @param active
     */
    public ConceptSearchDTO(int conceptEntityId, String conceptName, String preferredName, boolean active)
    {
        this.conceptEntityId = conceptEntityId;
        this.conceptName = conceptName;
        this.preferredName = preferredName;
        this.active = active;
    }
    
    public ConceptSearchDTO()
    {
        
    }

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /**
     * @return the conceptEntityId
     */
    public long getConceptEntityId()
    {
        return conceptEntityId;
    }

    /**
     * @param conceptEntityId the conceptEntityId to set
     */
    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }

    /**
     * @return the conceptName
     */
    public String getConceptName()
    {
        return conceptName;
    }

    /**
     * @param conceptName the conceptName to set
     */
    public void setConceptName(String conceptName)
    {
        this.conceptName = conceptName;
    }

    /**
     * @return the preferredName
     */
    public String getPreferredName()
    {
        return preferredName;
    }

    /**
     * @param preferredName the preferredName to set
     */
    public void setPreferredName(String preferredName)
    {
        this.preferredName = preferredName;
    }

    public int compareTo(Object o)
    {
        int result = -1;
        if (o instanceof ConceptSearchDTO)
        {
            result = ((ConceptSearchDTO)o).getPreferredName().compareTo(this.getPreferredName());
        }
        return result;
    }
}
