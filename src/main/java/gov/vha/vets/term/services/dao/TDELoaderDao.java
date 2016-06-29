package gov.vha.vets.term.services.dao;

/*
 * Created on Feb 21, 2007
 */

import gov.vha.vets.term.services.dto.delta.ConceptDelta;
import gov.vha.vets.term.services.dto.delta.DesignationDelta;
import gov.vha.vets.term.services.dto.delta.DesignationPropertyDelta;
import gov.vha.vets.term.services.dto.delta.PropertyDelta;
import gov.vha.vets.term.services.dto.delta.RelationshipDelta;
import gov.vha.vets.term.services.dto.delta.SubsetDelta;
import gov.vha.vets.term.services.dto.delta.SubsetRelationshipDelta;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.ChangeGroup;
import gov.vha.vets.term.services.model.RelationshipType;
import gov.vha.vets.term.services.business.VuidDelegate;
import gov.vha.vets.term.services.model.Vuid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author VHAISLJACOXB
 */
public class TDELoaderDao
{
    private static Logger log = Logger.getLogger(TDELoaderDao.class.getPackage().getName());

    public static final int VHAT_CODESYSTEM_ID = 1;
    public static final String TDE_SYSTEM = "TDE";
    public static final String VTS_SYSTEM = "VTS";
    public static final String TDE_SOURCE = ChangeGroup.SourceName.TDE.toString();

    /**
     * retrieve the differences between VTS1 and VTS2
     * 
     * @param connection
     * @return map of difference
     * @throws Exception
     */
    public static Map<String, List<DesignationDelta>> getDesignationDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        String vts2Sql = "d.NAME, d.code, cc.code AS conceptcode, " +
			"  (SELECT NAME FROM TYPE dt WHERE dt.kind = 'D' AND dt.ID = d.type_id) AS designationtype, " +
			"     (CASE WHEN d.active = '0' THEN 'Inactive' WHEN d.active = '1' THEN 'Active' END) AS active " +
			"  FROM concept d, concept cc, relationship dr " +
			" WHERE d.kind = 'D' AND cc.kind = 'C' AND dr.kind = 'D' " +
			"   AND cc.codesystem_id = (SELECT cs.ID FROM CODESYSTEM cs WHERE cs.NAME = 'VHAT') " +
			"   AND cc.entity_id = dr.source_entity_id  AND d.entity_id = dr.target_entity_id " +
			"   AND d.id = (SELECT MAX(dmax.ID) FROM concept dmax, changegroup cg " +
			"                  WHERE dmax.kind = 'D' AND dmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND d.entity_id = dmax.entity_id)  " +
			"   AND dr.id = (SELECT MAX(drmax.ID) FROM relationship drmax, changegroup cg " +
			"                  WHERE drmax.kind = 'D' AND drmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND dr.entity_id = drmax.entity_id) " +
         	"   AND cc.id = (SELECT MAX(ccmax.ID) FROM concept ccmax, changegroup cg " +
         	"                  WHERE ccmax.kind = 'C' AND ccmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND cc.entity_id = ccmax.entity_id) ";

        String tdeSql = " (SELECT VALUE FROM DBNAME.dl_property_str@DBLINK p WHERE p.con_gid = ddc.con_gid AND p.prop_gid = (SELECT gid FROM DBNAME.dl_property_def@DBLINK WHERE NAME = 'Designation_Text')) AS NAME, "
            + "  (select code from DBNAME.dl_concept@DBLINK where ddc.con_gid = gid) as code, " 
            + "  (SELECT code FROM DBNAME.dl_concept@DBLINK "
            + "   WHERE gid = a.value_gid) AS conceptCode, "
            + " (SELECT VALUE "
            + "    FROM DBNAME.dl_property_str@DBLINK p "
            + "   WHERE p.con_gid = ddc.con_gid "
            + "     AND p.prop_gid = (SELECT gid "
            + "                         FROM DBNAME.dl_property_def@DBLINK "
            + "                        WHERE NAME = 'Designation_Type')) "
            + "                                   AS designationtype, "
            + " (SELECT VALUE FROM DBNAME.dl_property_str@DBLINK p WHERE p.con_gid = ddc.con_gid AND p.prop_gid = (SELECT gid FROM DBNAME.dl_property_def@DBLINK WHERE NAME = 'Status')) as STATUS "
            + " FROM DBNAME.dl_defining_concept@DBLINK ddc, DBNAME.dl_con_assn@DBLINK a WHERE ddc.sup_gid = (SELECT gid "
            + "    FROM DBNAME.dl_concept@DBLINK WHERE NAME = 'Designations') AND a.con_gid = ddc.con_gid AND a.assn_gid = (SELECT gid "
            + "    FROM DBNAME.dl_association_def@DBLINK WHERE NAME = 'Associated_Concept')";

