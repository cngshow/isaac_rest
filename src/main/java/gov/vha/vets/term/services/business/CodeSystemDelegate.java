package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.CodeSystemDao;
import gov.vha.vets.term.services.dto.api.CodeSystemListViewDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.model.DesignationType;

import java.util.List;

public class CodeSystemDelegate
{

    /**
     * @param codeSystemName
     * @return
     * @throws STSNotFoundException
     */
    public static CodeSystem get(String name)
    {
        return CodeSystemDao.get(name);
    }
    
    public static List<CodeSystem> getCodeSystems()
    {
        return CodeSystemDao.getCodeSystems();
    }

    public static CodeSystemListViewDTO getCodeSystems(String codeSystemName, Integer pageSize, Integer pageNumber)
    {
        return CodeSystemDao.getCodeSystems(codeSystemName, pageSize, pageNumber);
    }

    public static CodeSystem create(String name, Long vuid, String description, String copyright, String copyrightURL, DesignationType designationType) throws STSNotFoundException
    {
    	return create(new CodeSystem(name, vuid, description, copyright, copyrightURL, designationType)); 
    }

    public static CodeSystem create(CodeSystem codeSystem) throws STSNotFoundException
    {
        CodeSystemDao.save(codeSystem);

        return codeSystem;
    }

    public static void remove(CodeSystem codeSystem)
    {
        CodeSystemDao.remove(codeSystem);
    }

	public static void verifyPreferredDesignationUnique(CodeSystem codeSystem) throws STSException
	{
		CodeSystemDao.verifyPreferredDesignationUnique(codeSystem);
	}

	public static CodeSystem get(long codeSystemId)
	{
		return CodeSystemDao.get(codeSystemId);
	}
	
	public static CodeSystem getByVuid(long vuid)
	{
		return CodeSystemDao.getByVuid(vuid);
	}
}
