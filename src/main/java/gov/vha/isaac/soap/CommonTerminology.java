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
package gov.vha.isaac.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.util.StringUtils;

import gov.va.med.term.services.exception.STSException;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.TaxonomyService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeService;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUtility;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.api.coordinate.LanguageCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.api.index.SearchResult;
import gov.vha.isaac.ochre.api.util.NumericUtils;
import gov.vha.isaac.ochre.associations.AssociationInstance;
import gov.vha.isaac.ochre.associations.AssociationUtilities;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.ochre.modules.vhat.VHATConstants;
import gov.vha.isaac.ochre.query.provider.lucene.indexers.SememeIndexer;
import gov.vha.isaac.soap.services.dao.TerminologyConfigHelper;
import gov.vha.isaac.soap.services.dto.config.MapSetConfig;
import gov.vha.isaac.soap.transfer.ConceptDetailTransfer;
import gov.vha.isaac.soap.transfer.DesignationDetailTransfer;
import gov.vha.isaac.soap.transfer.MapEntryValueListTransfer;
import gov.vha.isaac.soap.transfer.MapEntryValueTransfer;
import gov.vha.isaac.soap.transfer.PropertyTransfer;
import gov.vha.isaac.soap.transfer.RelationshipTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsListTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsTransfer;
import gov.vha.isaac.soap.transfer.ValueSetTransfer;
import javassist.runtime.Desc;

public class CommonTerminology {
	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final int MAX_PAGE_SIZE = 5000;

	public static final String AUTHORING_VERSION_NAME = "Authoring Version";

	private static Logger log = LogManager.getLogger(CommonTerminology.class);

	static TaxonomyService ts = Get.taxonomyService();

	private static StampCoordinate STAMP_COORDINATES = new StampCoordinateImpl(StampPrecedence.PATH,
			new StampPositionImpl(System.currentTimeMillis(), MetaData.DEVELOPMENT_PATH.getConceptSequence()),
			ConceptSequenceSet.EMPTY, State.ANY_STATE_SET);

	public static final String CONCEPT_CODE_TYPE = "ConceptCode";
	public static final String DESIGNATION_CODE_TYPE = "DesignationCode";
	public static final String DESIGNATION_NAME_TYPE = "DesignationName";
	public static final int MAP_ENTRIES_CALL = 1;
	public static final int MAP_ENTRIES_FROM_SOURCE_CALL = 2;

	public static final String DESIGNATION_TYPE_NAME_KEY_PREFIX = "DTN:";
	public static final String DESIGNATION_TYPE_ID_KEY_PREFIX = "DTID:";
	public static final String MAP_SET_KEY_PREFIX = "MS:";
	public static final String VERSION_NAME_KEY_PREFIX = "V_NAME:";
	public static final String VERSION_ID_KEY_PREFIX = "V_ID:";

	public static final String CURRENT_VERSION = "current";

