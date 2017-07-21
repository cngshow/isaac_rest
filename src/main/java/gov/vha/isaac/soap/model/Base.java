package gov.vha.isaac.soap.model;

import java.io.Serializable;


public abstract class Base implements Serializable, Cloneable
{
    protected long id;
    
    public abstract long getId();
    
    protected void setId(long id)
    {
        this.id = id;
    }
    
    public Object clone() throws CloneNotSupportedException
    {
        Base newBase = (Base)super.clone();

        return newBase;
    }
}
