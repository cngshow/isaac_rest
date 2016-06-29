package gov.vha.vets.term.services.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class StringKeyObjectMap
{
    private static StringKeyObjectMap _instance = null;
    private HashMap<String, Object> objectMap = new HashMap<String, Object>();
    private static long saveTime = System.currentTimeMillis();
    private static final long MILLISECONDS_IN_ONE_DAY = 86400L * 1000;
    private static final long EXPIRATION_TIME = MILLISECONDS_IN_ONE_DAY * 3;
    
    private StringKeyObjectMap()
    {
    }
    
    public static synchronized StringKeyObjectMap getInstance()
    {
        if (_instance == null)
        {
        	_instance = new StringKeyObjectMap();
        }
        
    	long currentTime = System.currentTimeMillis();
    	if (saveTime + EXPIRATION_TIME < currentTime)
    	{
    		// expire contents of the objectMap after one day
    		_instance.objectMap.clear();
    		saveTime = currentTime;
    	}
        return _instance;
    }
    
    public synchronized Object getObject(String key)
    {
        return objectMap.get(key);
    }

    public synchronized void putObject(String key, Object type)
    {
        objectMap.put(key, type);
    }

    public synchronized void removeObjectsUsingKeyPrefix(String prefix)
    {
    	List<String> keyList = new ArrayList<String>();
    	Iterator<String> mapKeys = objectMap.keySet().iterator();
    	while (mapKeys.hasNext())
		{
			String mapKey = mapKeys.next();
			if (mapKey.startsWith(prefix))
			{
				keyList.add(mapKey);
			}
		}
    	for (String key : keyList)
		{
			objectMap.remove(key);
		}
    }
}
