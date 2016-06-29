package gov.vha.vets.term.webservice.transfer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="ValueSetList")
@XmlType(propOrder={"totalNumberOfRecords", "valueSetTransfersDetails"})
public class ValueSetListTransfer 
{
	private Long totalNumberOfRecords;
	private List<ValueSetTransfer> valueSetTransfersDetails = new ArrayList<ValueSetTransfer>();
	
	@XmlElement(name="TotalNumberOfRecords")
	public Long getTotalNumberOfRecords() 
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	@XmlElement(name="ValueSet", required=false, nillable=true)
	public List<ValueSetTransfer> getValueSetTransfersDetails()
	{
		return valueSetTransfersDetails;
	}
	public void setValueSetTransfersDetails(
			List<ValueSetTransfer> valueSetTransfersDetails)
	{
		this.valueSetTransfersDetails = valueSetTransfersDetails;
	}
}
