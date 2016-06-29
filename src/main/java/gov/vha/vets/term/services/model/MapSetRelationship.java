package gov.vha.vets.term.services.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@SuppressWarnings("serial")
@Entity
@DiscriminatorValue("M")
public class MapSetRelationship extends Relationship
{
	private int sequence;
	private Long grouping;

	public MapSetRelationship()
	{
	}

	/**
	 * @param sourceEntityId
	 * @param targetEntityId
	 * @param version
	 * @param active
	 */
	public MapSetRelationship(long sourceEntityId, long targetEntityId,
			Version version, boolean active, int sequence, Long grouping)
	{
		super(sourceEntityId, targetEntityId, version, active);
		this.sequence = sequence;
		this.grouping = grouping;
	}
    @Column (name="Sequence", nullable=false)
	public int getSequence()
	{
		return sequence;
	}

	public void setSequence(int sequence)
	{
		this.sequence = sequence;
	}

    @Column (name="Grouping", nullable=true)
	public Long getGrouping()
	{
		return grouping;
	}

	public void setGrouping(Long grouping)
	{
		this.grouping = grouping;
	}

}
