package gov.vha.vets.term.services.util;

import gov.vha.vets.term.services.model.BaseEntity;
import gov.vha.vets.term.services.model.BaseVersion;
import gov.vha.vets.term.services.model.ChangeGroup;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

@SuppressWarnings("serial")
public class ServicesInterceptor extends EmptyInterceptor
{
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
    {
        boolean result = false;
        // only update instances of baseEntity
        if (entity instanceof BaseEntity )
        {
            // cast object to baseEntity
            BaseEntity baseEntity = (BaseEntity) entity;
            if (baseEntity.getEntityId() == 0)
            {
                for (int i = 0; i < propertyNames.length; i++)
                {
                    // i = will be the same for the state object
                    if ("entityId".equals(propertyNames[i]))
                    {
                        state[i] = id;
                        break;
                    }
                }
                result = true;
            }
        }
            
        if (entity instanceof BaseVersion)
        {
        	ChangeGroup changeGroup = ChangeGroupManager.getInstance().getChangeGroup();
            if (changeGroup == null)
            {
                throw new RuntimeException("Need to set the change group!");
            }
            else if (changeGroup.getId() == 0L)
			{
				HibernateSessionFactory.currentSession().save(changeGroup);
			}

            // cast object to baseEntity
            for (int i = 0; i < propertyNames.length; i++)
            {
                // i = will be the same for the state object
                if ("changeGroup".equals(propertyNames[i]))
                {
                    state[i] = changeGroup;
                    break;
                }
            }
            result = true;
        }
        return result;
    }
    
    public void beforeTransactionCompletion(Transaction tx) 
    {
    	ChangeGroup changeGroup = ChangeGroupManager.getInstance().getChangeGroup();
    	// make sure that there is a valid changeGroup 
    	// if the changeGroup's id is zero then we must not have ever
    	// needed it previously so we don't want to update the changeGroup
    	// and the changeGroup should not exist in the database
        if (changeGroup != null && changeGroup.getId() != 0L)
		{
	        changeGroup.setChangeDate(new Date());
	        HibernateSessionFactory.currentSession().update(changeGroup);
        	HibernateSessionFactory.currentSession().flush();
        	HibernateSessionFactory.currentSession().clear();
		}
		
        ChangeGroupManager.getInstance().clear();
    }   
}
