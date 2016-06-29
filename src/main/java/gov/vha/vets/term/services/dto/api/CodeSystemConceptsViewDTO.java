package gov.vha.vets.term.services.dto.api;

import gov.vha.vets.term.services.model.Designation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CodeSystemConceptsViewDTO
{
	CodedConceptListViewDTO codedConceptListView;
	Map<Long, Collection<Designation>> designationMap;

	public CodeSystemConceptsViewDTO(CodedConceptListViewDTO codedConceptListView, Map<Long, Collection<Designation>> designationMap)
	{
		this.codedConceptListView = codedConceptListView;
		this.designationMap = designationMap;
	}

	public CodedConceptListViewDTO getCodedConceptListView()
	{
		return codedConceptListView;
	}

	public void setCodedConceptListView(CodedConceptListViewDTO codedConceptListView)
	{
		this.codedConceptListView = codedConceptListView;
	}

	public Map<Long, Collection<Designation>> getDesignations()
	{
		return designationMap;
	}

	public void setDesignations(HashMap<Long, Collection<Designation>> designationMap)
	{
		this.designationMap = designationMap;
	}
}
