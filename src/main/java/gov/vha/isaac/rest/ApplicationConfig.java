package gov.vha.isaac.rest;

import static gov.vha.isaac.ochre.api.constants.Constants.DATA_STORE_ROOT_LOCATION_PROPERTY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Context;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.RemoteServiceInfo;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.util.ArtifactUtilities;
import gov.vha.isaac.ochre.api.util.DBLocator;
import gov.vha.isaac.ochre.api.util.DownloadUnzipTask;
import gov.vha.isaac.ochre.api.util.WorkExecutors;
import gov.vha.isaac.ochre.metadata.source.IsaacMetadataAuxiliary;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.RestSystemInfo;
import gov.vha.isaac.rest.session.PrismeServiceUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

@ApplicationPath(RestPaths.appPathComponent)
public class ApplicationConfig extends ResourceConfig implements ContainerLifecycleListener
{
	private static final AtomicInteger startup = new AtomicInteger(1);
	private Logger log = LogManager.getLogger();

	private static ApplicationConfig instance_;

	private StringProperty status_ = new SimpleStringProperty("Not Started");
	private boolean debugMode = true;
	private boolean shutdown = false;

	private static String databaseRootLocation;

	//Note - this injection works fine, when deployed as a war to tomcat.  However, when launched in the localJettyRunner from eclipse,
	//this remains null.
	@Context
	ServletContext context_;

	private String contextPath;

	private static byte[] secret_;

	private RestSystemInfo systemInfo_;
	private String warFileVersion_;  //read from prisme.properties

	private File dbLocation;
	private static final DateTimeFormatter fileDateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	//TODO we need to deal with contradictions properly whenever we pull things from a LatestVersion object.  See code in RestConceptChonology
	//for extracting the latest description.

	public ApplicationConfig()
	{
		//If we leave everything to annotations, is picks up the eclipse moxy gson writer, which doesn't handle abstract classes properly.
		//The goal here is to force it to use Jackson, but it seems that registering jackson disables scanning, so also have to re-enable
		//scanning.  It also seems ot forget to scan this class... so register itself..
		super(new ResourceConfig().packages("gov.vha.isaac.rest").register(JacksonFeature.class).register(ApplicationConfig.class));
	}

	public static ApplicationConfig getInstance()
	{
		return instance_;
	}

	@Override
	public void onReload(Container arg0)
	{
		// noop
	}

	@Override
	public void onShutdown(Container arg0)
	{
		shutdown = true;
		log.info("Stopping ISAAC");
		LookupService.shutdownIsaac();
		log.info("ISAAC stopped");
	}

	private void configureSecret()
	{
		//This hacking is to prevent fortify from flagging an external data source path
		File tempDirName = new File(gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(System.getProperty("java.io.tmpdir")));
		File file = new File(tempDirName, contextPath.replaceAll("/", "_") + "-tokenSecret");

		log.debug("Secret file for token encoding " + file.getAbsolutePath() + " " + (file.exists() ? "exists" : "does not exist"));

		if (file.exists())
		{
			try
			{
				byte[] temp = Files.readAllBytes(file.toPath());
				if (temp.length == 20)
				{
					secret_ = temp;
					log.info("Restored token secret");
				}
				else
				{
					log.warn("Unexpected data in token secret file.  Will calculate a new token. " + file.getCanonicalPath());
				}
			}
			catch (IOException e1)
			{
				log.warn("Failed opening token secret file.  Will calculate a new token.", e1);
			}
		}
		if (secret_ == null)
		{
			byte[] temp = new byte[20];

			log.info("Calculating a new token");
			//Don't use SecureRandom().getInstanceStrong() here, it hangs on linux, and we don't need that level of security.
			//Is supposed to be fixed in Java 9.
			new SecureRandom().nextBytes(temp);
			secret_ = temp;
			try
			{
				Files.write(file.toPath(), secret_);
			}
			catch (IOException e)
			{
				log.warn("Unexpected error storing token secret file", e);
			}
		}
	}

