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

import javax.xml.bind.annotation.XmlElement;
import gov.vha.isaac.ochre.api.State;
import gov.vha.isaac.ochre.api.identity.StampedVersion;

/**
 * 
 * {@link RestStampedVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
public class RestStampedVersion
{
	/**
	 * The stamp sequence of this version
	 */
	@XmlElement
	int stampSequence;
	
	/**
	 * The State of this version (active, inactive, primordial or cancelled)
	 */
	@XmlElement
	State state;
	
	/**
	 * The time stamp of this version (in standard java form)
	 */
	@XmlElement
	long time;
	
	/**
	 * The concept sequence of the concept that identifies the author of this version 
	 */
	@XmlElement
	int authorSequence;
	
	/**
	 * The concept sequence of the module that this version is in
	 */
	@XmlElement
	int moduleSequence;
	
	/**
	 * The concept sequence of the path that this version is in
	 */
	@XmlElement
	int pathSequence;

	public RestStampedVersion(StampedVersion sv)
	{
		stampSequence = sv.getStampSequence();
		state = sv.getState();
		time = sv.getTime();
		authorSequence = sv.getAuthorSequence();
		pathSequence = sv.getPathSequence();
	}

}
