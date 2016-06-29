/*
 * Created on Oct 18, 2004
 */

package gov.vha.vets.term.services.model;

import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="PROPERTY_SEQ", allocationSize=1 )
@Table(name="PROPERTY")
public class Property extends BaseVersion implements Comparator<Property>
{
    protected String value;
    protected long conceptEntityId;
    protected PropertyType propertyType;

    public Property()
    {

    }

    public Property(String value, long conceptEntityId, PropertyType propertyType, long entityId, Version version, boolean active)
    {
        this.value = value;
        this.conceptEntityId = conceptEntityId;
        this.propertyType = propertyType;
        this.entityId = entityId;
        this.version = version;
        this.active = active;
    }

    /**
     * @hibernate.property
     * @return
     */
    public boolean getActive()
    {
        return active;

    }

    /**
     * @return Returns the id.
     */
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }

    /**
     * @return
     */
    @ManyToOne (fetch=FetchType.EAGER)    
    @JoinColumn(name="VERSION_ID", nullable=false) 
    @ForeignKey(name="FK_PROPERTY_VERSION")    
    public Version getVersion()
    {
        return version;
    }

    /**
     * @return
     */
    @Column (name="ENTITY_ID", nullable=true)
    public long getEntityId()
    {
        return entityId;
    }

    public void setEntityId(long entityId)
    {
        this.entityId = entityId;
    }

    /**
     * @return Returns the PropertyType.
     */
    @ManyToOne (fetch=FetchType.EAGER)    
    @JoinColumn(name="PROPERTYTYPE_ID", nullable=false) 
    @ForeignKey(name="FK_PROPERTY_TYPE")    
    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    @ManyToOne   
    @JoinColumn(name="CHANGEGROUP_ID", nullable=true) 
    @ForeignKey(name="FK_PROPERTY_CHANGEGROUP")    
    public ChangeGroup getChangeGroup()
    {
        return changeGroup;
    }

    public void setChangeGroup(ChangeGroup changeGroup)
    {
        this.changeGroup = changeGroup;
    }
    
    /**
     * @param conceptPropertyType
     *            The conceptPropertyType to set.
     */
    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    /**
     * @return Returns the value.
     */
    @Column (name="PROPERTY_VALUE", length=2000)
    public String getValue()
    {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return Returns the concept.
     */
    @Column (name="CONCEPTENTITY_ID", nullable=false)
    public long getConceptEntityId()
    {
        return conceptEntityId;
    }

    /**
     * @param concept
     *            The concept to set.
     */
    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }

    public String toString()
    {
        String propertyTypeName = "";
        if (this.getPropertyType() != null)
        {
            propertyTypeName = this.getPropertyType().getName();
        }
        return " Id:" + this.getId() + "  type:" + propertyTypeName + " Value:" + this.getValue() + "  Entity:" + this.getEntityId() + "  active:"
                + this.getActive();
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public boolean equals(Object obj)
    {
        // check for self-comparison
        if (this == obj)
        {
            return true;
        }

        // use instanceof instead of getClass here for two reasons
        // 1. if need be, it can match any supertype, and not just one class;
        // 2. it renders an explict check for "that == null" redundant, since
        // it does the check for null already - "null instanceof [type]" always
        // returns false. (See Effective Java by Joshua Bloch.)
        if (!(obj instanceof Property))
        {
            return false;
        }
        // cast to native object is now safe
        Property property = (Property) obj;

        boolean result = false;
        // now a proper field-by-field evaluation can be made
        if (this.id == property.getId())
        {
            result = true;
        }
        return result;
    }

    public int compare(Property o1, Property o2)
    {
        return (o1.propertyType+o1.getValue()).compareTo(o2.getPropertyType()+o2.getValue());
    }

}
