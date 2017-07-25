package gov.vha.isaac.soap;

import gov.va.med.term.vhat.xml.model.ActionType;
import gov.va.med.term.vhat.xml.model.DesignationType;
import gov.va.med.term.vhat.xml.model.PropertyType;
import gov.va.med.term.vhat.xml.model.Terminology;
import gov.va.med.term.vhat.xml.model.Terminology.CodeSystem.Version.CodedConcepts.CodedConcept.Designations.Designation.SubsetMemberships.SubsetMembership;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.TaxonomyService;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.collections.ConceptSequenceSet;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.concept.ConceptService;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeType;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.ochre.api.coordinate.StampPrecedence;
import gov.vha.isaac.ochre.associations.AssociationInstance;
import gov.vha.isaac.ochre.associations.AssociationUtilities;
import gov.vha.isaac.ochre.impl.utility.Frills;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.concept.ConceptVersionImpl;
import gov.vha.isaac.ochre.model.configuration.LanguageCoordinates;
import gov.vha.isaac.ochre.model.coordinate.StampCoordinateImpl;
import gov.vha.isaac.ochre.model.coordinate.StampPositionImpl;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.soap.exception.STSException;
import gov.vha.isaac.soap.transfer.ConceptDetailTransfer;
import gov.vha.isaac.soap.transfer.DesignationDetailTransfer;
import gov.vha.isaac.soap.transfer.MapEntryValueListTransfer;
import gov.vha.isaac.soap.transfer.MapEntryValueTransfer;
import gov.vha.isaac.soap.transfer.PropertyTransfer;
import gov.vha.isaac.soap.transfer.RelationshipTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsListTransfer;
import gov.vha.isaac.soap.transfer.ValueSetContentsTransfer;
import gov.vha.isaac.soap.transfer.ValueSetTransfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommonTerminology {
	private static final int DEFAULT_PAGE_SIZE = 1000;
	private static final int MAX_PAGE_SIZE = 5000;

	private static Logger log = LogManager.getLogger(CommonTerminology.class);

	static TaxonomyService ts = Get.taxonomyService();

	// TODO: Source all the following hardcoded UUID values from MetaData, once
	// available
	// ConceptChronology: VHAT Attribute Types <261>
	// uuid:8287530a-b6b0-594d-bf46-252e09434f7e
	// VHAT Metadata -> "Attribute Types"
	final static UUID vhatPropertyTypesUUID = UUID.fromString("8287530a-b6b0-594d-bf46-252e09434f7e");
	final static int vhatPropertyTypesNid = Get.identifierService().getNidForUuids(vhatPropertyTypesUUID);

	// ConceptChronology: Refsets (ISAAC) <325>
	// uuid:fab80263-6dae-523c-b604-c69e450d8c7f
	// VHAT Metadata -> "Refsets"
	final static UUID vhatRefsetTypesUUID = UUID.fromString("fab80263-6dae-523c-b604-c69e450d8c7f");
	final static int vhatRefsetTypesNid = Get.identifierService().getNidForUuids(vhatRefsetTypesUUID);

	// conceptChronology: CODE (ISAAC) <77>
	// uuid:803af596-aea8-5184-b8e1-45f801585d17
	final static UUID codeAssemblageUUID = MetaData.CODE.getPrimordialUuid();
	final static int codeAssemblageConceptSeq = Get.identifierService().getConceptSequenceForUuids(codeAssemblageUUID);

	// ConceptChronology: Preferred Name (ISAAC) <257>
	// uuid:a20e5175-6257-516a-a97d-d7f9655916b8
	// VHAT Description Types -> Preferred Name
	final static UUID preferredNameExtendedType = UUID.fromString("a20e5175-6257-516a-a97d-d7f9655916b8");

	private static StampCoordinate STAMP_COORDINATES = new StampCoordinateImpl(StampPrecedence.PATH,
			new StampPositionImpl(System.currentTimeMillis(), MetaData.DEVELOPMENT_PATH.getConceptSequence()),
			ConceptSequenceSet.EMPTY, State.ANY_STATE_SET);

	public static ConceptDetailTransfer getConceptDetail(Long codeSystemVuid, String versionName, String code)
			throws STSException {
		prohibitAuthoringVersion(versionName);

		prohibitNullValue(codeSystemVuid, "Code System VUID");
		prohibitNullValue(versionName, "Version name");
		prohibitNullValue(code, "Concept code");

		ConceptService conceptService = Get.conceptService();

		Integer nid = Frills.getNidForVUID(codeSystemVuid).orElse(0);

		Optional<? extends ConceptChronology<? extends ConceptVersion<?>>> c = conceptService
				.getOptionalConcept(Get.identifierService().getConceptNid(nid));

		ConceptDetailTransfer conceptDetailTransfer = new ConceptDetailTransfer();

		if (c.isPresent()) {

			@SuppressWarnings("rawtypes")
			ConceptChronology concept = c.get();

			try {

				conceptDetailTransfer.setConceptCode(getCodeFromNid(concept.getNid()));
				conceptDetailTransfer
						.setConceptStatus(convertStateToString(concept.isLatestVersionActive(STAMP_COORDINATES)));

				// create and populate list of PropertyTransfer objects
				List<PropertyTransfer> properties = getConceptProperties(concept);
				if (properties != null && properties.size() > 0) {
					conceptDetailTransfer.setProperties(properties);
				}

				// create and populate list of DesignationDetailTransfer objects
				List<DesignationDetailTransfer> designations = getDesignations(concept);
				if (designations != null && designations.size() > 0) {
					conceptDetailTransfer.setDesignations(designations);
				}

				// create and populate list of RelationshipTransfer objects
				Collection<RelationshipTransfer> relationships = getRelationships(concept);
				if (relationships != null && relationships.size() > 0) {
					conceptDetailTransfer.setRelationships(relationships);
				}

			} catch (Exception ex) {

			}
		}

		return conceptDetailTransfer;
	}

	public static ValueSetContentsListTransfer getValueSetContents(Long subsetVuid, String versionName,
			String designationName, String membershipStatus, Integer pageSize, Integer pageNumber) throws STSException {
		prohibitAuthoringVersion(versionName);

		pageSize = validatePageSize(pageSize);
		pageNumber = validatePageNumber(pageNumber);

		ValueSetContentsListTransfer listTransfer = new ValueSetContentsListTransfer();
		
		//old code to be removed when code is complete 
		
		// SubsetContentsListView subsetContents =
		// TerminologyDelegate.getSubsetContents(subsetVuid, versionName,
		// designationName, membershipStatus, pageSize, pageNumber);
		// listTransfer.setTotalNumberOfRecords(subsetContents.getTotalNumberOfRecords());
		// List<ValueSetContentsTransfer> valueSetContent = new
		// ArrayList<ValueSetContentsTransfer>();
		// for (SubsetContentsView subsetContent :
		// subsetContents.getSubsetContentsView()) {
		// Long vuid = subsetContent.getDesignation().getVuid();
		// String name = subsetContent.getDesignation().getName();
		// String type = subsetContent.getDesignation().getType().getName();
		// String designationStatus =
		// (subsetContent.getDesignation().getActive()) ? "active" : "inactive";
		// String status = (subsetContent.getSubsetRelationship().getActive()) ?
		// "active" : "inactive";
		// valueSetContent.add(new ValueSetContentsTransfer(vuid, name,
		// designationStatus, type, status));
		// }
		// listTransfer.setValueSetContentsTransfers(valueSetContent);
		return listTransfer;
	}

	public static MapEntryValueListTransfer getMapEntriesFromSources(Long mapSetVuid, String mapSetVersionName,
			Collection<String> sourceValues, String sourceDesignationTypeName, String targetDesignationTypeName,
			Integer pageSize, Integer pageNumber) throws STSException {
		prohibitAuthoringVersion(mapSetVersionName);
		prohibitNullValue(mapSetVuid, "MapSet VUID");
		prohibitNullValue(mapSetVersionName, "MapSet version name");
		prohibitNullValue(sourceValues, "Source values");

		pageSize = validatePageSize(pageSize);
		pageNumber = validatePageNumber(pageNumber);

		MapEntryValueListTransfer mapEntryValueListTransfer = new MapEntryValueListTransfer();
		
		//old code to be removed when code is complete
		
		// List<Long> mapSetsNotAcccessibleVuidList =
		// TerminologyConfigDelegate.getMapSetsNotAccessibleVuidList();
		// if(mapSetsNotAcccessibleVuidList.contains(mapSetVuid)){
		// return mapEntryValueListTransfer;
		// }
		//
		// MapSetConfig mapSetConfig =
		// TerminologyDelegate.getMapSetConfig(mapSetVuid);
		// if (mapSetConfig.isFound() == false)
		// {
		// System.out.println("WARNING: MapSet configuration for VUID: " +
		// mapSetVuid + " not found - using defaults.");
		// }
		// MapEntryCacheListDTO mapEntryCacheList = null;
		//
		// mapEntryCacheList = TerminologyDelegate.getMapEntries(
		// TerminologyDelegate.MAP_ENTRIES_FROM_SOURCE_CALL, mapSetVuid,
		// mapSetVersionName,
		// sourceDesignationTypeName, targetDesignationTypeName,
		// sourceValues, mapSetConfig.getSourceType(), null,
		// mapSetConfig.getTargetType(),
		// null, null, null, null, pageSize, pageNumber);
		//
		// List<MapEntryValueTransfer> mapEntryValueTransferList = new
		// ArrayList<MapEntryValueTransfer>();
		// for (MapEntryCacheDTO mapEntryCacheDTO :
		// mapEntryCacheList.getMapEntryCaches())
		// {
		// MapEntryValueTransfer mapEntryValueTransfer = new
		// MapEntryValueTransfer();
		// mapEntryValueTransfer.setVuid(mapEntryCacheDTO.getMapEntryVuid());
		// if
		// (mapSetConfig.getSourceType().equals(TerminologyDelegate.CONCEPT_CODE_TYPE))
		// {
		// mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceConceptCode());
		// }
		// else if
		// (mapSetConfig.getSourceType().equals(TerminologyDelegate.DESIGNATION_CODE_TYPE))
		// {
		// mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceDesignationCode());
		// }
		// else if
		// (mapSetConfig.getSourceType().equals(TerminologyDelegate.DESIGNATION_NAME_TYPE))
		// {
		// mapEntryValueTransfer.setSourceValue(mapEntryCacheDTO.getSourceDesignationName());
		// }
		// DesignationType sourceDesType =
		// TerminologyDelegate.getCachedDesignationType(mapEntryCacheDTO.getSourceDesignationTypeId());
		// mapEntryValueTransfer.setSourceDesignationTypeName(sourceDesType.getName());
		//
		// if
		// (mapSetConfig.getTargetType().equals(TerminologyDelegate.CONCEPT_CODE_TYPE))
		// {
		// mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetConceptCode());
		// }
		// else if
		// (mapSetConfig.getTargetType().equals(TerminologyDelegate.DESIGNATION_CODE_TYPE))
		// {
		// mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetDesignationCode());
		// }
		// else if
		// (mapSetConfig.getTargetType().equals(TerminologyDelegate.DESIGNATION_NAME_TYPE))
		// {
		// mapEntryValueTransfer.setTargetValue(mapEntryCacheDTO.getTargetDesignationName());
		// }
		// DesignationType targetDesType =
		// TerminologyDelegate.getCachedDesignationType(mapEntryCacheDTO.getTargetDesignationTypeId());
		// mapEntryValueTransfer.setTargetDesignationTypeName(targetDesType.getName());
		// mapEntryValueTransfer.setTargetDesignationName(mapEntryCacheDTO.getTargetDesignationName());
		// Version targetVersion =
		// TerminologyDelegate.getCachedVersion(mapEntryCacheDTO.getTargetVersionId());
		// mapEntryValueTransfer.setTargetCodeSystemVuid(targetVersion.getCodeSystem().getVuid());
		// mapEntryValueTransfer.setTargetCodeSystemVersionName(targetVersion.getName());
		// mapEntryValueTransfer.setOrder(mapEntryCacheDTO.getMapEntrySequence());
		// mapEntryValueTransfer.setStatus(mapEntryCacheDTO.isMapEntryActive());
		// mapEntryValueTransferList.add(mapEntryValueTransfer);
		// }
		//
		// mapEntryValueListTransfer.setTotalNumberOfRecords(mapEntryCacheList.getTotalNumberOfRecords());
		// mapEntryValueListTransfer.setMapEntryValueTransfers(mapEntryValueTransferList);

		return mapEntryValueListTransfer;
	}

	private static Integer validatePageSize(Integer pageSize) throws STSException {
		if (pageSize == null) {
			pageSize = DEFAULT_PAGE_SIZE;
		} else if (pageSize > MAX_PAGE_SIZE) {
			throw new STSException("Page size exceeded maximum size of: " + MAX_PAGE_SIZE);
		} else if (pageSize < 1) {
			throw new STSException("Invalid page size (" + pageSize + ").");
		}

		return pageSize;
	}

	private static Integer validatePageNumber(Integer pageNumber) throws STSException {
		if (pageNumber == null) {
			pageNumber = 1;
		} else if (pageNumber < 1) {
			throw new STSException("Invalid page number (" + pageNumber + ").");
		}

		return pageNumber;
	}

	
	private static void prohibitAuthoringVersion(String versionName) throws STSException {
		// if
		// (HibernateSessionFactory.AUTHORING_VERSION_NAME.equals(versionName))
		// {
		// throw new STSException(HibernateSessionFactory.AUTHORING_VERSION_NAME
		// + " is not an allowed version name.");
		// }
	}

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

					if (sememe.getSememeType() == SememeType.DESCRIPTION) {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Optional<LatestVersion<DescriptionSememe>> descriptionVersion = ((SememeChronology) sememe)
								.getLatestVersion(DescriptionSememe.class, STAMP_COORDINATES);
						if (descriptionVersion.isPresent()) {
							DesignationDetailTransfer d = new DesignationDetailTransfer();

							d.setCode(getCodeFromNid(sememe.getNid()));
							d.setName(descriptionVersion.get().value().getText());
							d.setStatus(convertStateToString(descriptionVersion.get().value().getState()));

							// Get the extended type
							Optional<UUID> descType = Frills.getDescriptionExtendedTypeConcept(STAMP_COORDINATES,
									sememe.getNid());
							if (descType.isPresent()) {
								// not right
								// d.setType(getPreferredNameDescriptionType(concept.getNid()));
								d.setType("TODO");
							} else {
								log.warn("No extended description type present on description "
										+ sememe.getPrimordialUuid() + " "
										+ descriptionVersion.get().value().getText());
							}

							List<PropertyTransfer> properties = new ArrayList<>();
							List<ValueSetTransfer> subsets = new ArrayList<>();

							Get.sememeService().getSememesForComponent(sememe.getNid()).forEach((nestedSememe) -> {

								// skip code and vuid properties - they are
								// handled already
								if (nestedSememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()
										&& nestedSememe.getAssemblageSequence() != codeAssemblageConceptSeq) {
									if (ts.wasEverKindOf(nestedSememe.getAssemblageSequence(), vhatPropertyTypesNid)) {
										PropertyTransfer property = buildProperty(nestedSememe);
										if (property != null) {
											properties.add(property);
										}
									}

									// a refset that doesn't represent a mapset
									else if (ts.wasEverKindOf(nestedSememe.getAssemblageSequence(), vhatRefsetTypesNid)
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
				.getSememesForComponentFromAssemblage(componentNid, codeAssemblageConceptSeq).findFirst();
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
			else if (sc.get().getSememeType() == SememeType.DYNAMIC) 
			{
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

					if ( sememe.getAssemblageSequence() != MetaData.VUID.getConceptSequence()
							&& sememe.getAssemblageSequence() != codeAssemblageConceptSeq ) {
						
						if (ts.wasEverKindOf(sememe.getAssemblageSequence(), vhatPropertyTypesNid))
						{
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
			if (latestVersion.isPresent() && preferredNameExtendedType.equals(Frills
					.getDescriptionExtendedTypeConcept(STAMP_COORDINATES, sememeChronology.getNid()).orElse(null))) {
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

			subset.setName("name");

			long vuid = Frills
					.getVuId(Get.identifierService().getConceptNid(sememe.getAssemblageSequence()), STAMP_COORDINATES)
					.orElse(0L).longValue();
			if (vuid > 0) {
				subset.setVuid(vuid);
			} else {
				log.warn("No VUID found for Subset UUID: " + sememe.getPrimordialUuid());
			}

			subset.setStatus(sememe.isLatestVersionActive(STAMP_COORDINATES) ? "Active" : "Inactive");

			// subset.setVersionNames(versionNames);

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
			try {
				String code = null;

				if (ai.getTargetComponent().isPresent()) {
					code = getCodeFromNid(
							Get.identifierService().getNidForUuids(ai.getTargetComponent().get().getPrimordialUuid()));
					if (code == null || code.isEmpty()) {
						log.warn("Failed to find new target code for concept "
								+ ai.getTargetComponent().get().getPrimordialUuid());
					} else {
						relationship.setCode(code);
					}
				}

				relationship.setName(ai.getAssociationType().getAssociationName());
				relationship.setStatus(convertStateToString(ai.getData().getState()));
				relationship.setType(getPreferredNameDescriptionType(ai.getTargetComponent().get().getNid()));

			} catch (Exception e) {
				log.error("Association build failure");
			}
			if (relationship != null) {
				relationships.add(relationship);
			}
		}
		return relationships;
	}

	private static String convertStateToString(State state) {
		return (state.isActive()) ? "Active" : "Inactive";
	}

	private static String convertStateToString(Boolean state) {
		return (state) ? "Active" : "Inactive";
	}
}
