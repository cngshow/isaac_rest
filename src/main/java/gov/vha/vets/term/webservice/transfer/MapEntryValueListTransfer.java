package gov.vha.vets.term.webservice.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="MapEntryValueList")
@XmlType(propOrder={"totalNumberOfRecords", "mapEntryDetailTransfers"})
public class MapEntryValueListTransfer
{
	private Long totalNumberOfRecords;
	private List<MapEntryValueTransfer> mapEntryValueTransfers;
	
	@XmlElement(name="TotalNumberOfRecords")
	public Long getTotalNumberOfRecords()
	{
		return totalNumberOfRecords;
	}
	public void setTotalNumberOfRecords(Long totalNumberOfRecords)
	{
		this.totalNumberOfRecords = totalNumberOfRecords;
	}
	
	@XmlElement(name="MapEntryValues", required=false, nillable=true)
	public List<MapEntryValueTransfer> getMapEntryDetailTransfers()
	{
		return mapEntryValueTransfers;
	}
	public void setMapEntryValueTransfers(List<MapEntryValueTransfer> mapEntryValueTransfers)
	{
		this.mapEntryValueTransfers = mapEntryValueTransfers;
	}
}
