package gov.vha.isaac.rest.restClasses.concept;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.concept.ConceptVersion;
import gov.vha.isaac.rest.restClasses.RestStampedVersion;

@XmlRootElement
public class RestConceptVersion
{
	@XmlElement
	RestConceptChronology conChronology;
	@XmlElement
	RestStampedVersion conVersion;
	@XmlElement
	String foo = "fred";
	
	protected RestConceptVersion()
	{
		//for Jaxb
	}
	
	public RestConceptVersion(@SuppressWarnings("rawtypes") ConceptVersion cv)
	{
		conVersion = new RestStampedVersion(cv);
		conChronology = new RestConceptChronology(cv);
	}
}
