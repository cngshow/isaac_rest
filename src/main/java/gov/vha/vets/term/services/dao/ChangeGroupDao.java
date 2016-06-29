package gov.vha.vets.term.services.dao;

import gov.vha.vets.term.services.model.ChangeGroup;
import gov.vha.vets.term.services.util.HibernateSessionFactory;

public class ChangeGroupDao
{
	public static ChangeGroup save(ChangeGroup changeGroup)
	{
		HibernateSessionFactory.currentSession().save(changeGroup);
		
		return changeGroup;
	}
}
