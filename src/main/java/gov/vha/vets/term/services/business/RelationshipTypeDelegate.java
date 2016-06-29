package gov.vha.vets.term.services.business;

import java.util.List;

import gov.vha.vets.term.services.dao.ConceptRelationshipDao;
import gov.vha.vets.term.services.model.RelationshipType;

public class RelationshipTypeDelegate
{
	 public static RelationshipType getType(String relationshipTypeName)
	 {
	     return ConceptRelationshipDao.getType(relationshipTypeName);
	 }

	 public static RelationshipType createType(String relationshipTypeName)
	 {
	     return ConceptRelationshipDao.createType(relationshipTypeName);
	 }
	 
	 public static List<RelationshipType> getAllRelationshipTypes()
	 {
		 return null;
	 }

}
