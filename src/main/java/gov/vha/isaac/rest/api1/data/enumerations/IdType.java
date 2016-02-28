package gov.vha.isaac.rest.api1.data.enumerations;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public enum IdType
{
	UUID("uuid"), NID("nid"), CONCEPT_SEQUENCE("conceptSequence"), SEMEME_SEQUENCE("sememeSequence"), SCTID("sctid"), VUID("vuid");
	
	private String displayName_;
	
	private IdType(String displayName)
	{
		displayName_ = displayName;
	}
	
	public String getDisplayName()
	{
		return displayName_;
	}
	
	public static Optional<IdType> parse(String input)
	{
		if (StringUtils.isNotBlank(input))
		{
			String trimmed = input.trim();
			for (IdType idt : IdType.values())
			{
				if (("" + idt.ordinal()).equals(trimmed) || idt.name().equalsIgnoreCase(trimmed) 
						|| idt.getDisplayName().equalsIgnoreCase(trimmed))
				{
					return Optional.of(idt);
				}
			}
		}
		return Optional.empty();
	}
}
