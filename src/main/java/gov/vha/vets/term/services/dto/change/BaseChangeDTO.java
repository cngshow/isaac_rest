package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.BaseVersion;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

public abstract class BaseChangeDTO
{
    protected BaseVersion previous;
    protected BaseVersion recent;
    protected long versionId = HibernateSessionFactory.AUTHORING_VERSION_ID;

    public enum ChangeType { NEW, CHANGED, NONE }
    
    public enum StatusType { ACTIVATED, INACTIVATED, NONE }
    
    public abstract boolean isChanged();
    
    public ChangeType getChangeType()
    {
        ChangeType type = ChangeType.NONE;
        if (previous != null)
        {
            if (isChanged())
            {
                type = ChangeType.CHANGED;
            }
            else
            {
                type = ChangeType.NONE;
            }
        }
        else
        {
            if (recent.getVersion().getId() == getVersionId())
            {
                type = ChangeType.NEW;
            }
        }
        return type;
    }
    
    public StatusType getStatusType()
    {
        StatusType type = StatusType.NONE;

        if (previous != null && recent != null)
        {
            if (previous.getActive()  == true && recent.getActive() == false)
            {
                type = StatusType.INACTIVATED;
            }
            else if (previous.getActive() == false && recent.getActive() == true)
            {
                type = StatusType.ACTIVATED;
            }
            else
            {
                type = StatusType.NONE;
            }
        }
        return type;
    }

    public BaseVersion getPrevious()
    {
        return previous;
    }
    public void setPrevious(BaseVersion previous)
    {
        this.previous = previous;
    }
    public BaseVersion getRecent()
    {
        return recent;
    }
    public void setRecent(BaseVersion recent)
    {
        this.recent = recent;
    }
	public long getVersionId() {
		return versionId;
	}
	public void setVersionId(long versionId) {
		this.versionId = versionId;
	}
}
