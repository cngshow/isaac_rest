package gov.vha.vets.term.services.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NoInitialContextException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import gov.vha.vets.term.services.business.ConceptStateDelegate;
import gov.vha.vets.term.services.business.TerminologyConfigDelegate;
import gov.vha.vets.term.services.dto.config.StateConfig;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.DesignationType;
import gov.vha.vets.term.services.model.State;

/**
 * Configures and provides access to Hibernate sessions, tied to the current
 * thread of execution. Follows the Thread Local Session pattern, see
 * {@link http://hibernate.org/42.html}.
 */
public class HibernateSessionFactory
{
    public static final String AUTHORING_VERSION_NAME = "Authoring Version";
    public static final long AUTHORING_VERSION_ID = 9999999999L;
    public static final long LAST_FINALIZED_VERSION_ID = AUTHORING_VERSION_ID - 1;
    public static final String VHAT_NAME = "VHAT";
    static Logger log = Logger.getLogger(HibernateSessionFactory.class.getPackage().getName());
    static boolean isFirstRun = true;
    
    
    /**
     * Location of hibernate.cfg.xml file. NOTICE: Location should be on the
     * classpath as Hibernate uses #resourceAsStream style lookup for its
     * configuration file. That is place the config file in a Java package - the
     * default location is the default Java package.<br>
     * <br>
     * Examples: <br>
     * <code>CONFIG_FILE_LOCATION = "/gov/va/med/term/browser/properties/hibernate.conf.xml". 
     * CONFIG_FILE_LOCATION = "/com/foo/bar/myhiberstuff.conf.xml".</code>
     */
    private static String CONFIG_FILE_LOCATION = "/gov/va/med/term/services/config/hibernate.cfg.xml";
    private static String CONFIG_FILE_LOCAL = "/localHibernate.cfg.xml";

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
    @SuppressWarnings("unchecked")
    public static Session currentSession() throws HibernateException
    {
        Session session = (Session) threadLocal.get();

        if (session == null || !session.isConnected())
        {
            synchronized (CONFIG_FILE_LOCATION)
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
                        cfg.configure(CONFIG_FILE_LOCAL);
                    }
                    
                    try
                    {
                        Properties properties = cfg.getProperties();
                        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();)
                        {
                            Map.Entry entry = (Map.Entry)iter.next();
                            if (entry.getKey().equals("connection.url") || entry.getKey().equals("connection.username"))
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
            }
            try
            {
                session = sessionFactory.openSession(new ServicesInterceptor());
            }
            catch (NullPointerException npe)
            {
            	throw new HibernateException("Cannot get connection to the database.");
            }
            threadLocal.set(session);

            if (isFirstRun == true)
        	{
            	isFirstRun = false;
            	synchronizeStates(session);
                initializeDatabase(session);
                createDatabaseConstraints(session);
                createTemporaryTable(session);
        	}
        }

