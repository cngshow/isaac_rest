/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.system;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.webcohesion.enunciate.metadata.Facet;

import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.api.util.UUIDUtil;
import gov.vha.isaac.rest.ExpandUtil;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.RestPaths;
import gov.vha.isaac.rest.api1.data.SystemInfo;
import gov.vha.isaac.rest.api1.data.concept.RestConceptChronology;
import gov.vha.isaac.rest.api1.data.enumerations.RestConcreteDomainOperatorsType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;
import gov.vha.isaac.rest.api1.data.enumerations.RestNodeSemanticType;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSememeType;
import gov.vha.isaac.rest.api1.data.enumerations.RestSupportedIdType;
import gov.vha.isaac.rest.api1.data.logic.RestConceptNode;
import gov.vha.isaac.rest.api1.data.logic.RestFeatureNode;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeBoolean;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeFloat;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeInstant;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeInteger;
import gov.vha.isaac.rest.api1.data.logic.RestLiteralNodeString;
import gov.vha.isaac.rest.api1.data.logic.RestRoleNode;
import gov.vha.isaac.rest.api1.data.logic.RestTypedConnectorNode;
import gov.vha.isaac.rest.api1.data.logic.RestUntypedConnectorNode;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeTypedData;
import gov.vha.isaac.rest.api1.data.sememe.RestDynamicSememeVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeChronology;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeDescriptionVersion;
import gov.vha.isaac.rest.api1.data.sememe.RestSememeLogicGraphVersion;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeBoolean;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeByteArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeDouble;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeFloat;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;
import gov.vha.isaac.rest.api1.data.systeminfo.RestDependencyInfo;
import gov.vha.isaac.rest.api1.data.systeminfo.RestLicenseInfo;
import gov.vha.isaac.rest.session.RequestInfo;
import gov.vha.isaac.rest.session.RequestParameters;


