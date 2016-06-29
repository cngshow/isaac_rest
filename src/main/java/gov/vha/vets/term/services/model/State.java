/*
 * Created on Oct 18, 2004
 */
package gov.vha.vets.term.services.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="STATE_SEQ", allocationSize=1 )
@Table(name="STATE")
public class State extends Base
{
    public static final String INITIAL = "Initial";
    public static final String READY_TO_TEST = "Ready To Test";
    public static final String IN_TEST = "In Test";
    
    private String name;
    private String type;
    
    public State()
    {
    	
    }

    public State(String name, String type)
    {
        this.name = name;
        this.type = type;
    }
    
    public State(String name)
    {
    	this.name = name;
    }
    
    /**
     * @return Returns the id.
     * @hibernate.id generator-class="gov.vha.vets.term.services.util.TableNameSequenceGenerator"
     */
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQ_STORE")
    public long getId()
    {
        return id;
    }

    /**
     * @return Returns the name.
     * @hibernate.property not-null="true" unique="true"
     */
    @Column (name="NAME", nullable=false, unique=true)
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

    /**
     * @return Returns the name.
     * @hibernate.property not-null="false" unique="true"
     */
    @Column (name="type", nullable=false, unique=true)
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
    
    public String toString()
    {
        return this.getId()+" "+this.getName()+" "+this.getType();
    }
}
