package gov.vha.vets.term.services.dto;

import java.util.List;

public class SearchResultDTO
{
	private List<?> searchResultDTOList;
	private int totalRowCount;
	private long totalQueryTime;

	public SearchResultDTO(List<?> searchResultDTOs, int totalRowCount, long totalQueryTime)
	{
		this.searchResultDTOList = searchResultDTOs;
		this.totalRowCount = totalRowCount;
		this.totalQueryTime = totalQueryTime;
	}
	
	/**
	 * @return the searchResultDTOList
	 */
	public List<?> getSearchResultDTOList()
	{
		return searchResultDTOList;
	}
	
	/**
	 * @param searchResultDTOList the searchResultDTOList to set
	 */
	public void setSearchResultDTOList(List<?> searchResultDTOList)
	{
		this.searchResultDTOList = searchResultDTOList;
	}
	
	/**
	 * @return the totalRowCount
	 */
	public int getTotalRowCount()
	{
		return totalRowCount;
	}
	
	/**
	 * @param totalRowCount the totalRowCount to set
	 */
	public void setTotalRowCount(int totalRowCount)
	{
		this.totalRowCount = totalRowCount;
	}
	
	/**
	 * @return the totalQueryTime
	 */
	public long getTotalQueryTime()
	{
		return totalQueryTime;
	}
	
	/**
	 * @param totalQueryTime the totalQueryTime to set
	 */
	public void setTotalQueryTime(long totalQueryTime)
	{
		this.totalQueryTime = totalQueryTime;
	}
}
