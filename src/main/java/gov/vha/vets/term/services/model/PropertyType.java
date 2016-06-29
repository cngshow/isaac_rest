/*
 * Created on Oct 18, 2004
 */
package gov.vha.vets.term.services.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("P")
public class PropertyType extends Type
{
	public static final String DESCRIPTION = "Description";
	public static final String TEXT_DEFINITION = "Textual Definition";

	/**
	 * leave default constructor for reflection
	 */
	public PropertyType()
	{
	}
	public PropertyType(String name)
	{
		super(name);
	}
}
