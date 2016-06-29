package gov.vha.vets.term.webservice.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="CodeSystems")
public class CodeSystemsTransfer
{
	private List<CodeSystemTransfer> codeSystems;

	public CodeSystemsTransfer()
	{
		super();
	}

	public CodeSystemsTransfer(List<CodeSystemTransfer> codeSystems)
	{
		super();
		this.codeSystems = codeSystems;
	}

    @XmlElement(name="CodeSystem", required=true, nillable=false)
	public List<CodeSystemTransfer> getCodeSystems()
	{
		return codeSystems;
	}

	public void setCodeSystems(List<CodeSystemTransfer> codeSystems)
	{
		this.codeSystems = codeSystems;
	}
}
