package gov.vha.vets.term.services.util;

public class WeblogicDataSource
{
    protected String name;
    protected String url;
    protected String serverName;
    protected String sid;
    protected String userName;
    protected String password;

    
    public WeblogicDataSource()
    {
        super();
    }

    public WeblogicDataSource(String name, String url, String serverName,
            String sid, String userName, String password)
    {
        super();
        this.name = name;
        this.url = url;
        this.serverName = serverName;
        this.sid = sid;
        this.userName = userName;
        this.password = password;
    }
    
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getUrl()
    {
        return url;
    }
    public void setUrl(String url)
    {
        this.url = url;
    }
    public String getServerName()
    {
        return serverName;
    }
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }
    public String getSid()
    {
        return sid;
    }
    public void setSid(String sid)
    {
        this.sid = sid;
    }
    public String getUserName()
    {
        return userName;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }
    
}
