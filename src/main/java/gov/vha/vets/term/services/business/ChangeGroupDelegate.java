package gov.vha.vets.term.services.business;

import gov.vha.vets.term.services.dao.ChangeGroupDao;
import gov.vha.vets.term.services.model.ChangeGroup;

public class ChangeGroupDelegate
{
	public static ChangeGroup create(String source)
	{
		ChangeGroup changeGroup = new ChangeGroup(source);
		
		return ChangeGroupDao.save(changeGroup);
	}
}
