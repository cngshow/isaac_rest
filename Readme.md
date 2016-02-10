ISAAC-Rest 

Simplified ISAAC APIs with REST access

Notes on server support:

Tomcat - works
Grizzly - works - see 'LocalJettyRunner' in the src/test/java folder.

GlassFish - Fatal incompatibility: https://java.net/jira/browse/GLASSFISH-21509
WildFly / JBoss - Annoyances with JavaFX support (need custom JBoss files - see commit history in WEB-INF folder), never got it to work 
with a current version of JAX-RS.  Issues with Array type support (due to old version of JAX-RS)
WebLogic - Better than WildFly - but still issues with using an old version of JAX-RS.  See commit history for (unsuccessful) attempts
to upgrade it. 