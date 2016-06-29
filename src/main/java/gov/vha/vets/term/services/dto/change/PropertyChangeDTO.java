package gov.vha.vets.term.services.dto.change;

import gov.vha.vets.term.services.model.Property;

public class PropertyChangeDTO extends BaseChangeDTO implements Comparable<PropertyChangeDTO>
{
    @Override
    public boolean isChanged()
    {
        boolean result = false;
        // check to see if we have a previous version
        if (previous != null)
        {
            Property previousProperty = (Property)previous;
            Property recentProperty = (Property)recent;
            if (!recentProperty.getValue().equals(previousProperty.getValue()))
            {
                result = true;
            }
        }
        else
        {
            result = false;
        }
        return result;
    }

    public int compareTo(PropertyChangeDTO propertyChangeDTO)
    {
        int result = 0;

        Property thisProperty = (Property) this.getRecent();
        Property property = (Property) propertyChangeDTO.getRecent();
        
        String value1 =  thisProperty.getPropertyType().getName()+"-"+thisProperty.getValue();
        String value2 =  property.getPropertyType().getName()+"-"+property.getValue();
        result = value1.compareTo(value2);

        return result;
    }
}
