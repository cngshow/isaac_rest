ISAAC-Rest Changelog 

Any time a code change is made that impacts the API returned to callers, increment the value in API ChangeLog.md, and in RestSystemInfo

During development, we can increment this, so long as our client code (komet) is aware of the changes.

After an official release, any API change should be done by bumping the major version - and creating new rest paths (/rest/2/, /rest/write/2/)
If reverse compatibility is required to be maintained, then the rest/1 or rest/write/1 code must remain.

2016/09/26 - 1.4.7 - Fixing bug causing incorrect paths for WorkflowWriteAPIs REST methods
2016/09/19 - 1.4.6 - Adding description extended type to DTOs and populating in SememeAPIs, modifying workflow APIs to eliminate Enunciate errors caused by use of Map
2016/09/15 - 1.4.5 - Tweaking some aspects of mapset / map item APIs (adding fields, changing return type from int to UUID on create/edit)
2016/09/01 - 1.4.4 - Added ConceptWriteAPIs and replaced exposed OCHRE State enum with REST RestStateType. Also lowered visibility of DTO fields
2016/08/24 - 1.4.3 - Lots of workflow API stubs have been added, refactoring Rest APIs for mapping to fix design problems.
2016/07/25 - 1.4.2 - Adding Mapping / Comment APIs
2016/06/01 - 1.4.1 - Changed return value of SystemAPIs.getIdentifiedObjects() to return RestIdentifiedObjectsResult rather than List<Object>.
2016/05/16 - 1.4.0 - Renamed SystemInfo to RestSystemInfo for consistency.
2016/05/16 - 1.3.3 - Added a boolean field 'isConceptDefined' to RestConceptVersion.  Removed isaacGuiVersion, assemblyVersion
	and metadataVersion from SystemInfo, as they made no sense.
2016/05/16 - 1.3.2 - Change restVersion to apiImplementationVersion
2016/05/13 - 1.3.1 - Add restVersion to SystemInfo
2016/05/10 - 1.3 - Add the class 'RestCoordinates' a composed of all REST coordinate types to be returned by getCoordinates()
2016/05/05 - 1.2 - Add the field 'sememeMembership' to the RestSememeVersion object.  Carries the set of concept sequences
	(that represent sememes) that the returned concept is a member of.  Added a query parameter to control the population to 
	both the 1/concept/version API and the 1/taxonomy/version API.  
	- Removed the expand options 'parents', 'children', 'countChildren', 'countParents' from the 1/concept/version API, 
	instead replacing these with query parameters, in a manner consistent with how they are specified in the 1/taxonomy/version API.  
2016/05/?? - 1.1 - A bunch of changes relating to stamp coordinate parsing and handling 
2016/04/22 - 1.0 - Introduction of version parameter.



