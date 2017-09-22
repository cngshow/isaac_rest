package gov.vha.isaac.soap.transfer;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "ValueSetContentsList")
@XmlType(propOrder = { "totalNumberOfRecords", "valueSetContentsTransfers" })
public class ValueSetContentsListTransfer {
	private Long totalNumberOfRecords;
	private List<ValueSetContentsTransfer> valueSetContentsTransfers;

	@XmlElement(name = "TotalNumberOfRecords")
	public Long getTotalNumberOfRecords() {
		return totalNumberOfRecords;
	}

	public void setTotalNumberOfRecords(Long totalNumberOfRecords) {
		this.totalNumberOfRecords = totalNumberOfRecords;
	}

	@XmlElement(name = "ValueSetContents", required = false, nillable = true)
	public List<ValueSetContentsTransfer> getValueSetContentsTransfers() {
		return valueSetContentsTransfers;
	}

	public void setValueSetContentsTransfers(List<ValueSetContentsTransfer> valueSetContentsTransfers) {
		this.valueSetContentsTransfers = valueSetContentsTransfers;
	}

}
