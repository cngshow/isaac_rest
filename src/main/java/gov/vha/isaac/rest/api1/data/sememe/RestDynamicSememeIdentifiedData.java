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
import gov.vha.isaac.ochre.api.chronicle.ObjectChronologyType;
import gov.vha.isaac.rest.Util;
import gov.vha.isaac.rest.api1.data.RestIdentifiedObject;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;

/**
 * 
 * {@link RestDynamicSememeIdentifiedData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestDynamicSememeNid.class, RestDynamicSememeSequence.class, RestDynamicSememeUUID.class, 
	RestDynamicSememeNid[].class, RestDynamicSememeSequence[].class, RestDynamicSememeUUID[].class})
@JsonSeeAlso ({RestDynamicSememeNid.class, RestDynamicSememeSequence.class, RestDynamicSememeUUID.class, 
	RestDynamicSememeNid[].class, RestDynamicSememeSequence[].class, RestDynamicSememeUUID[].class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestDynamicSememeIdentifiedData extends RestDynamicSememeData
{
	/**
	 * When the data type of the sememe is nid, sequence, or UUID, this usually represents a concept or a sememe in the system.  This field carries 
	 * all of the information about that object, when it is a concept or sememe in the system.  For example, if the data type is nid, the 'data' field will 
	 * be an int that contains the nid.  This filed will contain the (same) nid, but also the sequence, and the data type (concept or sememe).  
	 * 
	 * In some cases, where the data type is a UUID - the UUID may not represent a concept or sememe, in which case, the dataIdentified will carry the UUID, 
	 * and the data type unknown (but sequence and nid would be blank).
	 * 
	 */
	@XmlElement
	private RestIdentifiedObject dataIdentified;
	
	/**
	 * If the dataObjectType represents is a concept, then this carries the "best" description for that concept.  This is selected based on the 
	 * attributes within the session for  stamp and language coordinates - or - if none present - the server default.  This is not populated if the 
	 * dataObjectType is not a concept type.
	 * Only populated when the expand parameter 'referencedDetails' is passed.
	 */
	@XmlElement
	String conceptDescription;

	
	protected RestDynamicSememeIdentifiedData(Integer columnNumber, Object data)
	{
		super(columnNumber, data);
		setTypedData();
	}
	
	protected void setTypedData()
	{
		if (data != null)
		{
			if (data instanceof Integer)
			{
				dataIdentified = new RestIdentifiedObject((int)data);
			}
			else if (data instanceof UUID)
			{
				dataIdentified = new RestIdentifiedObject((UUID)data);
			}
			else
			{
				throw new RuntimeException("Unexpected");
			}
			if (dataIdentified.type.enumId == ObjectChronologyType.CONCEPT.ordinal())
			{
				conceptDescription = Util.readBestDescription(dataIdentified.nid);
			}
		}
	}
	
	protected RestDynamicSememeIdentifiedData()
	{
		//for jaxb
	}
}
