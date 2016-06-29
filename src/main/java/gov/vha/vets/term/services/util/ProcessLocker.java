package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.exception.STSProcessLockedException;

import java.util.Date;
import java.util.HashMap;

public class ProcessLocker
{
    static ProcessLocker locker = null;
    HashMap<String, Date> lockMap = new HashMap<String, Date>();
    
    private ProcessLocker()
    {
    }
    
    public static synchronized ProcessLocker getInstance()
    {
        if (locker == null)
        {
            locker = new ProcessLocker();
        }
        return locker;
    }
    
    public synchronized boolean lockIt(String key) throws STSProcessLockedException
    {
        Date lockedDate = (Date)lockMap.get(key);
        if (lockedDate != null)
        {
            throw new STSProcessLockedException("Already Locked "+key,lockedDate);
        }
        else
        {
            lockMap.put(key, new Date());
        }
        return true;
    }
    
    public synchronized void unlockIt(String key)
    {
        lockMap.remove(key);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {

    }

}
