package gov.vha.isaac.rest.restClasses.sememe;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;

@XmlRootElement
public class RestSememeDescriptionVersion extends RestSememeVersion
{
	@XmlElement
	int caseSignificanceConceptSequence;
	@XmlElement
	int languageConceptSequence;
	@XmlElement
	String text;
	@XmlElement
	int descriptionTypeConceptSequence;

	protected RestSememeDescriptionVersion()
	{
		//for Jaxb
	}
	
	public RestSememeDescriptionVersion(@SuppressWarnings("rawtypes") DescriptionSememe dsv)
	{
		super(dsv);
		caseSignificanceConceptSequence = dsv.getCaseSignificanceConceptSequence();
		languageConceptSequence = dsv.getLanguageConceptSequence();
		text = dsv.getText();
		descriptionTypeConceptSequence = dsv.getDescriptionTypeConceptSequence();
	}
}