	/**
	 * 
	 * @param codeSystemVuid
	 * @param versionName
	 * @param code
	 * @return
	 * @throws STSException
	 */
	public static ConceptDetailTransfer getConceptDetail(Long codeSystemVuid, String versionName, String code)
			throws STSException {

		// validate input
		prohibitAuthoringVersion(versionName);

		prohibitNullValue(codeSystemVuid, "Code System VUID");

		// Version name always "current" until we add versions
		prohibitNullValue(versionName, "Version name");

		prohibitNullValue(code, "Concept code");

		ConceptService conceptService = Get.conceptService();
		SememeService sememeService = Get.sememeService();

		// get code system by codeSystemVUID
		Integer codeSystemNid = Frills.getNidForVUID(codeSystemVuid).orElse(null);
		log.debug("codeSystemNid:" + codeSystemNid);
		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> codeSystemConcept = conceptService
				.getOptionalConcept(Get.identifierService().getConceptNid(codeSystemNid));

		if (!codeSystemConcept.isPresent()) {
			throw new STSException("Code system VUID '" + codeSystemVuid + "' not found.");
		}

		log.debug("Terminology Type :" + codeSystemConcept.get().getConceptSequence() + " : "
				+ codeSystemConcept.get().getConceptDescriptionText());

		validateVersionName(versionName, codeSystemConcept.get().getConceptDescriptionText(), true);

		// if code is VUID do direct lookup, if contains ap
		Optional<Integer> intValue = NumericUtils.getInt(code);

		Optional<? extends SememeChronology<? extends SememeVersion<?>>> sememe;
		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept;

		if (intValue.isPresent()) {

			Integer nid = Frills.getNidForVUID(intValue.orElse(0)).orElse(0);
			if (nid != 0) {
				concept = conceptService.getOptionalConcept(Get.identifierService().getConceptNid(nid));
			} else {
				throw new STSException("Cannot find concept with code: " + code);
			}

		} else {
			// do search
			List<SearchResult> ochreSearchResults = LookupService.get().getService(SememeIndexer.class)
					.query("'" + code + "'", Integer.MAX_VALUE);

			if (ochreSearchResults == null || ochreSearchResults.size() < 1) {
				throw new STSException("Cannot find concept with code: " + code);
			}

			log.debug("count: " + ochreSearchResults.size());

			sememe = sememeService.getOptionalSememe(ochreSearchResults.get(0).getNid());

			concept = conceptService.getOptionalConcept(
					Get.identifierService().getConceptNid(sememe.get().getReferencedComponentNid()));

			if (!sememe.isPresent()) {
				throw new STSException("Cannot find concept with code: " + code);
			}

		}

		ConceptDetailTransfer conceptDetailTransfer = new ConceptDetailTransfer();

		if (concept.isPresent()) {
			@SuppressWarnings("rawtypes")
			ConceptChronology conceptChronology = concept.get();

			try {

				conceptDetailTransfer.setConceptCode(getCodeFromNid(conceptChronology.getNid()));
				conceptDetailTransfer.setConceptStatus(
						convertStateToString(conceptChronology.isLatestVersionActive(STAMP_COORDINATES)));

				// create and populate list of PropertyTransfer objects
				List<PropertyTransfer> properties = getConceptProperties(conceptChronology);
				conceptDetailTransfer.setProperties(properties);

				// create and populate list of DesignationDetailTransfer objects
				List<DesignationDetailTransfer> designations = getDesignations(conceptChronology);
				conceptDetailTransfer.setDesignations(designations);

				// create and populate list of RelationshipTransfer objects
				Collection<RelationshipTransfer> relationships = getRelationships(conceptChronology);
				conceptDetailTransfer.setRelationships(relationships);

			} catch (Exception ex) {
				String msg = String.format("A system error occured while searching for %s.", code);
				log.error(msg, ex);
				throw new STSException(msg);
			}
		} else {
			throw new STSException("Cannot find concept with code: " + code);
		}

		return conceptDetailTransfer;
	}

