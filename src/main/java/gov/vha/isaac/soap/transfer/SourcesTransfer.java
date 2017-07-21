package gov.vha.isaac.soap.transfer;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Source")
public class SourcesTransfer
{
    private Collection<String> sources;

    @XmlElement(name="Source", required=true, nillable=false)
    public Collection<String> getSources()
    {
        return sources;
    }

    public void setSources(Collection<String> sources)
    {
        this.sources = sources;
    }
}
