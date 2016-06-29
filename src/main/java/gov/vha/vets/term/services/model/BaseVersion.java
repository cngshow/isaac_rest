package gov.vha.vets.term.services.model;



public abstract class BaseVersion extends BaseEntity
{
    protected Version version;
    protected boolean active = true;
    public abstract Version getVersion();
    protected ChangeGroup changeGroup;

    public abstract boolean getActive();
    
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    public void setVersion(Version version)
    {
        this.version = version;
    }
    

    public Object clone() throws CloneNotSupportedException
    {
        BaseVersion newBase = (BaseVersion)super.clone();

        if (this.version != null)
            newBase.version = (Version)this.version.clone();

        return newBase;
    }
}
