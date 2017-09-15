ISAAC-rest Changelog 

This changelog summarizes changes and fixes which are a part of each revision.  For more details on the fixes, refer tracking numbers where provided, and the git commit history.  Note that this is not the same as the API Changelog.md.  This file will contain information on all changes - including bug fixes.  The API Changelog will only contain documentation on changes of the API - and those are tied to the 
API version number, not the release version number.

* 2017/09/15 - 5.08 - R5 release build v3
    * Fix a bug with trying to re-retire something that was already retired, which lead to a null pointer, which deadlocked the system
        from any further writes (sigh)
    * Pick up ISAAC updates which fix other bugs with xml import
    
* 2017/09/13 - 5.07 - R5 release build v2
    * Picking up a new release of isaac with some fixes for vhat delta import

* 2017/09/12 - 5.06
    * Changes to allow activating a previously inactive designation with an extended designation type
    * Official R5 release build

* 2017/09/08 - 5.05
    * Release to pick up ISAAC changes

* 2017/09/07 - 5.04
    * Release to pick up ISAAC changes
    
* 2017/09/06 - 5.03
    * Updated web wervice code

* 2017/09/01 - 5.02
    * Release to pick up ISAAC changes

* 2017/08/31 - 5.01
    * Adding a new system API which can be utilized to improve the module dropdowns in komet.
    * Fix 572778 - change designation type and value set type to use EnglishLanguagePreferredTerm. 

* 2017/08/25 - 5.00
    * ISAAC API alignment
    * Release 5 build

* 2017/08/24 - 4.24
    * Fix a bug with retirement or activation of extended description types on descriptions
    * Fix a bug with retirement of mapset sememes
    * Fix a bug with the design of the ssoToken handling, which was causing excessive prisme lookups for user / role information.
    * cleanup of logging output to reduce noise, add request processing timers to log.

* 2017/08/23 - 4.23
    * Added tests and documentation
    * Updates to align with ISAAC API changes

* 2017/08/22 - 4.22
    * Fix a bug where one could not attach an SCTID to a concept when you were using a database that doesn't currently contain any SCTIDs, such 
        as VETs.
    * Fix a bug where update description didn't work if language wasn't passed (even though language was documented as optional)

* 2017/08/21 - 4.21
    * Fix 575255 - Change ListValueSetContents web service to use Issac MetaData instead of VHAT.

* 2017/08/17 - 4.20
    * Changes to web services to handle missing and incorrect input values for ListMapEntriesFromSource.

* 2017/08/15 - 4.19
    * Fix 572073 - When entering incorrect code, Webservice is not giving expected response.
    * Fix 572778 - ReturnConceptDetails returning different results from CTT as compared to STS. Fix includes value set differences, add missing 
        Properties even when empty and designation type.
    * Search API enhancements per API changelog.  These enhancements REQUIRE a new index to work properly - you can either use a newly-build database,
        or trigger a reindex of the datastore by using the /write/1/system/rebuildIndex trigger.

* 2017/08/11 - 4.18
    * Added SOAP web service ListMapEntriesFromSource.

* 2017/08/07 - 4.17
    * Fixed system/extendedDescriptionTypes so it doesn't return VHAT extended types for the concept Isaac Module.
    * Fix for issue 567259. Application is logging SOAP details to catalina.out.

