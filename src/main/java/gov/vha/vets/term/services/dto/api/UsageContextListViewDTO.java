package gov.vha.vets.term.services.dto.api;

import java.util.ArrayList;
import java.util.List;

public class UsageContextListViewDTO 
{
	private Long totalNumberOfRecords;
	private List<UsageContextDetailViewDTO> usageContextDetailViewDTO = new ArrayList<UsageContextDetailViewDTO>();
	
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	public List<UsageContextDetailViewDTO> getUsageContextDetailViewDTO() {
		return usageContextDetailViewDTO;
	}
	public void setUsageContextDetailViewDTO(
			List<UsageContextDetailViewDTO> usageContextDetailViewDTO) {
		this.usageContextDetailViewDTO = usageContextDetailViewDTO;
	}

}
