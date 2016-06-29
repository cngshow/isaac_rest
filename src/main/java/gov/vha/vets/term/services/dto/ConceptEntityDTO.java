/**
 * 
 */
package gov.vha.vets.term.services.dto;

import java.io.Serializable;

/**
 * @author VHAISAOSTRAR
 * 
 */
public class ConceptEntityDTO implements Comparable<ConceptEntityDTO>, Serializable, Cloneable
{
	public enum Kind {CODEDCONCEPT, MAPSET};
	protected String name;
    protected long entityId;
    protected Kind kind;

    public ConceptEntityDTO()
    {
    	kind = Kind.CODEDCONCEPT;
    }

    public ConceptEntityDTO(String name, long entityId)
    {
        setName(name);
        setEntityId(entityId);
    }

    /**
     * @return the entityId
     */
    public long getEntityId()
    {
        return entityId;
    }

    /**
     * @param entityId
     *            the entityId to set
     */
    public void setEntityId(long entityId)
    {
        this.entityId = entityId;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(ConceptEntityDTO conceptEntityDTO2)
    {
    	int returnValue = 0;
    	if (this.getName()!= null && conceptEntityDTO2.getName() != null)
		{
    		returnValue = this.getName().compareTo(conceptEntityDTO2.getName());
		}
    	else if (this.getName()== null && conceptEntityDTO2.getName() != null)
		{
    		returnValue = 1;
		}
    	else if (this.getName()!= null && conceptEntityDTO2.getName() == null)
		{
    		returnValue = -1;
		}
    	
    	
        return returnValue;
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
        {
            return true;
        }
        boolean result = false;
        if (that instanceof ConceptEntityDTO)
        {
            ConceptEntityDTO thatConcept = (ConceptEntityDTO) that;
            result = this.getEntityId() == thatConcept.getEntityId() ? true : false;
        }
        return result;
    }
    public Object clone() throws CloneNotSupportedException
    {
        // First make exact bitwise copy
        ConceptEntityDTO copy = (ConceptEntityDTO) super.clone();

        return copy;
    } // clone

	public Kind getKind()
	{
		return kind;
	}

	public void setKind(Kind kind)
	{
		this.kind = kind;
	}
    
}
