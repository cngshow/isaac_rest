package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.SubsetRelationship;

public class SubsetContentsView
{
    Designation designation;
    SubsetRelationship subsetRelationship;

    
    public SubsetContentsView()
    {
    }

    public SubsetContentsView(Designation designation,
            SubsetRelationship subsetRelationship)
    {
        super();
        this.designation = designation;
        this.subsetRelationship = subsetRelationship;
    }
    
    public Designation getDesignation()
    {
        return designation;
    }
    public void setDesignation(Designation designation)
    {
        this.designation = designation;
    }
    public SubsetRelationship getSubsetRelationship()
    {
        return subsetRelationship;
    }
    public void setSubsetRelationship(SubsetRelationship subsetRelationship)
    {
        this.subsetRelationship = subsetRelationship;
    }
}
