ISAAC-rest Changelog 

This changelog summarizes changes and fixes which are a part of each revision.  For more details on the fixes, refer tracking numbers where provided, and the git commit history.  Note that this is not the same as the API Changelog.md.  This file will contain information on all changes - including bug fixes.  The API Changelog will only contain documentation on changes of the API - and those are tied to the 
API version number, not the release version number.

* 2017/06/?? - 4.6 - PENDING

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
