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
package gov.vha.isaac.rest.api1.data;

import java.util.UUID;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.commit.Stamp;
import gov.vha.isaac.ochre.api.identity.StampedVersion;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;

/**
 * 
 * {@link RestStampedVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestStampedVersion
{
	/**
	 * The State of this version (active, inactive, primordial or cancelled)
	 */
	@XmlElement
	public RestStateType state;
	
	/**
	 * The time stamp of this version (in standard java form)
	 */
	@XmlElement
	public long time;
	
	/**
	 * The UUID of the concept that identifies the author of this version 
	 */
	@XmlElement
	public UUID author;
	
	/**
	 * The UUID of the concept that identifies the module that this version is in
	 */
	@XmlElement
	public UUID module;
	
	/**
	 * The UUID of the concept that identifies the path that this version is in
	 */
	@XmlElement
	public UUID path;

	@XmlTransient
	public RestStateType getState() {
		return state;
	}

	RestStampedVersion() {
		// For JAXB only
	}
	
	public RestStampedVersion(StampedVersion sv)
	{
		state = new RestStateType(sv.getState());
		time = sv.getTime();
		author = Get.identifierService().getUuidPrimordialFromConceptId(sv.getAuthorSequence()).get();
		path = Get.identifierService().getUuidPrimordialFromConceptId(sv.getPathSequence()).get();
		module = Get.identifierService().getUuidPrimordialFromConceptId(sv.getModuleSequence()).get();
	}
	public RestStampedVersion(Stamp s)
	{
		state = new RestStateType(s.getStatus());
		time = s.getTime();
		author = Get.identifierService().getUuidPrimordialFromConceptId(s.getAuthorSequence()).get();
		path = Get.identifierService().getUuidPrimordialFromConceptId(s.getPathSequence()).get();
		module = Get.identifierService().getUuidPrimordialFromConceptId(s.getModuleSequence()).get();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestStampedVersion [state=" + state + ", time=" + time
				+ ", author=" + author + ", module=" + module + ", path="
				+ path + "]";
	}
}
