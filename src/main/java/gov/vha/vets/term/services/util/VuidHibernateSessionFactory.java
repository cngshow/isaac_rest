package gov.vha.vets.term.services.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NoInitialContextException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * Configures and provides access to Hibernate sessions, tied to the current
 * thread of execution. Follows the Thread Local Session pattern, see
 * {@link http://hibernate.org/42.html}.
 */
public class VuidHibernateSessionFactory
{
    /**
     * Location of hibernate.cfg.xml file. NOTICE: Location should be on the
     * classpath as Hibernate uses #resourceAsStream style lookup for its
     * configuration file. That is place the config file in a Java package - the
     * default location is the default Java package.<br>
     * <br>
     * Examples: <br>
     * <code>CONFIG_FILE_LOCATION = "/gov/va/med/term/vuid/cfg/hibernate.cfg.xml". 
     * CONFIG_FILE_LOCATION = "/com/foo/bar/myhiberstuff.conf.xml".</code>
     */
    private static String CONFIG_FILE_LOCATION = "/gov/va/med/term/services/config/vuidHibernate.cfg.xml";
    private static String CONFIG_FILE_LOCAL = "/localVuidHibernate.cfg.xml";

    /** Holds a single instance of Session */
    private static final ThreadLocal threadLocal = new ThreadLocal();

    /** The single instance of hibernate configuration */
    private static final AnnotationConfiguration cfg = new AnnotationConfiguration();

    /** The single instance of hibernate SessionFactory */
    private static org.hibernate.SessionFactory sessionFactory;

    /**
     * Returns the ThreadLocal Session instance. Lazy initialize the
     * <code>SessionFactory</code> if needed.
     * 
     * @return Session
     * @throws HibernateException
     */
    public static Session currentSession() throws HibernateException
    {
        Session session = (Session) threadLocal.get();

        if (session == null || !session.isConnected())
        {
            boolean runningThreeTier = false; 
            if (sessionFactory == null)
            {
                    
                // determine if we are running in weblogic or not
                try
                {
                    Context ctx = new InitialContext();
                    ctx.lookup("bogus");
                }
                catch (NoInitialContextException e)
                {
                    // Not running in weblogic
                    runningThreeTier = false;                        
                    System.err.println("Not running in weblogic");
                }
                catch (NameNotFoundException ex)
                {
                    // running in weblogic
                    runningThreeTier = true;                        
                    System.err.println("Running in weblogic");
                }
                catch (Exception ex2)
                {
                    runningThreeTier = false;
                }
                
                if (runningThreeTier)
                {
                    cfg.configure(CONFIG_FILE_LOCATION);
                }
                else
                {
                    try
                    {
                        File dir1 = new File (CONFIG_FILE_LOCAL);
                        System.out.println ("Current directory: " + dir1.getCanonicalPath()+ " Readable:"+dir1.canRead());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    cfg.configure(CONFIG_FILE_LOCAL);
                }
                
                try
                {
                    Properties properties = cfg.getProperties();
                    for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();)
                    {
                        Map.Entry entry = (Map.Entry)iter.next();
                        if (((String)entry.getKey()).startsWith("connection"))
                        {
                            System.out.println(entry.getKey()+" ="+entry.getValue());
                        }
                    }
                    sessionFactory = cfg.buildSessionFactory();
                }
                catch (Exception e)
                {
                    System.err.println("%%%% Error Creating SessionFactory %%%%");
                    e.printStackTrace();
                }
            }
            session = sessionFactory.openSession();
            threadLocal.set(session);
        }

        return session;
    }
    
    /**
     * set the configuration to some other than the default, if we already have it don't over write
     * @param location
     * @throws HibernateException
     */
    public static void setConfig(String location) throws HibernateException
    {
        if (sessionFactory == null)
        {
            cfg.configure(location);
            sessionFactory = cfg.buildSessionFactory();
        }
    }
    
    public static SessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    public static void reset() throws HibernateException
    {
        if (sessionFactory != null)
        {
            currentSession().close();
            sessionFactory.close();
        }
        sessionFactory = cfg.buildSessionFactory();
    }
    
    /**
     * This call is used when you want to have the db recreated with the hibernate.cfg file
     * @param location
     * @throws HibernateException
     */
    public static void setConfigWithReset(String location) throws HibernateException
    {
        if (sessionFactory != null)
        {
            currentSession().close();
            sessionFactory.close();
        }
        else
        {
            cfg.configure(location);
        }
        sessionFactory = cfg.buildSessionFactory();
    }

    /**
     * Close the single hibernate session instance.
     * 
     * @throws HibernateException
     */
    public static void closeSession() throws HibernateException
    {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);

        if (session != null)
        {
            session.close();
        }
    }

    public static Configuration getConfiguration()
    {
        return cfg;
    }

    public static void disconnect()
    {
        Session session = currentSession();
        Transaction transaction = session.getTransaction();
        if (transaction != null && transaction.isActive() == true)
        {
            Exception exception = new Exception("Error trying to disconnect session during a transaction");
        }
        else
        {
            session.disconnect();
        }
    }
    
    /**
     * Default constructor.
     */
    private VuidHibernateSessionFactory()
    {
    }

 }