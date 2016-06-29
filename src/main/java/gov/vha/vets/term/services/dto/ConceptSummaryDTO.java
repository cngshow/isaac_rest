package gov.vha.vets.term.services.dto;

import java.io.Serializable;
import java.util.Comparator;

@SuppressWarnings({ "serial", "unchecked" })
public class ConceptSummaryDTO implements Serializable, Comparable<ConceptSummaryDTO>, Comparator
{
	private long conceptEntityId;
	private String designationName;
	private String state;
	private boolean isSelected;

	public ConceptSummaryDTO()
	{
		super();
	}

	public ConceptSummaryDTO(long conceptEntityId, String designationName,
			String status, String state, String domain)
	{
		super();
		this.conceptEntityId = conceptEntityId;
		this.designationName = designationName;
		this.state = state;
	}

	public String getDesignationName()
	{
		return designationName;
	}

	public void setDesignationName(String designationName)
	{
		this.designationName = designationName;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public long getConceptEntityId()
	{
		return conceptEntityId;
	}

	public void setConceptId(long conceptEntityId)
	{
		this.conceptEntityId = conceptEntityId;
	}
	
	/**
	 * Indicates whether the item is selected
	 * @return
	 */
	public boolean isSelected()
	{
		return isSelected;
	}

	/**
	 * Set the item as selected
	 * @param isSelected
	 */
	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}
    @Override
    public int compareTo(ConceptSummaryDTO conceptSummary)
    {
        return this.getDesignationName().compareToIgnoreCase(conceptSummary.getDesignationName());
    }


    @Override
    public int compare(Object o1, Object o2)
    {
        // TODO Auto-generated method stub
        return ((String)o1).compareToIgnoreCase((String)o2);
    }
    
    public String toString()
    {
        return "Name: " + this.designationName + ", concept entityId: " + this.getConceptEntityId();
    }
}
