ISAAC-Rest Changelog 

Any time a code change is made that impacts the API returned to callers, increment the value in API ChangeLog.md, and in RestSystemInfo

During development, we can increment this, so long as our client code (komet) is aware of the changes.

After an official release, any API change should be done by bumping the major version - and creating new rest paths (/rest/2/, /rest/write/2/)
If reverse compatibility is required to be maintained, then the rest/1 or rest/write/1 code must remain.

* 2016/10/17 - 1.6.0: Cleaning up return types on /write APIs for consistency (everything now returns a RestWriteResponse), cleaning up inconsistent
    ways of changing the status on components.  There is a new method (/write/component/update/state) which can be used toggle the state of any component
    from active to inactive (concept or sememe).  In parallel, all update methods now include an (optional) active field in the update data, which may 
    be used to change the state during an update.  Finally, most (but not yet all) create methods also now support specifying the state during the component
    create.  The APIs that do not yet support this on create things that create concepts behind the scenes, like mapSet create, or associationCreate.
    Numerous confusing API methods for changing state of a concept were removed.
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
