package gov.vha.isaac.rest.restClasses.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.rest.restClasses.RestIdentifiedObject;

@XmlRootElement
public class RestSememeChronology
{
	@XmlElement
	int sememeSequence;
	@XmlElement
	int assemblageSequence;
	@XmlElement
	int referencedComponentNid;
	@XmlElement
	RestIdentifiedObject identifiers;
	
	protected RestSememeChronology()
	{
		//For Jaxb
	}

	public RestSememeChronology(@SuppressWarnings("rawtypes") SememeVersion sv)
	{
		identifiers = new RestIdentifiedObject(sv.getChronology().getUuidList());
		sememeSequence = sv.getSememeSequence();
		assemblageSequence = sv.getAssemblageSequence();
		referencedComponentNid = sv.getReferencedComponentNid();
	}
}
