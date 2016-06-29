package gov.vha.vets.term.services.dto;

import gov.vha.vets.term.services.model.Relationship;

import java.io.Serializable;

public class RelationshipDTO implements Serializable
{
    protected String name;
    protected Relationship relationship;

    public RelationshipDTO()
    {
        super();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public Relationship getRelationship()
    {
        return relationship;
    }

    public void setRelationship(Relationship relationship)
    {
        this.relationship = relationship;
    }


}