package gov.vha.isaac.soap.services.dto.config;

public class StateConfig
{
	private String name;
	private String type;

	/**
	 * Construct a new, empty State object
	 */
	public StateConfig()
	{
		super();
	}
	
	/**
	 * Construct a new State object
	 * @param name
	 */
	public StateConfig(String name, String type)
	{
		super();
		this.name = name;
		this.type = type;
	}

	/**
	 * Get the name of the State
	 * @return String name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Set the name of the State
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Get the type of the State
	 * @return String Type
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Set the type of the State
	 * @param type
	 */
	public void setType(String type)
	{
		this.type = type;
	}
}
