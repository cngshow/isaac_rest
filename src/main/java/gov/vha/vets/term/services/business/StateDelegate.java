package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.StateDao;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.State;

import java.util.List;

public class StateDelegate
{
    public static State get(String name) throws STSNotFoundException, STSException
    {
        return StateDao.get(name);
    }

    public static State getByType(String type) throws STSException
    {
        return StateDao.getByType(type);
    }

    public static List<State> getStates() throws STSException
    {
        return StateDao.getStates();
    }

	public static State getById(long stateId) throws STSException
	{
        return StateDao.getById(stateId);
	}
}
