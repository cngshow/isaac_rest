/*
 * Created on Oct 18, 2004
 */
package gov.vha.vets.term.services.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="TYPE_SEQ", allocationSize=1)
@Table(name="TYPE")
@DiscriminatorColumn(name="KIND",discriminatorType=DiscriminatorType.STRING, length=1)
public class Type extends Base
{
    private String name;
    
    public Type()
    {
    	
    }

    public Type(String name)
    {
    	this.name = name;
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
     * @return Returns the name.
     */
    @Column (name="NAME", nullable=false, unique=false)
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    public String toString()
    {
        return this.getName();
    }
}
