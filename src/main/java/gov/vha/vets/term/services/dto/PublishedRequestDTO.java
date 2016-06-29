package gov.vha.vets.term.services.dto;

public class PublishedRequestDTO
{
	private String name;
	private String category;
	
	public PublishedRequestDTO()
	{}
	
	public PublishedRequestDTO(String name, String category)
	{
		this.category = category;
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}
	
	public void setNAME(String name)
	{
		this.name = name;
	}
	
	public void setCATEGORY(String category)
	{
		this.category = category;
	}
}
