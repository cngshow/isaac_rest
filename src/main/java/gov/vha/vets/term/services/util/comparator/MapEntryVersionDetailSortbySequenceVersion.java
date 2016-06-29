package gov.vha.vets.term.services.util.comparator;


import gov.vha.vets.term.services.dto.MapEntryVersionDetailDTO;
import java.util.Comparator;

// This class was created for CCR 1262 to create the list of map entries in the correct order
// for displaying on the screen
public class MapEntryVersionDetailSortbySequenceVersion implements Comparator<MapEntryVersionDetailDTO>
{
    public int compare(MapEntryVersionDetailDTO entry1, MapEntryVersionDetailDTO entry2)
    {
    	if (entry1.getSequence() == entry2.getSequence())
    	{
    		// place active entry in the same sequence and version further down the list so show as more current
    		if (entry1.getVersionId() == entry2.getVersionId()) {
    			if (entry1.isActive() && entry2.isActive()) {
    				return 0;
    			} else if (entry1.isActive() && !entry2.isActive()) {
    				return -1;   // active so move behind
    			} else {  // the old choice left is 1 inactive and 2 active
    				return 1;  // inactive so move in front
    			}
    		} else {
    			// versions do not equal so put Higher version in front (displayed high to low)
    			return (int)(entry2.getVersionId()-entry1.getVersionId());
    		}
    	}
    	else
    	{
    		// put lower sequence in front
    		return (int)(entry1.getSequence()-entry2.getSequence());
    	}
    }
}
