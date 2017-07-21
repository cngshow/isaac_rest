/*
 * Created on Oct 18, 2004
 *
  */
package gov.vha.isaac.soap.model;

//import javax.persistence.DiscriminatorValue;
//import javax.persistence.Entity;


//@Entity
//@DiscriminatorValue("D")
public class DesignationType extends Type
{
	public static final String FULLY_SPECIFIED_NAME = "Fully Specified Name";
    public static final String PREFERRED_NAME = "Preferred Name";
    public static final String SYNONYM = "Synonym";	
    /**
	 * leave default constructor for reflection
	 */
	public DesignationType()
	{
	}	
	public DesignationType(String name)
	{
		super(name);
	}
	public String toString()
	{
	    return "Designation Type: Id="+this.getId()+ " Name=" + this.getName();
	}
}
