package gov.vha.isaac.soap.services.dto.config;

import java.io.Serializable;

public class MapSetConfig implements Serializable
{
    private String name; 
    private long vuid;
    private boolean gemContent;
    private boolean webServiceAccessible;
    private String sourceType;
    private String targetType;
    private boolean found;
    
	public MapSetConfig(boolean gemContent, boolean webServiceAccessible, String sourceType, String targetType)
	{
		super();
		this.name = null;
		this.vuid = 0L;
		this.gemContent = gemContent;
		this.webServiceAccessible = webServiceAccessible;
		this.sourceType = sourceType;
		this.targetType = targetType;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public long getVuid()
	{
		return vuid;
	}
	public void setVuid(long vuid)
	{
		this.vuid = vuid;
	}
	public boolean isGemContent()
	{
		return gemContent;
	}
	public void setGemContent(boolean gemContent)
	{
		this.gemContent = gemContent;
	}
	public boolean isWebServiceAccessible()
	{
		return webServiceAccessible;
	}
	public void setWebServiceAccessible(boolean webServiceAccessible)
	{
		this.webServiceAccessible = webServiceAccessible;
	}
	public String getSourceType()
	{
		return sourceType;
	}
	public void setSourceType(String sourceType)
	{
		this.sourceType = sourceType;
	}
	public String getTargetType()
	{
		return targetType;
	}
	public void setTargetType(String targetType)
	{
		this.targetType = targetType;
	}
	public boolean isFound()
	{
		return found;
	}
	public void setFound(boolean found)
	{
		this.found = found;
	}
}
