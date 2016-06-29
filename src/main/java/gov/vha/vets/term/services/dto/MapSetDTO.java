/**
 * 
 */
package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Version;

import java.io.Serializable;

public class MapSetDTO implements Serializable, Cloneable
{
    private long entityId;
    private String name;
    private Long vuid; 
    private Version sourceVersion;
    private Version targetVersion;
    private int count = 0;
    
    
	public MapSetDTO(long entityId, String name, Version sourceVersion,
			Version targetVersion, int count)
	{
		super();
		this.entityId = entityId;
		this.name = name;
		this.sourceVersion = sourceVersion;
		this.targetVersion = targetVersion;
		this.count = count;
	}
	
	public MapSetDTO()
	{
	}

	public long getEntityId()
    {
        return entityId;
    }
    public void setEntityId(long id)
    {
        this.entityId = id;
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
    public void setName(String name)
    {
        this.name = name;
    }
	/**
	 * @return the count
	 */
	public int getCount()
	{
		return this.count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(int count)
	{
		this.count = count;
	}
	
	
	public Version getSourceVersion()
	{
		return sourceVersion;
	}
	public void setSourceVersion(Version sourceVersion)
	{
		this.sourceVersion = sourceVersion;
	}
	public Version getTargetVersion()
	{
		return targetVersion;
	}
	public void setTargetVersion(Version targetVersion)
	{
		this.targetVersion = targetVersion;
	}
	public Long getVuid()
	{
		return vuid;
	}
	public void setVuid(Long vuid)
	{
		this.vuid = vuid;
	}
	
	public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    } 
}
