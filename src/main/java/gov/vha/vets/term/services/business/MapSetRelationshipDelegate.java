/**
 * 
 */
package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.MapSetRelationshipDao;
import gov.vha.vets.term.services.model.MapEntry;
import gov.vha.vets.term.services.model.MapSetRelationship;
import gov.vha.vets.term.services.model.Version;

import java.util.Collection;

/**
 * @author BORG
 *
 */
public class MapSetRelationshipDelegate
{
    
    public static void create(Version version, long mapSetEntityId, long mapEntryEntityId, boolean active, int sequence, Long grouping)
    {
        // create the mapSet relation to 'connect' the mapSet to the MapEntry
    	MapSetRelationship mapSetRelationship = new MapSetRelationship(mapSetEntityId, mapEntryEntityId, version, active, sequence, grouping);
        MapSetRelationshipDao.save(mapSetRelationship);
    }

    public static int setAuthoringToVersion(Collection<Long> conceptEntityIdList, Version version)
    {
    	return MapSetRelationshipDao.setAuthoringToVersion(conceptEntityIdList, version);
    }

    public static MapSetRelationship get(long mapSetEntityId, long mapEntryEntityId)
    {
    	return MapSetRelationshipDao.get(mapSetEntityId, mapEntryEntityId);
    }

    public static void update(MapSetRelationship mapSetRelationship, boolean active, int sequence, Long grouping)
    {
    	MapSetRelationship mapSetRelationshipUpdate = new MapSetRelationship(mapSetRelationship.getSourceEntityId(), mapSetRelationship.getTargetEntityId(),
    			VersionDelegate.getAuthoring(), active, sequence, grouping);
    	mapSetRelationshipUpdate.setEntityId(mapSetRelationship.getEntityId());
        MapSetRelationshipDao.save(mapSetRelationshipUpdate);
    }

    public static MapSetRelationship get(Long mapSetRelationshipEntityId)
    {
        return MapSetRelationshipDao.get(mapSetRelationshipEntityId);
    }
    
}
