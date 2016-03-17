/**
 * Contains classes that should never change in structure 
 * from one version of the API to another.
 * 
 * TESTENUNCIATE
 * 	@QueryParam(RequestParameters.stampCoordTime) @DefaultValue(RequestParameters.stampCoordTimeDefault) String stampCoordTime,
 *	@QueryParam(RequestParameters.stampCoordPath) @DefaultValue(RequestParameters.stampCoordPathDefault) String stampCoordPath,
 *	@QueryParam(RequestParameters.stampCoordPrecedence) @DefaultValue(RequestParameters.stampCoordPrecedenceDefault) String stampCoordPrecedence,
 *	@QueryParam(RequestParameters.stampCoordModules) @DefaultValue(RequestParameters.stampCoordModulesDefault) String stampCoordModules,
 *	@QueryParam(RequestParameters.stampCoordStates) @DefaultValue(RequestParameters.stampCoordStatesDefault) String stampCoordStates,
 *			
 *	@QueryParam(RequestParameters.langCoordLang) @DefaultValue(RequestParameters.langCoordLangDefault) String langCoordLang,
 *	@QueryParam(RequestParameters.langCoordDescTypesPref) @DefaultValue(RequestParameters.langCoordDescTypesPrefDefault) String langCoordDescTypesPref,
 *	@QueryParam(RequestParameters.langCoordDialectsPref) @DefaultValue(RequestParameters.langCoordDialectsPrefDefault) String langCoordDialectsPref
 */
package gov.vha.isaac.rest.api;