	/**
	 * 
	 * @param subsetVuid
	 *            VHAT subset VUID. Required.
	 * @param versionName
	 *            VHAT version. For now, only "current" is allowed.
	 * @param designationName
	 *            Name of designation to retrieve. Required.
	 * @param membershipStatus
	 *            Active or Inactive. Optional/null
	 * @param pageSize
	 *            Number of designations to retrieve. Optional
	 * @param pageNumber
	 * @return
	 * @throws STSException
	 */
	public static ValueSetContentsListTransfer getValueSetContents(Long subsetVuid, String versionName,
			String designationName, String membershipStatus, Integer pageSize, Integer pageNumber) throws STSException {

		// validate parameters
		subsetVuid = validateSubsetVuid(subsetVuid);
		prohibitAuthoringVersion(versionName);

		pageSize = validatePageSize(pageSize);
		pageNumber = validatePageNumber(pageNumber);

		ConceptService conceptService = Get.conceptService();

		ValueSetContentsListTransfer valueSetContentsList = new ValueSetContentsListTransfer();
		List<ValueSetContentsTransfer> valueSetContents = new ArrayList<>();

		Integer nid = Frills.getNidForVUID(subsetVuid).orElse(0);

		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> concept = conceptService
				.getOptionalConcept(Get.identifierService().getConceptNid(nid));

		if (concept.isPresent()) {

			versionName = validateVersionName(versionName, concept.get().getConceptDescriptionText(), true);

			Get.sememeService().getSememesFromAssemblage(concept.get().getConceptSequence()).forEach(sememe -> {

				SememeChronology<? extends SememeVersion<?>> ss = Get.sememeService()
						.getSememe(sememe.getReferencedComponentNid());

				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<DescriptionSememe>> descriptionVersion = ((SememeChronology) ss)
						.getLatestVersion(DescriptionSememe.class, STAMP_COORDINATES);

				if (descriptionVersion.isPresent()) {

					ValueSetContentsTransfer valueSet = new ValueSetContentsTransfer();

					@SuppressWarnings({ "rawtypes", "unchecked" })
					List<DescriptionSememe<?>> descSememeList = ((SememeChronology) ss)
							.getVisibleOrderedVersionList(STAMP_COORDINATES);
					Collections.reverse(descSememeList);

					for (DescriptionSememe<?> ds : descSememeList) {
						String name = ds.getText();
						String status = getSubsetMembershipStatus(ss);

						// do no add if no designation name or vuid or status
						if ((StringUtils.isNullOrEmpty(designationName)
								|| org.apache.commons.lang3.StringUtils.containsIgnoreCase(name, designationName))
								&& (StringUtils.isNullOrEmpty(membershipStatus) || status.equals(membershipStatus))) {

							String vuid = getCodeFromNid(ds.getNid());
							if (vuid != null) {
								valueSet.setDesignationVuid(Long.valueOf(vuid));
							}

							valueSet.setDesignationName(ds.getText());
							valueSet.setDesignationStatus(ds.getState() == State.ACTIVE ? "active" : "inactive");							
							valueSet.setDesignationType(getPreferredTermNameType(ds.getNid()));
							valueSet.setMembershipStatus(status);

							if (!StringUtils.isNullOrEmpty(valueSet.getDesignationName())
									|| (valueSet.getDesignationVuid() != null)
									|| !StringUtils.isNullOrEmpty(valueSet.getDesignationStatus())) {
								valueSetContents.add(valueSet);
							}
						}
					}
				}
			});

		} else {
			throw new STSException(String.format("Subset vuid '%s' does not exist!", subsetVuid));
		}

		int resultStart = (pageNumber - 1) * pageSize;
		int resultEnd = pageNumber * pageSize;
		resultEnd = (resultEnd < valueSetContents.size()) ? resultEnd : valueSetContents.size();

		if (resultEnd > resultStart) {
			valueSetContentsList.setValueSetContentsTransfers(valueSetContents.subList(resultStart, resultEnd));
		}
		valueSetContentsList.setTotalNumberOfRecords(Long.valueOf(valueSetContents.size()));

		return valueSetContentsList;

	}

	/**
	 * 
	 * @param versionName
	 * @return
	 */
	private static String validateVersionName(String versionName, String codeSystemName, boolean isRequired)
			throws STSException {

		if (isRequired && (versionName == null || versionName.trim().equals(""))) {
			throw new STSException("Version name is a required parameter.");
		}

		if (!versionName.equalsIgnoreCase(CURRENT_VERSION)) {
			throw new STSException(
					"Code system '" + codeSystemName + "' with version name '" + versionName + "' not found.");
		}

		return versionName;
	}

	/**
	 * 
	 * @param subsetVuid
	 * @return
	 */
	private static Long validateSubsetVuid(Long subsetVuid) {
		// TODO Auto-generated method stub
		return subsetVuid;
	}