        return session;
    }
    
    public static Session openSession()
    {
        return sessionFactory.openSession();
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
     * Close the single hibernate session instance.
     * 
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static void closeSession() throws HibernateException
    {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);

        if (session != null)
        {
            session.close();
        }
    }

    /**
     * Default constructor.
     */
    private HibernateSessionFactory()
    {
    }

    /*
     * initialize database with VHAT codeSystem, authoring version and preferred type
     */
    @SuppressWarnings("deprecation")
    static void initializeDatabase(Session session)
    {
        Connection connection; 
        PreparedStatement statement;
        ResultSet resultSet;

        connection = session.connection();
        
        try
        {
            long typeId = 0L;
            long codeSystemId = 0L;
            statement = connection.prepareStatement("select count(*) count from version where id = ?");
            statement.setLong(1, AUTHORING_VERSION_ID);
            resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                if (resultSet.getInt("count") == 0)
                {
                    statement = connection.prepareStatement("select id from codesystem where name = ?");
                    statement.setString(1, VHAT_NAME);
                    resultSet = statement.executeQuery();
                    if (resultSet.next())
                    {
                        codeSystemId = resultSet.getLong("id");
                    }
                    else
                    {
                        statement = connection.prepareStatement("select type_seq.nextval id from dual");
                        resultSet = statement.executeQuery();
                        if (resultSet.next())
                        {
                            typeId = resultSet.getLong("id");
                        }
                        
                        statement = connection.prepareStatement("select codesystem_seq.nextval id from dual");
                        resultSet = statement.executeQuery();
                        if (resultSet.next())
                        {
                            codeSystemId = resultSet.getLong("id");
                        }

                        statement = connection.prepareStatement("INSERT INTO TYPE (id, kind, name) VALUES (?,?,?)");
                        statement.setLong(1, typeId);
                        statement.setString(2, "D");
                        statement.setString(3, DesignationType.PREFERRED_NAME);
                        statement.executeUpdate();

                        statement = connection.prepareStatement("INSERT INTO CODESYSTEM (id, copyright, description, name, preferred_designation_type_id, vuid) "
                                + "VALUES (?,?,?,?,?,?)");
                        statement.setLong(1, codeSystemId);
                        statement.setString(2, "2007");
                        statement.setString(3, "VHA Terminology");
                        statement.setString(4, VHAT_NAME);
                        statement.setLong(5, typeId);
                        statement.setLong(6, 4707199L);  // actual VUID for VHAT code system
                        statement.executeUpdate();
                    }
                    
                    statement = connection.prepareStatement("insert into version (id, name, codeSystem_Id, description, deploymentdate, effectivedate, releasedate) "
                            + "values (?, ?, ?, 'This is the version that is given to authoring changes before they are finalized.', SYSDATE, SYSDATE, SYSDATE)");
                    statement.setLong(1, AUTHORING_VERSION_ID);
                    statement.setString(2, AUTHORING_VERSION_NAME);
                    statement.setLong(3, codeSystemId);
                    statement.executeUpdate();
                    connection.commit();
                }
            }

            // truncate mapentrydisplay table used for caching map entries
            // since this is a truncate, no rollback is possible and also is not needed
            statement = connection.prepareStatement("TRUNCATE TABLE mapentrydisplay");
            statement.executeUpdate();
            
            // truncate mapentrycache table used for caching map entries
            // since this is a truncate, no rollback is possible and also is not needed
            statement = connection.prepareStatement("TRUNCATE TABLE mapentrycache");
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            // don't throw an exception if the table does not exist
            if (e.getMessage().startsWith("ORA-00942: table or view does not exist") == false)
            {
                throw new HibernateException(e);
            }
        }
    }
    
    /*
     * Initialize database with complex constraints
     */
    static void createDatabaseConstraints(Session session)
    {
        Connection connection;
        PreparedStatement statement;
        
        connection = session.connection();
        
        try
        {
            //Create unique NAME and CODESYSTEM_ID combination
            statement = connection.prepareStatement("ALTER TABLE VERSION ADD " +
                                                    "(CONSTRAINT UNIQUE_NAME_CODESYS " +
                                                    " UNIQUE (NAME, CODESYSTEM_ID))");
            statement.execute();
            statement = connection.prepareStatement("ALTER TABLE TYPE ADD " +
                    "(CONSTRAINT UNIQUE_KIND_NAME_TYPE " +
                    " UNIQUE (KIND, NAME))");
            statement.execute();
            
        }
        catch(SQLException e)
        {
            //Continue as if nothing happened
        }
    }
    
	/*
	 * Create temporary table used for site data request
	 */
	static void createTemporaryTable(Session session)
	{
		Connection connection;
		PreparedStatement statement;

		connection = session.connection();

		try
		{
        	statement = connection.prepareStatement(
        			"CREATE GLOBAL TEMPORARY TABLE DiscoveryResults (" +
        			"  conceptEntityId number(19)," +
        			"  designationEntityId number(19)," +
        			"  entityId number(19)," +
        			"  active NUMBER(1)," +
        			"  name VARCHAR2(255)," +
        			"  subsetName VARCHAR2(255)," +
        			"  type VARCHAR2(1)," +
        			"  value VARCHAR2(2000)," +
        			"  vuid NUMBER(19)," +
        			"  changeType NUMBER(1)) " +
        			"ON COMMIT delete ROWS");
			statement.execute();
		}
		catch (SQLException e)
		{
			// Continue as if nothing happened
		}
	}

	/*
	 * synchronize state in TerminlogyConfig.xml with those in the state table
	 */
	@SuppressWarnings("unchecked")
	static void synchronizeStates(Session session)
	{
        Transaction tx = session.beginTransaction();

        List<State> databaseStates = (List<State>) session.createQuery("from State").list();
        List<StateConfig> configStates = null;
		try
		{
			configStates = TerminologyConfigDelegate.getStates();
		}
		catch (STSException e)
		{
			log.error(e.getMessage(), e);
			e.printStackTrace();
			return;
		}
		
        // Find items that need to be added to State table
        for (StateConfig configState : configStates)
        {
            boolean isInDatabase = true;
            // get config State values...
            String configStateName = configState.getName();
            String configStateType = configState.getType();
            if (configStateType == null)
            {
                configStateType = new String();
            }

            for (State dbState : databaseStates)
            {
                // ...and compare to each State in the database
                String dbStateName = dbState.getName();
                String dbStateType = dbState.getType();
                if (dbStateType == null)
                {
                    dbStateType = new String();
                }

                // TODO nest else if inside this if
                if (dbStateName.equals(configStateName))
                {
                    if (!dbStateType.equals(configStateType))
                    {
                        // State name exists in both locations but the type is
                        // different so update it in database
                        dbState.setType(configStateType);
                        session.update(dbState);

                        isInDatabase = true;
                        break;
                    }

                    // State name exists in both config and State table
                    // and has same type so leave database as-is
                    isInDatabase = true;
                    break;
                }
                else
                {
                    // name from config is not found in database State table so
                    // State from config has to be added to database
                    isInDatabase = false;
                }
            }

            if (isInDatabase == false || databaseStates.size() == 0)
            {
                // State in config is not in the database so add it
                if (session == null)
                {
                    session = HibernateSessionFactory.openSession();
                    tx = session.beginTransaction();
                }
                State newState = new State();
                newState.setName(configState.getName());
                newState.setType(configState.getType());
                session.save(newState);
            }
        }

        // Find items that need to be deleted from State table
        for (State dbState : databaseStates)
        {
            boolean isInConfig = true;
            // Get database State name...
            String dbStateName = dbState.getName();

            for (StateConfig configState : configStates)
            {
                // ...and compare to each State name in the database
                String configStateName = configState.getName();

                if (dbStateName.equals(configStateName))
                {
                    // The State in the database is found in config so leave
                    // as-is
                    isInConfig = true;
                    break;
                }
                else
                {
                    // State name is not found in the config so database State
                    // has to be removed
                    isInConfig = false;
                }
            }
            if (isInConfig == false)
            {
                // The State is not in config so delete it from the State table
                if (session == null)
                {
                    session = HibernateSessionFactory.openSession();
                    tx = session.beginTransaction();
                }
                // if there are currently no conceptStates with the given state
                // then delete the state
                if (ConceptStateDelegate.isStateExist(dbState) == false)
                {
                	session.delete(dbState);
                }
            }
        }
        if (tx != null)
        {
            tx.commit();
        }
    }

	/**
     * return hibernate configuration
     * 
     */
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
            log.warn(exception.fillInStackTrace());
        }
        else
        {
            session.disconnect();
        }
    }
}