/**
 * {@link SystemAPIs}
 * 
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@Path(RestPaths.systemPathComponent)
public class SystemAPIs
{
	private static Logger log_ = LogManager.getLogger();

	/**
	 * @param id The id for which to retrieve objects. May be a UUID, NID or sequence
	 * @param expand comma separated list of fields to expand.  Support depends on type of object identified by the passed id
	 * RestConceptChronology supports 'versionsAll', 'versionsLatestOnly'
	 * RestSememeChronology supports 'chronology', 'nestedSememes', 'referencedDetails'
	 * @param coordToken specifies an explicit serialized CoordinateToken string specifying all coordinate parameters.
	 * 
	 * @return
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.identifiedObjectsComponent + "{" + RequestParameters.id + "}")  
	public List<Object> getIdentifiedObjects(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.expand) String expand,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RequestInfo.get().readExpandables(expand);
		List<Object> identifiedObjects = new ArrayList<>();
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (intId.get() < 0)
			{
				// id is NID
				ObjectChronologyType objectChronologyType = Get.identifierService().getChronologyTypeForNid(intId.get());
				switch(objectChronologyType) {
				case CONCEPT:
					identifiedObjects.add(
							new RestConceptChronology(
									Get.conceptService().getConcept(intId.get()),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable)));
					break;
				case SEMEME:
					identifiedObjects.add(
							new RestSememeChronology(
									Get.sememeService().getSememe(intId.get()),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
					break;
				case UNKNOWN_NID:
				default:
					throw new RestException(RequestParameters.id, id, "Specified NID is for unsupported ObjectChronologyType " + objectChronologyType);
				}
				
				if (identifiedObjects.size() == 0) {
					throw new RestException(RequestParameters.id, id, "Specified NID does not correspond to an existing concept or sememe");
				}

				return identifiedObjects;
			}
			else
			{
				// id is either sememe or concept sequence

				int conceptNid = Get.identifierService().getConceptNid(intId.get());
				if (conceptNid != 0) {
					identifiedObjects.add(
							new RestConceptChronology(
									Get.conceptService().getConcept(conceptNid),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable)));
				}

				int sememeNid = Get.identifierService().getSememeNid(intId.get());
				if (sememeNid != 0) {
					identifiedObjects.add(
							new RestSememeChronology(
									Get.sememeService().getSememe(sememeNid),
									RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
									RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
									RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
				}
			}

			if (identifiedObjects.size() == 0) {
				throw new RestException(RequestParameters.id, id, "Specified sequence does not correspond to an existing concept or sememe");
			}

			return identifiedObjects;
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer nid = null;

				if (Get.identifierService().hasUuid(uuidId.get()) && (nid = Get.identifierService().getNidForUuids(uuidId.get())) != 0) {
					ObjectChronologyType objectChronologyType = Get.identifierService().getChronologyTypeForNid(nid);
				
					switch(objectChronologyType) {
					case CONCEPT:
						identifiedObjects.add(
								new RestConceptChronology(
										Get.conceptService().getConcept(nid),
										RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
										RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable)));
						break;
					case SEMEME:
						identifiedObjects.add(
								new RestSememeChronology(
										Get.sememeService().getSememe(nid),
										RequestInfo.get().shouldExpand(ExpandUtil.versionsAllExpandable),	
										RequestInfo.get().shouldExpand(ExpandUtil.versionsLatestOnlyExpandable),
										RequestInfo.get().shouldExpand(ExpandUtil.nestedSememesExpandable),
										RequestInfo.get().shouldExpand(ExpandUtil.referencedDetails)));
						break;
					case UNKNOWN_NID:
					default:
						throw new RestException(RequestParameters.id, id, "Specified UUID is for NID " + nid + " for unsupported ObjectChronologyType " + objectChronologyType);
					}
					
					if (identifiedObjects.size() == 0) {
						throw new RestException(RequestParameters.id, id, "Specified UUID is for NID " + nid + " that does not correspond to an existing concept or sememe");
					}

					return identifiedObjects;
				
				} else {
					throw new RestException(RequestParameters.id, id, "No concept or sememe exists corresponding to the passed UUID id.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified string id is not a valid identifier.  Must be a UUID, or integer NID or sequence.");
			}
		}
	}
	
	/**
	 * Return the RestObjectChronologyType of the component corresponding to the passed id
	 * @param id The id for which to determine RestObjectChronologyType
	 * If an int < 0 then assumed to be a NID, else ambiguous and treated as a sememe or concept sequence, each of which may or may not correspond to existing components
	 * If a String then parsed and handled as a UUID of either a concept or sequence
	 * @return Map of RestObjectChronologyType to RestId.  Will contain exactly one entry if passed a UUID or NID, or one or two entries if passed a sequence. if no corresponding ids found a RestException is thrown.
	 * @param coordToken specifies an explicit serialized CoordinateToken string specifying all coordinate parameters.
	 * 
	 * @throws RestException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.objectChronologyTypeComponent + "{" + RequestParameters.id + "}")  
	public RestObjectChronologyType getObjectChronologyType(
			@PathParam(RequestParameters.id) String id,
			@QueryParam(RequestParameters.coordToken) String coordToken) throws RestException
	{
		RestObjectChronologyType returnedType = null;
		Optional<Integer> intId = NumericUtils.getInt(id);
		if (intId.isPresent())
		{
			if (intId.get() < 0)
			{
				// id is NID
				returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(intId.get()));
			}
			else
			{
				// id is either sememe or concept sequence

				int conceptNid = Get.identifierService().getConceptNid(intId.get());
				if (conceptNid != 0) {
					returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(conceptNid));
				}

				int sememeNid = Get.identifierService().getSememeNid(intId.get());
				if (sememeNid != 0) {
					if (returnedType != null) {
						throw new RestException(RequestParameters.id, id, "Specified int id is ambiguous, as it may be either a sememe or concept sequence. Must be a UUID, or integer NID or sequence that uniquely identifies either a sememe or concept, but not both.");
					}
					returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(sememeNid));
				}
			}

			if (returnedType != null) {
				return returnedType;
			} else {
				throw new RestException(RequestParameters.id, id, "Specified int id is not a valid NID or sequence. Must be a UUID, or integer NID or sequence that uniquely identifies either a sememe or concept, but not both.");
			}
		}
		else
		{
			Optional<UUID> uuidId = UUIDUtil.getUUID(id);
			if (uuidId.isPresent())
			{
				// id is uuid

				Integer nid = null;

				if (Get.identifierService().hasUuid(uuidId.get()) && (nid = Get.identifierService().getNidForUuids(uuidId.get())) != 0) {
					return returnedType = new RestObjectChronologyType(Get.identifierService().getChronologyTypeForNid(nid));
				} else {
					throw new RestException(RequestParameters.id, id, "No concept or sememe exists corresponding to the passed UUID id.");
				}
			}
			else
			{
				throw new RestException(RequestParameters.id, id, "Specified string id is not a valid identifier.  Must be a UUID, or integer NID or sequence.");
			}
		}
	}

	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeDataTypeComponent)  
	public RestDynamicSememeDataType[] getRestDynamicSememeDataTypes()
	{
		return RestDynamicSememeDataType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestDynamicSememeValidatorTypeComponent)  
	public RestDynamicSememeValidatorType[] getRestDynamicSememeValidatorTypes()
	{
		return RestDynamicSememeValidatorType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestObjectChronologyTypeComponent)
	public RestObjectChronologyType[] getRestObjectChronologyTypes()
	{
		return RestObjectChronologyType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSememeTypeComponent)
	public RestSememeType[] getRestObjectSememeTypes()
	{
		return RestSememeType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestConcreteDomainOperatorTypes)
	public RestConcreteDomainOperatorsType[] getRestConcreteDomainOperatorTypes()
	{
		return RestConcreteDomainOperatorsType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestNodeSemanticTypes)
	public RestNodeSemanticType[] getRestNodeSemanticTypes()
	{
		return RestNodeSemanticType.getAll();
	}
	
	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.enumerationRestSupportedIdTypes)
	public RestSupportedIdType[] getRestSupportedIdTypes()
	{
		return RestSupportedIdType.getAll();
	}

	/**
	 * Enumerate the valid types for the system.  These values can be cached for the life of the connection.
	 * TODO move functionality in getSystemInfo() into OCHRE
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(RestPaths.systemInfoComponent)
	public SystemInfo getSystemInfo()
	{
		final SystemInfo data = new SystemInfo();

		//Read in other information from the package (pom.properties file during normal runtime, pom.xml files if running in a dev env)
		try
		{
			AtomicBoolean readDbMetadataFromProperties = new AtomicBoolean(false);
			AtomicBoolean readDbMetadataFromPom = new AtomicBoolean(false);
			AtomicBoolean readAppMetadata = new AtomicBoolean(false);
			
			//Read the db metadata
			java.nio.file.Path dbLocation = LookupService.get().getService(ConfigurationService.class).getChronicleFolderPath().getParent();
			//find the pom.properties file in the hierarchy
			File dbMetadata = new File(dbLocation.toFile(), "META-INF");
			if (dbMetadata.isDirectory())
			{
				Files.walkFileTree(dbMetadata.toPath(), new SimpleFileVisitor<java.nio.file.Path>()
				{
					/**
					 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
					 */
					@Override
					public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) throws IOException
					{
						File f = path.toFile();
						if (f.isFile() && f.getName().toLowerCase().equals("pom.properties"))
						{
							Properties p = new Properties();
							p.load(new FileReader(f));

							RestDependencyInfo dbDependency =
									new RestDependencyInfo(
											p.getProperty("project.groupId"),
											p.getProperty("project.artifactId"),
											p.getProperty("project.version"),
											p.getProperty("resultArtifactClassifier"),
											p.getProperty("chronicles.type"));
							data.setIsaacDbDependency(dbDependency);
							
							data.setMetadataVersion(p.getProperty("isaac-metadata.version"));
							readDbMetadataFromProperties.set(true);
							return readDbMetadataFromPom.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
						} else if (f.isFile() && f.getName().toLowerCase().equals("pom.xml")) {
							DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder builder;
							Document dDoc = null;
							XPath xPath = XPathFactory.newInstance().newXPath();

							try {
								builder = domFactory.newDocumentBuilder();

								dDoc = builder.parse(f);
								
								{
									NodeList dbLicensesNodes = ((NodeList) xPath.evaluate("/project/licenses/license/name", dDoc, XPathConstants.NODESET));

									log_.debug("Found {} license names in DB pom.xml", dbLicensesNodes.getLength());
									for (int i = 0; i < dbLicensesNodes.getLength(); i++) {
										Node currentLicenseNameNode = dbLicensesNodes.item(i);
										String name = currentLicenseNameNode.getTextContent();

										RestLicenseInfo license =
												new RestLicenseInfo(
														name,
														((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/url", dDoc, XPathConstants.NODE)).getTextContent(),
														((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/comments", dDoc, XPathConstants.NODE)).getTextContent());
										data.addDbLicense(license);

										log_.debug("Extracted license \"{}\" from DB pom.xml: {}", name, license.toString());
									}
								}
								
								{
									NodeList dbDependenciesNodes = ((NodeList) xPath.evaluate("/project/dependencies/dependency/artifactId", dDoc, XPathConstants.NODESET));

									log_.debug("Found {} dependency artifactIds in DB pom.xml", dbDependenciesNodes.getLength());
									for (int i = 0; i < dbDependenciesNodes.getLength(); i++) {
										Node currentDbDependencyArtifactIdNode = dbDependenciesNodes.item(i);
										
										String artifactId = currentDbDependencyArtifactIdNode.getTextContent();
										String groupId = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/groupId", dDoc, XPathConstants.NODE)).getTextContent();
										String version = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/version", dDoc, XPathConstants.NODE)).getTextContent();
							
										String classifier = null;
										try {
											classifier = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/classifier", dDoc, XPathConstants.NODE)).getTextContent();
										} catch (Throwable t) {
											log_.debug("Problem reading \"classifier\" element for {}", artifactId);
										}
										String type = null;
										try {
											type = ((Node)xPath.evaluate("/project/dependencies/dependency[artifactId='" + artifactId + "']/type", dDoc, XPathConstants.NODE)).getTextContent();
										} catch (Throwable t) {
											log_.debug("Problem reading \"type\" element for {}", artifactId);
										}

										RestDependencyInfo dependencyInfo = new RestDependencyInfo(groupId, artifactId, version, classifier, type);
										data.addDbDependency(dependencyInfo);

										log_.debug("Extracted dependency \"{}\" from DB pom.xml: {}", artifactId, dependencyInfo.toString());
									}
								}
							} catch (XPathExpressionException | SAXException | ParserConfigurationException e) {
								throw new IOException(e);
							}

							readDbMetadataFromPom.set(true);
							return readDbMetadataFromProperties.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
						}

						return FileVisitResult.CONTINUE;
					}
				});
			}
			
			if (!readDbMetadataFromProperties.get())
			{
				log_.warn("Failed to read the metadata about the database from the database package.");
			}
			else
			{
				for (RestDependencyInfo dependency : data.getDbDependencies())
				{
					if (dependency.version != null && "${isaac-metadata.version}".equals(dependency.version))
					{
						dependency.version = data.setMetadataVersion(data.metadataVersion);
					}
				}
				log_.debug("Successfully read db properties from maven config files.  dbGroupId: {} dbArtifactId: {} dbVersion: {} dbClassifier: {} dbType: {}", 
						data.isaacDbDependency.groupId, data.isaacDbDependency.artifactId, data.isaacDbDependency.version, data.isaacDbDependency.classifier, data.isaacDbDependency.type);
			}
			
			//read the app metadata
			
			//if running from eclipse - our launch folder should be "fx-gui-assembly".  Go up one directory, read the pom file.
			File f = new File("").getAbsoluteFile();
			if (f.getName().endsWith("fx-gui-assembly"))
			{
				File pom = new File(f.getParent(), "pom.xml");
				if (pom.isFile())
				{
					DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder builder = domFactory.newDocumentBuilder();
					Document dDoc = builder.parse(pom);

					XPath xPath = XPathFactory.newInstance().newXPath();
					data.isaacVersion = ((Node) xPath.evaluate("/project/properties/isaac.version", dDoc, XPathConstants.NODE)).getTextContent();
					data.scmUrl= ((Node) xPath.evaluate("/project/scm/url", dDoc, XPathConstants.NODE)).getTextContent();
					data.assemblyVersion= ((Node) xPath.evaluate("/project/version", dDoc, XPathConstants.NODE)).getTextContent();
					data.isaacGuiVersion= ((Node) xPath.evaluate("/project/properties/isaac-fx-gui.version", dDoc, XPathConstants.NODE)).getTextContent();
					
					NodeList appLicensesNodes = ((NodeList) xPath.evaluate("/project/licenses/license/name", dDoc, XPathConstants.NODESET));

					log_.debug("Found {} license names", appLicensesNodes.getLength());
					for (int i = 0; i < appLicensesNodes.getLength(); i++) {
						Node currentLicenseNameNode = appLicensesNodes.item(i);
						String name = currentLicenseNameNode.getTextContent();
						
						RestLicenseInfo appLicenseInfo =
								new RestLicenseInfo(
										name,
										((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/url", dDoc, XPathConstants.NODE)).getTextContent(),
										((Node)xPath.evaluate("/project/licenses/license[name='" + name + "']/comments", dDoc, XPathConstants.NODE)).getTextContent());
						data.addAppLicense(appLicenseInfo);
						
						log_.debug("Extracted license \"{}\" from app pom.xml: {}", name, appLicenseInfo.toString());
					}

					readAppMetadata.set(true);
				}
			}
			//otherwise, running from an installation - we should have a META-INF folder
			File appMetadata = new File("META-INF");
			if (appMetadata.isDirectory())
			{
				Files.walkFileTree(appMetadata.toPath(), new SimpleFileVisitor<java.nio.file.Path>()
				{
					/**
					 * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
					 */
					@Override
					public FileVisitResult visitFile(java.nio.file.Path path, BasicFileAttributes attrs) throws IOException
					{
						File visitFile = path.toFile();
						if (visitFile.isFile() && visitFile.getName().toLowerCase().equals("pom.properties"))
						{
							Properties p = new Properties();
							p.load(new FileReader(visitFile));

							data.scmUrl = p.getProperty("scm.url");
							data.isaacVersion = p.getProperty("isaac.version");
							data.isaacGuiVersion = p.getProperty("isaac-fx-gui.version");
							data.assemblyVersion = p.getProperty("project.version");
							readAppMetadata.set(true);
							return FileVisitResult.TERMINATE;
						}
						return FileVisitResult.CONTINUE;
					}
					
				});
			}
			
			if (!readAppMetadata.get())
			{
				log_.warn("Failed to read the metadata about the app");
			}
			else
			{
				log_.debug("Successfully read app properties from maven config files.  assembly version: {} scmUrl: {} isaacVersion: {} isaac FX GUI version",
						data.assemblyVersion, data.scmUrl, data.isaacVersion, data.isaacGuiVersion);
			}
		}
		catch (Exception ex)
		{
			log_.warn("Unexpected error reading app configuration information", ex);
		}
		return data;
	}
	
	//TODO the code below this point (noop, class Z) is a hack workaround for the bug 
	//https://github.com/stoicflame/enunciate/issues/336
	/**
	 * This is not a valid operation.  Do not call.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/noop")
	@Facet ("ignore")  //prevent enunciate from documenting this method
	public Z noop() throws RestException
	{
		throw new RestException("These are not the droids you are looking for");
	}
	
	/**
	 * The {@link Z} object will never be returned by an API call.
	 */
	//Any class that is only referenced by an @XmlSeeAlso should be referenced here, otherwise
	//the generated ruby library code will miss the class.
	@XmlRootElement
	@Facet ("ignore")
	private class Z
	{
		//Put a reference here to any class that is only referenced by an @XmlSeeAlso
		@XmlElement RestConceptNode a1 = null;
		@XmlElement RestUntypedConnectorNode a2 = null;
		@XmlElement RestTypedConnectorNode a3 = null;
		@XmlElement RestLiteralNodeBoolean a4 = null;
		@XmlElement RestLiteralNodeInteger a5 = null;
		@XmlElement RestLiteralNodeFloat a6 = null;
		@XmlElement RestLiteralNodeString a7 = null;
		@XmlElement RestLiteralNodeInstant a8 = null;
		@XmlElement RestRoleNode a9 = null;
		@XmlElement RestDynamicSememeValidatorType a10 = null;
		@XmlElement RestDynamicSememeDataType a11 = null;
		@XmlElement RestObjectChronologyType a12 = null;
		@XmlElement RestSememeType a13 = null;
		@XmlElement RestFeatureNode a14 = null;
		@XmlElement RestSememeDescriptionVersion a15 = null;
		@XmlElement RestDynamicSememeVersion a16 = null;
		@XmlElement RestSememeLogicGraphVersion a17 = null;
		@XmlElement RestDynamicSememeVersion a19 = null;
		@XmlElement RestDynamicSememeArray a20 = null;
		@XmlElement RestDynamicSememeBoolean a21 = null;
		@XmlElement RestDynamicSememeByteArray a22 = null;
		@XmlElement RestDynamicSememeDouble a23 = null;
		@XmlElement RestDynamicSememeFloat a24 = null;
		@XmlElement RestDynamicSememeInteger a25 = null;
		@XmlElement RestDynamicSememeLong a26 = null;
		@XmlElement RestDynamicSememeNid a27 = null;
		@XmlElement RestDynamicSememeSequence a28 = null;
		@XmlElement RestDynamicSememeString a29 = null;
		@XmlElement RestDynamicSememeUUID a30 = null;
		@XmlElement RestDynamicSememeTypedData a31 = null;
	}
}
