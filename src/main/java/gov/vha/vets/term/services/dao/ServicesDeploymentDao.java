package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.business.CodeSystemDelegate;
import gov.vha.vets.term.services.business.DesignationDelegate;
import gov.vha.vets.term.services.dto.ChecksumPropertyDTO;
import gov.vha.vets.term.services.dto.ChecksumRelationshipDTO;
import gov.vha.vets.term.services.dto.ConceptEntityDTO;
import gov.vha.vets.term.services.dto.DomainDTO;
import gov.vha.vets.term.services.dto.RegionDTO;
import gov.vha.vets.term.services.dto.RegionEntityDTO;
import gov.vha.vets.term.services.dto.delta.DiscoveryDeltaDTO;
import gov.vha.vets.term.services.dto.delta.DiscoveryMappingResultsDTO;
import gov.vha.vets.term.services.exception.STSException;
import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ServicesDeploymentDao extends BaseDao
{
    @SuppressWarnings("deprecation")
   public static Map<String, List<ConceptEntityDTO>> getReadyToTest() throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();
        Connection connection = session.connection();
        PreparedStatement statement = null;
        Map<String, List<ConceptEntityDTO>> results = new HashMap<String, List<ConceptEntityDTO>>();        

        /**
         * ( all concepts that have a designation not in a subset or all
         * concepts that do not have a designation) and all concepts not in a
         * subset
         */
        String query = 
            "SELECT DISTINCT (SELECT NAME FROM concept WHERE id = (select max(id) from concept where kind = 'S' AND entity_id = sr.source_entity_id)) as subsetName,  " +
            "      cs.concept_entity_id, con.kind  " +
            "FROM relationship dr " +
            "   INNER JOIN conceptstate cs ON dr.source_entity_id = cs.concept_entity_id    " +
            "     AND dr.kind = 'D' AND dr.id = (select max(id) from Relationship dr2 where kind = 'D' and dr.entity_id = dr2.entity_id)  " +
            "     AND cs.state_id = (SELECT ID FROM state WHERE type = 'Ready To Test')   " +
            "   INNER JOIN concept con ON cs.concept_entity_id = con.entity_id and con.codesystem_id = (select id from codesystem where name = 'VHAT')  " +
            "     AND con.id = (select max(id) from concept where entity_id = cs.concept_entity_id)  " +
            "   LEFT OUTER JOIN relationship sr ON dr.target_entity_id = sr.target_entity_id AND sr.kind = 'S' AND sr.active = 1 "; 

        try
        {
            
            statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            Set<Long> conceptEntityIds = new HashSet<Long>();
            while (resultSet.next())
            {
                String subsetName = resultSet.getString("subsetName");
                long conceptEntityId = resultSet.getLong("concept_entity_id");
                String kind = resultSet.getString("kind");
                
                if (kind.equals("C"))
                {
                    if (subsetName == null || subsetName.length() == 0)
                    {
                            subsetName = RegionDTO.NON_SUBSET;
                    }
                }
                else if (kind.equals("M"))
                {
                    subsetName = DomainDTO.MAP_SETS;
                }
                List<ConceptEntityDTO> conceptEntityDTOs = results.get(subsetName);
                if (conceptEntityDTOs == null)
                {
                    conceptEntityDTOs = new ArrayList<ConceptEntityDTO>();
                    results.put(subsetName, conceptEntityDTOs);
                }
                conceptEntityDTOs.add(new ConceptEntityDTO(null,conceptEntityId));
                conceptEntityIds.add(conceptEntityId);
            }
            if (results.size() > 0)
            {
                Map<Long, Designation> designationMap = DesignationDelegate.getConceptDescriptionsByEntityIds(CodeSystemDelegate.get(HibernateSessionFactory.VHAT_NAME), 
                        HibernateSessionFactory.AUTHORING_VERSION_ID, conceptEntityIds);
                for (Iterator iter = results.keySet().iterator(); iter.hasNext();)
                {
                    String key = (String)iter.next();
                    List<ConceptEntityDTO> list = results.get(key);
                    for (ConceptEntityDTO conceptEntityDTO : list)
                    {
                        Designation designation = designationMap.get(conceptEntityDTO.getEntityId());
                        if (designation != null)
                        {
                            conceptEntityDTO.setName(designation.getName());
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new STSException(e);
        }
        return results;
        
    }    
    /**
     * Get all relationships for the named region by version and/or deployment identifier
     * @param region
     * @param versionId
     * @param moduleDeploymentIds
     * @param relationshipTypes
     * @param inverse
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static Map<Long, Map<String, List<Long>>> getChecksumRelationships(String region, long versionId, List<Long> moduleDeploymentIds, List<String> relationshipTypes, boolean inverse) 
    {
        Session session = HibernateSessionFactory.currentSession();
        
        if (moduleDeploymentIds.size() < 1)
        {
            moduleDeploymentIds.add(new Long(0));
        }
        
        String relationshipSqlQuery = getRelationshipQuery(inverse);
        
        String header = "/* VersionId ["+versionId+"] regionName ["+region+"] relationshipTypes ["+relationshipTypes+"] moduleDeploymentIds ["+moduleDeploymentIds+"] */";
        SQLQuery sqlQuery = session.createSQLQuery(header+relationshipSqlQuery);
        sqlQuery.setLong("versionId", versionId);
        sqlQuery.setString("regionName", region);
        sqlQuery.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        sqlQuery.setParameterList("relationshipTypes", relationshipTypes);
        sqlQuery.setParameterList("moduleDeploymentIds", moduleDeploymentIds);
            
        List<Object[]> list = (List<Object[]>)sqlQuery.list();

        Map<Long, ChecksumRelationshipDTO> checksumRelationshipMap = new HashMap<Long, ChecksumRelationshipDTO>();
            
        // read results and put into map to remove duplicates(target changed) and inactives
        for(Object[] object : list)
        {
            Long conceptEntityId = ((BigDecimal)object[1]).longValue();
            String relationshipType = (String)object[2];
            Long designationVuid = ((BigDecimal)object[4]).longValue();
            Long relationshipEntityId = ((BigDecimal)object[5]).longValue();
            boolean remove = ((BigDecimal)object[6]).intValue() == 1 ? true : false;
            
            if (remove)
            {
                checksumRelationshipMap.remove(relationshipEntityId);
            }
            else
            {
                ChecksumRelationshipDTO checksumRelationshipDTO = new ChecksumRelationshipDTO(conceptEntityId, relationshipEntityId, relationshipType, designationVuid);
                checksumRelationshipMap.put(relationshipEntityId, checksumRelationshipDTO);
            }
        }

        // process the remaining entries and put them into the result
        Map<Long, Map<String, List<Long>>> map = new HashMap<Long, Map<String, List<Long>>>();
        for (Iterator<ChecksumRelationshipDTO> iter = checksumRelationshipMap.values().iterator(); iter.hasNext();)
        {
            ChecksumRelationshipDTO checksumRelationshipDTO = iter.next();
            Map<String, List<Long>> relmap = map.get(checksumRelationshipDTO.getConceptEntityId());
            if (relmap == null)
            {
                relmap = new HashMap<String, List<Long>>();
                map.put(checksumRelationshipDTO.getConceptEntityId(), relmap);
            }
            List<Long> vuidList = relmap.get(checksumRelationshipDTO.getRelationshipType());
            if (vuidList == null)
            {
                vuidList = new ArrayList<Long>();
                relmap.put(checksumRelationshipDTO.getRelationshipType(), vuidList);
            }
            vuidList.add(checksumRelationshipDTO.getVuid());
        }
        
        return map;
    }

    
    /**
     * get a list of RegionEntitiesDTO for a given region, version, and list of
     * concept entities
     * 
     * @param region
     * @param version
     * @param moduleDeploymentIds
     * @return
     * @throws STSException 
     * @throws SQLException 
     */
    @SuppressWarnings({ "unchecked" })
    public static List<RegionEntityDTO> getRegionEntities(String region, long versionId, List<Long> moduleDeploymentIds) throws STSException 
    {
        Session session = HibernateSessionFactory.currentSession();

        if (moduleDeploymentIds.size() < 1)
        {
            moduleDeploymentIds.add(new Long(0));
        }

        String sql = getDesignationsQuery(false);

        SQLQuery sqlQuery = session.createSQLQuery(sql);
        sqlQuery.setString("regionName", region);
        sqlQuery.setLong("versionId", versionId);
        sqlQuery.setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        sqlQuery.setParameterList("moduleDeploymentIds", moduleDeploymentIds);

        List<Object[]> list = (List<Object[]>)sqlQuery.list();
        
        HashMap<Long, RegionEntityDTO> entityMap = new HashMap<Long, RegionEntityDTO>();
        for (Object[] object : list)
        {
            long conceptEntityId = ((BigDecimal)object[0]).longValue();
            long designationEntityId = ((BigDecimal)object[1]).longValue();
            String name = (String)object[2];
            long vuid = ((BigDecimal)object[3]).longValue();
            int changeType = ((BigDecimal)object[5]).intValue(); 
            boolean remove = (changeType == 1) ? true : false;
            
            if (remove)
            {
            	entityMap.remove(vuid);
            }
            else
            {
                RegionEntityDTO regionEntity = new RegionEntityDTO();
                regionEntity.setConceptEntityId(conceptEntityId);
                regionEntity.setDesignationEntityId(designationEntityId);
                regionEntity.setDesignationName(name);
                regionEntity.setVuid(vuid);
                entityMap.put(vuid, regionEntity);
            }
        }

        
        List<RegionEntityDTO> regionEntities = new ArrayList<RegionEntityDTO>();
        regionEntities.addAll(entityMap.values());
        Collections.sort(regionEntities);

        return regionEntities;
    }
    

    /**
     * Return the regions (subsets) that are valid and associated with the list
     * of conceptEntityIds
     * 
     * @param validRegions
     * @param conceptEntityIds
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Collection<String> getRegionNames(List<String> validRegions, List<Long> conceptEntityIds)
    {
        Session session = HibernateSessionFactory.currentSession();

        String hql = "SELECT DISTINCT sub.name from Subset sub, SubsetRelationship sr, Designation des, "
                + "DesignationRelationship dr, CodedConcept con " + "   where sr.sourceEntityId = sub.entityId and sr.targetEntityId = des.entityId "
                + "     and dr.sourceEntityId = con.entityId and dr.targetEntityId = des.entityId "
                + "     and con.entityId in (:conceptEntityIds) and sub.name in (:validRegions)";

         Query query = session.createQuery(hql).setParameterList("validRegions", validRegions);
        
        List<Object> subsetNames = executeQuery(query, "conceptEntityIds", conceptEntityIds);
        
        Collection<String> regionNames = new HashSet<String>();
        for (Iterator iter = subsetNames.iterator(); iter.hasNext();)
        {
            Object object = iter.next();
            regionNames.add((String) object);
        }

        return regionNames;
    }
    
    public static void createDiscoveryProperties(String regionName, Long versionId, List<Long> deploymentIds, List<String> propertyTypes)
    {
        Session session = HibernateSessionFactory.currentSession();
        
        String mainQuery = getPropertyQuery();
        String finalQuery = "INSERT INTO DiscoveryResults (conceptEntityId, designationEntityId, active, name, subsetName, type, value, changeType, entityId) " +
        		"SELECT con_entity_id, des_entity_id, 1, property_type, :regionName as subset, 'P', property_value, changeType, prop_entity_id " +
        	    " FROM (" + mainQuery + ")"; 
		SQLQuery sqlQuery = session.createSQLQuery(finalQuery);
		sqlQuery.setString("regionName", regionName);
		sqlQuery.setLong("versionId", (versionId == null) ? 0 : versionId);
		sqlQuery.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		sqlQuery.setParameterList("deploymentIds", deploymentIds);
		if (propertyTypes.size() == 0)
		{
		    propertyTypes.add("");
		}
		sqlQuery.setParameterList("propertyTypes", propertyTypes);
		sqlQuery.executeUpdate();
    }
    
    private static String getPropertyQuery()
    {
        String query = /* concept version */
        	"select * from (" +
            "select con.entity_id as con_entity_id, 0 as des_entity_id, " +
            "(select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value," +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 0 as changeType " +
            " from relationship subrel, concept con, relationship desrel, property prop " +
            "  where subrel.target_entity_id = desrel.target_entity_id  " +
            "and desrel.kind = 'D' " +
            "and con.kind = 'C' and con.active = 1 " +
            "and subrel.kind = 'S' " +
            "and con.entity_id = prop.conceptentity_id and prop.active = 1 " +
            "and prop.id = (select max(prop2.id) from Property prop2 where prop2.version_id <= :versionId and prop.entity_id = prop2.entity_id) " + 
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and con.id = (select max(con2.id) from Concept con2 where con2.kind = 'C' and con2.version_Id <= :versionId and con2.entity_id = con.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.version_id <= :versionId and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and subrel.id in  " +
            " (select max(id) from relationship subrel2  " +
            " where subrel2.kind = 'S'  " +
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName)  " +
            " group by subrel2.entity_id) " +
            "UNION " + /* concept deployment - active properties */
            "select con.entity_id as con_entity_id, 0 as des_entity_id, " +
            "(select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value, " +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 2 as changeType " +
            " from relationship subrel, concept con, relationship desrel, property prop " +
            "  where subrel.target_entity_id = desrel.target_entity_id " +
            "and desrel.kind = 'D' " +
            "and con.kind = 'C' and con.active = 1 " +
            "and subrel.kind = 'S' " +
            "and con.entity_id = prop.conceptentity_id and prop.active = 1 and (prop.version_id = :authoringVersionId or prop.version_id <= :versionId) " +
            "and prop.id = (select max(prop2.id) from Property prop2 where prop2.entity_id = prop.entity_id) " +
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and con.id = (select max(con2.id) from Concept con2 where con2.kind = 'C' and con2.entity_id = con.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and subrel.id in  " +
            "(select max(subrel2.id) from relationship subrel2 " +
            " where subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' " +
            "        and source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:deploymentIds))) " +
            " and subrel2.kind = 'S'  " +
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName)  " +
            " group by subrel2.entity_id) " +
            "UNION  " + /* concept deployment - inactive properties */
            "select con.entity_id as con_entity_id, 0 as des_entity_id, " +
            " (select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value, " +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 1 as changeType " +
            " from relationship subrel, concept con, relationship desrel, property prop " +
            "  where subrel.target_entity_id = desrel.target_entity_id " +
            "and desrel.kind = 'D' " +
            "and con.kind = 'C' and con.active = 1 " +
            "and subrel.kind = 'S' " +
            "and con.entity_id = prop.conceptentity_id and prop.active = 0 and prop.version_id = 9999999999 " +
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and con.id = (select max(con2.id) from Concept con2 where con2.kind = 'C' and con2.entity_id = con.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and subrel.id in  " +
            "(select max(subrel2.id) from relationship subrel2 " +
            " where subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' " +
            "     and source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:deploymentIds))) " +  
            " and subrel2.kind = 'S'  " +
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName) " + 
            " group by subrel2.entity_id) " +
            "UNION " + /* designation version */
            "select desrel.source_entity_id as con_entity_id, des.entity_id as des_entity_id, " +
            "(select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value, " +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 0 as changeType " +
            " from relationship subrel, concept des, relationship desrel, property prop " +
            "  where subrel.target_entity_id = desrel.target_entity_id  " +
            "and desrel.kind = 'D' " +
            "and subrel.kind = 'S' " +
            "and des.kind = 'D' and des.active = 1 " +
            "and des.entity_id = prop.conceptentity_id and prop.active = 1 " +
            "and prop.id = (select max(prop2.id) from Property prop2 where prop2.version_id <= :versionId and prop.entity_id = prop2.entity_id) " + 
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and des.id = (select max(des2.id) from Concept des2 where des2.kind = 'D' and des2.version_Id <= :versionId and des2.entity_id = des.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.version_id <= :versionId and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and des.ENTITY_ID = subrel.TARGET_ENTITY_ID  " +
            "and subrel.id in  " +
            " (select max(id) from relationship subrel2  " +
            " where subrel2.kind = 'S'  " +
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName)  " +
            " group by subrel2.entity_id) " +
            "UNION " + /* designation deployment - active properties */
            "select desrel.source_entity_id as con_entity_id, des.entity_id as des_entity_id, " +
            "(select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value, " +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 2 as changeType " +
            " from relationship subrel, concept des, relationship desrel, property prop " +
            "  where subrel.target_entity_id = desrel.target_entity_id " +
            "and desrel.kind = 'D' " +
            "and subrel.kind = 'S' " +
            "and des.kind = 'D' and des.active = 1 " +
            "and des.entity_id = prop.conceptentity_id and prop.active = 1 and (prop.version_id = :authoringVersionId or prop.version_id <= :versionId) " +
            "and prop.id = (select max(prop2.id) from Property prop2 where prop2.entity_id = prop.entity_id) " +
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and des.id = (select max(des2.id) from Concept des2 where des2.kind = 'D' and des2.entity_id = des.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and des.ENTITY_ID = subrel.TARGET_ENTITY_ID  " +
            "and subrel.id in  " +
            "(select max(subrel2.id) from relationship subrel2 " +
            " where  " +
            " subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' and " +
            "    source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:deploymentIds)))   " +
            " and subrel2.kind = 'S'  " +
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName)  " +
            " group by subrel2.entity_id) " + 
            "UNION " + /* designation deployment - inactive properties */
            "select desrel.source_entity_id as con_entity_id, des.entity_id as des_entity_id, " +
            "(select name from type where kind = 'P' and id = prop.propertytype_id) as property_type, prop.property_value, " +
            "  prop.entity_id as prop_entity_id, prop.id as prop_id, 1 as changeType " +
            "from relationship subrel, concept des, relationship desrel, property prop " +
            "where subrel.target_entity_id = desrel.target_entity_id " +
            "and desrel.kind = 'D' " +
            "and subrel.kind = 'S' " +
            "and des.kind = 'D' and des.active = 1 " +
            "and des.entity_id = prop.conceptentity_id and prop.active = 0 and prop.version_id = 9999999999 " +
            "and prop.propertyType_id in (select id from type where kind = 'P' and name in (:propertyTypes)) " +
            "and des.id = (select max(des2.id) from Concept des2 where des2.kind = 'D' and des2.entity_id = des.entity_id) " +
            "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.kind = 'D' and desrel2.entity_id = desrel.entity_id) " +
            "and desrel.TARGET_ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and des.ENTITY_ID = subrel.TARGET_ENTITY_ID " +
            "and subrel.id in " +
            "(select max(subrel2.id) from relationship subrel2 " +
            " where " +
            " subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' and " +
            "    source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:deploymentIds))) " +
            " and subrel2.kind = 'S' " + 
            " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName) " + 
            " group by subrel2.entity_id) " +
            ") order by prop_entity_id, prop_id";

        return query;
    }

    public static void createDiscoveryDesignations(String regionName, Long versionId, List<Long> moduleDeploymentIds) throws STSException 
    {
        Session session = HibernateSessionFactory.currentSession();
        
        if (moduleDeploymentIds.size() < 1)
        {
            moduleDeploymentIds.add(new Long(0));
        }
        
        String query = getDesignationsQuery(true);
        String sql = "INSERT INTO DiscoveryResults (conceptEntityId, designationEntityId, active, name, subsetName, type, value, vuid, changeType, entityId) " +
                     "SELECT entity_id, designationEntityId, active, 'Term', :regionName, 'T', value, vuid, changeType, 0 as entityId " +
                     "FROM " +
                     "(" +
                        query +
                     ")";
        
        SQLQuery sqlQuery = session.createSQLQuery(sql);
        sqlQuery.setString("regionName", regionName);
        sqlQuery.setLong("versionId", (versionId==null) ? 0 : versionId);
        sqlQuery.setLong("authoringId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        sqlQuery.setParameterList("moduleDeploymentIds", moduleDeploymentIds);
        
        sqlQuery.executeUpdate();
    }
    private static String getDesignationsQuery(boolean includeInactives)
    {
        String sql = 
            "/* BASELINE - Get all the entries for a given version */ "
            + "select con.entity_id, des.entity_id as designationEntityId, des.name as value, des.vuid, desrel.id, 0 as changeType"
            + ((includeInactives) ? ", case when des.active=1 and subrel.active=1 then 1 else 0 end as active " : " ")
            + "from relationship subrel, concept con, concept des, relationship desrel where  "
            + "subrel.target_entity_id = desrel.target_entity_id "
            + "and desrel.kind = 'D' "
            + "and con.kind = 'C' "
            + "and des.kind = 'D' "
            + "and con.id = (select max(con2.id) from Concept con2 where con2.version_id <= :versionId and con2.kind = 'C' and con2.entity_id = con.entity_id) "
            + "and des.id = (select max(des2.id) from Concept des2 where des2.version_id <= :versionId and des2.kind = 'D' and des2.entity_id = des.entity_id) "
            + "and desrel.id = (select max(desrel2.id) from relationship desrel2 where desrel2.version_id <= :versionId and desrel2.kind = 'D' and desrel2.entity_id = desrel.entity_id) "
            + "and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  "
            + "and des.ENTITY_ID = subrel.TARGET_ENTITY_ID  "
            + ((includeInactives) ? "" : "and subrel.active = 1 and des.active = 1 ")
            + "and  "
            + "subrel.id in  "
            + "(select max(id) from relationship subrel2  "
            + " where subrel2.version_Id <= :versionId   "
            + " and subrel2.kind = 'S'  "
            + " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName )  "
            + " and subrel2.entity_id = subrel.entity_id) "
            + " UNION  /* Add all the entries that were activated or created */"
            + " select con.entity_id, des.entity_id as designationEntityId, des.name, des.vuid, desrel.id, 2 as changeType "
            + ((includeInactives) ? ", 1 as active" : "")
            + " from relationship subrel, concept con, concept des, relationship desrel where  "
            + " subrel.target_entity_id = desrel.target_entity_id "
            + " and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  "
            + " and des.ENTITY_ID = subrel.TARGET_ENTITY_ID  "
            + " and des.id = ( select max(des2.id) from concept des2 where des2.entity_id = des.entity_id) "
            + " and desrel.id =  ( select max(desrel2.id) from relationship desrel2 where kind ='D' and desrel2.entity_id = desrel.entity_id)"
            + " and desrel.kind = 'D' "
            + " and con.kind = 'C' "
            + " and des.kind = 'D' "
            + " and des.active = 1 "
            + " and subrel.active = 1 "
            + " and  "
            + " subrel.id in  "
            + " (select max(subrel2.id) from relationship subrel2 "
            + " where  "
            + " subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' and source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:moduleDeploymentIds))) "
            + " and subrel2.kind = 'S'  "
            + " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName )  "
            + " and subrel2.entity_id = subrel.entity_id)"
            + " UNION /* remove all the entries that were inactivated */"
            + " select con.entity_id, des.entity_id as designationEntityId, des.name, des.vuid, desrel.id, 1 as changeType "
            + ((includeInactives) ? ", 0 as active" : "")
            + " from relationship subrel, concept con, concept des, relationship desrel where  "
            + " subrel.target_entity_id = desrel.target_entity_id "
            + " and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID  "
            + " and des.ENTITY_ID = subrel.TARGET_ENTITY_ID  "
            + " and des.id = ( select max(des2.id) from concept des2 where des2.entity_id = des.entity_id) "
            + " and desrel.id =  ( select max(desrel2.id) from relationship desrel2 where kind ='D' and desrel2.entity_id = desrel.entity_id)"
            + " and desrel.kind = 'D' "
            + " and con.kind = 'C' "
            + " and des.kind = 'D' "
            + " AND ((des.version_id = :authoringId AND des.active = 0) or (subrel.active = 0 and subrel.version_id = :authoringId))"
            + " and  "
            + " subrel.id in  "
            + " (select max(subrel2.id) from relationship subrel2 "
            + " where  "
            + " subrel2.target_ENTITY_ID IN (select target_entity_id from relationship where kind = 'D' and source_entity_id in (select entityid from deploymentconcept where moduledeploymentid in (:moduleDeploymentIds))) "
            + " and subrel2.kind = 'S'  "
            + " and subrel2.source_entity_id = (select entity_id from concept where kind = 'S' and name = :regionName )  "
            + " and subrel2.entity_id = subrel.entity_id)"
            + " order by vuid, id";
        return sql;
    }
    
    /**
     * Populate the DiscoverResults temporary table using a wrapped checksum relationship query.
     * @param region
     * @param versionId
     * @param moduleDeploymentIds
     * @param relationshipTypes
     * @param inverse
     * @param isActive
     */
    public static void createDiscoveryRelationships(String regionName, Long versionId, List<Long> moduleDeploymentIds, List<String> relationshipTypes, boolean inverse)
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	if (moduleDeploymentIds.size() < 1)
        {
            moduleDeploymentIds.add(new Long(0));
        }
    	
    	String relationshipQuery = getRelationshipQuery(inverse);
    	String sql = "INSERT INTO DiscoveryResults (CONCEPTENTITYID, NAME, SUBSETNAME, TYPE, VALUE, VUID, changeType, entityId) " +
    				 "SELECT conceptEntityId, relationshipType, :regionName, 'R', desname, vuid, changeType, entity_id " +
    				 "FROM " +
    				 "(" +
    				 	relationshipQuery +
    				 ")";
    	
    	SQLQuery sqlQuery = session.createSQLQuery(sql);
    	sqlQuery.setString("regionName", regionName);
    	sqlQuery.setLong("versionId", (versionId == null) ? 0 : versionId);
    	sqlQuery.setParameterList("moduleDeploymentIds", moduleDeploymentIds);
    	sqlQuery.setParameterList("relationshipTypes", relationshipTypes);
        sqlQuery.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
        
    	sqlQuery.executeUpdate();
    }
    
    /**
     * Run the query to get differences between DiscoveryResults temporary table and the SiteData table.
     * @param siteId
     * @param region
     * @param versionId
     * @param moduleDeploymentIds
     * @param fieldTypeNames
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<DiscoveryDeltaDTO> getDiscoveryDeltas(Long siteId, String regionName,
    		Long versionId, List<Long> moduleDeploymentIds, List<String> fieldTypeNames,
    		String type, List<String> relationshipTypes, boolean isActiveComparison)
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String vtsSQL = 
    		"NAME, VALUE, VUID " +
    		"   FROM DiscoveryResults " +
    		"   WHERE SUBSETNAME = :subsetName" +
    		"	  AND TYPE = :type " +
    		"     and active = :isActive " +
    		"	  AND name IN (:fieldTypeNames)";
    	
        String vistaSQL =
			"NAME, VALUE, VUID " +
			"	FROM SITEDATA " +
			"   WHERE subsetName = :subsetName " +
			"	  AND siteId = :siteId " +
			"     AND type = :type " +
            "     and active = :isActive " +
			"     AND name IN (:fieldTypeNames) ";
        
        String query = getCompleteDiscoveryQuery(vtsSQL, vistaSQL);
        
        SQLQuery sqlQuery = session.createSQLQuery(query);
        sqlQuery.setString("subsetName", regionName);
        sqlQuery.setLong("siteId", siteId);
        if (relationshipTypes.size() == 0)
        {
            relationshipTypes.add("");
        }
        sqlQuery.setParameterList("fieldTypeNames", fieldTypeNames);
        sqlQuery.setString("type", type);
        sqlQuery.setBoolean("isActive", isActiveComparison);
        
        List<Object[]> resultSet = (List<Object[]>)sqlQuery.list();
        
        List<DiscoveryDeltaDTO> results = new ArrayList<DiscoveryDeltaDTO>();
        
        for(Object[] object : resultSet)
        {
        	String system = (String)object[0];
        	String name = (String)object[1];
        	String value = (String)object[2];
        	Long vuid = ((BigDecimal)object[3]).longValue();
        	results.add(new DiscoveryDeltaDTO(system, value, vuid, name));
        }
        
        return results;
    }
    
    private static String getRelationshipQuery(boolean inverse)
    {
        String select = 
            "select des.vuid as vuid,  "
          + "        con.entity_id as conceptentityid,  "
          + "        (select name from type where id = cr.type_id) as relationshipType,  "
          + "        targetDes.name as desName,  "
          + "        targetDes.vuid as desVuid, cr.entity_id, ";
          
        String from = 
            "    from concept subset, relationship subrel, concept con, concept des, relationship desrel,  "
          + "         concept targetCon, relationship cr, relationship targetDesRel, concept targetDes  "
          + "    where 1=1   ";

        String kinds = 
          "    /* kinds  */"
        + "    and desrel.kind = 'D'  "
        + "    and con.kind = 'C'  "
        + "    and des.kind = 'D'  "
        + "    and targetCon.KIND = 'C' "
        + "    and cr.kind = 'C' "
        + "    and targetDesRel.kind = 'D' "
        + "    and targetDes.kind = 'D' "
        + "    and subset.kind = 'S' "
        + "    and subrel.kind = 'S' ";
        
        String commonFilters = 
            "    /* filters */"
          + "    and "+((inverse) ? "" : "target")+"Des.TYPE_ID = (select id from type where kind = 'D' and name = 'Preferred Name') "
          + "    and cr.type_id in (select id from type where name in (:relationshipTypes))  "
          + "    and subset.name = :regionName ";
          
        String joinLogic = 
          "    /* join logic */"
        + "    and subrel.target_entity_id = desrel.target_entity_id "
        + "    and cr.source_entity_id = "+((inverse) ? "targetCon" : "con")+".entity_id "
        + "    and cr.target_entity_id = "+((inverse) ? "con" : "targetCon")+".entity_id "
        + "    and targetDesRel.source_entity_id = targetCon.entity_id "
        + "    and targetDes.entity_id = targetDesRel.TARGET_ENTITY_ID "
        + "    and desrel.SOURCE_ENTITY_ID = con.ENTITY_ID   "
        + "    and des.ENTITY_ID = subrel.TARGET_ENTITY_ID "
        + "    and subset.entity_id = subrel.source_entity_id "
        + "    and subrel.target_entity_id = des.entity_id ";

        String versionFilters =
            "    /* Version Stuff */"
          + "    and subrel.id = (select max(id) from relationship subrel2 where subrel2.entity_id = subrel.entity_id and subrel2.kind = 'S' and subrel2.version_id <= :ID)  "
          + "    and subset.id = (select max(id) from concept subset2 where subset2.entity_id = subset.entity_id and subset2.kind = 'S' and subset2.version_id <= :ID) "
          + "    and con.id = (select max(id) from concept con2 where con2.entity_id = con.entity_id and con2.kind = 'C' and con2.version_id <= :ID) "
          + "    and des.id = (select max(id) from concept des2 where des2.entity_id = des.entity_id and des2.kind = 'D' and des2.version_id <= :ID)  "
          + "    and desRel.id = (select max(id) from relationship desRel2 where desRel2.entity_id = desRel.entity_id and desRel2.kind = 'D' and desRel2.version_id <= :ID)  "
          + "    and targetCon.id = (select max(id) from concept targetCon2 where targetCon2.entity_id = targetCon.entity_id and targetCon2.kind = 'C' and targetCon2.version_id <= :ID) "
          + "    and cr.id = (select max(id) from relationship cr2 where cr2.entity_id = cr.entity_id and cr2.kind = 'C' and cr2.version_id <= :ID) "
          + "    and targetDesRel.id = (select max(id) from relationship targetDesRel2 where targetDesRel2.entity_id = targetDesRel.entity_id and targetDesRel2.kind = 'D' and targetDesRel2.version_id <= :ID)  "
          + "    and targetDes.id = (select max(id) from concept targetDes2 where targetDes2.entity_id = targetDes.entity_id and targetDes2.kind = 'D' and targetDes2.version_id <= :ID) ";

        String sort = " order by entity_id, id";   

        String mainQuery =
          " /* Relationship inverse "+inverse+" BASELINE - Get all the entries for a given version */ "
        + select
        + "        0 as changeType, cr.id "
        + from
        + kinds
        + commonFilters
        + "    and ("+((inverse) ? "target" : "")+"des.active = 1 and cr.active = 1 and subrel.active = 1)   "
        + joinLogic
        + versionFilters.replace(":ID", ":versionId")
        + "union  /* Add all the entries that were activated or created */ "
        + select
        + "        2 as changeType, cr.id "
        + from
        + kinds
        + commonFilters
        + "    and ("+((inverse)? "target" : "")+"des.active = 1 and cr.active = 1 and subrel.active = 1)   "
        + "    and "+((inverse)? "targetCon" : "con")+".entity_id in (select entityid from deploymentconcept where moduledeploymentid in ( :moduleDeploymentIds )) "
        + joinLogic
        + versionFilters.replace(":ID", ":authoringVersionId")
        + "union  /* remove all the entries that were inactivated */ "
        + select
        + "        1 as changeType, cr.id "
        + from
        + kinds
        + commonFilters
        + "    and ("+((inverse)? "target" : "")+"des.active = 0 or cr.active = 0 or subrel.active = 0)   "
        + "    and "+((inverse)? "targetCon" : "con")+".entity_id in (select entityid from deploymentconcept where moduledeploymentid in ( :moduleDeploymentIds )) "
        + joinLogic
        + versionFilters.replace(":ID", ":authoringVersionId")
        + sort;
        
        return mainQuery;
        
    }
    
    public static void createDiscoveryMappings(Long mapSetEntityId, Long versionId, String baseMappingQuery)
    {
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String sql = "INSERT INTO DiscoveryResults(ACTIVE, NAME, SUBSETNAME, TYPE, VALUE, VUID) " +
    				 "SELECT ACTIVE, NAME, SUBSETNAME, TYPE, VALUE, VUID " +
    				 "FROM " +
    				 "(" +
    				 "	SELECT 'SourceCode' as NAME , sourceCode as VALUE, vuid as VUID, name as SUBSETNAME, 'M' as TYPE, active " +
    				 "	  FROM " +
    				 "        (" +
    				 			baseMappingQuery +
    				 "        )" +
    				 "  UNION " +
    				 "  SELECT 'TargetCode' as NAME, targetCode as VALUE, vuid as VUID, name as SUBSETNAME, 'M' as TYPE, active " +
    				 "	   FROM " +
    				 "         (" +
			 				     baseMappingQuery +
    				 "		   )" +
    				 "  UNION " +
    				 "  SELECT 'Order' as NAME, TO_CHAR(sequence) as VALUE, vuid as VUID, name as SUBSETNAME, 'M' as TYPE, active " +
    				 "	   FROM " +
    				 "         (" +
			 				     baseMappingQuery +
    				 "		   )" +
    				 ")";
    	
    	Query query = session.createSQLQuery(sql);
    	query.setLong("mapSetEntityId", mapSetEntityId);
    	query.setLong("versionId", versionId);
    	query.executeUpdate();
    }
    
    @SuppressWarnings("unchecked")
    public static List<DiscoveryMappingResultsDTO> getDiscoveryMappingDeltas(Long siteId, String regionName, boolean isActiveComparison)
    {
    	List<DiscoveryMappingResultsDTO> mappingDeltas = new ArrayList<DiscoveryMappingResultsDTO>();
    	Session session = HibernateSessionFactory.currentSession();
    	
    	String vtsSQL = "  vuid, " +
				"         (SELECT VALUE " +
				"            FROM DiscoveryResults " +
				"           WHERE vuid = tbl.vuid AND name = 'SourceCode' ) " +
				"              AS SourceCode, " +
				"         (SELECT VALUE " +
				"            FROM DiscoveryResults " +
				"           WHERE vuid = tbl.vuid AND name = 'TargetCode' ) " +
				"              AS TargetCode, " +
				"         (SELECT VALUE " +
				"            FROM DiscoveryResults " +
				"           WHERE vuid = tbl.vuid AND name = 'Order' ) " +
				"              AS sequence " +
				"   FROM DiscoveryResults tbl " +
				"   WHERE subsetname = :regionName " +
				"     AND TYPE = 'M' " +
				"     AND active = :isActive " +
				"GROUP BY vuid ";
    			
    	String vistaSQL = "  vuid, " +
				"         (SELECT VALUE " +
				"            FROM sitedata " +
				"           WHERE vuid = tbl.vuid AND name = 'SourceCode' AND siteid = :siteId) " +
				"              AS SourceCode, " +
				"         (SELECT VALUE " +
				"            FROM sitedata " +
				"           WHERE vuid = tbl.vuid AND name = 'TargetCode' AND siteid = :siteId) " +
				"              AS TargetCode, " +
				"         (SELECT VALUE " +
				"            FROM sitedata " +
				"           WHERE vuid = tbl.vuid AND name = 'Order' AND siteid = :siteId) " +
				"              AS sequence " +
				"   FROM SiteData tbl " +
				"   WHERE siteid = :siteId and subsetname = :regionName " +
				"     AND TYPE = 'M' " +
				"     AND active = :isActive " +
				"GROUP BY vuid ";
    	
    	String query = getCompleteDiscoveryQuery(vtsSQL, vistaSQL);
    	
    	SQLQuery sqlQuery = session.createSQLQuery(query);
    	sqlQuery.setLong("siteId", siteId);
    	sqlQuery.setString("regionName", regionName);
    	sqlQuery.setBoolean("isActive", isActiveComparison);
    	
    	List<Object[]> resultSet = (List<Object[]>)sqlQuery.list();
    	
    	for(Object[] object :resultSet)
    	{
    		String system = (String)object[0];
    		Long vuid = ((BigDecimal)object[1]).longValue();
    		String sourceCode = (String)object[2];
    		String targetCode = (String)object[3];
    		String sequence = (String)object[4];
    		mappingDeltas.add(new DiscoveryMappingResultsDTO(system, vuid, sourceCode, targetCode, sequence));
    	}
    	
    	return mappingDeltas;
    }
    
    @SuppressWarnings("unchecked")
	public static Map<String, Map<String, List<String>>> getChecksumProperties(String regionName, long versionId, List<Long> deploymentIds, List<String> propertyTypes) throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();
        Map<String, Map<String, List<String>>> resultMap = new HashMap<String, Map<String, List<String>>>();        

        String query = getPropertyQuery();
		SQLQuery sqlQuery = session.createSQLQuery(query);
		sqlQuery.setString("regionName", regionName);
		sqlQuery.setLong("versionId", versionId);
		sqlQuery.setLong("authoringVersionId", HibernateSessionFactory.AUTHORING_VERSION_ID);
		sqlQuery.setParameterList("deploymentIds", deploymentIds);
		sqlQuery.setParameterList("propertyTypes", propertyTypes);

		List<Object[]> list = (List<Object[]>)sqlQuery.list();
        
        HashMap<Long, ChecksumPropertyDTO> checksumPropertyMap = new HashMap<Long, ChecksumPropertyDTO>();  
        for(Object[] object : list)
        {
            long conceptEntityId = ((BigDecimal)object[0]).longValue();
            long designationEntityId = ((BigDecimal)object[1]).longValue();
            String propertyType = (String)object[2];
            String propertyValue = (String)object[3];
            long propEntityId = ((BigDecimal)object[4]).longValue();
            int changeType = ((BigDecimal)object[6]).intValue();
            boolean remove = (changeType == 1) ? true : false;
            
            if (remove)
            {
                checksumPropertyMap.remove(propEntityId);
            }
            else
            {
                ChecksumPropertyDTO checksumPropertyDTO = new ChecksumPropertyDTO();
                checksumPropertyDTO.setConceptEntityId(conceptEntityId);
                checksumPropertyDTO.setDesignationEntityId(designationEntityId);
                checksumPropertyDTO.setPropertyType(propertyType);
                checksumPropertyDTO.setPropertyValue(propertyValue);
                checksumPropertyMap.put(propEntityId, checksumPropertyDTO);
            }
        }
        
        for (Iterator<ChecksumPropertyDTO> iterator = checksumPropertyMap.values().iterator(); iterator.hasNext();)
        {
			ChecksumPropertyDTO checksumPropertyDTO = iterator.next();

            String key = checksumPropertyDTO.getConceptEntityId() + "-" + checksumPropertyDTO.getDesignationEntityId();
            
            Map<String, List<String>> propertyMap = resultMap.get(key);
            if (propertyMap == null)
            {
                List<String> propertyValues = new ArrayList<String>();
                propertyValues.add(checksumPropertyDTO.getPropertyValue());
                propertyMap = new HashMap<String, List<String>>();
                propertyMap.put(checksumPropertyDTO.getPropertyType(), propertyValues);
                resultMap.put(key, propertyMap);
            }
            else
            {
                List<String> propertyValues = propertyMap.get(checksumPropertyDTO.getPropertyType());
                if (propertyValues == null)
                {
                    propertyValues = new ArrayList<String>();
                    propertyValues.add(checksumPropertyDTO.getPropertyValue());
                    propertyMap.put(checksumPropertyDTO.getPropertyType(), propertyValues);
                }
                else
                {
                    propertyValues.add(checksumPropertyDTO.getPropertyValue());
                }
            }
        }

        return resultMap;
    }
    
    public static void cleanOutDiscoveryResults()
    {
        Session session = HibernateSessionFactory.currentSession();
        session.createSQLQuery("delete from discoveryresults").executeUpdate();
    }
    public static void postProcessDiscoveryResults()
    {
        Session session = HibernateSessionFactory.currentSession();
    	
        // populate the vuid column of the each property from the vuid in the terms (concepts)
		session.createSQLQuery(
				"update discoveryResults dr set dr.vuid = (select distinct vuid from discoveryResults dr2 " +
				"           where dr2.conceptEntityId = dr.conceptEntityId and dr2.designationEntityId = dr.designationEntityId and dr2.type = 'T') " +
				"  where dr.type = 'P'").executeUpdate();
		
		// Set all the designation level properties  with the vuid
		session.createSQLQuery(
                "update discoveryResults dr set dr.vuid = (select distinct vuid from discoveryResults dr2 " +
                "           where dr2.conceptEntityId = dr.conceptEntityId " +
                "                 and dr2.type = 'T' and dr.designationEntityId = dr2.designationEntityId) " +
                "  where dr.type = 'P' and dr.designationentityid = 0").executeUpdate();
		
		// Create concept level properties for each designation that exists on the subset - this will create duplicate records except for the designation entity Id but the next query will remove them
		session.createSQLQuery(
		        "INSERT INTO DiscoveryResults (conceptEntityId, designationEntityId, active, entityId, name, subsetName, type, value, vuid, changeType) "
		        + "SELECT dr.conceptentityid, dr2.designationEntityId, dr2.active, dr.entityId, dr.name, dr.subsetname, dr.type, dr.value, dr2.vuid, dr.changetype FROM discoveryResults dr, discoveryResults dr2 WHERE dr.type = 'P'  "
		        + "AND dr.designationentityid = 0 AND dr.conceptEntityId = dr2.conceptEntityId  "
		        + "AND dr2.designationEntityId <> 0 "
		        + "and dr2.type = 'T' ").executeUpdate();
		
		// remove any entries that do not have a vuid
        session.createSQLQuery("delete from discoveryResults where vuid is null").executeUpdate();
        
        // delete the 'remove' columns for duplicate rows with the same vuid
        session.createSQLQuery(
                "delete from discoveryResults dr where dr.changeType = 0 and (dr.type = 'R' or dr.type = 'P') and (select count(*) from discoveryResults dr2 where " +
                "     dr.type = dr2.type and dr2.entityId = dr.entityId and dr.vuid = dr2.vuid) > 1 ").executeUpdate();
        
        // remove baseline entry if there was a change that has been included
        session.createSQLQuery(
                "delete from discoveryResults dr where dr.changeType = 0 and dr.type = 'T' and (select count(*) from discoveryResults dr2 where " +
                "     dr.type = dr2.type and dr2.designationEntityId = dr.designationEntityId) > 1 ").executeUpdate();
        
        // delete any row that is a relationship or property because they are logically deleteted and do not show with the inactive designation
        session.createSQLQuery(
                "delete from discoveryResults dr where dr.changeType = 1 and (type = 'R' or type = 'P')").executeUpdate();
        
		// set the active flag for each type based on whether a 'remove' is present or not
		session.createSQLQuery(
				"update discoveryResults dr set dr.active = (select active from discoveryResults dr2 " +
				"           where dr2.type = 'T' and dr2.vuid = dr.vuid) " +
				"  where dr.conceptentityid = (select conceptentityid from discoveryResults dr3 " +
				"                    where dr3.type = 'T' and dr3.vuid = dr.vuid)").executeUpdate();
    }
    
    /**
     * Concatenate the queries into one query that includes a UNION and MINUSes.
     * @param vtsQuery
     * @param vistaQuery
     * @return String
     */
    private static String getCompleteDiscoveryQuery(String vtsQuery, String vistaQuery)
    {
    	return  "(SELECT 'VTS' AS SYSTEM, " + vtsQuery + " MINUS " + "SELECT DISTINCT 'VTS' AS SYSTEM, " + vistaQuery +") " +
        		"UNION (SELECT DISTINCT 'VISTA' AS SYSTEM, " + vistaQuery + " MINUS " + "SELECT 'VISTA' AS SYSTEM, " + vtsQuery +")";
    }
    
	public static void syncConceptNameTextIndex()
	{
        Session session = HibernateSessionFactory.currentSession();
        session.createSQLQuery("{call ctxsys.ctx_ddl.sync_index('concept_name_t_idx', '2M')}").executeUpdate();
	}
    
    public static final void main(String[] args) throws STSException
    {
        Session session = HibernateSessionFactory.currentSession();
        Transaction tx = session.beginTransaction();
        
        List<String> relationshipTypes = new ArrayList<String>();
//        relationshipTypes.add("has_VistA_category");
        relationshipTypes.add("has_qualifier");
        
        List<Long> moduleDeploymentIds = new ArrayList<Long>();
        moduleDeploymentIds.add(new Long(82));
//    	createDiscoveryRelationships("Vital Qualifiers", 22L, moduleDeploymentIds, relationshipTypes, true);
    	
    	tx.commit();
    	
//    	List<String> propertyTypes = new ArrayList<String>();
//    	propertyTypes.add("Search_Term");
//    	propertyTypes.add("Allergy_Type");
//    	
//    	List<Long> deploymentIds = new ArrayList<Long>();
//    	deploymentIds.add(54L);
//    	deploymentIds.add(53L);
//    	
//    	Map<String, Map<String, List<String>>> resultMap = ServicesDeploymentDao.getChecksumProperties("Reactants", 19, deploymentIds, propertyTypes);
//    	
//    	int count = 1;
//    	Set<String> resultMapSet = resultMap.keySet();
//    	for (Iterator<String> iterator = resultMapSet.iterator(); iterator.hasNext();)
//    	{
//			String resultMapKey = iterator.next();
//			Map<String, List<String>> typeMap = resultMap.get(resultMapKey);
//			Set<String> typeMapSet = typeMap.keySet();
//			for (Iterator<String> iterator2 = typeMapSet.iterator(); iterator2.hasNext();)
//			{
//				String typeMapKey = iterator2.next();
//				List<String> values = typeMap.get(typeMapKey);
//				for (Iterator<String> iterator3 = values.iterator(); iterator3.hasNext();)
//				{
//					String value = iterator3.next();
//        			System.out.println("Count: "+(count++)+", key: " + resultMapKey + ", type: " + typeMapKey + ", value: "+value);
//				}
//			} 
//		}
    }
}
