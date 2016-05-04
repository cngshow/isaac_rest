/**
 * 
 * <h1>ISAAC Web Server</h1>
 * <p>
 * A REST server with a simple data model for accessing <u>ISAAC</u> functionality.
 * Primarily supports the <u>ISAAC web GUI</u>.
 * </p>
 * 
 * <p></p>
 * <p></p>
 * <p></p>
 * 
 * <h2>Coordinate Parameters</h2>
 * <p>
 * Most ISAAC REST Server calls return results that are contingent upon the value of coordinates.  Passing relevant parameters can change the results of the query, depending on the data, the request and whether or not the parameter value differs from its respective default value.
 * </p>
 * 
 * <h3><u>Coordinate Token Parameters</u></h3>
 * <p>The coordinates token comprises Taxonomy, Stamp, Language and Logic coordinates</p>
 *
 * <h3><u>Taxonomy Coordinate (<code>RestTaxonomyCoordinate</code>) Parameters</u></h3>
 * <p>The <code>RestTaxonomyCoordinate</code> comprises a Premise/Taxonomy type (represented as boolean <code>stated</code>)</p>
 * <p><code>stated</code> - specifies premise/taxonomy type of <i>stated</i> when <code>stated</code> is <code>true</code> and <i>inferred</i> when <code>false</code>.</p>
 * 
 * <h3><u>Language Coordinate (<code>RestLanguageCoordinate</code>) Parameters</u></h3>
 * <p><code>langCoordDescTypesPref</code> - specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
 * <p><code>langCoordDialectsPref</code> - specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
 * <p><code>langCoordLang</code> - specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
 * 
 * <h3><u>Stamp Coordinate (<code>RestStampCoordinate</code>) Parameters</u></h3>
 * <p><code>stampCoordModules</code> - specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
 * <p><code>stampCoordPath</code> - specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
 * <p><code>stampCoordPrecedence</code> - specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
 * <p><code>stampCoordStates</code> - specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".
 * <p><code>stampCoordTime</code> - specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".
 * 
 * <h3><u>Logic Coordinate (<code>RestLogicCoordinate</code>) Parameters</u></h3>
 * <p><code>logicCoordStated</code> - specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>logicCoordInferred</code> - specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>logicCoordDesc</code> - specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>logicCoordClassifier</code> - specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 *
 */
package gov.vha.isaac.rest.api;