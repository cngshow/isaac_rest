/*
 * Created on Oct 18, 2004
 */
package gov.vha.vets.term.services.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;


@Entity
@javax.persistence.SequenceGenerator(name="SEQ_STORE", sequenceName="CONCEPTSTATE_SEQ", allocationSize=1 )

@Table(name="CONCEPTSTATE")
public class ConceptState extends Base
{
    protected long conceptEntityId;
    protected State state;
    
    public ConceptState()
    {
    	
    }

    public ConceptState(long conceptEntityId, State state)
    {
        super();
        this.conceptEntityId = conceptEntityId;
        this.state = state;
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
     * @return the conceptEntityId
     */

    @Column (name="CONCEPT_ENTITY_ID", nullable=false, unique=true)
    public long getConceptEntityId()
    {
        return conceptEntityId;
    }

    public void setConceptEntityId(long conceptEntityId)
    {
        this.conceptEntityId = conceptEntityId;
    }
    /**
     * @return Returns the status of a concept.
     */
    @ManyToOne    
    @JoinColumn(name="STATE_ID", nullable=false) 
    @ForeignKey(name="FK_CS_STATE")    
    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    public String toString()
    {
        return "Entity: "+conceptEntityId+" State:"+state.getName();
    }

}
