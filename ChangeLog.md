ISAAC-rest Changelog 

This changelog summarizes changes and fixes which are a part of each revision.  For more details on the fixes, refer tracking numbers 
where provided, and the git commit history.  Note that this is not the same as the API Changelog.md.  This file will contain information
on all changes - including bug fixes.  The API Changelog will only contain documentation on changes of the API - and those are tied to the 
API version number, not the release version number.

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