	/**
	 * 
	 * @param mapSetVuid
	 * @param mapSetVersionName
	 * @param sourceValues
	 * @param sourceDesignationTypeName
	 * @param targetDesignationTypeName
	 * @param pageSize
	 * @param pageNumber
	 * @return
	 * @throws STSException
	 */
	public static MapEntryValueListTransfer getMapEntriesFromSources(Long mapSetVuid, String mapSetVersionName,
			Collection<String> sourceValues, String sourceDesignationTypeName, String targetDesignationTypeName,
			Integer pageSize, Integer pageNumber) throws STSException {

		prohibitAuthoringVersion(mapSetVersionName);
		prohibitNullValue(mapSetVuid, "MapSet VUID");
		prohibitNullValue(mapSetVersionName, "MapSet version name");
		prohibitNullValue(sourceValues, "Source values");

		// only allowing current for now
		if (StringUtils.isNullOrEmpty(mapSetVersionName) || !mapSetVersionName.equalsIgnoreCase(CURRENT_VERSION)) {
			throw new STSException("Version name '" + mapSetVersionName + "' does not exist.");
		}

		pageSize = validatePageSize(pageSize);
		pageNumber = validatePageNumber(pageNumber);

		MapEntryValueListTransfer mapEntryValueListTransfer = new MapEntryValueListTransfer();
		List<MapEntryValueTransfer> mapEntryValueTransferList = new ArrayList<>();
		mapEntryValueListTransfer.setTotalNumberOfRecords(0L);

		List<Long> mapSetsNotAcccessibleVuidList = TerminologyConfigHelper.getMapSetsNotAccessibleVuidList();

		if (mapSetsNotAcccessibleVuidList.contains(mapSetVuid)) {
			return mapEntryValueListTransfer;
		}

		MapSetConfig mapSetConfig = TerminologyConfigHelper.getMapSet(mapSetVuid);
		if (mapSetConfig.isFound() == false) {
			log.info("WARNING: MapSet configuration for VUID: " + mapSetVuid + " not found - using defaults.");
		}

		Integer nid = Frills.getNidForVUID(mapSetVuid).orElse(0);

		ConceptService conceptService = Get.conceptService();

		ConceptChronology<? extends ConceptVersion<?>> concept = conceptService
				.getOptionalConcept(Get.identifierService().getConceptNid(nid)).orElse(null);

		if (concept != null) {

			Get.sememeService()
					.getSememesForComponentFromAssemblage(concept.getConceptSequence(),
							IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getConceptSequence())
					.forEach(mappingSememe -> {

						// Source and Target CodeSystem
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Optional<LatestVersion<? extends DynamicSememe>> mappingSememeVersion = ((SememeChronology) mappingSememe)
								.getLatestVersion(DynamicSememe.class, STAMP_COORDINATES);

						if (mappingSememeVersion.isPresent()) {
							// Get referenced component for the MapSet values
							ConceptChronology<? extends ConceptVersion<?>> cc = Get.conceptService()
									.getConcept(mappingSememeVersion.get().value().getReferencedComponentNid());
							@SuppressWarnings({ "rawtypes", "unchecked" })
							Optional<LatestVersion<ConceptVersion<?>>> cv = ((ConceptChronology) cc)
									.getLatestVersion(ConceptVersion.class, STAMP_COORDINATES);

							if (cv.isPresent()) {
								mapEntryValueTransferList.addAll(readMapEntryTypes(
										cv.get().value().getChronology().getNid(), mapSetConfig, sourceValues));

							}
						}
					});
		} else {
			throw new STSException("Map Set VUID: " + mapSetVuid + " does not exist.");
		}

		int resultStart = (pageNumber - 1) * pageSize;
		int resultEnd = pageNumber * pageSize;
		resultEnd = (resultEnd < mapEntryValueTransferList.size()) ? resultEnd : mapEntryValueTransferList.size();

		if (resultEnd > resultStart) {
			mapEntryValueListTransfer
					.setMapEntryValueTransfers(mapEntryValueTransferList.subList(resultStart, resultEnd));
		}

		mapEntryValueListTransfer.setTotalNumberOfRecords(Long.valueOf(mapEntryValueTransferList.size()));

		return mapEntryValueListTransfer;
	}

	/**
	 * 
	 * @param pageSize
	 * @return
	 * @throws STSException
	 */
	private static Integer validatePageSize(Integer pageSize) throws STSException {
		if (pageSize == null) {
			pageSize = DEFAULT_PAGE_SIZE;
		} else if (pageSize > MAX_PAGE_SIZE) {
			throw new STSException(String.format("Page size exceeded maximum size of: %s", MAX_PAGE_SIZE));
		} else if (pageSize < 1) {
			throw new STSException(String.format("Invalid page size (%s).", pageSize));
		}

		return pageSize;
	}

	/**
	 * 
	 * @param pageNumber
	 * @return
	 * @throws STSException
	 */
	private static Integer validatePageNumber(Integer pageNumber) throws STSException {
		if (pageNumber == null) {
			pageNumber = 1;
		} else if (pageNumber < 1) {
			throw new STSException(String.format("Invalid page number (%s).", pageNumber));
		}

		return pageNumber;
	}

	/**
	 * 
	 * @param versionName
	 * @throws STSException
	 */
	private static void prohibitAuthoringVersion(String versionName) throws STSException {
		if (AUTHORING_VERSION_NAME.equals(versionName)) {
			throw new STSException(String.format("%s is not an allowed version name.", AUTHORING_VERSION_NAME));
		}
	}

	/**
	 * 
	 * @param value
	 * @param valueName
	 * @throws STSException
	 */
	private static void prohibitNullValue(Object value, String valueName) throws STSException {
		if (value == null) {
			String singularOrPlural = (valueName.endsWith("s") == true) ? "are" : "is";
			throw new STSException(valueName + " " + singularOrPlural + " required.");
		}
	}

