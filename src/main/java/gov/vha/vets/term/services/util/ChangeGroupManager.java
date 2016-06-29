package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.model.ChangeGroup;


/**
 * @author VHAISLNOBLEB
 */
public class ChangeGroupManager
{
    private static ChangeGroupManager changeGroupManager;
    
	@SuppressWarnings("unchecked")
	private ThreadLocal threadLocal = null;

    /**
     * Default constructor.
     */
    @SuppressWarnings("unchecked")
	private ChangeGroupManager()
    {
        threadLocal = new ThreadLocal();
    }
    
	public static synchronized ChangeGroupManager getInstance()
	{
		if (changeGroupManager == null)
		{
			changeGroupManager = new ChangeGroupManager();
		}
		
		return changeGroupManager;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
    

	/**
	 * @return the changeGroup
	 */
	public ChangeGroup getChangeGroup()
	{
		ChangeGroup changeGroup = null;
		
		if (threadLocal != null)
		{
			changeGroup = (ChangeGroup) threadLocal.get();
		}
		
		return changeGroup;
	}

	/**
	 * @param sourceName the sourceName to set
	 */
	@SuppressWarnings("unchecked")
	public void setChangeGroup(ChangeGroup changeGroup)
	{
		threadLocal.set(changeGroup);
	}
	
	@SuppressWarnings("unchecked")
	public void clear()
	{
		threadLocal.set(null);
	}
}
