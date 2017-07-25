package gov.vha.isaac.soap.model;

public abstract class BaseEntity extends Base
{

    protected long entityId;

    public BaseEntity()
    {
        // TODO Auto-generated constructor stub
    }

    public abstract long getEntityId();

    public void setEntityId(long entityId)
    {
        this.entityId = entityId;
    }
}
