/**
 * 
 */
package gov.vha.vets.term.services.dto.publish;

import java.io.Serializable;

/**
 * @author VHAISAOSTRAR
 *
 */
public class NameValueDTO implements Serializable 
{
	private String name;
	private String value;
	
	public NameValueDTO(String name, String value)
    {
        super();
        this.name = name;
        this.value = value;
    }
    /**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public String getValue()
	{
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	
}
