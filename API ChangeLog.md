ISAAC-Rest Changelog 

Any time a code change is made that impacts the API returned to callers, increment the value in API ChangeLog.md.

During development, we can increment this, so long as our client code (komet) is aware of the changes.

After an official release, any API change should be done by bumping the major version - and creating new rest paths (/rest/2/)
If reverse compatibility is required to be maintained, then the rest/1 code must remain.

2016/05/05 - 1.2 - Add the field 'sememeMembership' to the RestSememeVersion object.  Carries the set of concept sequences
	(that represent sememes) that the returned concept is a member of.  Added a query parameter to control the population to 
	both the 1/concept/version API and the 1/taxonomy/version API.  
	- Removed the expand options 'parents', 'children', 'countChildren', 'countParents' from the 1/concept/version API, 
	instead replacing these with query parameters, in a manner consistent with how they are specified in the 1/taxonomy/version API.  
2016/05/?? - 1.1 - A bunch of changes relating to stamp coordinate parsing and handling 
2016/04/22 - 1.0 - Introduction of version parameter.



