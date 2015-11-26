package gov.vha.isaac.rest.restClasses.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.rest.restClasses.RestStampedVersion;

@XmlRootElement
public class RestSememeVersion 
{
	@XmlElement
	RestSememeChronology sememeChronology;
	@XmlElement
	RestStampedVersion sememeVersion;

	protected RestSememeVersion()
	{
		//For jaxb
	}
	
	public RestSememeVersion(@SuppressWarnings("rawtypes") SememeVersion sv)
	{
		sememeChronology = new RestSememeChronology(sv);
		sememeVersion = new RestStampedVersion(sv);
	}
}