* 2017/08/03 - 4.16
    * Increment fake vuids within a session (vuids generated when a real vuid server couldn't be contacted)

* 2017/07/25 - 4.15
    * Changes for has_parent found during testing.
    
* 2017/07/25 - 4.14
    * API enhancements as noted

* 2017/07/21 - 4.13
    * Refactoring editToken code for maintainability, adding missing role-expiration checks, so it now updates roles from prisme every 5 minutes
        (as it was always supposed to, but was neglected)
    * Hooking up vuid generation through to the XML importer

* 2017/07/13 - 4.12
    * Hooked up the xml intake API to the real processing code 

* 2017/07/06 - 4.11
    * fix some exception handling issues that were causing the useful error message to appear in the wrong field upon return from vuid validation.
    * Make it not fail vuid validation on sememe write if no vuid server is available, when we are in debug mode.
    * add metadata terminology type flag to any concept that is a child of ISAAC Metadata, when the 'terminologyTypes' field is requested on a 
        RestConceptVersion or a RestConceptChronology
    * Change to the updated isaac search API that allows good performance while doing 'prefix search' with less than 3 characters, and a type filter.
        This feature will NOT work properly without a rebuild index (via the latest version of ISAAC) - or having a a complete new DB that was built 
        with the latest version of ISAAC.
    * Added an API /write/1/system/rebuildIndex which will regenerate the lucene index in a background thread.  To trigger this in a developer environment, 
        get an edit token: http://localhost:8180/rest/1/coordinate/editToken?ssoToken=TEST and then submit that edit token via a POST call (for example): 
        http://localhost:8180/rest/write/1/system/rebuildIndex?editToken=sCe3jZqsv04=AQAAAAEAAAFdCoNGCwADYlQAAABUAAAACKBR5iBP4VF0l9lT284urQ0HAAAAAgAAAAMAAAAEAAAABQAAAAYAAAAHAAAACA==

* 2017/06/30 - 4.10
    * Modifying getObjectForVuid to work on specific view coordinate parameters and changing to validate VUID uniqueness and validity before allowing 
        create or edit of VUID sememe.

* 2017/06/27 - 4.9
    * Intermediate build for testers.

* 2017/06/26 - 4.8
    * Changes for VUID server error message handling.

* 2017/06/22 - 4.7
    * Updated context for most developer installs.
    * Updating log event with latest dev box token.
    * Adding commented-out example values to prisme.properties under src/test/resources.

* 2017/06/16 - 4.6
    * Improvement and cleanup for ssoToken handling.
    * Documentation improvements and code cleanup. 

* 2017/06/14 - 4.5
    * Fix a bunch of issues with how we were handling the ssoToken internally, which lead to prisme not being able to parse back the ssoToken in some cases.
    * More error handling and debug code to aid investigating issues.

* 2017/06/08 - 4.4
    * Changes for VUID-rest service
    * Fortify fixes.
    * Adding extendedDescriptionTypeConceptSequence ctor parameters and javadoc an adding some, but not all, required  RestTest unit tests for description create and modify
    * change the intake api to return either json or xml
    * Moving extendedDescriptionTypeConcept from RestSememeDescriptionCreate down into RestSememeDescriptionUpdate

* 2017/06/01 - 4.3
    * Allowing IntakeWriteAPIs to consume json.
    * Updating prisme.properties with a log event token that will work locally in development.
    * Adding UserRoleConstants.SUPER_USER role to VuidWriteAPIs.

* 2017/05/21 - 4.2 
    * Changed default edit module to VHAT_EDIT.
    * Various API enhancements per the api changelog.
    * Sorted the returns of extendedDescriptionTypes and terminologyTypes
    * Added missing documentation on ssoToken

* 2017/05/05 - 4.1
    * Fix a bug where the translate API was returning "Optional[x]" instead of just "x" for VUIDs and SCTIDs.
    * Refactoring of some rest auth code for reuse

* 2017/05/02 - 4.0
    * Fixed 430032 - create on map set was ignoring active/inactive portion of request (always creating the new mapset as active)

* 2017/04/27 - 3.3
    * Change isaac metadata version to AUXILIARY_METADATA_VERSION
    * Changing recursive taxonomy methods to log and return partial results on
encountering exceptions/errors and improving exception in ConceptAPIs to
include problem concept id

* 2017/04/20 - 3.2
    * Log isaac metadata version number from codebase and database

* 2017/04/11 - 3.1
    * Added a configuration to the pom file that allows launching the server from the command line.  Execute: 'mvn compile -Pstart-server'

* 2017/03/20 - 3.0
    * Reversioned from 1.43
    * Production build for Release 3

* 2017/03/15 - 1.42
    * Just building to align ISAAC dependencies with what PRISME needs

* 2017/03/02 - 1.41
    * Minor fixes to logging configurations to fix mis-named log files.
    * Documentation enhancements to the API
    * Incorporate upstream metadata changes from ISAAC

* 2017/02/16 - 1.40
    * fix a couple of bugs with update and create mapset, in handling extended fields, where the extended field data was passed with an empty string value.
    * Sorted the response of 1/mapping/fields alphanumerically
    * Simplifying to use ConceptSpecification methods rather than REST calls

* 2017/02/09  - 1.39
    * Internally refatoring the computed display column code to simplify.
    * Changed the way that the RestMappingSetDisplayField.description was calulated, so that now for mapsets it will return useful values for the column headers - by saying things like "source description" or "target description" rather than saying SOURCE with no context.
    * Adding RestMappingItemVersionPage to contain paged mapping item results
    * Changing qualifier to equivalenceType in some places and adding TODOs in places that would break APIs

* 2017/02/03 - 1.38
    * Fixed the root cause of 439352, which was (sometimes) occurring when isaac-rest was deployed - especially if it needed to download a new database.
    * Fixed a bug in the map set definitions, where the wrong columns may have been returned for map items, if the target and qualifier columns were not 
        in positions 0 and 1 of the sememe
    * Defect 440248 Mapping Qualifier renamed to Equivalence Type with different values.
    * Task 444801 Rest API support for computed columns (map item display fields) in map sets
    * Fix null vs empty string checks on map item create / update for optional fields of qualifier and target.

* 2017/01/26 - 1.37
    * Many RestMapSet changes
    * Validation and tests added for mapping code

* 2017/01/19 - 1.36
    * Bugfixes in prisme log sending
    * Add git changeset integration (436032)
    * API changes from 1.9.6

* 2017/01/12 - 1.35
    * Making redundant null pointer checks for Fortify which is dumb.
    * Synchronizing access to CLIENT and eliminating very unlikely-to-occur null pointer exception.
    * Workflow changes to align with web, correct time conversion.
    * Multiple Prisme log service and runlevel modifications
    * Improving documentation, logging and shutdown, adding null check on shutdown.
    * Enabling logging from within the PrismeLogSenderService.
    * Adding per-event maximum retries.

* 2017/01/05 - 1.34
    * Adding initial support for writing errors and warnings back to PRISME
    * API changes per the ChangeLog file.