	/**
	 * 
	 * @param concept
	 * @param startDate
	 * @param endDate
	 * @param constructor
	 * @return a List of DesignationTypes for the concept
	 */
	private static List<DesignationDetailTransfer> getDesignations(ConceptChronology<?> concept) {

		List<DesignationDetailTransfer> designations = new ArrayList<>();

		Get.sememeService().getSememesForComponent(concept.getNid())
				.filter(s -> s.getSememeType() == SememeType.DESCRIPTION).forEach(sememe -> {

					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<LatestVersion<DescriptionSememe>> descriptionVersion = ((SememeChronology) sememe)
							.getLatestVersion(DescriptionSememe.class, STAMP_COORDINATES);
					if (descriptionVersion.isPresent()) {
						DesignationDetailTransfer d = new DesignationDetailTransfer();

						d.setCode(getCodeFromNid(sememe.getNid()));
						d.setName(descriptionVersion.get().value().getText());
						d.setStatus(convertStateToString(descriptionVersion.get().value().getState()));
						d.setType(getPreferredTermNameType(sememe.getNid()));
						
						List<PropertyTransfer> properties = new ArrayList<>();
						List<ValueSetTransfer> subsets = new ArrayList<>();

						Get.sememeService().getSememesForComponent(sememe.getNid()).forEach((nestedSememe) -> {

							// skip code and vuid properties - they are
							// handled already
							if (nestedSememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()
									&& nestedSememe.getAssemblageSequence() != MetaData.CODE.getConceptSequence()) {
								if (ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
										VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid())) {
									PropertyTransfer property = buildProperty(nestedSememe);
									if (property != null) {
										properties.add(property);
									}
								}

								// a refset that doesn't represent a mapset
								else if (ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
										VHATConstants.VHAT_REFSETS.getNid())
										&& !ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
												IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE
														.getNid())) {
									ValueSetTransfer subset = buildSubsetMembership(nestedSememe);
									if (subset != null) {
										subsets.add(subset);
									}
								}
							}
						});
						d.setProperties(properties);
						d.setSubsets(subsets);
						designations.add(d);
					}
				});
		return designations;
	}

	/**
	 * 
	 * @param componentNid
	 * @return the Code value found based on the Nid
	 */
	private static String getCodeFromNid(int componentNid) {

		Optional<SememeChronology<? extends SememeVersion<?>>> sc = Get.sememeService()
				.getSememesForComponentFromAssemblage(componentNid, MetaData.CODE.getConceptSequence()).findFirst();

		// FIX: get code when code is not a VUID. ie LOINC vs VHAT.

		if (sc.isPresent()) {
			// There was a bug in the older terminology loaders which loaded
			// 'Code' as a static sememe, but marked it as a dynamic sememe.
			// So during edits, new entries would get saves as dynamic sememes,
			// while old entries were static. Handle either....

			if (sc.get().getSememeType() == SememeType.STRING) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<StringSememe<?>>> sv = ((SememeChronology) sc.get())
						.getLatestVersion(StringSememe.class, STAMP_COORDINATES);
				if (sv.isPresent()) {
					return sv.get().value().getString();
				}
			}
			// this path will become dead code, after the data is fixed.
			else if (sc.get().getSememeType() == SememeType.DYNAMIC) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Optional<LatestVersion<? extends DynamicSememe>> sv = ((SememeChronology) sc.get())
						.getLatestVersion(DynamicSememe.class, STAMP_COORDINATES);
				if (sv.isPresent()) {
					if (sv.get().value().getData() != null && sv.get().value().getData().length == 1) {
						return sv.get().value().getData()[0].dataToString();
					}
				}
			} else {
				log.error("Unexpected sememe type for 'Code' sememe on nid " + componentNid);
			}
		}
		return null;
	}

	private static List<PropertyTransfer> getConceptProperties(ConceptChronology<?> concept) {

		List<PropertyTransfer> properties = new ArrayList<>();

		Get.sememeService().getSememesForComponent(concept.getNid())
				.filter(s -> s.getSememeType() != SememeType.DESCRIPTION).forEach(sememe -> {
					if (sememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()
							&& sememe.getAssemblageSequence() != MetaData.CODE.getConceptSequence() && ts.wasEverKindOf(
									sememe.getAssemblageSequence(), VHATConstants.VHAT_ATTRIBUTE_TYPES.getNid())) {
						PropertyTransfer property = buildProperty(sememe);
						if (property != null) {
							properties.add(buildProperty(sememe));
						}
					}
				});

		return properties;
	}

	/**
	 * 
	 * @param sememe
	 * @return A PropertyType object for the property, or null
	 */
	private static PropertyTransfer buildProperty(SememeChronology<?> sememe) {
		PropertyTransfer property = new PropertyTransfer();

		if (sememe.getSememeType() == SememeType.DYNAMIC) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<? extends DynamicSememe>> sememeVersion = ((SememeChronology) sememe)
					.getLatestVersion(DynamicSememe.class, STAMP_COORDINATES);
			if (sememeVersion.isPresent() && sememeVersion.get().value().getData() != null
					&& sememeVersion.get().value().getData().length > 0) {

				if (!"has_parent".equals(sememeVersion.get().value().getData())) {
					property.setValue(sememeVersion.get().value().getData()[0] == null ? null
							: sememeVersion.get().value().getData()[0].dataToString());
					property.setStatus(convertStateToString(sememeVersion.get().value().getState()));
				}
			}
		} else if (sememe.getSememeType() == SememeType.STRING) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Optional<LatestVersion<? extends StringSememe>> sememeVersion = ((SememeChronology) sememe)
					.getLatestVersion(StringSememe.class, STAMP_COORDINATES);
			if (sememeVersion.isPresent()) {

				if (!"has_parent".equals(sememeVersion.get().value().getString())) {
					property.setValue(sememeVersion.get().value().getString());
					property.setStatus(convertStateToString(sememeVersion.get().value().getState()));
				}
			}
		} else {
			log.warn("Unexpectedly passed sememe " + sememe + " when we only expected a dynamic or a string type");
			return null;
		}

		property.setType(
				getPreferredNameDescriptionType(Get.identifierService().getConceptNid(sememe.getAssemblageSequence())));
		return property;
	}

	/**
	 * 
	 * @param conceptNid
	 * @return the preferred description type for the concept
	 */
	private static String getPreferredNameDescriptionType(int conceptNid) {
		ArrayList<String> descriptions = new ArrayList<>(1);
		ArrayList<String> inActiveDescriptions = new ArrayList<>(1);
		Get.sememeService().getDescriptionsForComponent(conceptNid).forEach(sememeChronology -> {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			Optional<LatestVersion<DescriptionSememe<?>>> latestVersion = ((SememeChronology) sememeChronology)
					.getLatestVersion(DescriptionSememe.class, STAMP_COORDINATES);
			if (latestVersion.isPresent() && VHATConstants.VHAT_PREFERRED_NAME.getPrimordialUuid().equals(
					Frills.getDescriptionExtendedTypeConcept(STAMP_COORDINATES, sememeChronology.getNid(), true).orElse(null))) {
				if (latestVersion.get().value().getState() == State.ACTIVE) {
					descriptions.add(latestVersion.get().value().getText());
				} else {
					inActiveDescriptions.add(latestVersion.get().value().getText());
				}
			}
		});

		if (descriptions.size() == 0) {
			descriptions.addAll(inActiveDescriptions);
		}
		if (descriptions.size() == 0) {
			// This doesn't happen for concept that represent subsets, for
			// example.
			log.debug("Failed to find a description flagged as preferred on concept "
					+ Get.identifierService().getUuidPrimordialForNid(conceptNid));
			String description = Frills.getDescription(conceptNid, STAMP_COORDINATES,
					LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate()).orElse("ERROR!");
			if (description.equals("ERROR!")) {
				log.error("Failed to find any description on concept "
						+ Get.identifierService().getUuidPrimordialForNid(conceptNid));
			}
			return description;
		}
		if (descriptions.size() > 1) {
			log.warn("Found " + descriptions.size() + " descriptions flagged as the 'Preferred' vhat type on concept "
					+ Get.identifierService().getUuidPrimordialForNid(conceptNid));
		}
		return descriptions.get(0);
	}

	/**
	 *
	 * @param sememe
	 *            the Chronicle object (concept) representing the Subset
	 * @param startDate
	 * @param endDate
	 * @return the SubsetMembership object built for the sememe, or null
	 */
	private static ValueSetTransfer buildSubsetMembership(SememeChronology<?> sememe) {
		if (sememe.getSememeType() == SememeType.DYNAMIC) {
			ValueSetTransfer subset = new ValueSetTransfer();

			subset.setName(getPreferredNameDescriptionType(
					Get.identifierService().getConceptNid(sememe.getAssemblageSequence())));

			long vuid = Frills
					.getVuId(Get.identifierService().getConceptNid(sememe.getAssemblageSequence()), STAMP_COORDINATES)
					.orElse(0L).longValue();
			if (vuid > 0) {
				subset.setVuid(vuid);
			} else {
				log.warn("No VUID found for Subset UUID: " + sememe.getPrimordialUuid());
			}

			subset.setStatus(sememe.isLatestVersionActive(STAMP_COORDINATES) ? "Active" : "Inactive");

			return subset;
		} else {
			log.error("Unexpected sememe type! " + sememe);
			return null;
		}
	}

	/**
	 *
	 * @param concept
	 * @param startDate
	 * @param endDate
	 * @return a List of Relationship objects for the concept
	 */
	private static Collection<RelationshipTransfer> getRelationships(ConceptChronology<?> concept) {

		List<RelationshipTransfer> relationships = new ArrayList<>();

		for (AssociationInstance ai : AssociationUtilities.getSourceAssociations(concept.getNid(), STAMP_COORDINATES)) {
			RelationshipTransfer relationship = new RelationshipTransfer();
			String name = null;
			String code = null;

			try {

				if (ai.getTargetComponent().isPresent()) {
					name = getPreferredNameDescriptionType(ai.getTargetComponent().get().getNid());
					if (name != null && !name.isEmpty()) {
						relationship.setName(name);
					}
					code = getCodeFromNid(ai.getTargetComponent().get().getNid());
					if (code != null && !code.isEmpty()) {
						relationship.setCode(code);
					}
				}

				relationship.setType(ai.getAssociationType().getAssociationName());
				relationship.setStatus(convertStateToString(ai.getData().getState()));

			} catch (Exception e) {
				log.error("Association build failure");
			}
			if (relationship != null) {
				relationships.add(relationship);
			}
		}
		return relationships;
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	private static String convertStateToString(State state) {
		return (state.isActive()) ? "Active" : "Inactive";
	}

	/**
	 * 
	 * @param state
	 * @return
	 */
	private static String convertStateToString(Boolean state) {
		return (state) ? "Active" : "Inactive";
	}

	/**
	 * 
	 * @param sememe
	 * @return
	 */
	private static String getSubsetMembershipStatus(SememeChronology<?> sememe) {

		List<String> status = new ArrayList<>();

		Get.sememeService().getSememesForComponent(sememe.getNid()).forEach((nestedSememe) -> {
			if (nestedSememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()
					&& nestedSememe.getAssemblageSequence() != MetaData.CODE.getConceptSequence()) {
				if (ts.wasEverKindOf(nestedSememe.getAssemblageSequence(), VHATConstants.VHAT_REFSETS.getNid())
						&& !ts.wasEverKindOf(nestedSememe.getAssemblageSequence(),
								IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_SEMEME_TYPE.getNid())) {
					status.add(sememe.isLatestVersionActive(STAMP_COORDINATES) ? "active" : "inactive");
				}
			}
		});

		return (status != null && status.size() > 0) ? status.get(0) : null;
	}

	private static boolean stringExistsInList(Collection<String> collection, String stringToFind) {

		for (String s : collection) {
			if (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(stringToFind, s)) {
				return true;
			}
		}
		return false;
	}

	private static List<MapEntryValueTransfer> readMapEntryTypes(int componentNid, MapSetConfig mapSetConfig,
			Collection<String> sourceValues) {

		List<MapEntryValueTransfer> mapEntryValueTransferList = new ArrayList<>();

		Get.sememeService().getSememesFromAssemblage(Get.identifierService().getConceptSequence(componentNid))
				.forEach(sememe -> {

					@SuppressWarnings({ "unchecked", "rawtypes" })
					Optional<LatestVersion<? extends DynamicSememe>> sememeVersion = ((SememeChronology) sememe)
							.getLatestVersion(DynamicSememe.class, STAMP_COORDINATES);
					if (sememeVersion.isPresent() && sememeVersion.get().value().getData() != null
							&& sememeVersion.get().value().getData().length > 0) {
						MapEntryValueTransfer mapEntry = new MapEntryValueTransfer();

						mapEntry.setVuid(Frills.getVuId(sememe.getNid(), STAMP_COORDINATES).orElse(null));

						String code = getCodeFromNid(sememeVersion.get().value().getReferencedComponentNid());
						if (null == code) {
							code = Frills.getDescription(sememeVersion.get().value().getReferencedComponentNid())
									.orElse("");
						}

						mapEntry.setSourceValue(getPreferredNameDescriptionType(
								sememeVersion.get().value().getReferencedComponentNid()));

						// TODO: this shouldn't be hard coded.
						mapEntry.setSourceDesignationTypeName("Preferred Name");

						mapEntry.setStatus(sememeVersion.get().value().getState() == State.ACTIVE);

						DynamicSememeUtility ls = LookupService.get().getService(DynamicSememeUtility.class);
						if (ls == null) {
							throw new RuntimeException(
									"An implementation of DynamicSememeUtility is not available on the classpath");
						} else {
							DynamicSememeColumnInfo[] dsci = ls.readDynamicSememeUsageDescription(
									sememeVersion.get().value().getAssemblageSequence()).getColumnInfo();
							DynamicSememeData dsd[] = sememeVersion.get().value().getData();

							for (DynamicSememeColumnInfo d : dsci) {
								UUID columnUUID = d.getColumnDescriptionConcept();
								int col = d.getColumnOrder();

								if (dsd[col] != null && columnUUID != null) {
									if (columnUUID.equals(DynamicSememeConstants
											.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT
													.getPrimordialUuid())) {

										mapEntry.setTargetValue(Frills
												.getDescription(UUID.fromString(dsd[col].getDataObject().toString()))
												.orElse(""));

										UUID targetUuid = UUID.fromString(dsd[col].getDataObject().toString());
										try {
											ConceptChronology<? extends ConceptVersion<?>> targetComponent = Get
													.conceptService().getConcept(targetUuid);

											// TODO: FIX Target Designation Name
											mapEntry.setTargetDesignationName(
													getPreferredNameDescriptionType(targetComponent.getNid()));

											// TODO: FIX Target Code System Vuid
											mapEntry.setTargetCodeSystemVuid(
													Frills.getVuId(targetComponent.getNid()).orElse(9999999L));

										} catch (Exception ex) {
											// TODO
										}

										// TODO: this shouldn't be hard coded.
										mapEntry.setTargetDesignationTypeName("Preferred Name");
									} else if (columnUUID.equals(
											IsaacMappingConstants.get().MAPPING_CODE_DESCRIPTION.getPrimordialUuid())) {
										log.debug("MAPPING_CODE_DESCRIPTION:" + dsd[col].getDataObject().toString());
									} else if (columnUUID.equals(
											IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_EQUIVALENCE_TYPE
													.getPrimordialUuid())) {
										// Currently ignored, no XML
										// representation
									} else if (columnUUID
											.equals(IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_SEQUENCE
													.getPrimordialUuid())) {
										mapEntry.setOrder(Integer.parseInt(dsd[col].getDataObject().toString()));
									} else {
										log.warn("No mapping match found for UUID: ", columnUUID);
									}
								}
							}

							Get.sememeService()
									.getSememesForComponentFromAssemblage(componentNid,
											IsaacMappingConstants.get().DYNAMIC_SEMEME_MAPPING_STRING_EXTENSION
													.getConceptSequence())
									.forEach(mappingStrExt -> {
										Optional<LatestVersion<? extends DynamicSememe>> mappingStrExtVersion = ((SememeChronology) mappingStrExt)
												.getLatestVersion(DynamicSememe.class, STAMP_COORDINATES);

										if (mappingStrExtVersion.isPresent()) {
											DynamicSememeData dsd2[] = mappingStrExtVersion.get().value().getData();
											if (dsd2.length == 2) {
												if (dsd2[0].getDataObject().equals(
														IsaacMappingConstants.get().MAPPING_TARGET_CODE_SYSTEM_VERSION
																.getNid())) {
													mapEntry.setTargetCodeSystemVersionName(
															dsd2[1].getDataObject().toString());
												}
											}
										}
									});
						}

						// filter records based on match to source values based
						// on mapset config
						if (stringExistsInList(sourceValues, mapEntry.getSourceValue())) {
							mapEntryValueTransferList.add(mapEntry);
						}
					}
				});

		return mapEntryValueTransferList;
	}
	
	private static String getPreferredTermNameType(int sememeNid) {
		
		Optional<UUID> descType = Frills.getDescriptionExtendedTypeConcept(STAMP_COORDINATES, sememeNid, true);
		
		if (descType.isPresent())
		{
			LanguageCoordinate lang = Get.coordinateFactory().getUsEnglishLanguagePreferredTermCoordinate();
			return Frills.getDescription(descType.get(), STAMP_COORDINATES, lang).orElse(null);
		}
		else
		{
			return null;
		}
		
		
	}
	
}
