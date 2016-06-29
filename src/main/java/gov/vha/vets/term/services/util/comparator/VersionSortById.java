package gov.vha.vets.term.services.util.comparator;

import gov.vha.vets.term.services.model.Version;

import java.util.Comparator;

public class VersionSortById implements Comparator<Version>
{
    public int compare(Version version1, Version version2)
    {
        return (int) (version1.getId()-version2.getId());
//        return version1.compareTo(version2);
    }
}
