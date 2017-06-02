ISAAC-Rest Changelog 

Any time a code change is made that impacts the API returned to callers, increment the value in API ChangeLog.md (here), and in RestSystemInfo

During development, we can increment this, so long as our client code (komet) is aware of the changes.

After we gain outside customers on the API, any change should be done by bumping the major version - and creating new rest paths (/rest/2/, /rest/write/2/)
If reverse compatibility is required to be maintained, then the rest/1 or rest/write/1 code must remain.

Until that happens, however - we follow the procedure of bumping the release version if it is an API change that won't break KOMET code - such as adding a new 
parameter.  However, any change that will break KOMET code - such as changing the operation of an existing method (which KOMET uses) - then the minor revision 
(and/or the major revision) should be bumped, to force a KOMET runtime breakage due to the incompatibility. 

Bug fixes should not be documented here, rather, than should be documented in the changelog file.

*** Don't forget to update the value in the class RestSystemInfo ***
* 2018/06/02 - 1.15.0
	* Adding VuidAPIs for isVuidValid() and changing RestVuidBlockData to hold long instead of int

* 2018/05/18 - 1.14.0
    * Added VUID_REQUESTOR role for VuidWriteAPIs  

* 2018/05/18 - 1.13.1
    * Added the terminologyType parameter to the concept Chronology read API.  
    * Changed the behavior of the terminologyType paremter on the concept Version read API, so that it now takes into account the view coordianate (the chronology read
        API retains the previous behavior of ignoring the view coordinate entirely)
    * In RestAssociationItemVersion, when source or target concepts are requested to be returned, these concept chonologies now include the terminologyType information
    * In RestAssociationTypeVersion, when the referenced concept is requested to be returned, the concept chronology now includes the terminologyType information.
    * Added terminologyType field to RestMappingSetVersion, which is always populated.
    * In RestSearchResult, when the referencedConcept is requested to be returned, the concept chronology now includes the terminologyType information.
    * In RestIdentifiedObjectsResult, when the identified object is a concept, the terminologyType information is now returned.
    * Added a 'description' field to RestIdentifiedObject, which is ONLY populated when the service is deployed in debug mode, and then, is only (sometimes) populated.
        This field should NOT be relied on for production use, it is a developers debug tool. 
    * Added an API for submitting VETS VHAT XML content:  /write/1/intake/vetsXML 

* 2017/05/17 - 1.13.0
    * Changed the RestConceptNode and RestTypedConnectorNode objects, recursively replacing all concept sequences with corresponding RestIdentifiedObject.

* 2017/05/17 - 1.12.0
    * Changed the RestLanguageCoordinate, RestStampCoordinate and RestLogicCoordinate objects, recursively replacing all concept sequences with corresponding RestIdentifiedObject.

* 2017/05/17 - 1.11.0
    * Changed the RestStampedVersion object, removing authorSequence, moduleSequence and pathSequence - replacing them with authorUUID, moduleUUID and pathUUID - which 
        are now UUIDs, instead of sequences.

* 2017/05/16 - 1.10.1
    * Adding VuidWriteAPIs to allow allocation and return of blocks of VUIDs.  The parameters are ssoToken, blockSize and reason, and the data type returned is 
        RestVuidBlockData, which contains the upper and lower bound of a contiguous range of integer VUID numbers.

* 2017/05/16 - 1.10.0
    * Changed the datatype of RestConceptVersion sememeMembership and terminologyTypes to and Array of RestIdentifiedObject, instead of a Set of Integers.

