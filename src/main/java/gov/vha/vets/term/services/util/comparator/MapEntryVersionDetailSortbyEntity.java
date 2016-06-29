package gov.vha.vets.term.services.util.comparator;


import gov.vha.vets.term.services.dto.MapEntryVersionDetailDTO;
import java.util.Comparator;


public class MapEntryVersionDetailSortbyEntity implements Comparator<MapEntryVersionDetailDTO>
{
    public int compare(MapEntryVersionDetailDTO dto1, MapEntryVersionDetailDTO dto2)
    {
    	if (dto1.getEntityId().equals(dto2.getEntityId()))
    	{
    		return (int)-(dto1.getVersionId()-dto2.getVersionId());
    	}
    	else
    	{
    		return (int)(dto1.getEntityId().longValue()-dto2.getEntityId().longValue());
    	}
    }
}
