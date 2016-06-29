package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.Designation;

import java.util.List;

public class DesignationChangeDTO extends BaseChangeDTO implements Comparable<DesignationChangeDTO>
{
    protected List<PropertyChangeDTO> properties;
    protected List<SubsetRelationshipChangeDTO> subsetRelationships;
    
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null)
        {
            Designation previousDesignation = (Designation)previous;
            Designation recentDesignation = (Designation)recent;
            if (!recentDesignation.getName().equals(previousDesignation.getName())
                    || recentDesignation.getVuid().longValue() != previousDesignation.getVuid().longValue()
                    || recentDesignation.getType().getId() != previousDesignation.getType().getId())
            {
                result = true;
            }
        }
        return result;
    }
    
    /**
	 * @return the isDesignationRemoved
	 */
	public boolean isDesignationRemoved()
	{
    	if (previous != null && recent == null)
    	{
    		// this designation has been removed (assigned to another concept)
    		return true;
    	}
    	
        return false;
	}

	public List<PropertyChangeDTO> getProperties()
    {
        return properties;
    }
    public void setProperties(List<PropertyChangeDTO> properties)
    {
        this.properties = properties;
    }

    public List<SubsetRelationshipChangeDTO> getSubsetRelationships()
    {
        return subsetRelationships;
    }

    public void setSubsetRelationships(List<SubsetRelationshipChangeDTO> subsetRelationships)
    {
        this.subsetRelationships = subsetRelationships;
    }

    public int compareTo(DesignationChangeDTO designationChangeDTO)
    {
        int result = 0;
        String value1 = null;
        String value2 = null;

        Designation thisDesignation = (Designation) this.getRecent();
        Designation designation = (Designation) designationChangeDTO.getRecent();

        if (thisDesignation != null)
        {
            value1 =  thisDesignation.getName();
        }
        else
        {
        	value1 = ((Designation) this.getPrevious()).getName();
        }
        
        if (designation != null)
        {
            value2 =  designation.getName();
        }
        else
        {
        	value2 = ((Designation) designationChangeDTO.getPrevious()).getName();
        }

        result = value1.compareTo(value2);

        return result;
    }
}
