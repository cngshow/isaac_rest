package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.Vuid;
import gov.vha.vets.term.services.util.VuidHibernateSessionFactory;
import gov.vha.vets.term.services.exception.VuidDataException;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Session;

/**
 * @author vhaislmurdoh
 *
 */
public class VuidDao 
{
    /**
     * 
     * @param vuidCode
     * @param userInitials
     * @param checkDigit
     * @throws Exception
     */
    public static void saveVuid(Vuid vuid) throws VuidDataException, SQLException 
    {       
    	VuidHibernateSessionFactory.currentSession().save(vuid);
    }    
    
    @SuppressWarnings("unchecked")
    public static List<Vuid> getVuidSummary()
    {
        Session session = VuidHibernateSessionFactory.currentSession();
        String qrystr = "from Vuid order by requestDate DESC, startVuid DESC";
        List<Vuid> vuidList = session.createQuery(qrystr).list();
        
        return vuidList;
    }
    
    @SuppressWarnings("unchecked")
    public static Vuid getLastVuidRecord()
    {
        Session session = VuidHibernateSessionFactory.currentSession();
        String qrystr = "from Vuid v where v.id = (select max(vo.id) from Vuid vo)";
        Vuid vuid = (Vuid) session.createQuery(qrystr).uniqueResult();
                
        return vuid;
    }
}
