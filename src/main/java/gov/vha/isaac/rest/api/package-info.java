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
 * <h3><u>Coordinates Token (<code>RestCoordinatesToken</code>)</u></h3>
 * <p>The RestCoordinatesToken token serializes all values comprising the Taxonomy, Stamp, Language and Logic coordinates</p>.
 * <p>The pattern to use is to call getCoordinatesToken() with or without additional coordinate-specific parameters to get a token configured and returned.
 * From then on, the returned serialized token string is passed to each subsequent API call as an argument to the <code>coordToken</code> parameter,
 * along with any additional modifying parameters.</p>
 *
 * <h3><u>Taxonomy Coordinate (<code>RestTaxonomyCoordinate</code>) Parameters</u></h3>
 * <p>The <code>RestTaxonomyCoordinate</code> comprises a Premise/Taxonomy type (represented as boolean <code>stated</code>), as well as the other coordinates
 * <code>RestStampCoordinate</code>, <code>RestLanguageCoordinate</code> and <code>RestLogicCoordinate</code></p>
 * <p><code>stated</code> - specifies premise/taxonomy type of <i>stated</i> when <code>stated</code> is <code>true</code> and <i>inferred</i> when <code>false</code>.</p>
 * 
 * <h3><u>Stamp Coordinate (<code>RestStampCoordinate</code>) Parameters</u></h3>
 * <p><code>modules</code> - specifies modules of the StampCoordinate. Value may be a comma delimited list of module concept UUID or int ids.</p>	
 * <p><code>path</code> - specifies path component of StampPosition component of the StampCoordinate. Values is path UUID, int id or the term "development" or "master".  The default is "development".</p>
 * <p><code>precedence</code> - specifies precedence of the StampCoordinate. Values are either "path" or "time".  The default is "path".</p>
 * <p><code>allowedStates</code> - specifies allowed states of the StampCoordinate. Value may be a comma delimited list of State enum names.  The default is "active".</p>
 * <p><code>time</code> - specifies time component of StampPosition component of the StampCoordinate. Values are Long time values or "latest".  The default is "latest".</p>
 *
 * <h3><u>Language Coordinate (<code>RestLanguageCoordinate</code>) Parameters</u></h3>
 * <p><code>descriptionTypePrefs</code> - specifies the order preference of description types for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "fsn", "synonym" and/or "definition".  The default is "fsn,synonym".</p>
 * <p><code>dialectPrefs</code> - specifies the order preference of dialects for the LanguageCoordinate. Values are description type UUIDs, int ids or the terms "us" or "gb".  The default is "us,gb".</p>
 * <p><code>language</code> - specifies language of the LanguageCoordinate. Value may be a language UUID, int id or one of the following terms: "english", "spanish", "french", "danish", "polish", "dutch", "lithuanian", "chinese", "japanese", or "swedish".  The default is "english".</p>
 * 
 * <h3><u>Logic Coordinate (<code>RestLogicCoordinate</code>) Parameters</u></h3>
 * <p><code>logicStatedAssemblage</code> - specifies stated assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>logicInferredAssemblage</code> - specifies inferred assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>descriptionLogicProfile</code> - specifies description profile assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 * <p><code>classifier</code> - specifies classifier assemblage of the LogicCoordinate. Value may be a concept UUID string or int id.</p>	
 *
 * <h2>Expandables</h2>
 * <p>The server has the ability to return data to help understand the API in the form of "expandables".  By default, expandables are on, when the server
 * is deployed in a debug enviornment.  Expandables are disabled when deployed in a production environment.  To override the default, and return expandable
 * metadata in a production environment, add this parameter to any call:
 * <code>expandables=true</code>
 * </p>
 * 
 * <h2>Data Model</h2>
 * <p>
 * The only native data types required for storing terminology information within ISAAC are Concepts and Sememes. Sememes are highly flexible and configurable 
 * constructs in the system.
 * <br><br>
 * Each sememe that is defined (at runtime, by the needs of the terminology) can be thought of as a database table of its own, in a traditional database system.
 * All of the typical elements from a terminology data model, such as descriptions, relationships, attributes, refsets, etc. - can be mapped into either ISAAC 
 * concepts or ISAAC sememes, with no loss of fidelity. Furthermore, the sememes can be defined at the time that the data is imported, dynamically. The data model 
 * of the system does not have to change to be able to store new types of data.
 * <br><br>
 * Another core notion of the system is a Chronology and a Version. Each unique Concept or Sememe in the system has a Chronology - which carries that attributes 
 * of the entry that never change. For example, the identifier is never allowed to change - if the identifier is entered wrong, the item must be retired, and replaced 
 * by a new item with the correct identifier.
 * <br><br>
 * The Version carries that attributes of the item that DO change from version to version. For example, in a sememe that carries a definition - if there is a misspelling 
 * in the text value, a new version of the sememe would be created that has the corrected text. Both versions of the sememe link to the same sememe chronology, and have 
 * the same identifier. Now there are two different versions.
 * <br><br>
 * Chronology objects carry a list of all versions of the object. Chronology objects also all extend from OchreExternalizable, which means that they know how to serialize 
 * and deserialize themselves to an array of bytes - their most compact representation for storage.
 * <br><br>
 * The primary import, export, and change set formats of the system revolve around reading and writing the byte representation of the chronologies and versions from and 
 * to storage.
 * The ISAAC-Rest APIs are layered on top of this low-level java implementation - and provide access to both the lowest level of storage (concepts, sememes, chronologies, 
 * versions) but also provide various convenience abstractions, such as the notion of associations, mapsets, descriptions, etc.
 * <br>The Rest APIs also provide extensive search capabilities across the content of the system.
 * <br><br>
 * <img src="doc/ISAAC Core API.png"/>
 * </p>
 */
package gov.vha.isaac.rest.api;