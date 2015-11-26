package gov.vha.isaac.rest.restClasses;

import javax.xml.bind.annotation.XmlElement;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.chronicle.StampedVersion;

public class RestStampedVersion
{
	@XmlElement
	int stampSequence;
	@XmlElement
	State state;
	@XmlElement
	long time;
	@XmlElement
	int authorSequence;
	@XmlElement
	int moduleSequence;
	@XmlElement
	int pathSequence;

	public RestStampedVersion(StampedVersion sv)
	{
		stampSequence = sv.getStampSequence();
		state = sv.getState();
		time = sv.getTime();
		authorSequence = sv.getAuthorSequence();
		pathSequence = sv.getPathSequence();
	}

}
