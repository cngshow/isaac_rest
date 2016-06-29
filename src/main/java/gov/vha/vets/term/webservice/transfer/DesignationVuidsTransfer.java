package gov.vha.vets.term.webservice.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="DesignationVUIDs")
public class DesignationVuidsTransfer
{
    private Collection<Long> designationVUIDs;

    @XmlElement(name="DesignationVUID", required=true, nillable=false)
    public Collection<Long> getDesignationVUIDs()
    {
        return designationVUIDs;
    }

    public void setDesignationVUIDs(Collection<Long> designationVUIDs)
    {
        this.designationVUIDs = designationVUIDs;
    }
}
