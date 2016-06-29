/**
 * 
 */
package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.VuidDao;
import gov.vha.vets.term.services.model.Vuid;
import gov.vha.vets.term.services.model.VuidDetail;
import gov.vha.vets.term.services.util.HibernateSessionFactory;
import gov.vha.vets.term.services.util.VuidHibernateSessionFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * @author VHAISLMURDOH
 *
 */
public class VuidDelegate
{
    /**
     * 
     * @return
     * @throws VuidDataException
     * @throws SQLException
     */
    private static Logger log = Logger.getLogger(VuidDelegate.class.getPackage().getName());

    /**
     * 
     * @param numCodes
     * @param userInitials
     * @return
     * @throws Exception
     * @throws IOException
     */
    public Vuid createVuidRange(int numCodes, String userInitials) throws Exception, IOException
    {
        //no request reason supplied when coming from deployment server use userintials as reason
        String requestReason = userInitials;
        return createVuidRange(numCodes, userInitials, requestReason);
    }
    
    /**
     * Return a set of VUID's 
     * @param numCodes
     * @param userinitials
     * @return
     * @throws IOException 
     * @throws Exception 
     * @throws Exception
     */
    synchronized public Vuid createVuidRange(int numCodes, String userInitials, String requestReason) throws Exception
    {    
        long endVuid = -1;
        long startVuid = -1;
        Vuid vuid = null;
        
        if (numCodes <= 0)
        {
            throw new Exception("Number of Vuids to create must be greater than zero");
        }
        
        Session session = VuidHibernateSessionFactory.currentSession();
        Transaction tx = session.beginTransaction();
        
        try
        {
            Vuid lastVuidRecord = VuidDao.getLastVuidRecord();
            if (lastVuidRecord != null)
            {
                startVuid = lastVuidRecord.getEndVuid() + 1;
            }
            else
            {
                startVuid = 1;  // very first vuid number
            }
            endVuid = startVuid + numCodes - 1;
            
            vuid = new Vuid(startVuid, endVuid, userInitials, new Date(), requestReason);
            VuidDao.saveVuid(vuid);
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            log.debug("Failed to create " + numCodes + " VUIDs requested by " + userInitials, e);
            throw e;
        }
        
        return vuid;
    }

  /**
   * 
   * @return
 * @throws ParseException 
   */
    public static List<VuidDetail> displayVuidSummary() throws ParseException
    {
        Session session = VuidHibernateSessionFactory.currentSession();
        Transaction tx = session.beginTransaction();
        
        SimpleDateFormat simple = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss aaa");
        
        List<VuidDetail> vuidDetailList = new ArrayList<VuidDetail>();
        List<Vuid> vuidSummaryList = VuidDao.getVuidSummary();
        
        for (Iterator iterator = vuidSummaryList.iterator(); iterator.hasNext();)
        {
            Vuid vuid = (Vuid) iterator.next();
            int vuidCount = (int) ((vuid.getEndVuid() - vuid.getStartVuid()) + 1);
            String dateString = simple.format(vuid.getRequestDate());
            Date today = simple.parse(dateString);
            VuidDetail vuidDetail = new VuidDetail(vuid.getStartVuid(), vuid.getEndVuid(), vuid.getUserInitials(), today, vuid.getRequestReason(), vuidCount);
            vuidDetailList.add(vuidDetail);
        }
        tx.commit();
        
        return vuidDetailList;
   }
    public static void resetHibernate()
    {
        HibernateSessionFactory.reset();
    }
    
}    
