/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Version;
import java.io.Serializable;

/**
 * @author VHAISLMURDOH
 *
 */
public class SubsetDetailDTO implements Serializable
{
    private String name;
    private Version version;
    private long entityId;
    private boolean active;

    public SubsetDetailDTO(String subsetName, Version version, long entityId, boolean active)
    {
        this.name = subsetName;
        this.version = version;
        this.entityId = entityId;
        this.active = active;
    }

    /**
     * @return the active
     */
    public boolean getActive()
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
    public long getEntityId()
    {
        return entityId;
    }

    /**
     * @param conceptEntityId the conceptEntityId to set
     */
    public void setEntityId(int entityId)
    {
        this.entityId = entityId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String subsetName)
    {
        this.name = subsetName;
    }

    /**
     * @return the version
     */
    public Version getVersion()
    {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Version version)
    {
        this.version = version;
    }
}
