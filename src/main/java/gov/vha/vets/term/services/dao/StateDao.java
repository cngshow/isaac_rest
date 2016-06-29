package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class StateDao
{
    public static void delete(Session session, State state)
    {
        session.delete(state);
    }

    public static State get(String stateName) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();
        State state = (State)session.createCriteria(State.class).add(Restrictions.eq("name", stateName)).uniqueResult();
        if (state == null)
        {
        	throw new STSNotFoundException("State name '"+stateName+"' is not found.");
        }
        return state;
    }
    
    public static State getByType(String type) throws STSNotFoundException
    {
        Session session = HibernateSessionFactory.currentSession();
        State state = (State)session.createCriteria(State.class).add(Restrictions.eq("type", type)).uniqueResult();
        if (state == null)
        {
        	throw new STSNotFoundException("In getByType the State type '"+type+"' is not found.");
        }
        return state;
    }
    
    public static List<State> getStates()
    {
        Session session = HibernateSessionFactory.currentSession();
        List<State> stateList = session.createQuery("from State").list();

        return stateList;
    }

	public static State getById(long stateId)
	{
        Session session = HibernateSessionFactory.currentSession();
        State state = (State)session.createCriteria(State.class).add(Restrictions.eq("id", stateId)).uniqueResult();
        
        return state;
	}
}
