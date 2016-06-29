package gov.vha.vets.term.services.dto.delta;

import java.util.List;

public class DiscoveryResultsDTO
{
	private List<DiscoveryDeltaDTO> designationDeltas;
	private List<DiscoveryDeltaDTO> propertyDeltas;
	private List<DiscoveryDeltaDTO> relationshipDeltas;
	
	public List<DiscoveryDeltaDTO> getDesignationDeltas()
	{
		return designationDeltas;
	}
	
	public void setDesignationDeltas(List<DiscoveryDeltaDTO> designationDeltas)
	{
		this.designationDeltas = designationDeltas;
	}
	
	public List<DiscoveryDeltaDTO> getPropertyDeltas()
	{
		return propertyDeltas;
	}
	
	public void setPropertyDeltas(List<DiscoveryDeltaDTO> propertyDeltas)
	{
		this.propertyDeltas = propertyDeltas;
	}
	
	public List<DiscoveryDeltaDTO> getRelationshipDeltas()
	{
		return relationshipDeltas;
	}
	
	public void setRelationshipDeltas(List<DiscoveryDeltaDTO> relationshipDeltas)
	{
		this.relationshipDeltas = relationshipDeltas;
	}
}
