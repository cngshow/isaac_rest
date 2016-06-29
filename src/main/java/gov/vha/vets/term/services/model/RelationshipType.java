/*
 * Created on Oct 18, 2004
 *
  */
package gov.vha.vets.term.services.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("R")
public class RelationshipType extends Type
{
    public static final String HAS_PARENT = "has_parent";
    public static final String HAS_ROOT = "has_root";
    public static final String HAS_DESIGNATION = "has_designation";
    
	/**
	 * leave default constructor for reflection
	 */
	public RelationshipType()
	{
	}
	public RelationshipType(String name)
	{
		super(name);
	}
}
