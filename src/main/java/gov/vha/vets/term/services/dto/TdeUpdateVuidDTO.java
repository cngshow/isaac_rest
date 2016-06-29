package gov.vha.vets.term.services.dto;


/**
 * 
 * @author 
 *
 */
public class TdeUpdateVuidDTO 
{
    private int conId;
    private int propId;


    public TdeUpdateVuidDTO(int conId, int propId)
    {
        super();
        this.conId = conId;
        this.propId = propId;
    }


    public int getConId()
    {
        return conId;
    }


    public void setConId(int conId)
    {
        this.conId = conId;
    }


    public int getPropId()
    {
        return propId;
    }


    public void setPropId(int propId)
    {
        this.propId = propId;
    }
    
}
