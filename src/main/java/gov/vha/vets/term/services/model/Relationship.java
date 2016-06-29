/*
 * Created on Oct 18, 2004
 */

package gov.vha.vets.term.services.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
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
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="RELATIONSHIP_SEQ", allocationSize=10 )
@Table(name="RELATIONSHIP")
@DiscriminatorColumn(name="KIND",discriminatorType=DiscriminatorType.STRING, length=1)
public abstract class Relationship extends BaseVersion
{
	private RelationshipType relationshipType;
	protected long sourceEntityId;
	protected long targetEntityId;

	public Relationship()
	{
		
	}
	
	public Relationship(long sourceEntityId, long targetEntityId, Version version, boolean active)
	{
		this.sourceEntityId = sourceEntityId;
		this.targetEntityId = targetEntityId;
		this.version = version;
		this.active = active;
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
    @Column (name="ENTITY_ID", nullable=false)
	public long getEntityId()
	{
		return entityId;
	}

	/**
	 * make this visible
	 */
	public void setEntityId(long entityId)
	{
		this.entityId = entityId;
	}
	/**
	 * @hibernate.property
	 * @return
	 */
    @Column (name="ACTIVE", nullable=false)
	public boolean getActive()
	{
		return active;

	}

	/**
	 * @return
	 */
    @ManyToOne (fetch=FetchType.EAGER)    
    @JoinColumn(name="VERSION_ID", nullable=false) 
    @ForeignKey(name="FK_RELATIONSHIP_VERSION")    
	public Version getVersion()
	{
		return version;
	}

	/**
	 * @return Returns the relationshipType.
	 */
    @ManyToOne (fetch=FetchType.EAGER)    
    @JoinColumn(name="TYPE_ID", nullable=true) 
    @ForeignKey(name="FK_RELATIONSHIP_TYPE")    
	public RelationshipType getRelationshipType()
	{
		return relationshipType;
	}

	/**
	 * @param relationshipType
	 *            The relationshipType to set.
	 */
	public void setRelationshipType(RelationshipType relationshipType)
	{
		this.relationshipType = relationshipType;
	}

    @ManyToOne   
    @JoinColumn(name="CHANGEGROUP_ID", nullable=true) 
    @ForeignKey(name="FK_RELATIONSHIP_CHANGEGROUP")    
    public ChangeGroup getChangeGroup()
    {
        return changeGroup;
    }

    public void setChangeGroup(ChangeGroup changeGroup)
    {
        this.changeGroup = changeGroup;
    }

	/**
	 * This is used internally, use protected so it is not exposed 
	 * @return the sourceEntityId
	 */
    @Column (name="SOURCE_ENTITY_ID", nullable=false)
	public long getSourceEntityId()
	{
		return sourceEntityId;
	}

	/**
	 * This is used internally, use protected so it is not exposed 
	 * @param sourceEntityId the sourceEntityId to set
	 */
	public void setSourceEntityId(long sourceEntityId)
	{
		this.sourceEntityId = sourceEntityId;
	}

	/**
	 * This is used internally, use protected so it is not exposed 
	 * @return the targetEntityId
	 */
    @Column (name="TARGET_ENTITY_ID", nullable=false)
	public long getTargetEntityId()
	{
		return targetEntityId;
	}

	/**
	 * This is used internally, use protected so it is not exposed 
	 * @param targetEntityId the targetEntityId to set
	 */
	public void setTargetEntityId(long targetEntityId)
	{
		this.targetEntityId = targetEntityId;
	}
    public String toString()
    {
        String relationshipTypeName = "";
        if (this.getRelationshipType() != null)
        {
            relationshipTypeName = this.getRelationshipType().getName();
        }
        return " Id:"+this.getId()+"  Entity:"+ this.getEntityId()+"  type:"+relationshipTypeName+"  SourceEntityId:"+this.getSourceEntityId()+"  TargetEntityId:"+this.getTargetEntityId()+"  active:"+this.getActive();
    }
}
