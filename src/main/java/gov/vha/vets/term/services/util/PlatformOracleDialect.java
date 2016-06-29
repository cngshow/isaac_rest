/**
 * 
 */
package gov.vha.vets.term.services.util;

import org.hibernate.dialect.OracleDialect;

/**
 * @author vhaislchevaj
 * 
 */
public class PlatformOracleDialect extends OracleDialect
{
    public Class getNativeIdentifierGeneratorClass()
    {
        return TableNameSequenceGenerator.class;
    }
}