        ResultSet resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
        Map<String, List<DesignationDelta>> map = new HashMap<String, List<DesignationDelta>>();
        try
        {
            while (resultSet.next())
            {
                DesignationDelta delta = new DesignationDelta();
                delta.setSystem(resultSet.getString("system"));
                delta.setCode(resultSet.getString("code"));
                delta.setConceptCode(resultSet.getString("conceptCode"));
                delta.setType(resultSet.getString("designationType"));
                delta.setName(resultSet.getString("name"));
                String status = resultSet.getString("status");
                if (status == null)
                {
					throw new STSException("Status is null for Designation: " + delta.getName() + " with code: " + delta.getCode());
                }
                if ("active".equalsIgnoreCase(status))
                {
                    delta.setActive(true);
                }
                else
                {
                    delta.setActive(false);
                }

                long vuid = Long.valueOf(delta.getCode());
                delta.setVuid(vuid);

                delta.setVuid(vuid);
                List<DesignationDelta> deltas = map.get(delta.getCode());
                if (deltas == null)
                {
                    deltas = new ArrayList<DesignationDelta>();
                    map.put(delta.getCode(), deltas);
                }
                deltas.add(delta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception(e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }

        return map;
    }

    /**
     * Retrieve the concept differences between VTS and VTS2.
     * 
     * @param connection
     * @return HashMap of the differences between VTS and VTS2
     * @throws Exception
     */
    public static Map<String, List<ConceptDelta>> getConceptDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        String tdeSql = " c2.CODE, LTRIM(RTRIM(SUBSTR(c2.name, 1, INSTR(c2.name, '[C]', -1 ,1)-1))) as NAME,   "
                + " (SELECT VALUE FROM DBNAME.dl_property_str@DBLINK p WHERE p.con_gid = c2.gid AND p.prop_gid = (SELECT gid FROM DBNAME.dl_property_def@DBLINK WHERE NAME = 'Status')) as STATUS "
                + " from DBNAME.dl_defining_concept@DBLINK ddc, DBNAME.dl_concept@DBLINK c, DBNAME.dl_concept@DBLINK c2   "
                + " where ddc.SUP_GID = c.gid and c.name = 'Concepts' and ddc.CON_GID = c2.gid   ";

        String vts2Sql = "cc.code AS code, cc.NAME AS NAME, "
                + "  (CASE WHEN cc.active = '0' THEN 'Inactive' WHEN cc.active = '1' THEN 'Active' END) AS status"
                + "  FROM concept cc, codesystem cs"
               	+ " WHERE cc.kind = 'C' AND cc.codesystem_id = (SELECT cs.ID FROM CODESYSTEM cs WHERE cs.NAME = 'VHAT') "
             	+ "     AND cc.id = (SELECT MAX(ccmax.ID) from concept ccmax, changegroup cg "
             	+ "                  WHERE ccmax.kind = 'C' AND ccmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND cc.entity_id = ccmax.entity_id) ";


        ResultSet resultSet = null;
        Map<String, List<ConceptDelta>> map = null;
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            map = new HashMap<String, List<ConceptDelta>>();
            
            while (resultSet.next())
            {
                ConceptDelta delta = new ConceptDelta();
                delta.setSystem(resultSet.getString("SYSTEM"));
                delta.setCode(resultSet.getString("CODE"));
                delta.setName(resultSet.getString("NAME"));
                
                Long vuid = Long.valueOf(delta.getCode());
                
				delta.setVuid(vuid);
                String status = resultSet.getString("status");
                if (status == null)
                {
					throw new STSException("Status is null for Coded Concept: " + delta.getName() + " with code: " + delta.getCode());
                }
                if ("active".equalsIgnoreCase(status))
                {
                    delta.setActive(true);
                }
                else
                {
                    delta.setActive(false);
                }

                List<ConceptDelta> deltas = map.get(delta.getCode());
                if (deltas == null)
                {
                    deltas = new ArrayList<ConceptDelta>();
                    map.put(delta.getCode(), deltas);
                }
                deltas.add(delta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getConceptDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }

        return map;
    }

    /**
     * retrieve the property differences between VTS and VTS2
     * 
     * @return list of differences
     * @throws Exception
     */
    public static Map<String, List<PropertyDelta>> getPropertyDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        Map<String, List<PropertyDelta>> map = new HashMap<String, List<PropertyDelta>>();

        String tdeSql = 
                  " p.VALUE, pt.NAME, (SELECT c.code FROM DBNAME.DL_CONCEPT@DBLINK c WHERE c.GID = p.CON_GID) AS code  "
                + "  FROM (select * from DBNAME.DL_PROPERTY_STR@DBLINK union all select * from DBNAME.dl_long_property_str@DBLINK) p, DBNAME.DL_PROPERTY_DEF@DBLINK pt "
                + " WHERE p.PROP_GID = pt.ID AND pt.name <> 'VUID' and pt.name <> 'Status'"
                + "   AND con_gid IN (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK"
                + "   WHERE gid IN (SELECT con_gid FROM DBNAME.DL_DEFINING_CONCEPT@DBLINK  "
                + "       WHERE sup_gid = (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK WHERE NAME = 'Concepts'))) ";

        String vts2Sql = 
                  "pp.property_value AS VALUE, cpt.name AS name, cc.code AS code "
                + "  FROM property pp, concept cc, type cpt, codesystem cs "
                + "WHERE "
                + "   cc.kind = 'C' AND cc.codesystem_id = (SELECT cs.ID FROM CODESYSTEM cs WHERE cs.NAME = 'VHAT') "
                + "   AND pp.active = 1 "
                + "   AND cpt.kind = 'P' AND cpt.id = pp.propertytype_id  "
                + "   AND cc.entity_id = pp.conceptEntity_id "
                + "   AND cc.id = (SELECT MAX(ccmax.ID) from concept ccmax, changegroup cg  "
                + "        WHERE ccmax.kind = 'C' AND ccmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND cc.entity_id = ccmax.entity_id) "
                + "   AND pp.id = (SELECT MAX(propmax.id) from property propmax, changegroup cg "
                + "        WHERE propmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND propmax.entity_id = pp.entity_id) ";

        ResultSet resultSet = null;        
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            while (resultSet.next())
            {
                PropertyDelta delta = new PropertyDelta();
                delta.setSystem(resultSet.getString("system"));
                delta.setValue(resultSet.getString("value"));
                delta.setType(resultSet.getString("name"));
                delta.setCode(resultSet.getString("code"));

                String key = delta.getCode()+"+"+delta.getType();
                List<PropertyDelta> deltas = map.get(key);
                if (deltas == null)
                {
                    deltas = new ArrayList<PropertyDelta>();
                    map.put(key, deltas);
                }
                deltas.add(delta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getPropertyDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }

        return map;
    }

    /**
     * retrieve the property differences between VTS and VTS2
     * 
     * @return list of differences
     * @throws Exception
     */
    public static Map<String, List<DesignationPropertyDelta>> getDesignationPropertyDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        String tdeSql = "p.VALUE,  pt.NAME, "
                + "(select con.code from DBNAME.dl_concept@DBLINK con where con.gid = p.con_gid) as code "
                + "FROM (select * from DBNAME.DL_PROPERTY_STR@DBLINK union all select * from DBNAME.dl_long_property_str@DBLINK) p, DBNAME.DL_PROPERTY_DEF@DBLINK pt "
                + " WHERE p.PROP_GID = pt.ID "
                + " AND pt.name <> 'VUID' AND pt.name <> 'Status' AND pt.name <> 'Designation_Type' and pt.name <> 'Designation_Text' "
                + " AND con_gid IN (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK "
                + " WHERE gid IN (SELECT con_gid FROM DBNAME.DL_DEFINING_CONCEPT@DBLINK WHERE sup_gid = (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK WHERE NAME = 'Designations'))) ";

        String vts2Sql = "p.property_value as value, t.name as name, d.code "
                + "FROM PROPERTY p, Concept d, Relationship dr, Concept con, Type t "
                + "WHERE con.kind = 'C' AND p.propertytype_id = t.id "
                + "  AND p.active = 1 "
                + "  AND p.conceptentity_id = d.entity_id "
                + "  AND d.kind = 'D' AND con.kind = 'C' "
                + "  AND d.entity_id = dr.target_entity_id "
                + "  AND dr.kind = 'D' "
                + "  AND dr.source_entity_id = con.entity_id "
                + "  AND con.codesystem_id = (SELECT cs.id FROM codesystem cs WHERE cs.name = 'VHAT') "
                + "  AND p.id = (SELECT MAX(propmax.id) FROM property propmax, changegroup cg "
                + "        WHERE propmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND propmax.entity_id = p.entity_id) "                    
                + "  AND d.id = (SELECT MAX (dmax.id) FROM concept dmax, changegroup cg "
                + "        WHERE dmax.kind = 'D' AND dmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND dmax.entity_id = d.entity_id) "
                + "  AND dr.id = (SELECT MAX(drmax.ID) FROM relationship drmax, changegroup cg " 
                + "        WHERE drmax.kind = 'D' AND drmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND dr.entity_id = drmax.entity_id) "
                + "  AND con.id = (SELECT MAX(ccmax.ID) from concept ccmax, changegroup cg " 
                + "        WHERE ccmax.kind = 'C' AND ccmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND con.entity_id = ccmax.entity_id) ";

        Map<String, List<DesignationPropertyDelta>> map = new HashMap<String, List<DesignationPropertyDelta>>();
        ResultSet resultSet = null;
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            while (resultSet.next())
            {
                DesignationPropertyDelta delta = new DesignationPropertyDelta();
                delta.setSystem(resultSet.getString("system"));
                delta.setValue(resultSet.getString("value"));
                delta.setType(resultSet.getString("name"));
                delta.setCode(resultSet.getString("code"));

                String key = delta.getCode()+delta.getType();
                List<DesignationPropertyDelta> deltas = map.get(key);
                if (deltas == null)
                {
                    deltas = new ArrayList<DesignationPropertyDelta>();
                    map.put(key, deltas);
                }
                deltas.add(delta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getDesignationPropertyDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }

        return map;
    }

    /**
     * retrieve the results
     * 
     * @param tdeSql
     * @param vtsSql
     * @param tdeStation
     * @return the resultSet
     * @throws Exception
     * 
     * retrieve the differences between TDE and VTS
     * 
     * @return map of difference
     * @throws Exception
     */
    public static Map<String, List<SubsetRelationshipDelta>> getSubsetRelationshipDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        String tdeSql = " (SELECT NAME FROM DBNAME.dl_concept@DBLINK WHERE gid = r.con_gid) AS subsetName, "
                + " (select code from DBNAME.dl_concept@DBLINK where r.value_gid = gid) AS designationcode "
                + " FROM DBNAME.dl_defining_role@DBLINK r WHERE role_gid = (SELECT gid "
                + "    FROM DBNAME.dl_role_def@DBLINK WHERE NAME = 'has_member') ";

        String vts2Sql = "  sub.name as subsetName, des.code as designationcode "
        	+ "FROM relationship sr, concept sub, concept des "
        	+ " WHERE sr.kind = 'S' AND sub.kind = 'S' AND des.kind = 'D' "
        	+ "   AND sr.source_entity_id = sub.entity_id AND sr.target_entity_id = des.entity_id " 
        	+ "   AND sr.active = 1 "
        	+ "   AND sr.id = (SELECT MAX (srmax.id) FROM relationship srmax, changegroup cg " 
        	+ "          WHERE srmax.kind = 'S' AND srmax.changegroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND srmax.entity_id = sr.entity_id) "
        	+ "   AND sub.id = (SELECT MAX (submax.id) FROM concept submax, changegroup cg " 
        	+ "          WHERE submax.kind = 'S' AND submax.changegroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND submax.entity_id = sub.entity_id) "
        	+ "   AND des.id = (SELECT MAX (desmax.id) FROM concept desmax, changegroup cg " 
        	+ "          WHERE desmax.kind = 'D' AND desmax.changegroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND desmax.entity_id = des.entity_id) ";   

        Map<String, List<SubsetRelationshipDelta>> map = new HashMap<String, List<SubsetRelationshipDelta>>();
        int count = 0;
        ResultSet resultSet = null;
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            while (resultSet.next())
            {
                String system = resultSet.getString("SYSTEM");
                String subsetName = resultSet.getString("subsetname");
                String designationCode = resultSet.getString("designationCode");
                SubsetRelationshipDelta delta = new SubsetRelationshipDelta(system, subsetName, designationCode);

                String key = delta.getSubsetName()+delta.getDesignationCode();
                List<SubsetRelationshipDelta> deltas = map.get(key);
                if (deltas == null)
                {
                    deltas = new ArrayList<SubsetRelationshipDelta>();
                    map.put(key, deltas);
                }
                deltas.add(delta);
                count++;
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getSubsetRelationshipDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }
        return map;
    }

    public static Map<String, List<SubsetDelta>> getSubsetDeltas(Connection connection,String databaseName, String databaseLink) throws Exception
    {
        String tdeSql = " dlc.NAME as NAME, " 
                + " (SELECT p.VALUE FROM DBNAME.dl_property_str@DBLINK p WHERE p.con_gid = dldc.con_gid AND p.prop_gid = (SELECT gid "
                + " FROM DBNAME.dl_property_def@DBLINK WHERE NAME = 'Status')) as STATUS, dlc.code "
                + " FROM DBNAME.dl_defining_concept@DBLINK dldc, DBNAME.dl_concept@DBLINK dlc WHERE dldc.con_gid = dlc.GID "
                + "  AND dldc.SUP_GID in (SELECT CON_GID FROM DBNAME.dl_defining_concept@DBLINK ddc, "
                + " DBNAME.dl_concept@DBLINK s, DBNAME.dl_concept@DBLINK s2 WHERE ddc.SUP_GID = s.gid AND s.name = 'Subsets' "
                + " AND ddc.CON_GID = s2.gid) ";

        String vts2Sql = " sub.NAME, (CASE WHEN sub.ACTIVE = '0' THEN 'Inactive' "
                + " WHEN sub.ACTIVE = '1' THEN 'Active' END) as STATUS, sub.code FROM CONCEPT sub WHERE sub.kind = 'S' "
                + "    AND sub.id = (SELECT MAX (submax.id) FROM concept submax, changegroup cg "
                + "        WHERE submax.kind = 'S' AND submax.code IS NOT NULL AND submax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' "
                + "              AND sub.entity_id = submax.entity_id) ";

        ResultSet resultSet = null;
        Map<String, List<SubsetDelta>> map = new HashMap<String, List<SubsetDelta>>();

        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            while (resultSet.next())
            {
                SubsetDelta delta = new SubsetDelta();
                delta.setSystem(resultSet.getString("SYSTEM"));
                delta.setCode(resultSet.getString("code"));
                delta.setName(resultSet.getString("NAME"));

                long vuid= Long.valueOf(delta.getCode());
                
                delta.setVuid(vuid);
                String status = resultSet.getString("status");
                if ("active".equalsIgnoreCase(status))
                {
                    delta.setStatus(true);
                }
                else
                {
                    delta.setStatus(false);
                }

                List<SubsetDelta> deltas = map.get(delta.getCode());
                if (deltas == null)
                {
                    deltas = new ArrayList<SubsetDelta>();
                    map.put(delta.getCode(), deltas);
                }
                deltas.add(delta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getSubsetDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }

        return map;
    }

    /**
     * 
     * @param tdeStation
     * @return
     * @throws Exception
     */
    public static Map<String, List<RelationshipDelta>> getRelationshipDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {

        String tdeSql = " (SELECT c.code FROM DBNAME.DL_CONCEPT@DBLINK c WHERE c.ID = ROLE.VALUE_GID) AS target, "
            + " (SELECT c.code FROM DBNAME.DL_CONCEPT@DBLINK c WHERE c.ID = ROLE.con_gid) AS SOURCE, " + "  rd.NAME as TYPE"
            + "  FROM DBNAME.DL_DEFINING_ROLE@DBLINK ROLE, DBNAME.DL_ROLE_DEF@DBLINK rd " + " WHERE ROLE.role_gid = rd.gid "
            + "   AND rd.name <> 'has_designation'" + "   AND con_gid IN (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK WHERE gid IN (SELECT con_gid "
            + "   FROM DBNAME.DL_DEFINING_CONCEPT@DBLINK WHERE sup_gid = (SELECT ID FROM DBNAME.DL_CONCEPT@DBLINK WHERE NAME = 'Concepts'))) ";

        String vts2Sql = "cont.code AS target, cons.code AS source, t.name AS type "
            + " FROM relationship rel, type t, concept cont, concept cons "
            + "WHERE rel.kind = 'C' AND rel.active = 1 "
            + "  AND cont.kind = 'C' AND cons.kind = 'C' "
            + "  AND rel.source_entity_id = cons.entity_id "
            + "  AND rel.target_entity_id = cont.entity_id "
            + "  AND rel.type_id = t.id AND t.kind = 'R' AND t.name <> '"+RelationshipType.HAS_PARENT+"' "
            + "  AND cons.codesystem_id = (SELECT cs.ID FROM CODESYSTEM cs WHERE cs.NAME = 'VHAT') "
            + "  AND cont.codesystem_id = (SELECT cs.ID FROM CODESYSTEM cs WHERE cs.NAME = 'VHAT') "
            + "  AND rel.id = (SELECT MAX(relmax.ID) FROM relationship relmax, changegroup cg " 
            + "      WHERE relmax.kind = 'C' AND relmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND rel.entity_id = relmax.entity_id) "
            + "  AND cons.id = (SELECT MAX(conmax.ID) FROM concept conmax, changegroup cg " 
            + "      WHERE conmax.kind = 'C' AND conmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND cons.entity_id = conmax.entity_id) "
            + "  AND cont.id = (SELECT MAX(conmax.ID) FROM concept conmax, changegroup cg " 
            + "      WHERE conmax.kind = 'C' AND conmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND cont.entity_id = conmax.entity_id) ";
        Map<String, List<RelationshipDelta>> map = new HashMap<String, List<RelationshipDelta>>();
        ResultSet resultSet = null;
        
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            while (resultSet.next())
            {
                RelationshipDelta relationshipDelta = new RelationshipDelta();
                relationshipDelta.setSystem(resultSet.getString("System"));
                relationshipDelta.setSourceCode(resultSet.getString("Source"));
                relationshipDelta.setTargetCode(resultSet.getString("Target"));
                relationshipDelta.setRelationshipType(resultSet.getString("Type"));
                String key = relationshipDelta.getSourceCode() + relationshipDelta.getRelationshipType();
                List<RelationshipDelta> deltas = map.get(key);
                if (deltas == null)
                {
                    deltas = new ArrayList<RelationshipDelta>();
                    map.put(key, deltas);
                }
                deltas.add(relationshipDelta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getRelationshipDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }
        return map;
    }

    /**
     * 
     * @param tdeStation
     * @return
     * @throws ETSDataException
     */
    public static Map<String, List<RelationshipDelta>> getParentChildRelationshipDeltas(Connection connection, String databaseName, String databaseLink) throws Exception
    {
        ResultSet resultSet = null;

        String tdeSql = " sup.code as target, con.code as source,'has_parent' as type  "
        + "    from DBNAME.dl_defining_concept@DBLINK, DBNAME.dl_concept@DBLINK con, DBNAME.dl_concept@DBLINK sup "
        + "where con.gid = con_gid and sup.gid = sup_gid  and con.name like '%[C]' and sup.name like '%[C]' ";

        String vts2Sql = "cont.code as target, cons.code as source, t.name as type "
        	+ "FROM relationship rel, concept cons, concept cont, type t "
        	+ " WHERE rel.kind = 'C' AND cons.kind = 'C' AND cont.kind = 'C' AND t.kind = 'R' "
        	+ "   AND rel.source_entity_id = cons.entity_id AND rel.target_entity_id = cont.entity_id "
        	+ "   AND rel.type_id = t.id AND t.name = 'has_parent' "
        	+ "   AND cons.codesystem_id = (SELECT cs.id FROM codesystem cs WHERE cs.name = 'VHAT') "
        	+ "   AND rel.active = 1 "
        	+ "   AND rel.id = (SELECT MAX(relmax.id) FROM relationship relmax, changegroup cg "
        	+ "              WHERE relmax.kind = 'C' AND relmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND relmax.entity_id = rel.entity_id) "
        	+ "   AND cons.id = (SELECT MAX(conmax.id) FROM concept conmax, changegroup cg "
        	+ "              WHERE conmax.kind = 'C' AND conmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND conmax.entity_id = cons.entity_id) "
        	+ "   AND cont.id = (SELECT MAX(conmax.id) FROM concept conmax, changegroup cg "
        	+ "              WHERE conmax.kind = 'C' AND conmax.changeGroup_id = cg.id AND cg.source = '"+TDE_SOURCE+"' AND conmax.entity_id = cont.entity_id) "; 

        Map<String, List<RelationshipDelta>> map = new HashMap<String, List<RelationshipDelta>>();
        
        try
        {
            resultSet = getDeltasResultSet(connection, databaseName, databaseLink, tdeSql, vts2Sql);
            
            while (resultSet.next())
            {
                RelationshipDelta relationshipDelta = new RelationshipDelta();
                relationshipDelta.setSystem(resultSet.getString("System"));
                relationshipDelta.setSourceCode(resultSet.getString("Source"));
                relationshipDelta.setTargetCode(resultSet.getString("Target"));
                relationshipDelta.setRelationshipType(resultSet.getString("Type"));
                String key = relationshipDelta.getSourceCode() + relationshipDelta.getRelationshipType();
                List<RelationshipDelta> deltas = map.get(key);
                if (deltas == null)
                {
                    deltas = new ArrayList<RelationshipDelta>();
                    map.put(key, deltas);
                }
                deltas.add(relationshipDelta);
            }
        }
        catch (SQLException e)
        {
            throw new Exception("Failed in getParentChildRelationshipDeltas: ["+e.getMessage()+"]", e);
        }
        finally
        {
            finalize(null, null, resultSet);
        }
        return map;
    }

    protected static ResultSet getDeltasResultSet(Connection connection, String databaseName, String databaseLink, String tdeSql, String vtsSql) throws Exception
    {
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        tdeSql = fixupQuery(databaseName, databaseLink, tdeSql);

        String system1 = "SELECT '" + TDE_SYSTEM + "' as SYSTEM, ";
        String system2 = "SELECT '" + VTS_SYSTEM + "' as SYSTEM, ";
        String sql = system1 + tdeSql + " minus " + system1 + vtsSql + " union all " + system2 + vtsSql + " minus " + system2 + tdeSql;
        statement = connection.prepareStatement(sql);
        resultSet = statement.executeQuery();
        
        return resultSet;
    }

    /**
     * @param databaseName
     * @param databaseLink
     * @param tdeSql
     * @return
     */
    private static String fixupQuery(String databaseName, String databaseLink, String tdeSql)
    {
        if (databaseName != null && databaseName.length() > 1)
        {
            databaseName = databaseName+".";
        }
        tdeSql = tdeSql.replaceAll("DBNAME.", databaseName);
        if (databaseLink == null || "".equals(databaseLink))
        {
            tdeSql = tdeSql.replaceAll("@DBLINK", "");
        }
        else
        {
            tdeSql = tdeSql.replaceAll("DBLINK", databaseLink);
        }
        return tdeSql;
    }

    

    /**
     * Get a count of the number of vuids needed for stamping the TDE database
     * 
     * @param connection
     * @param databaseName
     * @param databaseLink
     * @return
     * @throws STSException
     */
    public static void updateVUIDsInTDE(Connection connection, String databaseName, String databaseLink) throws STSException
    {
        PreparedStatement statement = null;
        int id = 0;
        int vuidCount = 0;
        String sql = "select c.code, p.con_prop_id as id, p.value from DBNAME.dl_concept@DBLINK c left outer join DBNAME.dl_property_str@DBLINK p on p.con_gid = c.gid and p.PROP_GID = ? where c.code like 'C%'";
        Set<String> codes = new HashSet<String>();
        try
        {
            statement = connection.prepareStatement(fixupQuery(databaseName, databaseLink, "select id from DBNAME.dl_property_def@DBLINK where name = 'VUID'"));
            ResultSet result = statement.executeQuery();
            if(result.next())
            {
                id = result.getInt("id");
            }
            result.close();
            statement.close();
            
            statement = connection.prepareStatement(fixupQuery(databaseName, databaseLink, sql));
            statement.setInt(1, id);
            result = statement.executeQuery();
            while(result.next())
            {
                String code = result.getString("code");
                String vuid = result.getString("value");
                long propId = result.getLong("id");
                if (vuid == null || vuid.isEmpty())
                {
                    vuidCount++;
                    codes.add(code);
                }
                else
                {
                    updateVUID(connection, databaseName, databaseLink, code, vuid, propId);                
                }
            }
            result.close();
            statement.close();
            if (vuidCount > 0)
            {
                VuidDelegate vuidDelegate = new VuidDelegate();
                Vuid vuid = vuidDelegate.createVuidRange(vuidCount, "TDS", "TDE Import");
                long startVuid = vuid.getStartVuid();
                for (String code : codes)
                {
                    updateVUID(connection, databaseName, databaseLink, code, String.valueOf(startVuid), null);
                    startVuid++;
                }
            }
        }
        catch (Exception ex)
        {
            throw new STSException(ex);
        }
    }


    /**
     * Update VUID for a given entry
     * @param connection
     * @param databaseName
     * @param databaseLink
     * @param data
     * @param vuid
     * @throws STSException
     */
    public static void updateVUID(Connection connection, String databaseName, String databaseLink, String code, String vuid, Long propId) throws STSException
    {

        try
        {
            String updateSql1 = "update DBNAME.dl_concept@DBLINK set code = ? where code = ?";
            PreparedStatement updateStatement = connection.prepareStatement(fixupQuery(databaseName, databaseLink, updateSql1));
            updateStatement.setString(1, vuid);
            updateStatement.setString(2, code);
            if (updateStatement.executeUpdate() == 0)
            {
                throw new STSException("Failed to Update TDE Database for code: "+code);
            }
            updateStatement.close();
            
            if (propId != null)
            {
                String updateSql2 = "delete from DBNAME.dl_property_str@DBLINK where con_prop_id = ?";
                updateStatement = connection.prepareStatement(fixupQuery(databaseName, databaseLink, updateSql2));
                updateStatement.setLong(1, propId);
                if (updateStatement.executeUpdate() == 0)
                {
                    throw new STSException("Failed to Remove VUID property for code: "+code);
                }
                updateStatement.close();
            }
        }
        catch (SQLException e)
        {
            throw new STSException(e);
        }
    }

    /**
     * @param connection
     * @param statement
     * @param resultSet
     */
    protected static void finalize(Connection connection, PreparedStatement statement, ResultSet resultSet)
    {
        try
        {
            if (resultSet != null)
            {
                resultSet.close();
            }
            if (statement != null)
            {
                statement.close();
            }
            if (connection != null)
            {
                connection.close();
            }
        }
        catch (Exception ex)
        {
            log.error(ex);
        }
    }
}
