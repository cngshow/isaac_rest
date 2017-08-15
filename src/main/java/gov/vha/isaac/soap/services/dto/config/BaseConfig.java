/*
 * Created on Jan 11, 2006
 *
 */
package gov.vha.isaac.soap.services.dto.config;

import java.io.Serializable;

/**
 * @author BORG4
 *  
 */
public class BaseConfig implements Serializable
{
    protected boolean isAllowEmpty;
    protected boolean isList;
    protected String name;

    /**
     * sole constructor - create a property filter
     */
    public BaseConfig(String name, boolean isAllowEmpty, boolean isList)
    {
        this.name = name;
        this.isAllowEmpty = isAllowEmpty;
        this.isList = isList;
    }

    /**
     * @return Returns the isAllowEmpty.
     */
    public boolean isAllowEmpty()
    {
        return isAllowEmpty;
    }
    /**
     * @param isAllowEmpty The isAllowEmpty to set.
     */
    public void setAllowEmpty(boolean isAllowEmpty)
    {
        this.isAllowEmpty = isAllowEmpty;
    }
    /**
     * @return Returns the isList.
     */
    public boolean isList()
    {
        return isList;
    }
    /**
     * @param isList The isList to set.
     */
    public void setList(boolean isList)
    {
        this.isList = isList;
    }
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }
}
