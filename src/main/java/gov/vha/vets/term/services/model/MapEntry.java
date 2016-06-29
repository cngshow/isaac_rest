package gov.vha.vets.term.services.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SecondaryTables;



@SuppressWarnings("serial")
@Entity
@DiscriminatorValue("E")
@SecondaryTables(
	{@SecondaryTable
		( name="MapEntryExtension", pkJoinColumns=
			{@PrimaryKeyJoinColumn(name="MapEntryId", referencedColumnName="id")}
		)
	}
)
public class MapEntry extends Concept
{
	protected String sourceCode;
	protected String targetCode;
    protected Date effectiveDate;
    
    /**
     * @param name
     * @param code
     * @param vuid
     * @param
     * @param type
     * @param code
     * @param codeSystem
     */
    public MapEntry(String name, String code, Long vuid, CodeSystem codeSystem, Version version, boolean active, String sourceCode,
    		String targetCode, Date effectiveDate)
    {
        this.name = name;
        this.code = code;
        this.vuid = vuid;
        this.codeSystem = codeSystem;
        this.version = version;
        this.active = active;
        this.sourceCode = sourceCode;
        this.targetCode = targetCode;
        this.effectiveDate = effectiveDate;
    }

    public MapEntry()
    {
    }


    public Object clone () throws CloneNotSupportedException
    {
        MapEntry mapEntry = (MapEntry)super.clone();

        if (this.codeSystem != null)
            mapEntry.codeSystem = (CodeSystem)this.codeSystem.clone();
        
        return mapEntry;
    }

    @Column(table="MapEntryExtension")
	public String getSourceCode() 
	{
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) 
	{
		this.sourceCode = sourceCode;
	}

    @Column(table="MapEntryExtension")
	public String getTargetCode() 
	{
		return targetCode;
	}

	public void setTargetCode(String targetCode) 
	{
		this.targetCode = targetCode;
	}

	@Column(table="MapEntryExtension")
	public Date getEffectiveDate()
	{
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate)
	{
		this.effectiveDate = effectiveDate;
	}
}
