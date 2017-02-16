ISAAC-rest Changelog 

This changelog summarizes changes and fixes which are a part of each revision.  For more details on the fixes, refer tracking numbers 
where provided, and the git commit history.  Note that this is not the same as the API Changelog.md.  This file will contain information
on all changes - including bug fixes.  The API Changelog will only contain documentation on changes of the API - and those are tied to the 
API version number, not the release version number.

* 2017/02/??  - 1.40 - PENDING
    * fix a couple of bugs with update and create mapset, in handling extended fields, where the extended field data was passed with an empty string value.
    * Sorted the response of 1/mapping/fields alphanumerically

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
