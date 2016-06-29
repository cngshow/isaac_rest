package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.TDELoaderDao;
import gov.vha.vets.term.services.dto.TdeUpdateVuidDTO;
import gov.vha.vets.term.services.dto.delta.ConceptDelta;
import gov.vha.vets.term.services.dto.delta.DesignationDelta;
import gov.vha.vets.term.services.dto.delta.DesignationPropertyDelta;
import gov.vha.vets.term.services.dto.delta.PropertyDelta;
import gov.vha.vets.term.services.dto.delta.RelationshipDelta;
import gov.vha.vets.term.services.dto.delta.SubsetDelta;
import gov.vha.vets.term.services.dto.delta.SubsetRelationshipDelta;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.ChangeGroup;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Version;
import gov.vha.vets.term.services.util.ChangeGroupManager;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ServicesTDEImportDelegate
{
    protected static final String VHAT_CODESYSTEM_NAME = "VHAT";

    public static final String TDE_SYSTEM = "TDE";
    public static final String VTS_SYSTEM = "VTS";
    private static final int FLUSH_SIZE = 30;

    protected Transaction tx;

    static Logger log = Logger.getLogger(ServicesTDEImportDelegate.class.getPackage().getName());

    Version version = null;
    CodeSystem codeSystem = null;
    
    protected State initialState = null;
    private HashSet<String> allowableConceptUpdateSet;

    public ServicesTDEImportDelegate()
    {
    }

    /**
     * Make changes here
     * 
     * @throws STSNotFoundException
     */
    protected void setupImport() throws STSNotFoundException, Exception
    {
    	// clear out allowableConceptUpdateSet at beginning of each import
        allowableConceptUpdateSet = new HashSet<String>();

        codeSystem = CodeSystemDelegate.get(VHAT_CODESYSTEM_NAME);
        if (codeSystem == null)
        {
        	// close the session so the database will be initialized
            Session session = HibernateSessionFactory.currentSession();
            if (session != null)
            {
            	session.disconnect();
            }
            throw new STSNotFoundException(VHAT_CODESYSTEM_NAME + "  could not be found");
        }

        version = VersionDelegate.getAuthoring();
        if (version == null)
        {
            throw new STSNotFoundException(HibernateSessionFactory.AUTHORING_VERSION_NAME + "  could not be found");
        }

        initialState = StateDelegate.getByType(State.INITIAL);
        if (initialState == null)
        {
            throw new STSNotFoundException(State.INITIAL + " state could not be found");
        }
    }

    /**
     * don't make changes to this method, make the changes to the
     * @param databaseName
     * @param databaseLink
     * 
     * @throws Exception
     */
    public synchronized void importChanges(String databaseName, String databaseLink) throws Exception
    {
        
        try
        {
            Session session = HibernateSessionFactory.currentSession();
            session.clear();

            tx = session.beginTransaction();
            TDELoaderDao.updateVUIDsInTDE(session.connection(), databaseName, databaseLink);
            
            ChangeGroupManager.getInstance().setChangeGroup(new ChangeGroup(ChangeGroup.SourceName.TDE.toString()));

            setupImport();
            Connection connection = session.connection();
            // if it already exists then it won't throw and error
            // createPropertyTypes(TDELoaderDao.getPropertyTypes(connection));
            // createRelationshipTypes(TDELoaderDao.getRelationshipTypes(connection));
            // createDesignationTypes(TDELoaderDao.getDesignationTypes(HibernateSessionFactory.currentSession().connection()));

            log.info("updating concepts");
            Map<String, List<ConceptDelta>> conceptMap = TDELoaderDao.getConceptDeltas(connection, databaseName, databaseLink);
            updateConcepts(conceptMap);

            log.info("updating designations");
            Map<String, List<DesignationDelta>> designationMap = TDELoaderDao.getDesignationDeltas(connection,databaseName, databaseLink);
            updateDesignations(designationMap);

            // make sure there are no duplicate VUIDs in entire database
            log.info("checking for duplicate VUIDs");
            session.flush();
            session.clear();
            CodedConceptDelegate.checkForDuplicateVUIDs();

            // make sure there are no duplicate preferred designation types for each concept
            log.info("checking for duplicate preferred designation types");
            CodeSystem vhatCodeSystem = CodeSystemDelegate.get(VHAT_CODESYSTEM_NAME);
            CodeSystemDelegate.verifyPreferredDesignationUnique(vhatCodeSystem);

            log.info("updating designation properties");
            Map<String, List<DesignationPropertyDelta>> map = TDELoaderDao.getDesignationPropertyDeltas(connection, databaseName, databaseLink);
            updateDesignationProperties(map);
           
            log.info("updating relationships");
            Map<String, List<RelationshipDelta>> updateRelationshipsList = TDELoaderDao.getRelationshipDeltas(connection, databaseName, databaseLink);
            updateRelationshipsList.putAll(TDELoaderDao.getParentChildRelationshipDeltas(connection, databaseName, databaseLink));
            updateRelationships(updateRelationshipsList);

            log.info("updating properties");
            Map<String, List<PropertyDelta>> propertyMap = TDELoaderDao.getPropertyDeltas(connection, databaseName, databaseLink);
            updateProperties(propertyMap);

            log.info("updating subsets");
            Map<String, List<SubsetDelta>> subsetDeltaMap = TDELoaderDao.getSubsetDeltas(connection, databaseName, databaseLink);
            updateSubsets(subsetDeltaMap);

            log.info("updating subsetRelationships");
            Map<String, List<SubsetRelationshipDelta>> subsetRelationshipList = TDELoaderDao.getSubsetRelationshipDeltas(connection, databaseName, databaseLink);
            updateSubsetRelationships(subsetRelationshipList);

            // make sure we remove any entries that are incorrect - must do a flush here
            log.info("removing inconsistent concept states");
            session.flush();
            session.clear();
            ConceptStateDelegate.removeInconsistentConceptStates();
            
            log.info("ready to commit transaction");
            tx.commit();
            log.info("import transaction committed - done");

            session.clear();
        }
        catch (Exception e)
        {
            // do each rollback in separate try/catch
            if (tx != null)
            {
                tx.rollback();
            }
            log.error(e);
            throw e;
        }
        finally
        {
        	HibernateSessionFactory.disconnect();
        }
    }

    protected void updateRelationships(Map<String, List<RelationshipDelta>> updateRelationshipsList) throws Exception
    {
        try
        {
        	int updateCount = 0;
            for (Iterator<String> iterator = updateRelationshipsList.keySet().iterator(); iterator.hasNext();)
            {
                String key = iterator.next();
                List<RelationshipDelta> relationshipDeltaList = updateRelationshipsList.get(key);

                if (relationshipDeltaList.size() == 2
                        && !relationshipDeltaList.get(0).getSystem().equalsIgnoreCase(relationshipDeltaList.get(1).getSystem()))
                {
                    RelationshipDelta oldDelta = null;
                    RelationshipDelta newDelta = null;
                    for (RelationshipDelta delta : relationshipDeltaList)
                    {
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                    checkAllowableUpdateToConcept(newDelta.getSourceCode());
                    ConceptRelationshipDelegate.update(codeSystem, newDelta.getSourceCode(), newDelta.getRelationshipType(),
                            oldDelta.getTargetCode(), newDelta.getTargetCode(), initialState);
                }
                else
                {
                    for (RelationshipDelta delta : relationshipDeltaList)
                    {
                        checkAllowableUpdateToConcept(delta.getSourceCode());
                        if (delta.getSystem().equals("TDE"))
                        {
                            // create relationship
                            ConceptRelationshipDelegate.create(codeSystem, delta.getSourceCode(), delta.getTargetCode(), delta.getRelationshipType(), initialState);
                        }
                        else
                        {
                            ConceptRelationshipDelegate.activate(codeSystem, delta.getSourceCode(), delta.getTargetCode(), delta
                                    .getRelationshipType(), initialState, false);
                        }
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateRelationships: "+e.getMessage(), e);
        }
    }

    protected void updateConcepts(Map<String, List<ConceptDelta>> conceptMap) throws Exception
    {
        try
        {
            Set<String> set = conceptMap.keySet();
            Iterator<String> setIter = set.iterator();

        	int updateCount = 0;
            while (setIter.hasNext())
            {
                String key = setIter.next();
                List<ConceptDelta> conceptDeltaList = conceptMap.get(key);

                if (conceptDeltaList.size() == 2)
                {
                    ConceptDelta newDelta = null;
                    ConceptDelta oldDelta = null;
                    for (ConceptDelta delta : conceptDeltaList)
                    {
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                    // check to make sure we don't update a concept with an invalid name
                    if (newDelta.getName() == null)
                    {
                    	throw new STSException("Coded concept with code: " + newDelta.getCode()
                    			+ " could not be imported due to invalid name. Error could be caused by incorrect formatting of the [C]");
                    }
                    else if (newDelta.getName().indexOf('|') >= 0)
                    {
                    	throw new STSException("Coded concept: " + newDelta.getName() + " with code: " + newDelta.getCode() +
                    			" could not be imported due to invalid pipe character (|) in its name.");
                    }

                    checkAllowableUpdateToConcept(newDelta.getCode());
                    CodedConceptDelegate.update(codeSystem, newDelta.getCode(), oldDelta.getName(), newDelta.getName(), oldDelta.isActive(), newDelta
                            .isActive(), oldDelta.getVuid(), newDelta.getVuid(), initialState);
                }
                else
                // we have only one of TDE delta or VTS delta in the list
                {
                    for (ConceptDelta delta : conceptDeltaList)
                    {
                        if (delta.getSystem().equals(ServicesTDEImportDelegate.TDE_SYSTEM))
                        {
                            // check to make sure we don't update a concept with an invalid name
                            if (delta.getName() == null)
                            {
                            	throw new STSException("Coded concept with code: " + delta.getCode()
                            			+ " could not be imported due to invalid name. Error could be caused by incorrect formatting of the [C]");
                            }
                            else if (delta.getName().indexOf('|') >= 0)
                            {
                            	throw new STSException("Coded concept: " + delta.getName() + " with code: " + delta.getCode() +
                            			" could not be imported due to invalid pipe character (|) in its name.");
                            }
                            
                        	allowableConceptUpdateSet.add(delta.getCode());  // brand new concept - add code to allowableConceptUpdateSet
                            CodedConceptDelegate.create(delta.getCode(), delta.getName(), delta.getVuid(), delta.isActive(), initialState);
                        }
                        else
                        {
                            checkAllowableUpdateToConcept(delta.getCode());
                            CodedConceptDelegate.inactivate(codeSystem, delta.getCode(), initialState);
                        }
                    }
                }
                
                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateConcepts: "+e.getMessage(), e);
        }
    }

    /**
     * Update designations
     * 
     * @param designationMap
     * @throws Exception
     */
    protected void updateDesignations(Map<String, List<DesignationDelta>> designationMap) throws Exception
    {
        try
        {
        	int updateCount = 0;
            for (Iterator<String> iter = designationMap.keySet().iterator(); iter.hasNext();)
            {
                String key = iter.next();

                List<DesignationDelta> designations = designationMap.get(key);


                if ((designations.size() == 2) 
                		&& (!designations.get(0).getSystem().equalsIgnoreCase(designations.get(1).getSystem())))
                {
                    // this is an update
                    DesignationDelta oldDelta = null;
                    DesignationDelta newDelta = null;
                    
                    for (Iterator<DesignationDelta> iterator = designations.iterator(); iterator.hasNext();)
                    {
                        DesignationDelta delta = iterator.next();

                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                    checkAllowableUpdateToConcept(newDelta.getConceptCode());
                    DesignationDelegate.update(codeSystem, newDelta.getConceptCode(), newDelta.getCode(), newDelta.getType(), oldDelta.getName(), newDelta.getName(),
                            newDelta.getVuid(), newDelta.isActive(), initialState);
                    if (oldDelta.getConceptCode().equals(newDelta.getConceptCode()) == false)
                    {
                        checkAllowableUpdateToConcept(newDelta.getConceptCode());
                        DesignationRelationshipDelegate.update(codeSystem, newDelta.getCode(), oldDelta.getConceptCode(), newDelta.getConceptCode(), initialState);
                    }
                }
                else
                {
                    for (DesignationDelta delta : designations)
                    {
                        checkAllowableUpdateToConcept(delta.getConceptCode());

                        // insert or delete
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            DesignationDelegate.create(codeSystem, delta.getCode(), delta.getConceptCode(), delta.getName(), delta.getVuid(), delta.getType(), delta
                                    .isActive(), initialState);
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            DesignationDelegate.delete(codeSystem, delta.getConceptCode(), delta.getCode());
                        }
                        else
                        {
                            throw new Exception("Invalid system of '" + delta.getSystem() + "'");
                        }
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateDesignations: "+e.getMessage(), e);
        }
    }

    /**
     * Update subset relationships
     * 
     * @param subsetRelationshipList
     * @throws Exception
     */
    protected void updateSubsetRelationships(Map<String, List<SubsetRelationshipDelta>> subsetRelationshipMap) throws Exception
    {
        try
        {
        	int updateCount = 0;
            for (Iterator<String> iterator = subsetRelationshipMap.keySet().iterator(); iterator.hasNext();)
            {
                String key = iterator.next();
                List<SubsetRelationshipDelta> deltaList = subsetRelationshipMap.get(key);

                for (SubsetRelationshipDelta delta : deltaList)
                {
                	CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(delta.getDesignationCode());
                    checkAllowableUpdateToConcept(codedConcept.getCode());
                    if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                    {
                        SubsetRelationshipDelegate.create(codeSystem, delta.getSubsetName(), codedConcept.getCode(), delta.getDesignationCode(),  initialState);
                    }
                    else 
                    {
                        SubsetRelationshipDelegate.inactivate(codeSystem, delta.getSubsetName(), codedConcept.getCode(), delta.getDesignationCode(), initialState);
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateSubsetRelationships: "+e.getMessage(), e);
        }
    }

    protected void updateProperties(Map<String, List<PropertyDelta>> propertyMap) throws Exception
    {
        try
        {
        	int updateCount = 0;
            for (Iterator<String> iterator = propertyMap.keySet().iterator(); iterator.hasNext();)
            {
                String key = iterator.next();
                List<PropertyDelta> deltaList = propertyMap.get(key);

                if (deltaList.size() == 2 && !deltaList.get(0).getSystem().equalsIgnoreCase(deltaList.get(1).getSystem()))
                {
                    PropertyDelta oldDelta = null;
                    PropertyDelta newDelta = null;
                    for (PropertyDelta delta : deltaList)
                    {
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                    checkAllowableUpdateToConcept(oldDelta.getCode());
                    PropertyDelegate.updateVHAT(codeSystem, oldDelta.getCode(), oldDelta.getType(), oldDelta.getValue(), newDelta.getValue(), initialState);
                }
                else
                {
                    for (PropertyDelta delta : deltaList)
                    {
                        checkAllowableUpdateToConcept(delta.getCode());
                        if (delta.getSystem().equals("TDE"))
                        {
                            PropertyDelegate.createVHAT(codeSystem, delta.getCode(), delta.getType(), delta.getValue(), initialState);
                        }
                        else
                        {
                            PropertyDelegate.inactivateVHAT(codeSystem, delta.getCode(), delta.getType(), delta.getValue(), initialState);
                        }
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateProperties: "+e.getMessage(), e);
        }
    }

    protected void updateDesignationProperties(Map<String, List<DesignationPropertyDelta>> map) throws Exception
    {
        try
        {
        	int updateCount = 0;
            for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();)
            {
                String key = iterator.next();
                List<DesignationPropertyDelta> deltaList = map.get(key);

                if (deltaList.size() == 2 && !deltaList.get(0).getSystem().equalsIgnoreCase(deltaList.get(1).getSystem()))
                {
                    DesignationPropertyDelta oldDelta = null;
                    DesignationPropertyDelta newDelta = null;
                    for (DesignationPropertyDelta delta : deltaList)
                    {
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                	CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(oldDelta.getCode());
                    checkAllowableUpdateToConcept(codedConcept.getCode());
                    DesignationPropertyDelegate.update(codeSystem, codedConcept.getCode(), oldDelta.getCode(), oldDelta.getType(), oldDelta.getValue(), newDelta.getValue(),  initialState);
                }
                else
                {
                    for (DesignationPropertyDelta delta : deltaList)
                    {
                    	CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(delta.getCode());
                        checkAllowableUpdateToConcept(codedConcept.getCode());
                        if (delta.getSystem().equals("TDE"))
                        {
                            DesignationPropertyDelegate.create(codeSystem, codedConcept.getCode(), delta.getCode(), delta.getType(), delta.getValue(), initialState);
                        }
                        else
                        {
                            DesignationPropertyDelegate.inactivate(codeSystem, codedConcept.getCode(), delta.getCode(), delta.getType(), delta.getValue(), initialState);
                        }
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateDesignationProperties: "+e.getMessage(), e);
        }

    }

    protected void updateSubsets(Map<String, List<SubsetDelta>> subsetMap) throws Exception
    {
        try
        {
            Set<String> set = subsetMap.keySet();
            Iterator<String> setIter = set.iterator();

            int updateCount = 0;
            while (setIter.hasNext())
            {
                String key = setIter.next();
                List<SubsetDelta> subsetDeltaList = subsetMap.get(key);

                if (subsetDeltaList.size() == 2)
                {
                    SubsetDelta newDelta = null;
                    SubsetDelta oldDelta = null;
                    for (SubsetDelta delta : subsetDeltaList)
                    {
                        if (delta.getSystem().equals(TDELoaderDao.TDE_SYSTEM))
                        {
                            newDelta = delta;
                        }
                        else if (delta.getSystem().equals(TDELoaderDao.VTS_SYSTEM))
                        {
                            oldDelta = delta;
                        }
                    }
                    if (newDelta.getVuid() != oldDelta.getVuid())
                    {
                    	throw new STSException("The VUID for subset: " + newDelta.getName() + " is not allowed to be changed.");
                    }
                    if (newDelta.getStatus() != oldDelta.getStatus())
                    {
                    	throw new STSException("System does not allow the changing of status on subset: " + newDelta.getName());
                    }
                    checkAllowableUpdateToConcept(newDelta.getCode());
                    SubsetDelegate.update(newDelta.getCode(), newDelta.getName(), newDelta.getStatus());
                }
                else
                // we have only one of TDE delta or VTS delta in the list
                {
                    for (SubsetDelta delta : subsetDeltaList)
                    {
                        checkAllowableUpdateToConcept(delta.getCode());
                        if (delta.getSystem().equals(ServicesTDEImportDelegate.TDE_SYSTEM))
                        {
                            SubsetDelegate.create(delta.getCode(), delta.getName(), delta.getVuid(), delta.getStatus());
                        }
                        else
                        {
                        	throw new STSException("System does not allow the removal of subset: " + delta.getName());
                            //SubsetDelegate.inactivate(delta.getCode());
                        }
                    }
                }

                if (++updateCount % FLUSH_SIZE == 0)
                {
                    HibernateSessionFactory.currentSession().flush();
                    HibernateSessionFactory.currentSession().clear();
                }
            }
        }
        catch (STSUpdateException e)
        {
        	// throw exception without wrapping so message gets displayed to user
        	throw e;
        }
        catch (Exception e)
        {
            throw new Exception("failed in updateSubsets: "+e.getMessage(), e);
        }
    }
    
    private void checkAllowableUpdateToConcept(String code) throws STSException
    {
    	// if code is in set then we allow the concept to be changed
    	if (allowableConceptUpdateSet.contains(code))
    	{
    		return;
    	}

    	CodedConcept codedConcept = CodedConceptDelegate.get(codeSystem, code);
    	if (codedConcept != null)
    	{
        	ConceptState conceptState = ConceptStateDelegate.get(codedConcept.getEntityId());
        	if (conceptState != null)
        	{
        		if (!conceptState.getState().getType().equals(State.INITIAL))
        		{
        			// concept is not allowed to be changed - throw an error
        			throw new STSUpdateException("Cannot import change to concept with code: " + code + " that is in state: " + conceptState.getState().getName());
        		}
        	}
    	}
    	
    	allowableConceptUpdateSet.add(code);
    }
    
    public static final void main(String[] args) throws Exception
    {
    	ServicesTDEImportDelegate loaderDelegate = new ServicesTDEImportDelegate();
        loaderDelegate.importChanges("sample1", null);
    }
}
