/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.data.sememe;

import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.webcohesion.enunciate.metadata.json.JsonSeeAlso;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api1.data.enumerations.RestObjectChronologyType;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;

/**
 * 
 * {@link RestDynamicSememeTypedData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestDynamicSememeNid.class, RestDynamicSememeSequence.class, RestDynamicSememeUUID.class, 
	RestDynamicSememeNid[].class, RestDynamicSememeSequence[].class, RestDynamicSememeUUID[].class})
@JsonSeeAlso ({RestDynamicSememeNid.class, RestDynamicSememeSequence.class, RestDynamicSememeUUID.class, 
	RestDynamicSememeNid[].class, RestDynamicSememeSequence[].class, RestDynamicSememeUUID[].class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestDynamicSememeTypedData extends RestDynamicSememeData
{
	/**
	 * The type of the object that is referenced by the data.  This would tell you if the (nid, sequence or UUID) represents a concept or a sememe (or unknown).
	 * Especially in the case of a RestDynamicSememeSequence, the value may come back as unknown - the caller will have to refer to the documentation of the sememe
	 * to determine the actual type of the data stored here in those cases.
	 */
	@XmlElement
	private RestObjectChronologyType dataObjectType;
	
	/**
	 * If the dataObjectType represents is a concept, then this carries the "best" description for that concept.  This is selected based on the 
	 * attributes within the session for  stamp and language coordinates - or - if none present - the server default.  This is not populated if the 
	 * dataObjectType is not a concept type.
	 * Only populated when the expand parameter 'referencedDetails' is passed.
	 */
	@XmlElement
	String conceptDescription;

	
	protected RestDynamicSememeTypedData(Integer columnNumber, Object data, ObjectChronologyType dataType)
	{
		super(columnNumber, data);
		setTypedData(dataType);
	}
	
	protected void setTypedData(ObjectChronologyType dataType)
	{
		dataObjectType = new RestObjectChronologyType(dataType);
		if (dataObjectType.enumId == ObjectChronologyType.CONCEPT.ordinal())
		{
			int nid;
			if (data instanceof Integer)
			{
				if ((int)data < 0)
				{
					nid = (int)data;
				}
				else
				{
					nid = Get.identifierService().getConceptNid((int)data);
				}
			}
			else if (data instanceof UUID)
			{
				nid = Get.identifierService().getNidForUuids((UUID)data);
			}
			else
			{
				throw new RuntimeException("Unexpected");
			}
			conceptDescription = Util.readBestDescription(nid);
		}
	}
	
	protected RestDynamicSememeTypedData()
	{
		//for jaxb
	}
}
