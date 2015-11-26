package gov.vha.isaac.rest.restClasses.concept;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.rest.restClasses.RestIdentifiedObject;

@XmlRootElement
public class RestConceptChronology 
{
	@XmlElement
	RestIdentifiedObject identifiers;
	
	protected RestConceptChronology()
	{
		//for JaxB
	}
	
	
	public RestConceptChronology(@SuppressWarnings("rawtypes") ConceptVersion cv)
	{
		identifiers = new RestIdentifiedObject(cv.getChronology().getUuidList());
	}
}
