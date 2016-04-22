ISAAC-Rest Changelog 

Any time a code change is made that impacts the API returned to callers, increment the value in API ChangeLog.md.

During development, we can increment this, so long as our client code (komet) is aware of the changes.

After an official release, any API change should be done by bumping the major version - and creating new rest paths (/rest/2/)
If reverse compatibility is required to be maintained, then the rest/1 code must remain.

1.0 - Introduction of version parameter. 



