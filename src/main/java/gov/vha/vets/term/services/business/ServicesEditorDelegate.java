package gov.vha.vets.term.services.business;

import java.io.IOException;

import gov.vha.vets.term.services.dao.ConceptRelationshipDao;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.exception.STSUpdateException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.CodedConcept;
import gov.vha.vets.term.services.model.ConceptRelationship;
import gov.vha.vets.term.services.model.ConceptState;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Property;
import gov.vha.vets.term.services.model.State;
import gov.vha.vets.term.services.model.Subset;
import gov.vha.vets.term.services.model.SubsetRelationship;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.model.Vuid;


public class ServicesEditorDelegate
{
	private static CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
	
	/**
	 * cannot use static initializer for the state, because if the db is cleared out, the state never gets recreated
	 * @return
	 */
	private static State getInitialState()
    {
		State initialState = null;
		try
		{
			initialState = StateDelegate.getByType(State.INITIAL);
		}
		catch (STSException e)
		{
			e.printStackTrace();
		}
		return initialState;
	}
	
    public static CodedConcept getConcept(String code)
    {
        return CodedConceptDelegate.get(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), code);
    }

    public static void updateConcept(String code, String newConceptName, Long newVuid, boolean newActive) throws STSException
    {
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        CodedConcept concept = CodedConceptDelegate.get(codeSystem, code);
        checkAllowableUpdateToConcept(concept.getEntityId());
        CodedConceptDelegate.update(codeSystem, concept.getCode(), concept.getName(), newConceptName, concept.getActive(), newActive, concept.getVuid(), newVuid, getInitialState());
    }

    private static void checkAllowableUpdateToConcept(long conceptEntityId) throws STSException
    {
        ConceptState conceptState = ConceptStateDelegate.get(conceptEntityId);
        if (conceptState != null)
        {
            if (!conceptState.getState().getType().equals(State.INITIAL) && !conceptState.getState().getType().equals(State.READY_TO_TEST))
            {
                // concept is not allowed to be changed - throw an error
                throw new STSUpdateException("Cannot update concept with entityId: " + conceptEntityId + " that is in state: " + conceptState.getState().getName());
            }
        }
    }
    
    private static void checkAllowableUpdateToConcept(String code) throws STSException
    {
    	CodedConcept codedConcept = CodedConceptDelegate.get(codeSystem, code);
    	
    	checkAllowableUpdateToConcept(codedConcept.getEntityId());
    }
    
    public static CodedConcept createConcept(String code, String newConceptName, Long newVuid, boolean newActive) throws STSException
    {
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        CodedConcept concept = CodedConceptDelegate.get(codeSystem, code);
        if (concept != null)
        {
            throw new STSException("Code: "+code+" already exists");
        }
        if (newVuid == null)
        {
            newVuid  = assignVuid();
        }
        return CodedConceptDelegate.create(code, newConceptName, newVuid, newActive, getInitialState());
    }
    
    public static void createRelationship(String sourceCode, String targetCode, String relationshipTypeName, boolean newActive) throws STSException
    {
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        ConceptRelationship existingRelationship = ConceptRelationshipDao.get(codeSystem, sourceCode, targetCode, relationshipTypeName);
        if (existingRelationship != null && existingRelationship.getActive())
        {
            throw new STSException("ConceptRelationship: sourceCode = "
                            + sourceCode + ", targetCode = "+ targetCode+" already exists");
        }
        ConceptRelationshipDelegate.create(codeSystem, sourceCode, targetCode, relationshipTypeName, StateDelegate.getByType(State.INITIAL));
    }
    
    public static void updateRelationship(String sourceCode, String oldTargetCode, String newTargetCode, String relationshipTypeName, boolean newActive) throws STSNotFoundException, STSException
    {
    	ConceptRelationship existingRelationship = ConceptRelationshipDao.get(codeSystem, sourceCode, oldTargetCode, relationshipTypeName);
    	if (!oldTargetCode.equals(newTargetCode))
		{
    		ConceptRelationshipDelegate.update(codeSystem, sourceCode, relationshipTypeName, oldTargetCode, newTargetCode, getInitialState());
		}
    	else
    	{
	        if (existingRelationship.getActive() != newActive)
			{
	        	if (!newActive)
				{
	        		ConceptRelationshipDelegate.activate(existingRelationship, getInitialState(), newActive);
				}
	        	else // activating a new
	        	{
	        		ConceptRelationshipDelegate.create(codeSystem, sourceCode, newTargetCode, relationshipTypeName, getInitialState());
	        	}
			}
    	}
    }
    
    public static ConceptRelationship getConceptRelationship(String sourceCode, String targetCode, String relationshipTypeName)
    {
    	return ConceptRelationshipDao.get(codeSystem, sourceCode, targetCode, relationshipTypeName);
    }
    
    public static Property getProperty(String code, String type, String value)
    {
        return PropertyDelegate.get(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), code, type, value);
    }
    
    public static Property getDesignationProperty(String code, String type, String value) throws STSNotFoundException
    {
        return DesignationPropertyDelegate.getProperty(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), code, type, value);
    }


    public static void createProperty(String code, String type, String value,
            boolean newActive) throws STSException
    {
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        Property property = PropertyDelegate.get(codeSystem, code, type, value);
        if (property != null && property.getActive())
        {
            throw new STSException("Code: "+code+" Type: "+type+" Value: "+value+" already exists");
        }
        
        PropertyDelegate.createVHAT(codeSystem, code, type, value, getInitialState());
    }


	public static void createDesignationProperty(String code, String type, String value, boolean newActive) throws STSException
	{
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        Property property = DesignationPropertyDelegate.getProperty(codeSystem, code, type, value);
        if (property != null && property.getActive())
        {
            throw new STSException(" Code: "+code+" Type: "+type+" Value: "+value+" already exists");
        }
        CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(code);
        checkAllowableUpdateToConcept(codedConcept.getEntityId());
        DesignationPropertyDelegate.create(codeSystem, codedConcept.getCode(), code, type, value, StateDelegate.getByType(State.INITIAL));
        
	}
	
    public static void updateProperty(String code, String type, String oldValue, String newValue,
            boolean newActive) throws STSException
    {
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        Property property = PropertyDelegate.get(codeSystem, code, type, oldValue);            
        checkAllowableUpdateToConcept(property.getConceptEntityId());
        if (!oldValue.equals(newValue))
        {
            PropertyDelegate.updateVHAT(codeSystem, code, type, oldValue, newValue, getInitialState());
        }
        if (newActive != property.getActive())
        {
            if (newActive == false)
            {
                PropertyDelegate.inactivateVHAT(codeSystem, code, type, newValue, getInitialState());
            }
            else
            {
                PropertyDelegate.createVHAT(codeSystem, code, type, newValue, getInitialState());
            }
        }
    }


	public static void updateDesignationProperty(String code, String type, String oldValue, String newValue, boolean newActive) throws STSException
	{
		CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        Property property = DesignationPropertyDelegate.getProperty(codeSystem, code, type, oldValue);            
        checkAllowableUpdateToConcept(property.getConceptEntityId());
        CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(code);
        checkAllowableUpdateToConcept(codedConcept.getEntityId());
        if (!oldValue.equals(newValue))
        {
            DesignationPropertyDelegate.update(codeSystem, codedConcept.getCode(), code, type, oldValue, newValue, getInitialState());
        }
        if (newActive != property.getActive())
        {
            if (newActive == false)
            {
            	DesignationPropertyDelegate.inactivate(codeSystem, codedConcept.getCode(), code, type, newValue, getInitialState());
            }
            else
            {
            	DesignationPropertyDelegate.create(codeSystem, codedConcept.getCode(), code, type, newValue, getInitialState());
            }
        }
	}
    
    public static Subset getSubsetByName(String name)
    {
        return SubsetDelegate.getByName(name);
    }

    public static Subset getSubsetByCode(String code)
    {
        return SubsetDelegate.getByCode(code);
    }

    public static void updateSubset(String code, String subsetName, Long vuid,
            boolean newActive) throws STSException
    {
        Subset subset = SubsetDelegate.getByCode(code);
        if (!subset.getName().equals(subsetName))
        {
            SubsetDelegate.update(code, subsetName, newActive);
        }
        if (subset.getActive() != newActive)
        {
            if (newActive == true)
            {
                if (vuid == null)
                {
                    vuid  = assignVuid();
                }
                SubsetDelegate.create(code, subsetName, vuid, newActive);
            }
            else
            {
                SubsetDelegate.inactivate(code);
            }
        }
    }

    public static Subset createSubset(String code, String subsetName, Long vuid,
            boolean active) throws STSException
    {
        Subset subset = SubsetDelegate.getByCode(code);
        if (subset != null)
        {
            throw new STSException("Code: "+code+" already exists");
        }
        if (vuid == null)
        {
            vuid  = assignVuid();
        }
        return SubsetDelegate.create(code, subsetName, vuid, active);
	}
    
	public static Designation getDesignation(String code) throws STSNotFoundException
    {
        return DesignationDelegate.get(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), code);
    }

	public static CodedConcept getConceptFromDesignatonVuid(long designationVuid)
	{
		return CodedConceptDelegate.getConceptFromDesignationVuid(designationVuid);
	}
	
	public static void updateDesignation(String designationCode, String currentConceptCode,
			String editedConceptCode, String currentName, String editedName, Long vuid, String type, boolean active) throws STSException
	{
        checkAllowableUpdateToConcept(currentConceptCode);
        DesignationDelegate.update(codeSystem, currentConceptCode, designationCode, type, currentName, editedName, vuid,
                active, getInitialState());
        if (currentConceptCode.equals(editedConceptCode) == false)
        {
            checkAllowableUpdateToConcept(editedConceptCode);
            DesignationRelationshipDelegate.update(codeSystem, designationCode, currentConceptCode, editedConceptCode, getInitialState());
        }
	}		

	public static Designation createDesignation(String conceptCode, String designationCode, String name, Long vuid, String type, boolean status)
		throws STSException
	{
        CodeSystem codeSystem = CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME);
        
        Designation designation = DesignationDelegate.get(codeSystem, designationCode);
        if (designation != null)
        {
            throw new STSException("Designation: "+designationCode+" already exists");
        }
        if (vuid == null)
        {
            vuid  = assignVuid();
        }
        
        return DesignationDelegate.create(codeSystem, designationCode, conceptCode, name, vuid, type, status, getInitialState());
    }

    /**
     * @return
     * @throws Exception
     * @throws IOException
     */
    private static Long assignVuid() throws STSException
    {
        VuidDelegate vuidDelegate = new VuidDelegate();
        Vuid vuidRequest;
        try
        {
            vuidRequest = vuidDelegate.createVuidRange(1, "Editor");
        }
        catch (Exception e)
        {
            throw new STSException(e);
        }
        return vuidRequest.getStartVuid();
    }
    public static SubsetRelationship getSubsetRelationship(String subsetName,
            String designationCode) throws STSException
    {
        return SubsetRelationshipDelegate.get(subsetName, codeSystem, designationCode);
    }

    public static void createSubsetRelationship(String subsetName,
            String designationCode, boolean active) throws STSException
    {
        CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(designationCode);
        checkAllowableUpdateToConcept(codedConcept.getEntityId());
        SubsetRelationshipDelegate.create(codeSystem, subsetName, codedConcept.getCode(), designationCode, getInitialState());
    }

    public static void updateSubsetRelationship(String subsetName,
            String designationCode, boolean active) throws STSException
    {
        CodedConcept codedConcept = CodedConceptDelegate.getConceptFromDesignationCode(designationCode);
        checkAllowableUpdateToConcept(codedConcept.getEntityId());
        SubsetRelationshipDelegate.inactivate(codeSystem, subsetName, codedConcept.getCode(), designationCode, getInitialState());
	}

    public static void resetHibernate()
    {
        HibernateSessionFactory.reset();
    }
}