	@Override
	public void onStartup(Container container)
	{
		log.info("onStartup called");
		if (instance_ != null)
		{
			throw new RuntimeException("Unexpected!");
		}
		instance_ = this;

		//context is null when run from eclipse with the local jetty runner.
		if (context_ == null)
		{
			debugMode = true;
			contextPath = "rest";
		}
		else
		{
			contextPath = context_.getContextPath().replace("/", "");
			debugMode = (contextPath.contains("SNAPSHOT") ? true : false);
		}

		log.info("Context path of this deployment is '" + contextPath + "' and debug mode is " + debugMode);

		configureSecret();

		issacInit();
	}

	public boolean isIsaacReady()
	{
		return LookupService.isIsaacStarted();
	}

	public String getStatusMessage()
	{
		return status_.get();
	}

	private void issacInit()
	{
		log.info("Isaac Init called");
		if (startup.getAndDecrement() == 1)
		{
			log.info("Executing initial ISAAC Init in background thread");
			//do startup in this thread
			LookupService.get();
			LookupService.startupWorkExecutors();

			Runnable r = new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						log.info("ISAAC Init thread begins");
						
						//Place git config info into the configuration service
						RemoteServiceInfo gitConfig = new RemoteServiceInfo()
						{
							private String gitUsername, url;
							private char[] gitPw;
							
							{
								Properties props = PrismeServiceUtils.getPrismeProperties();
								url =  props.getProperty("git_root", "");
								gitUsername = props.getProperty("git_user", "");
								gitPw = props.getProperty("git_pwd", "").toCharArray();
							}
							
							@Override
							public String getUsername()
							{
								return gitUsername;
							}
							
							@Override
							public String getURL()
							{
								return url;
							}
							
							@Override
							public char[] getPassword()
							{
								return gitPw;
							}
						};
						Get.configurationService().setGitConfiguration(gitConfig);

						databaseRootLocation = gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(System.getProperty(DATA_STORE_ROOT_LOCATION_PROPERTY));
						if (StringUtils.isBlank(databaseRootLocation) || !Files.isDirectory(Paths.get(databaseRootLocation)))
						{
							//if there isn't an official system property set, check this one or if the directory does not exist
							String sysProp = gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(System.getProperty("isaacDatabaseLocation"));
							if (StringUtils.isBlank(sysProp) || !Files.isDirectory(Paths.get(sysProp)))
							{
								databaseRootLocation = sysProp;
								//No ISAAC default property set, nor the isaacDatabaseLocation property is set.  Download a DB.
								log.info("Downloading a database for use");
								status_.set("Downloading DB");
								try
								{
									dbLocation = downloadDB();
								}
								catch (Exception e)
								{
									status_.unbind();
									status_.set("Download Failed: " + e);
									throw new RuntimeException(e);
								}
							}
							else
							{
								dbLocation = new File(sysProp);
							}

							if (shutdown)
							{
								return;
							}

							File dataStoreLocation = DBLocator.findDBFolder(dbLocation);

							if (!dataStoreLocation.exists())
							{
								throw new RuntimeException("Couldn't find a data store from the input of '" + dataStoreLocation.getAbsoluteFile().getAbsolutePath() + "'");
							}
							if (!dataStoreLocation.isDirectory())
							{
								throw new RuntimeException("The specified data store: '" + dataStoreLocation.getAbsolutePath() + "' is not a folder");
							}

							//use the passed in JVM parameter location
							LookupService.getService(ConfigurationService.class).setDataStoreFolderPath(dataStoreLocation.toPath());
							System.out.println("  Setup AppContext, data store location = " + dataStoreLocation.getAbsolutePath());
						}

						if (shutdown)
						{
							return;
						}

						try {
							status_.set("Starting ISAAC");
							LookupService.startupIsaac();
														
							//log metadata versions of codebase and database
							String auxiliaryMetadataVersion = getDatabaseIsaacMetadataVersion();
														
							log.info("Isaac metadata versions - Codebase {} - Database {}.",
									IsaacMetadataAuxiliary.AUXILIARY_METADATA_VERSION,
									auxiliaryMetadataVersion);
							
							if (!IsaacMetadataAuxiliary.AUXILIARY_METADATA_VERSION.equals(auxiliaryMetadataVersion)) {
								log.warn("Isaac Metadata Auxiliary versions do not match between code and database!");
							}
								
						}
						catch(Exception e){
							log.error("Startup failed due to ", e);
							//rename directory of existing database and restart startup.
							//if database exists, rename folder
							if (dbLocation != null && dbLocation.exists())
							{
								log.info("Database deemed corrupted.  Shutdown database, move it to CORRUPT directory, download new database, and re-startup system.");

								// Shutdown ISAAC
								log.info("Shutting down database");
								LookupService.shutdownIsaac();

								// Move corrupted database
								log.info("Moving corrupted database");
								File corruptDbLocation = new File(gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(dbLocation.getParent() + File.separator + "CORRUPT" + File.separator + LocalDateTime.now().format(fileDateTimeFormatter)));
								log.info("DB Location: " + dbLocation.getAbsolutePath() + " to " + corruptDbLocation.getAbsolutePath());
								FileUtils.moveDirectoryToDirectory(dbLocation, corruptDbLocation, true);
								FileUtils.deleteDirectory(dbLocation);

								//download a new database
								log.info("Downloading new database");
								dbLocation = downloadDB();
								File dataStoreLocation = DBLocator.findDBFolder(dbLocation);
								LookupService.getService(ConfigurationService.class).setDataStoreFolderPath(dataStoreLocation.toPath());
								log.info("Setup AppContext, data store location = " + dataStoreLocation.getAbsolutePath());

								// Re-start ISAAC
								log.info("Re-starting ISAAC");
								status_.set("Starting ISAAC");
								LookupService.startupIsaac();

								log.info("Directories containing the corrupt database were renamed.  A new database was downloaded and ibdf file(s) loaded");
							}
						}

						systemInfo_ = new RestSystemInfo();
						log.info(systemInfo_.toString());

						try
						{
							if (StringUtils.isNotBlank(warFileVersion_) && !warFileVersion_.equals(systemInfo_.getApiImplementationVersion()))
							{
								log.warn("The WAR file version found in the prisme.properties file does not match the version from the pom.xml in the war file!  Found "
										+ systemInfo_.getApiImplementationVersion() + " and " + warFileVersion_);
							}
						}
						catch (Exception e)
						{
							log.error("Unexpected error validating war file versions!", e);
						}
						finally
						{
							warFileVersion_ = null;  //No longer need this
						}

						status_.set("Ready");
						System.out.println("Done setting up ISAAC");

						System.out.println(String.format("Application started.\nTry out %s%s\nStop the application by pressing enter.",
								"http://localhost:8180/", RestPaths.conceptVersionAppPathComponent + MetaData.CONCRETE_DOMAIN_OPERATOR.getNid()));
					}
					catch (Exception e)
					{
						log.error("Failure starting ISAAC", e);
						status_.unbind();
						status_.set("FAILED!");
					}
				}

			};

			LookupService.get().getService(WorkExecutors.class).getExecutor().execute(r);
		}
	}

	private boolean validateExistingDb(File targetDBLocation, String groupId, String artifactId, String version, String classifier) {
		//We need to read the pom.xml file that we find inside of targetDBLocation - and validate that each and every
		//parameter perfectly matches.  If it doesn't match, then the DB must be deleted, and downloaded.
		//If we don't do this, we won't catch the case where the isaac-rest server was undeployed, then redeployed with a different DB configuration.
		//The pom file we need to read will be at targetDbLocation\*.data\META-INF\maven\{groupId}\{artifactId}\pom.xml
		//We need to validate <groupId>, <artifactId>, <version> and <resultArtifactClassifier> keeping in mind that classifer
		//is optional

		log.info("Checking specified parameters against existing db in folder: " + targetDBLocation.getAbsolutePath() + " - expecting to find content for " 
				+ "group {}, artifact {}, version {} and classifier {}", groupId, artifactId, version, classifier );

		status_.set("Validating existing DB directory");

		if (! targetDBLocation.isDirectory()) {
			log.warn("Validation of existing DB failed. Invalid DB directory: {}", targetDBLocation.getAbsoluteFile());

			return false;
		}

		File pomFile = null;
		for (File file : targetDBLocation.listFiles()) {
			if (file.isDirectory() && file.getName().endsWith(".data")) {
				pomFile = new File(gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(file.getAbsolutePath() + File.separatorChar + "META-INF" + File.separatorChar + "maven" + File.separatorChar + groupId + File.separatorChar + artifactId  + File.separatorChar + "pom.xml"));
				if (pomFile.exists() && pomFile.isFile()) {
					log.info("Found the expected existing pom file at: {}" + pomFile.getAbsoluteFile());
					break;
				}
				else
				{
					pomFile = null;
				}
			}
		}
		if (pomFile == null || ! pomFile.isFile()) {
			log.warn("Validation of existing DB failed.  " + (pomFile == null ? "The expected pom file was not found." : "The expected pom file location is not a file")); 
			return false;
		}

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		try {
			domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document dDoc = builder.parse(pomFile);
			XPath xPath = XPathFactory.newInstance().newXPath();

			String existingDbGroupId = ((Node) xPath.evaluate("/project/groupId", dDoc, XPathConstants.NODE)).getTextContent();
			String existingDbArtifactId = ((Node) xPath.evaluate("/project/artifactId", dDoc, XPathConstants.NODE)).getTextContent();
			String existingDbVersion = ((Node) xPath.evaluate("/project/version", dDoc, XPathConstants.NODE)).getTextContent();

			Node existingDbClassifierNode = (Node) xPath.evaluate("/project/properties/resultArtifactClassifier", dDoc, XPathConstants.NODE);
			String existingDbClassifier = existingDbClassifierNode != null ? existingDbClassifierNode.getTextContent() : null;

			if (! existingDbGroupId.trim().equals(groupId.trim())) {
				log.warn("Validation of existing DB pom file failed. Existing groupId {} != {}", existingDbGroupId, groupId);

				return false;
			}
			if (! existingDbArtifactId.trim().equals(artifactId.trim())) {
				log.warn("Validation of existing DB pom file failed. Existing artifactId {} != {}", existingDbArtifactId, artifactId);
				return false;
			}
			if (! existingDbVersion.trim().equals(version.trim())) {
				log.warn("Validation of existing DB pom file failed. Existing version {} != {}", existingDbVersion, version);
				return false;
			}

			if (StringUtils.isBlank(classifier) && StringUtils.isBlank(existingDbClassifier)) {
				return true;
			} else if (classifier == null || existingDbClassifier == null) {
				log.warn("Validation of existing DB pom file failed. Existing classifier {} != {}", existingDbClassifier, classifier);

				return false;
			} else if (classifier.trim().equals(existingDbClassifier.trim())) {
				return true;
			} else {
				log.warn("Validation of existing DB pom file failed. Existing classifier {} != {}", existingDbClassifier, classifier);
				return false;
			}
		} catch (Exception e) {
			log.warn("Validation of existing DB pom file failed", e);
		}

		return false;
	}

	private File downloadDB() throws Exception
	{
		File tempDbFolder = null;
		try
		{
			String baseMavenURL = null;
			String mavenUsername = null;
			String mavenAccessCode = null;
			String groupId = null;
			String artifactId = null;
			String version = null;
			String classifier = null;

			//First, see if there is a properties file embedded in the war (PRISME places this during deployment)
			Properties props = PrismeServiceUtils.getPrismeProperties();
			try 
			{
				if (props.size() == 0)
				{
					log.info("No prisme.properties file was found on the classpath");
				}
				else
				{
					log.info("Reading database configuration from prisme.properties file");
					baseMavenURL = props.getProperty("nexus_repository_url");
					mavenUsername = props.getProperty("nexus_user");
					mavenAccessCode = props.getProperty("nexus_pwd");
					groupId = props.getProperty("db_group_id");
					artifactId = props.getProperty("db_artifact_id");
					version = props.getProperty("db_version");
					classifier = props.getProperty("db_classifier");
					warFileVersion_ = props.getProperty("war_version");
				}
			}
			catch (Exception e1)
			{
				log.error("Unexpected error trying to read properties from the prisme.properties file", e1);
				throw new RuntimeException(e1);
			}

			if (StringUtils.isBlank(version))
			{
				log.warn("Unable to determine specified DB - using developer default options!");
				baseMavenURL = "https://vadev.mantech.com:8080/nexus/content/groups/everything/";
				mavenUsername = "system";
				mavenAccessCode = "system";
				groupId = "gov.vha.isaac.db";
				artifactId = "vhat";
				version = "2017.05.04-1.13";
				classifier = "all";
			}

			log.info("Checking for existing DB");

			File targetDBLocation = new File(gov.vha.isaac.ochre.api.util.StringUtils.stringForFortify(System.getProperty("java.io.tmpdir")), "ISAAC." + contextPath + ".db");

			if (targetDBLocation.isDirectory())
			{
				if (validateExistingDb(targetDBLocation, groupId, artifactId, version, classifier)) {
					log.info("Using existing db folder: " + targetDBLocation.getAbsolutePath());

					return targetDBLocation;
				} else {
					log.warn("Removing existing db because consistency validation failed");

					FileUtils.deleteDirectory(targetDBLocation);
				}
			}

			tempDbFolder = File.createTempFile("ISAAC-DATA", "");
			tempDbFolder.delete();
			tempDbFolder.mkdirs();
			log.info("Downloading DB to " + tempDbFolder.getAbsolutePath());
			URL cradle = ArtifactUtilities.makeFullURL(baseMavenURL, mavenUsername, mavenAccessCode, groupId, artifactId, version, classifier, "cradle.zip");
			Task<File> task = new DownloadUnzipTask(mavenUsername, mavenAccessCode, cradle, true, true, tempDbFolder);
			status_.bind(task.messageProperty());
			Get.workExecutors().getExecutor().submit(task);
			try
			{
				task.get();
			}
			catch (InterruptedException e)
			{
				task.cancel(true);
				throw e;
			}
			status_.unbind();

			URL lucene = ArtifactUtilities.makeFullURL(baseMavenURL, mavenUsername, mavenAccessCode, groupId, artifactId, version, classifier, "lucene.zip");
			task = new DownloadUnzipTask(mavenUsername, mavenAccessCode, lucene, true, true, tempDbFolder);
			status_.bind(task.messageProperty());
			Get.workExecutors().getExecutor().submit(task);
			try
			{
				task.get();
			}
			catch (InterruptedException e)
			{
				task.cancel(true);
				throw e;
			}
			status_.unbind();
			status_.set("Download complete");

			log.debug("Renaming " + tempDbFolder.getCanonicalPath() + " to " + targetDBLocation.getCanonicalPath());
			if (tempDbFolder.renameTo(targetDBLocation))
			{
				if (validateExistingDb(targetDBLocation, groupId, artifactId, version, classifier)) {
					log.info("Using existing db folder: " + targetDBLocation.getAbsolutePath());

					return targetDBLocation;
				} else {
					log.error("Failed to validate the new database");
					throw new RuntimeException("Failed to validate the new DB");
				}
			}
			else
			{
				log.error("Failed to rename the database");
				throw new RuntimeException("Failed to rename the DB folder");
			}
		}
		catch (Exception e)
		{
			log.error("existing downloadDB method with error: " + e);
			//cleanup
			try
			{
				if (tempDbFolder != null)
				{
					FileUtils.deleteDirectory(tempDbFolder);
				}
			}
			catch (Exception e1)
			{
				log.error("Unexpected error during cleanup", e1);
			}
			throw e;
		}
	}

	/**
	 * @return true if this is a debug deployment (in eclipse, or context contains SNAPSHOT)
	 */
	public boolean isDebugDeploy()
	{
		return debugMode;
	}

	/**
	 * @return String context path, which is a hard-coded value if in eclipse Jetty
	 */
	public String getContextPath() {
		return contextPath;
	}

	/**
	 * @return
	 */
	public static byte[] getSecret()
	{
		return secret_;
	}

	public RestSystemInfo getSystemInfo()
	{
		return systemInfo_;
	}

	public ServletContext getServletContext()
	{
		return context_;
	}
	
	private String getDatabaseIsaacMetadataVersion() {
		String version = "";
		Stream<SememeChronology<? extends DescriptionSememe<?>>> ds = Get.sememeService()
				.getDescriptionsForComponent(MetaData.ISAAC_METADATA.getNid());
		for (Object o : ds.toArray()) {
			SememeChronology<? extends DescriptionSememe<?>> x = (SememeChronology<? extends DescriptionSememe<?>>)o;
			for(DescriptionSememe d : x.getVersionList()) {
				if (d.getText() != null && "version".equalsIgnoreCase(d.getText()) && StringUtils.isNotEmpty(version));
				{
					version = d.getText().substring(d.getText().lastIndexOf(":")+1);
				}
			}
			if (StringUtils.isNotEmpty(version)) break;
		};
		return version;
	}
}
