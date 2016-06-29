package gov.vha.vets.term.services.dto.history;

import gov.vha.vets.term.services.model.Designation;
import gov.vha.vets.term.services.model.Version;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class ConceptHistoryDTO implements Serializable
{
	private Version currentVersion;
	private List<Version> versions;
	private Designation preferredDesignation;
	private EntityHistoryDTO codedConcept;
	private List<EntityHistoryDTO> properties;
	private List<DesignationEntityHistoryDTO> designations;
	private List<EntityHistoryDTO> designationRelationships;
	private List<EntityHistoryDTO> relationships;
	private List<EntityHistoryDTO> parents;
	private List<EntityHistoryDTO> children;

	public ConceptHistoryDTO(Version currentVersion, List<Version> versions,
			EntityHistoryDTO codedConcept, List<EntityHistoryDTO> properties,
			List<DesignationEntityHistoryDTO> designations,
			List<EntityHistoryDTO> designationRelationships,
			List<EntityHistoryDTO> relationships,
			List<EntityHistoryDTO> parents, List<EntityHistoryDTO> children)
	{
		super();
		this.currentVersion = currentVersion;
		this.versions = versions;
		this.codedConcept = codedConcept;
		this.properties = properties;
		this.designations = designations;
		this.designationRelationships = designationRelationships;
		this.relationships = relationships;
		this.parents = parents;
		this.children = children;
	}

	/**
	 * @return the versions
	 */
	public List<Version> getVersions()
	{
		return versions;
	}

	/**
	 * @param versions
	 *            the versions to set
	 */
	public void setVersions(List<Version> versions)
	{
		this.versions = versions;
	}

	/**
	 * @return the codedConcept
	 */
	public EntityHistoryDTO getCodedConcept()
	{
		return codedConcept;
	}

	/**
	 * @param codedConcept
	 *            the codedConcept to set
	 */
	public void setCodedConcept(EntityHistoryDTO codedConcept)
	{
		this.codedConcept = codedConcept;
	}

	/**
	 * @return the properties
	 */
	public List<EntityHistoryDTO> getProperties()
	{
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(List<EntityHistoryDTO> properties)
	{
		this.properties = properties;
	}

	/**
	 * @return the designations
	 */
	public List<DesignationEntityHistoryDTO> getDesignations()
	{
		return designations;
	}

	/**
	 * @param designations
	 *            the designations to set
	 */
	public void setDesignations(List<DesignationEntityHistoryDTO> designations)
	{
		this.designations = designations;
	}

	/**
	 * @return the relationships
	 */
	public List<EntityHistoryDTO> getRelationships()
	{
		return relationships;
	}

	/**
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(List<EntityHistoryDTO> relationships)
	{
		this.relationships = relationships;
	}

	/**
	 * @return the parents
	 */
	public List<EntityHistoryDTO> getParents()
	{
		return parents;
	}

	/**
	 * @param parents
	 *            the parents to set
	 */
	public void setParents(List<EntityHistoryDTO> parents)
	{
		this.parents = parents;
	}

	/**
	 * @return the children
	 */
	public List<EntityHistoryDTO> getChildren()
	{
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<EntityHistoryDTO> children)
	{
		this.children = children;
	}

	public List<EntityHistoryDTO> getDesignationRelationships()
	{
		return designationRelationships;
	}

	public void setDesignationRelationships(
			List<EntityHistoryDTO> designationRelationships)
	{
		this.designationRelationships = designationRelationships;
	}

	public Version getCurrentVersion()
	{
		return currentVersion;
	}

	public void setCurrentVersion(Version currentVersion)
	{
		this.currentVersion = currentVersion;
	}

	public Designation getPreferredDesignation()
	{
		return preferredDesignation;
	}

	public void setPreferredDesignation(Designation preferredDesignation)
	{
		this.preferredDesignation = preferredDesignation;
	}

}