* 2017/05/16 - 1.9.21
    * Added a parameter to the ConceptVersion and Taxonomy Version APIs 'terminologyType' - which may be set to true, to include a new set of concept sequences
        that represent each terminology that a concept is a member of.  The concepts returned will be direct children of the MODULE metadata constant.
    * Added system/extendedDescriptionTypes to return the applicable extended description types for a particular terminology.
    * Added system/terminologyTypes api to provide a list of generally supported terminologies (the same ids that can be passed into extendedDescriptionTypes
        to find out the extended types per terminology.
    


* 2017/04/03 - 1.9.20
    * Changed behavior and accepted parameters for update APIs to make latest-version retrieval criteria more permissive
        Parameters time and allowedStates are not allowed (or are ignored, as components of passed coordToken), because all comparisons are made with time=LATEST and allowedStates=ANY. If no coordToken or stamp coordinate parameters are passed, then default values are used.
        The update API first attempts to retrieve the most recent time=LATEST version of the specified chronology conforming to the module specified in the editToken and the non-module parameters of the effective stamp coordinate (with time=LATEST and allowedStates=ANY).
        If a matching version is found, then its data fields are compared to the corresponding data fields in the passed form data.
        If no differences are found, then the update write is not performed.
        If differences are found, then the update write is performed.
        If no matching version is found (based on the editToken module), then the update API attempts to retrieve the most recent (time=LATEST) version of ANY state (allowedStates=ANY) of the specified chronology conforming to the module and non-module parameters of the effective stamp coordinate.
        If no differences are found, then the update write is not performed.
        If differences are found, then the update write is performed.

* 2017/03/29 - 1.9.19
    * Changed the behavior of the modules parameter, when passed in for construction of a stamp Coordinate - such that if a module is passed that represents
        VHA Modules, for example - and that module concept has children - the constructed stamp coordinate will contain the passed concept, and any of its children. 

* 2017/02/15 - 1.9.18
    * Moved the field 'active' from RestMappingSetExtensionValueUpdate down to the parent class of RestMappingSetExtensionValueBase (which will make it available in 
        RestMappingSetExtensionValue) - the net effect is upon read of a mapset, the extension fields now report if they are active or inactive.  The API for update/create
        should be unchanged - on read, there is a new boolean field 'active'

* 2017/02/02 - 1.9.17
    * Adding RestMappingItemVersionPage to contain paged map items
    * Adding /1/mapping/mappingItemsPaged API to return mapping items paged

* 2017/02/02 - 1.9.16
    * removed the 1/mapping/field API, as there was no use case for it
    * Changed the RestMappingSetDisplayFieldCreate.componentType to fieldComponentType.  Changed the accepted type from RestMapSetItemComponentType to String, 
        so it now accepts either an enum ID, or a enum name for a specified RestMapSetItemComponentType.  This resolves issues with .equals not working on the passed
        in values on create, if it doesn't align perfectly.
    * Adding generation of default map display fields for both maps and items
    * Changing all instances of QUALIFIER to EQUIVALENCE_TYPE

* 2017/02/02 - 1.9.15
    * Changed the field displayFields in RestMappingItemVersion (which was of type  RestMappingItemDisplayField) to computedDisplayFields, (now of type 
        RestMappingItemComputedDisplayField).  Several unnecessary fields were removed from RestMappingItemComputedDisplayField.  The class 
        RestMappingItemDisplayFieldWithValue was also removed (and its contents condensed into RestMappingItemComputedDisplayField)
    
* 2017/02/01 - 1.9.14:
    * Eliminating FULLY_SPECIFIED_NAME, PREFERRED_TERM and UUID item display fields
    * Adding DESCRIPTION item display field, which uses view coordinate
    * Eliminating ITEM item display field component type
    * Eliminating description fields from RestMappingItemVersion
    * Adding available and default item display fields (DESCRIPTION fields with component types of SOURCE, TARGET and QUALIFIER)
        changing mapset creation API behavior to create the following default item display fields in the following order:
        DESCRIPTION(SOURCE), DESCRIPTION(TARGET), DESCRIPTION(QUALIFIER)
        followed by any and all item extended fields, in order
    
* 2017/01/31 - 1.9.13:
    * Adding support for QUALIFIER and ITEM_EXTENDED RestMapSetItemComponentType type
    * Adding support for ITEM_EXTENDED RestMapSetItemComponentType type, in which "id" refers to extended field column order
    * Adding support for UUID map item display field
    * Changing map set and item display fields member from "name" to "id"
    * Adding new RestMappingItemDisplayFieldWithValue to allow returning some fields with value and others (of ITEM_EXTENDED RestMapSetItemComponentType) without values

* 2017/01/30 - 1.9.12:
    * Changing map display fields returned within RestMappingItemVersion to be of type RestMappingItemDisplayField
    * Removing value member from RestMappingSetDisplayField
    * Removing "calculated" member from RestMappingSetDisplayField

* 2017/01/30 - 1.9.11:
    * Added isaacDbId to the RestSystemInfo - this tells you the globally unique ID assigned to the database that this instance is deployed on top of.
    * Added warId to the RestSystemInfo - this tells you the UUID assigned by PRISME during deployment (this is read from prisme.properties: war_uuid)
        This only populated if deployed via PRISME.

* 2017/01/24 - 1.9.10:
    * Changing identifier for fully specified name description map item display field to a string ("FULLY_SPECIFIED_NAME") literal rather than a UUID ("00791270-77c9-32b6-b34f-d932569bd2bf")
    * Dynamically adding availability of some potential map item display fields

* 2017/01/24 - 1.9.9:
    * Adding user friendly description to map item display fields
    * Adding API MappingAPIs.getAvailableMappingSetDisplayFieldComponentTypes() (fieldComponentTypes/) to return available map item display field component types (i.e. "SOURCE", "TARGET")

* 2017/01/24 - 1.9.8:
    * Modifying Mapping item display fields APIs to populate item fields
    * Changing some map item display field related data transfer object member names
    * Changing to allow specification of map item display field component by enum

* 2017/01/23 - 1.9.7:
    * Mapping APIs allow creation, update and retrieval of map set display fields 

* 2017/01/18 - 1.9.6:
    * Workflow API returns with various changes / corrections.
    * Added the ability to update mapping set extended values

* 2017/01/05 - 1.9.5:
    * Change the behavior of the create description API, so that is no longer adds a preferred US English dialect if no preferred dialects are specified.  
        It will now only builds the dialects specifically requested.

* 2016/12/02 - 1.9.4:
    * Add an api for search by internal ID (UUID, Nid, Sequence)

* 2016/11/29 - 1.9.3:
    * change update API methods so that they automatically ignore update requests that don't actually change any data.  Prevents creation of new (identical) versions
    * fix an issue where passing a UUID data column (instead of a nid) when attempting to update a component nid sememe results in a failure.  Tim ran into this when 
        trying to update a dialect.  It will now properly map either a UUID or a nid to a component_nid legacy sememe type.
    * Fix the prefix search implementation so that when you restrict to 'sememe' type, it will return results for "VUID", "Code" and "Snomed Integer Id"

* 2016/11/22 - 1.9.2:
    * Change the return type of a concept create call to RestWriteResponseConceptCreate from RestWriteResponse.  The new type carries back additional information
        about the various objects created during a concept create.
    * Added a calculateSemanticTag variable to the RestConceptCreateData object.  Allows control over the creation of 1 or 2 descriptions, and the automatic calculation
        (or not) of a semantic tag
    
* 2016/11/16 - 1.9.1:
    * adding role-based security authentication to API classes
    * adding use of PRISME SSO APIs
    * returning json-serialized RestExceptionResponse on API request failure

* 2016/11/16 - 1.9.0:
    * renamed RestDynamicSememeTypedData to RestDynamicSememeIdentifiedData (but this was an abstract parent class, so it shouldn't impact the API)
    * renamed RestDynamicSememeIdentifiedData.dataObjectType to 'dataIdentifed' and changed the type to a RestIdentifiedObject.
    * changed RestDynamicSememeDefinition.assemblageConceptId into a RestIdentifiedObject
    * added assemblageConceptDescription to RestDynamicSememeDefinition
    * removed extensionNameConcept from RestMappingSetExtensionValue (was a duplicate of extensionNameConceptIdentifiers)

* 2016/11/15 - 1.8.0:
    * Enhanced the RestIdentifiedObject return type, so that it also includes nid, sequence, and type fields.  The previously existing uuids
        field is unchanged.
    * Enhanced the RestWriteResponse to return type (concept or sememe)
    * Broke the Comment create / read API - RestCommentVersionBaseCreate 'commentedItem' field now takes in a string, instead of an integer, and can properly handle
        a uuid or nid identifier.  On Return, the RestCommentVersion object now returns the 'commentedItem' field as a RestIdentifiedObject, rather than an integer.
    * Fixed (via the above changes) a design flaw in the comment create API, where it was assuming that incoming sequence identifiers were concepts, rather than 
        rejecting them as unknowable.
    * Replaced RestCommentVersionBaseCreate with RestCommentVersionCreate
    * Broke the Mapping read / write APIs for better handling of IDs.
    * In the Mapping APIs, all instances where a concept sequence is returned (mapSetConcept, sourceConcept, targetConcept, qualifierConcept) have been changed
        to the RestIdentifiedObject type, which returns UUID/Sequence/Nid
    * 'sememeSequence' has been removed from RestMappingItemVersion, as this was (now) carrying back duplicate information already returned by identifiers.sequence
    * 'active' was removed from RestMappingItemVersion, as this was carrying back duplicate information already returned by mappingItemStamp.state
    * RestMappingItemVersionBaseCreate was replaced with RestMappingItemVersionCreate
    * The updateMappingItem call that previously took in a RestMappingItemVersionBase now takes in a RestMappingItemVersionUpdate
    * On update or create, the concept fields (mapSetConcept, sourceConcept, targetConcept, qualifierConcept) now take in a string object type.  They now accept
        UUID, nid or sequence.
    * Removed conceptSequence from RestMappingSetVersion, as this now carried back duplicate information available in identifiers.sequence
    * Changed all path references of "by" to "for" (to clean up internal API consistency / reduce confusion)
      - comment/version/byReferencedComponent -> comment/version/forReferencedComponent
      - search/byReferencedComponent -> search/forReferencedComponent
      - sememe/byReferencedComponent -> sememe/forReferencedComponent
      - sememe/byAssemblage -> sememe/forAssemblage
    * Removed unnecessary "stampSequence" variable from the RestStampedVersion object.
    * In the concept create API, the RestConceptCreateData had all fields that accept concepts changed to type string (parentConceptIds, descriptionLanguageConceptId,
        descriptionPreferredInDialectAssemblagesConceptIds, extendedDescriptionTypeConcept)
        - Some of the above fields had renames: descriptionExtendedTypeConceptId -> extendedDescriptionTypeConcept
    * In the description create API, the RestSememeDescriptionCreateData had all the fields that accept concepts changed to type string (caseSignificanceConcept,
        languageConcept, descriptionTypeConcept, extendedDescriptionTypeConcept, preferredInDialectAssemblagesIds, acceptableInDialectAssemblagesIds referencedComponentId)
        - Many of the fiels above had renames: caseSignificanceConceptSequence -> caseSignificanceConcept, languageConceptSequence -> languageConcept, 
        descriptionTypeConceptSequence -> descriptionTypeConcept, extendedDescriptionTypeConceptSequence -> extendedDescriptionTypeConcept, 
        referencedComponentNid -> referencedComponentId
    * Removed the field 'stampSequence' from RestStampedVersion
    * Fixed a number of bugs with concept / description creation where extended description types and/or dialects were not being added.
    * Added a bunch of missing validation on the concept inputs to create concept / create description.
    * Removed 'associationConceptSequence' from RestAssociationTypeVersion as it now duplicated information returned in 'identifiers'
    * Removed 'conceptSequence' from RestConceptChronology as it now duplicated information returned in 'identifiers'
    * Removed 'sememeSequence' from RestSememeChronology as it now duplicated information returned in 'identifiers'
    * Realign the Association APIs with the newest patterns
      - RestAssociationVersionBase renamed to RestAssociationItemVersionUpdate - targetNid renamed to targetId and changed to a string.
      - RestAssociationItemVersionCreate - Fields changed to strings, and renamed: associationTypeSequence -> associationType, sourceNid -> sourceId
      - RestAssociationTypeVersionBaseCreate renamed to RestAssociationTypeVersionCreate
      - RestAssociationItemVersion - Fields changed to RestIdentifiedObject and renamed : associationTypeSequence -> associationType, sourceNid -> sourceId
          targetNid renamed to targetId
    * Realign the API for creating mapsets (portions missed above)
        - RestMappingSetExtensionValueBaseCreate renamed to RestMappingSetExtensionValueCreate, extensionNameConcept changed to a String type.
        - RestMappingSetExtensionValue - extensionNameConcept changed to a RestIdentifiedObject
    * RestDynamicSememeColumnInfo columnConceptSequence renamed to columnLabelConcept, changed to a RestIdentifiedObject
    * RestDynamicSememeColumnInfoCreate columnLabelConcept changed from int to string
    * Realign description update API to new patterh
        - RestSememeDescriptionCreateData renamed to RestSememeDescriptionCreate
        - RestSememeDescriptionUpdateData renamed to RestSememeDescriptionUpdate, fields renamed and changed to strings: 
            caseSignificanceConceptSequence -> caseSignificanceConcept, languageConceptSequence -> languageConcept, descriptionTypeConceptSequence -> descriptionTypeConcept
    * Realign RestSememeChronology - fields renamed / changed to RestIdentifiedObjects: assemblageSequence -> assemblage, referencedComponenentNid -> referencedComponent, 
        removed referencedComponentNidObjectType as no longer necessary
    * Realign RestSememeDescriptionVersion - fields renamed / changed to RestIdentifiedObjets: caseSignificanceConceptSequence -> caseSignificancConcept, 
        languageConceptSequence -> languageConcept, descriptionTypeConceptSequence -> descriptionTypeConcept, descriptionExtendedTypeConceptSequence -> 
        descriptionExtendedTypeConcept


* 2016/11/08 - 1.7.4: 
    * Fixed a bug where the create comment API was requiring a comment context (when it should be optional) and then fixed some issues with the 
        comment APIs and their default parsing of JSON (they were requiring @class notions, when they shouldn't) 
    * Fixed a bug where it wasn't reading comments properly on a mapset.
    * API BREAKAGE - fix an inconsistent method for write/comment/update which was taking in the id as a query param, instead of a path param.

* 2016/10/27 - 1.7.3: 
    * Fixed a bug where mapItems were being returned with column index info that started at 2, instead of 0
    * Fixed a bug where creates or edits of map items were not properly honoring passed in column numbers
    * Fixed a bug where the submission of a 'null' in a list of columns for sememe or mapping data would result in an error.

* 2016/10/27 - 1.7.2: 
    * Added the optional parameter 'restrictTo'to the search/prefix API.  Supports "association, mapset, sememe, metadata" - see docs for more details.
    * Added the optional parameter 'mergeOnConcept' to the search/prefix API.  When true, returned search results will be merged, such that there is only 
        one result per concept (even if the concept has multiple descriptions that matched the query)

* 2016/10/27 - 1.7.1: 
    * Fixed a bug where a readback of extended fields on a map set definition was returning two fields it should not.
    * Fixed a bug where the 'active' field wasn't being populated when reading mapsets / map items
    * Fixed bugs with the handling / activation of dynamic sememe column validators
    * Fixed bugs with 'default' columns of dynamic sememes not being set to their default value appropriately

* 2016/10/26 - 1.7.0: 
    * Commented out all of the workflow APIs.  Fixed inadvertent breakages being caused by previously merged workflow code.
    
* 2016/10/25 - 1.6.4: 
    * Adding a system/user call to hand back the user information about a given user concept.  This will include various SSO information that was 
        used when the user concept was created.

* 2016/10/20 - 1.6.3: 
    * Clarifying the 'names' returned for all of the /1/system/enumeration/* calls - now returning an enumName, friendlyName (optional) and enumId.
    * For 1/system/enumeration/restDynamicSememeDataType, an attribute of 'classType' has been added, which contains the full value that needs to 
        be included during sememe data column creation of a particular sememe type.
* 2016/10/19 - 1.6.2: 
    * Fixed the inconsistent url /write/component to /write/1/component
    * Now requiring / honoring column numbers for sememe create / edit operations
    * Fixed a bug reading DynamicSememeUUID objects
    * Changed sememe/byReferencedComponent so it no longer returns association or mapping sememes by default - new parameters added to allow it to return
        those types of sememes.
* 2016/10/18 - 1.6.1:
    * Changed the field RestDynamicSememeColunInfoCreate.columnDataType from RestDynamicSememeDataType to String for ease of creation / parsing.
    * Changed the field RestDynamicSememeColunInfoCreate.columnValidatorTypes from RestDynamicSememeValidatorType to String for ease of creation / parsing.
      * The string values for either of these fields may be populated with the name or enumId from the enum types.  See updated docs on the field.  This
        vastly simplifies the object that needs to be constructed during writes to the server.  On reads, the full Rest...Type is still returned for each enum.
    * renamed the field RestDynamicSememeColumnInfoCreate.columnConceptLabelConcept to columnLabelConcept (which may break a mapping call)
    * Added a sememe write API method for defining a new sememe.  Added much more robust tests, worked outnumerous bugs with different data types and serialization.
    * Removed RestWorkflowLockingData as no longer necessary since only 2 fields are ProcessId and Boolean.  Instead, updated WorkflowWriteAPI.setProcessLock to 
        pass in a boolean as type String.
    * Removed RestWorkflowProcessComponentSpecificationData as no longer necessary since only 2 fields are ProcessId and Integer.  Instead, updated 
        WorkflowWriteAPI.removeComponentFromProcess to pass in an integer as type String.
    * Remove WorkflowAPI.isProcessLocked() as not needed once RestWorkflowProcess has an OwnerId added.  Now same information that the REST call supplied 
        can be derived from the RestWorkflowProcess object.

* 2016/10/17 - 1.6.0: 
    * Cleaning up return types on /write APIs for consistency (everything now returns a RestWriteResponse)
    * Cleaning up inconsistent ways of changing the status on components (DTO vs parameter on update / create calls)
    * There is a new method (/write/component/update/state) which can be used toggle the state of any component from active to inactive (concept or sememe).
    * In parallel, all update methods now include an (optional) active field in the update data, which may  be used to change the state during an update.
    * Finally, most (but not yet all) create methods also now support specifying the state during the component create.  
      * The APIs that do not yet support this on create things that create concepts behind the scenes, like mapSet create, or associationCreate.
    * Numerous confusing API methods for changing state of a concept were removed.
    * Added initial implementation of the sememe write APIs.  Create is partially tested, update is not yet tested. 
* 2016/10/17 - 1.5.9: Removed processId from RestWorkflowProcessComponentSpecificationData (used with removeComponentFromProcess()) because it should be in EditToken
* 2016/10/13 - 1.5.8: Changed return type of all write methods in the Associations API to be RestWriteResponse.  Bug fixes in association API
    implementation code.  BUG - getTargetAssociations does not work for newly created association types / associations.  (problem with underlying
    lucene indexes not updating after the commit)
* 2016/10/13 - 1.5.7: Changing WorkflowWriteAPIs APIs to return RestWriteResponse object containing renewed EditToken along with optional identifiers
* 2016/10/13 - 1.5.6:
    a.) Changing RestWorkflowAvailableAction to contain RestUserRoleType instead of UserRole
    b.) Changing RestWorkflowComponentToStampMapEntry to contain RestStampedVersion instead of Stamp
    c.) Changing RestWorkflowDefinitionDetail to contain Set<RestUserRoleType> instead of Set<UserRole>
* 2016/10/13 - 1.5.5 - Cleaned up all Workflow APIs.  This includes: 
    a) removal of 4 methods (2 in WorkflowAPI and 2 in WorkflowWriteAPI) 
    b) renaming of methods and their fields to be more straight forward and consistent
    c) simplified method and field comments 
    d) simplified paths to call 
    e) removed commented out or no longer utilized resources like Workflow Data classes or items in RestPaths
    API changes are below that will break compatibility...    
    * Removals
        * getDefaultDefinition 
            * workflow/defaultDefinition
            * RestPaths.defaultDefinitionComponent
        * getAllRoles()
            * workflow/allRoles
            * RestPaths.allRolesComponent
        * releaseWorkflowLock ()
            * workflow/update/releaseWorkflowLock 
            * RestPaths.updatePathComponent + RestPaths.acquireWorkflowLockComponent 
    * Renames
        * getAvailableDefinitions()
            * Now Method Called:  getDefinitions()
            * New Rest Path:
                * workflow/definition/all (was workflow/availableDefinitions) 
                * RestPaths.allDefinitions (was RestPaths. availableDefinitionsComponent)
        * getProcess()
            * New Rest Path: 
                * workflow/definition/all (was workflow/process) 
                * RestPaths.locked (was RestPaths.workflowLockStateComponent)
            * Renamed Inputs
                * processId (was wfProcessId)
         * getHistoriesForProcess ()
            * Now Method Called: getProcessHistory ()
            * New Rest Path: 
                * workflow/process/history (was workflow/historiesForProcess)
                * RestPaths.history (was RestPaths.historiesForProcessComponent)
            * Renamed Inputs
                * processId (was wfProcessId) 
        * isWorkflowLocked ()
            * Now Method Called: isProcessLocked()
            * New Rest Path: 
                * workflow/process/locked (was workflow/workflowLockState)
                * RestPaths.locked (was RestPaths.workflowLockStateComponent)
            * Renamed Inputs
                * processId (was wfProcessId) 
        * getActionsForProcessAndUser ()
            * Now Method Called: getProcessActions()
            * New Rest Path: 
                * workflow/process/actions (was workflow/actionsForProcessAndUser)
                * RestPaths.actions (was RestPaths.actionsForProcessAndUserComponent)
            * Renamed Inputs
                * processId (was wfProcessId) 
            * Other Note: originally, fields were ordered “wfProcessId, editToken”.  The order was reversed to “editToken, processId”
        * getActionsForProcessAndUser()
            * Now Method Called: getAvailableProcesses()
            * New Rest Path: 
                * workflow/process/available (was workflow/advanceableProcessInformation)
                * RestPaths.available (was RestPaths.advanceableProcessInformationComponent)
            * Renamed Inputs
                * definitionId (was wfDefinitionId) 
        * createWorkflowProcess ()
            * Now Method Called: createInstance ()
            * New Rest Path: 
                * workflow/create/process/create (was workflow/create/createWorkflowProcess)
                * RestPaths.createPathComponent + RestPaths.createProcess (was RestPaths.createPathComponent + RestPaths.createWorkflowProcessComponent)
            * Renamed Inputs
                * definitionId (was wfDefinitionId) 
        * advanceWorkflowProcess ()
            * Now Method Called: advanceProcess ()
            * New Rest Path: 
                * workflow/update/process/advance (was workflow/update/advanceWorkflowProcess)
                * RestPaths.createPathComponent + RestPaths.advanceProcess (was RestPaths.createPathComponent + RestPaths. advanceWorkflowProcessComponent)
        * acquireWorkflowLock ()
            * Now Method Called: setProcessLock ()
            * New Rest Path: 
                * workflow/update/process/lock (was workflow/update/acquireWorkflowLock)
                * RestPaths. updatePathComponent + RestPaths.lock (was RestPaths.updatePathComponent + RestPaths.acquireWorkflowLockComponent)
            * Changed Inputs
                * RestUUID processId (was RestWorkflowLockAquisitionData lockAquisitionData) 
            * New Input
                * RestUUID ownerId 
        * removeComponentFromWorkflow ()
            * Now Method Called: removeComponentFromProcess ()
            * New Rest Path: 
                * workflow/update/process/component (was workflow/update/removeComponentFromWorkflow)
                * RestPaths. updatePathComponent + RestPaths.removeComponent (was RestPaths.updatePathComponent + RestPaths. removeComponentFromWorkflowComponent)
* 2016/10/13 - 1.5.4 - Replaced all references to String roles with instances of new UserRole enum or corresponding RestUserRoleType Enumeration, 
    handling specific test String values of ssoToken in getEditToken() ("TEST_JSON1", "TEST_JSON2", "TEST_JSON3").  Changed RestWorkflowAvailableAction 
    and RestWorkflowDefinitionDetail DTOs, which will break APIs.
* 2016/10/10 - 1.5.3 - Removing user id from wf parameters and parameter DTO, replacing with calls to RequestInfo.getEditCoordinate().  All relevant edit 
    and workflow REST APIs now accept only editToken parameter, which can now be created and retrieved only by calling CoordinateAPIs.getEditToken() with 
    ssoToken and/or editToken parameters, along with additional documented optional parameters
* 2016/10/10 - 1.5.2 - Add an API for reading a specific association description.  Integrating fortify fixes (shouldn't cause any API / behavior change)  
* 2016/10/06 - 1.5.1 - Adding Association APIs.  Cleaned up the returned pagination data, so that it stops returning page links that don't make 
    sense when you are at the first or last page.  Added a field to pagination data to indicate if the approximateTotal is an estimate, or exact.
    Fixed a regression in the json serializer that was leading to the serialization of null fields - null fields are no longer serialized.
* 2016/10/03 - 1.5.0 - Getting rid of "...Versions" api classes that were confusing wrappers.  Changing return types to Array instead.  Specific 
    API changes are below that will break compatibility...
    * Removals
      * RestSememeDescriptionVersions (This probably wasn't used in Komet)
      * RestStrings (couldn't find any use)
      * RestUUIDs
      * RestCommentVersions
      * RestMappingItemVersions
      * RestMappingSetVersions
      * RestWorkflowAvailableActions
      * RestWorkflowDefiniationDetails (couldn't find any use)
      * RestWorkflowProcesses (couldn't find any use)
      * RestWorkflowProcessHistories
      * RestWorkflowProcoessHistoriesMap
    * Renames
      * RestSearchResults -> RestSearchResultPage - the internal RestSearchResult list changed to a RestSearchResult[]
      * RestSememeVersions -> RestSememeVersionPage - the internal RestSememeVersion list changed to a RestSememeVersion[]
    * More details on the replacement of certain removed items:
      * 1/comments/byReferencedComponent now returns RestCommentVersion[] instead of RestCommentVersions
      * 1/sememe/byReferencedComponent now returns RestSememeVersion[] instead of a list (doesn't change json or xml representation)
      * 1/concept/descriptions now returns RestSememeDescriptionVersion[] instead of a list (may not change json representation)
      * 1/mapping/mappingSets changes from RestMappingSetVersions to RestMappingSetVersion[]
      * 1/mapping/mappingItems changes from RestMappingItemVersions to RestMappingItemVersion[]
      * 1/workflow/historiesForProcess changes from RestWorkflowProcessHistories to RestWorkflowProcessHistory[]
      * 1/workflow/advanceableProcessInformation changes from RestWorkflowProcessHistoriesMap to RestWorkflowProcessHistoriesMapEntry[]
      * 1/workflow/actionsForProcessAndUser changes from RestWorkflowAvailableActions to RestWorkflowAvailableAction[]


* 2016/10/01 - 1.4.10 - Changing componentToIntitialEditMap in RestWorkflowProcess to be Set of RestWorkflowComponentNidToInitialEditEpochMapEntry to avoid 
    Enunciate errors
* 2016/09/26 - 1.4.9 - Adding WorkflowAPIs getDefaultDefinition() and getAvailableDefinitions()
* 2016/09/26 - 1.4.8 - Removing preferred term and acceptable dialects from RestConceptCreateData, changing field names in RestConceptCreateData, changing 
    getDescriptionExtendedTypeConceptId() to return Integer rather than int 
* 2016/09/26 - 1.4.7 - Fixing bug causing incorrect paths for WorkflowWriteAPIs REST methods, sorted comments by time, fixed a bug in reading / parsing
    mapset entries.  Limited mapset entries to 1000, until we get the API switched to paging.
* 2016/09/19 - 1.4.6 - Adding description extended type to DTOs and populating in SememeAPIs, modifying workflow APIs to eliminate Enunciate errors caused by use of Map
* 2016/09/15 - 1.4.5 - Tweaking some aspects of mapset / map item APIs (adding fields, changing return type from int to UUID on create/edit)
* 2016/09/01 - 1.4.4 - Added ConceptWriteAPIs and replaced exposed OCHRE State enum with REST RestStateType. Also lowered visibility of DTO fields
* 2016/08/24 - 1.4.3 - Lots of workflow API stubs have been added, refactoring Rest APIs for mapping to fix design problems.
* 2016/07/25 - 1.4.2 - Adding Mapping / Comment APIs
* 2016/06/01 - 1.4.1 - Changed return value of SystemAPIs.getIdentifiedObjects() to return RestIdentifiedObjectsResult rather than List<Object>.
* 2016/05/16 - 1.4.0 - Renamed SystemInfo to RestSystemInfo for consistency.
* 2016/05/16 - 1.3.3 - Added a boolean field 'isConceptDefined' to RestConceptVersion.  Removed isaacGuiVersion, assemblyVersion
  and metadataVersion from SystemInfo, as they made no sense.
* 2016/05/16 - 1.3.2 - Change restVersion to apiImplementationVersion
* 2016/05/13 - 1.3.1 - Add restVersion to SystemInfo
* 2016/05/10 - 1.3 - Add the class 'RestCoordinates' a composed of all REST coordinate types to be returned by getCoordinates()
* 2016/05/05 - 1.2 - Add the field 'sememeMembership' to the RestSememeVersion object.  Carries the set of concept sequences
    (that represent sememes) that the returned concept is a member of.  Added a query parameter to control the population to 
    both the 1/concept/version API and the 1/taxonomy/version API.  
    - Removed the expand options 'parents', 'children', 'countChildren', 'countParents' from the 1/concept/version API, 
    instead replacing these with query parameters, in a manner consistent with how they are specified in the 1/taxonomy/version API.  
* 2016/05/?? - 1.1 - A bunch of changes relating to stamp coordinate parsing and handling 
* 2016/04/22 - 1.0 - Introduction of version parameter.
