package gov.vha.vets.term.webservice.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DomainBindings")
public class SubsetsTransfer
{
	private Collection<SubsetTransfer> Subsets;

	public SubsetsTransfer()
	{
		super();
	}

	public SubsetsTransfer(Collection<SubsetTransfer> Subsets)
	{
		super();
		this.Subsets = Subsets;
	}

    @XmlElement(name="DomainBinding", required=true, nillable=false)
	public Collection<SubsetTransfer> getSubsets()
	{
		return Subsets;
	}

	public void setSubsets(Collection<SubsetTransfer> Subsets)
	{
		this.Subsets = Subsets;
	}
}
