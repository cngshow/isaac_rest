package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ConceptStateDao;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.State;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ConceptStateDelegate
{

    public static void createOrUpdate(long conceptEntityId, State state)
    {
        ConceptState conceptState = ConceptStateDao.get(conceptEntityId);
        if (conceptState == null)
        {
            conceptState = new ConceptState(conceptEntityId, state);
            ConceptStateDao.save(conceptState);
        }
        else
        {
            if (state != null)
			{
				if (conceptState.getState() == null || conceptState.getState().getName().equals(state.getName()) == false)
				{
					conceptState.setState(state);
					ConceptStateDao.save(conceptState);
				}
			}
        }
    }
    
    public static void update(Collection<Long> conceptEntityIdList, State state)
    {
        ConceptStateDao.update(conceptEntityIdList, state);
    }
    
    public static ConceptState get(long conceptEntityId)
    {
        return ConceptStateDao.get(conceptEntityId);
    }

    /**
     * Get the list of concept states (if possible) for the following entity Ids
     * @param conceptEntityIds
     * @return
     */
    public static List<ConceptState> get(List<Long> conceptEntityIds)
    {
        return ConceptStateDao.get(conceptEntityIds);
    }
    
    public static void remove(Collection<Long> conceptEntityIdList)
    {
        ConceptStateDao.remove(conceptEntityIdList);
    }
    
    /** Retrieve the map of conceptStates given the ConceptEntityId's in this state.
	 * @param state
	 * @return
	 */
	public static HashMap<Long, ConceptState> getConceptStatesMap(Collection<Long> conceptEntityIds)
	{
		return ConceptStateDao.getConceptStatesMap(conceptEntityIds);
	}

    /** Return boolean whether there are ConceptState objects with the given State.
     * @param state
     * @return
     */
    public static boolean isStateExist(State state)
    {
        
        return ConceptStateDao.isStateExist(state);
    }
    
    /**
     * Remove any concept states that do not have entities in the authoring version
     * @return
     * @throws STSException
     */
    public static int removeInconsistentConceptStates() throws STSException
    {
        return ConceptStateDao.removeInconsistentConceptStates();
    }
    
}
