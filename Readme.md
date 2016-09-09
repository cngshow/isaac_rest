ISAAC-Rest 

Simplified ISAAC APIs with REST access

Notes on server support:

- Tomcat - works

- Grizzly - works - see 'LocalJettyRunner' in the src/test/java folder.

- GlassFish - Fatal incompatibility: https://java.net/jira/browse/GLASSFISH-21509

- WildFly / JBoss - Annoyances with JavaFX support (need custom JBoss files - see commit history in WEB-INF folder), never got it to work 
with a current version of JAX-RS.  Issues with Array type support (due to old version of JAX-RS)

- WebLogic - Better than WildFly - but still issues with using an old version of JAX-RS.  See commit history for (unsuccessful) attempts
to upgrade it. 

- To run in Eclipse, set a system property called -DisaacDatabaseLocation pointing to the location of the .data file. For example, on my system the path is -DisaacDatabaseLocation=c:\temp\database\vhat-2016.01.07-1.0-SNAPSHOT-all.data. In Eclipse, put this in the VM Argument tab under the Run Configurations menu.

- To run HP Fortify scan (assuming Fortify application and license installed)
	$ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId=isaac-rest -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:clean
	$ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId=isaac-rest -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:translate
	$ mvn -Dmaven.test.skip=true -Dfortify.sca.buildId=isaac-rest -Dfortify.sca.toplevel.artifactId=isaac-parent com.hpe.security.fortify.maven.plugin:sca-maven-plugin:scan

