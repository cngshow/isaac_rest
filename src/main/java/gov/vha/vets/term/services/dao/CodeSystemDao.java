package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.dto.api.CodeSystemListViewDTO;
import gov.vha.vets.term.services.dto.api.CodeSystemViewDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.exception.STSNotFoundException;
import gov.vha.vets.term.services.model.CodeSystem;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class CodeSystemDao
{

    /**
     * @param name
     * @return
     * @throws STSNotFoundException
     */
    public static CodeSystem get(String name)
    {
        Session session = HibernateSessionFactory.currentSession();
        CodeSystem codeSystem = (CodeSystem) session.createCriteria(CodeSystem.class).add(Restrictions.eq("name", name)).uniqueResult();
        return codeSystem;
    }

    public static List<CodeSystem> getCodeSystems()
    {
        Session session = HibernateSessionFactory.currentSession();
        List<CodeSystem> codeSystemList = session.createQuery("from CodeSystem order by name").list();

        return codeSystemList;
    }

    public static CodeSystemListViewDTO getCodeSystems(String codeSystemNameFilter, Integer pageSize, Integer pageNumber)
    {
        Session session = HibernateSessionFactory.currentSession();
        Long codeSystemCount = null;
        
        String codeSystemNameQuery = (codeSystemNameFilter == null) ? "" : "where upper(name) like upper(:nameFilter)";
        
        if (pageNumber == 1)
        {
            Query query = session.createQuery("select count(*) from CodeSystem "+codeSystemNameQuery);
            if (codeSystemNameFilter != null)
            {
                query.setParameter("nameFilter", "%"+codeSystemNameFilter+"%");
            }
            codeSystemCount = (Long) query.uniqueResult();
        }

        Query query = session.createQuery("from CodeSystem "+codeSystemNameQuery+" order by name");
        if (codeSystemNameFilter != null)
        {
            query.setParameter("nameFilter", "%"+codeSystemNameFilter+"%");
        }
        
        query.setFirstResult((pageNumber - 1) * pageSize);
        query.setMaxResults(pageSize);
        List<CodeSystem> codeSystemList = (List<CodeSystem>) query.list();
        
        CodeSystemListViewDTO codeSystemListViewDTO = new CodeSystemListViewDTO();
        List<CodeSystemViewDTO> codeSystemViewDTOs = new ArrayList<CodeSystemViewDTO>();
        for (CodeSystem codeSystem : codeSystemList)
		{
			CodeSystemViewDTO codeSystemViewDTO = new CodeSystemViewDTO();
			codeSystemViewDTO.setCodeSystem(codeSystem);
			codeSystemViewDTOs.add(codeSystemViewDTO);
		}
        codeSystemListViewDTO.setCodeSystems(codeSystemViewDTOs);

        if (pageNumber != null && pageNumber == 1)
        {
        	codeSystemListViewDTO.setTotalNumberOfRecords(codeSystemCount);
        }

        return codeSystemListViewDTO;
    }

    public static void save(CodeSystem codeSystem)
    {
        HibernateSessionFactory.currentSession().save(codeSystem);
    }

    public static void remove(CodeSystem codeSystem)
    {
        Session session = HibernateSessionFactory.currentSession();
        session.delete(codeSystem);
    }

	public static void verifyPreferredDesignationUnique(CodeSystem codeSystem) throws STSException
	{
        Session session = HibernateSessionFactory.currentSession();
        
        String query = "select con.code from CodedConcept con, Designation des, DesignationRelationship desRel, CodeSystem cs "
        	+ "  where  cs.id = :codeSystemId and con.codeSystem.id = cs.id and des.codeSystem.id = cs.id "
        	+ "     and des.type.id = cs.preferredDesignationType.id and des.active = 1 "
        	+ "     and con.entityId = desRel.sourceEntityId and des.entityId = desRel.targetEntityId "
        	+ "     and con.id = (select max(id) from CodedConcept conMax where conMax.codeSystem.id = :codeSystemId and con.entityId = conMax.entityId) "
        	+ "     and des.id = (select max(id) from Designation desMax where desMax.codeSystem.id = :codeSystemId and des.entityId = desMax.entityId) "
        	+ "     and desRel.id = (select max(id) from DesignationRelationship desRelMax where desRel.entityId = desRelMax.entityId) "
        	+ "  group by con.code having count(con.code) > 1";
        
        List<String[]> entries = session.createQuery(query).setLong("codeSystemId", codeSystem.getId()).list();
        
        if (entries.size() > 0)
        {
            throw new STSException("Code system: " + codeSystem.getName() + ", coded concept code(s): " + entries +
            		" have more than one active preferred designation of type: " + codeSystem.getPreferredDesignationType().getName());
        }
	}
    public static Set<Long> getPreferredForConcepts(CodeSystem codeSystem) throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String query = "select con.entityId from CodedConcept con, Designation des, DesignationRelationship desRel, CodeSystem cs "
            + "  where  cs.id = :codeSystemId and con.codeSystem.id = cs.id and des.codeSystem.id = cs.id "
            + "     and des.type.id = cs.preferredDesignationType.id and des.active = 1 "
            + "     and con.entityId = desRel.sourceEntityId and des.entityId = desRel.targetEntityId "
            + "     and con.id = (select max(id) from CodedConcept conMax where conMax.codeSystem.id = :codeSystemId and con.entityId = conMax.entityId) "
            + "     and des.id = (select max(id) from Designation desMax where desMax.codeSystem.id = :codeSystemId and des.entityId = desMax.entityId) "
            + "     and desRel.id = (select max(id) from DesignationRelationship desRelMax where desRel.entityId = desRelMax.entityId) ";
        
        List<Long> entries = session.createQuery(query).setLong("codeSystemId", codeSystem.getId()).list();
        return new HashSet<Long>(entries);
    }

	public static CodeSystem get(long codeSystemId)
	{
        Session session = HibernateSessionFactory.currentSession();
        CodeSystem codeSystem = (CodeSystem) session.createCriteria(CodeSystem.class).add(Restrictions.eq("id", codeSystemId)).uniqueResult();
        
        return codeSystem;
	}

	public static CodeSystem getByVuid(long vuid)
	{
		String hql = "from CodeSystem where vuid = :vuid";
        Session session = HibernateSessionFactory.currentSession();
        
        CodeSystem codeSystem = (CodeSystem)session.createQuery(hql).setLong("vuid", vuid).uniqueResult();
        
        return codeSystem;
	}
}
