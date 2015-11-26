package gov.vha.isaac.rest.restClasses;

import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;

public class RestIdentifiedObject
{
	@XmlElement
	List<UUID> uuids;
	
	public RestIdentifiedObject(List<UUID> uuids)
	{
		this.uuids = uuids;
	}
}
