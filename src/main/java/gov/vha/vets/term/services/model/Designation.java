/*
 * Created on Oct 18, 2004
 */

package gov.vha.vets.term.services.model;

import gov.vha.vets.term.services.util.HibernateSessionFactory;

import java.util.Comparator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;



@Entity
@DiscriminatorValue("D")
public class Designation extends Concept implements Comparator<Designation>
{
    
	public Designation()
	{
		
	}
	
	public Designation(CodeSystem codeSystem, String code, String name, Long vuid, Version version,
						DesignationType type,  boolean active)
	{
        this.codeSystem = codeSystem;
        this.code = code;
		this.name = name;
        this.vuid = vuid;
		this.version = version;
		this.type = type;
		this.active = active;
	}

	
	public String toString()
	{
		return "Code: "+code+" name: "+this.getName()+" active: "+active+" type: "+type+" version: "+version;
	}

    public int compare(Designation first, Designation second)
    {
        String object1Prefix = null;
        boolean firstIsVhat = false;
        boolean secondIsVhat = false;
        
        if (first.getCodeSystem().getName().equals(HibernateSessionFactory.VHAT_NAME))
        {
            firstIsVhat = true;
        }
        if (second.getCodeSystem().getName().equals(HibernateSessionFactory.VHAT_NAME))
        {
            secondIsVhat = true;
        }
        
        if (first.getCodeSystem().getPreferredDesignationType().getId() == first.getType().getId())
        {
            object1Prefix = "1";
        }
        else if (firstIsVhat && DesignationType.SYNONYM.equals(first.getType().getName()))
        {
            object1Prefix = "2";
        }
        else
        {
            object1Prefix = "3";
        }
                
        String object2Prefix = null;
        if (first.getCodeSystem().getPreferredDesignationType().getId() == second.getType().getId())
        {
            object2Prefix = "1";
        }
        else if (secondIsVhat && DesignationType.SYNONYM.equals(second.getType().getName()))
        {
            object2Prefix = "2";
        }
        else
        {
            object2Prefix = "3";
        }
        String string1 = (firstIsVhat) ? object1Prefix+"-"+first.getType().getName()+"-"+first.getName() :
            object1Prefix+"-"+first.getName();
        String string2 = (secondIsVhat) ? object2Prefix+"-"+second.getType().getName()+"-"+second.getName() :
            object2Prefix+"-"+second.getName();
        
        return string1.compareTo(string2);
    }
}
