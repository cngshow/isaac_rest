
package gov.vha.isaac.soap.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class was generated by Apache CXF 3.1.12
 * Wed Jul 12 08:43:11 PDT 2017
 * Generated source version: 3.1.12
 */

@XmlRootElement(name = "ReturnConceptDetails", namespace = "urn:gov:va:med:sts:webservice:ct")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnConceptDetails", namespace = "urn:gov:va:med:sts:webservice:ct", propOrder = {"CodeSystemVUID", "VersionName", "Code"})

public class ReturnConceptDetails {

    @XmlElement(name = "CodeSystemVUID")
    private java.lang.Long CodeSystemVUID;
    @XmlElement(name = "VersionName")
    private java.lang.String VersionName;
    @XmlElement(name = "Code")
    private java.lang.String Code;

    public java.lang.Long getCodeSystemVUID() {
        return this.CodeSystemVUID;
    }

    public void setCodeSystemVUID(java.lang.Long newCodeSystemVUID)  {
        this.CodeSystemVUID = newCodeSystemVUID;
    }

    public java.lang.String getVersionName() {
        return this.VersionName;
    }

    public void setVersionName(java.lang.String newVersionName)  {
        this.VersionName = newVersionName;
    }

    public java.lang.String getCode() {
        return this.Code;
    }

    public void setCode(java.lang.String newCode)  {
        this.Code = newCode;
    }

}

