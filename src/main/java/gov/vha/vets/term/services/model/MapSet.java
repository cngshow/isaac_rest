package gov.vha.vets.term.services.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;

@Entity
@DiscriminatorValue("M")
@SecondaryTables(
	{@SecondaryTable
		( name="MapSetExtension", pkJoinColumns=
			{@PrimaryKeyJoinColumn(name="MapSetId", referencedColumnName="id")}
		)
	}
)

public class MapSet extends Concept
{
	private long sourceVersionId;
	private long targetVersionId;
    protected Date effectiveDate;
	
    /**
     * @param name
     * @param code
     * @param vuid
     * @param
     * @param type
     * @param code
     * @param codeSystem
     * @param targetVersionId2 
     * @param sourceVersionId2 
     */
    public MapSet(String name, String code, long vuid, CodeSystem codeSystem, Version version, boolean active, long sourceVersionId, long targetVersionId)
    {
    	
        this.name = name;
        this.code = code;
        this.vuid = vuid;
        this.codeSystem = codeSystem;
        this.version = version;
        this.active = active;
        this.sourceVersionId = sourceVersionId;
        this.targetVersionId = targetVersionId;
    }

    public MapSet()
    {
    }


    public Object clone () throws CloneNotSupportedException
    {
        MapSet mapSet = (MapSet)super.clone();

        if (this.codeSystem != null)
            mapSet.codeSystem = (CodeSystem)this.codeSystem.clone();
        
        return mapSet;
    }

    @Column(table="MapSetExtension")
	public Long getSourceVersionId() 
    {
		return sourceVersionId;
	}

	public void setSourceVersionId(long sourceVersionId) 
	{
		this.sourceVersionId = sourceVersionId;
	}

    @Column(table="MapSetExtension")
	public Long getTargetVersionId() 
    {
		return targetVersionId;
	}

	public void setTargetVersionId(long targetVersionId) 
	{
		this.targetVersionId = targetVersionId;
	}

    @Column(table="MapSetExtension")
	public Date getEffectiveDate()
	{
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
	
}
