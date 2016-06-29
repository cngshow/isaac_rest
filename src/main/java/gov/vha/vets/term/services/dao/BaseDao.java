package gov.vha.vets.term.services.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Query;

public class BaseDao
{
    static final int PARAMETER_LIST_MAX = 1000; 

	@SuppressWarnings("unchecked")
	protected static List executeQuery(Query sessionQuery, String parameter, Collection parameterList)
	{
		HashSet hashSet = new HashSet(parameterList);  // remove duplicates from parameterList
		List newParameterList = new ArrayList(hashSet);
	    List completeList = new ArrayList();
	    int listQueryCounter = 0;
	    int toRange;
	    
	    do 
	    {
	    	int fromRange = listQueryCounter * PARAMETER_LIST_MAX;
	    	toRange = fromRange + PARAMETER_LIST_MAX;
	    	if (toRange > newParameterList.size())
	    	{
	    		toRange = newParameterList.size();
	    	}
	        // get concept for the given code
	        List list = sessionQuery.setParameterList(parameter, newParameterList.subList(fromRange, toRange)).list();
	        completeList.addAll(list);
	        listQueryCounter++;
	    }
	    while (toRange < newParameterList.size());
	    
	    return completeList;
	}

	@SuppressWarnings("unchecked")
	protected static List executeQuery(Query sessionQuery, String parameter1, Collection parameter1List, String parameter2, Collection parameter2List)
	{
		HashSet hashSet1 = new HashSet(parameter1List);  // remove duplicates from parameter1List
		List newParameter1List = new ArrayList(hashSet1);
		HashSet hashSet2 = new HashSet(parameter2List);  // remove duplicates from parameter2List
		List newParameter2List = new ArrayList(hashSet2);
	    List completeList = new ArrayList();
	    int toRange1, toRange2;
	    
	    int list1QueryCounter = 0;
	    do 
	    {
	    	int fromRange1 = list1QueryCounter * PARAMETER_LIST_MAX;
	    	toRange1 = fromRange1 + PARAMETER_LIST_MAX;
	    	if (toRange1 > newParameter1List.size())
	    	{
	    		toRange1 = newParameter1List.size();
	    	}
		    int list2QueryCounter = 0;
		    do 
		    {
		    	int fromRange2 = list2QueryCounter * PARAMETER_LIST_MAX;
		    	toRange2 = fromRange2 + PARAMETER_LIST_MAX;
		    	if (toRange2 > newParameter2List.size())
		    	{
		    		toRange2 = newParameter2List.size();
		    	}
		        // get concept for the given code
		        List list = sessionQuery.setParameterList(parameter1, newParameter1List.subList(fromRange1, toRange1)).setParameterList(parameter2, newParameter2List.subList(fromRange2, toRange2)).list();
		        completeList.addAll(list);
		        list2QueryCounter++;
		    }
		    while (toRange2 < newParameter2List.size());
	        list1QueryCounter++;
	    }
	    while (toRange1 < newParameter1List.size());
	    
	    return completeList;
	}

	@SuppressWarnings("unchecked")
	protected static int executeUpdate(Query sessionQuery, String parameter, Collection parameterList)
	{
		HashSet hashSet = new HashSet(parameterList);
		List newParameterList = new ArrayList(hashSet);
	    int completeCount = 0;
	    int listQueryCounter = 0;
	    int toRange;
	    
	    do 
	    {
	    	int fromRange = listQueryCounter * PARAMETER_LIST_MAX;
	    	toRange = fromRange + PARAMETER_LIST_MAX;
	    	if (toRange > newParameterList.size())
	    	{
	    		toRange = newParameterList.size();
	    	}
	        // get concept for the given code
	        int count = sessionQuery.setParameterList(parameter, newParameterList.subList(fromRange, toRange)).executeUpdate();
	        completeCount += count;
	        listQueryCounter++;
	    }
	    while (toRange < newParameterList.size());
	    
	    return completeCount;
	}
}