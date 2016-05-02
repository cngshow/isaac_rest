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

package gov.vha.isaac.rest.api1.data.coordinate;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.coordinate.StampCoordinate;
import gov.vha.isaac.rest.api1.data.enumerations.RestStampPrecedenceType;
import gov.vha.isaac.rest.api1.data.enumerations.RestStateType;

/**
 * 
 * {@link RestStampCoordinate}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestStampCoordinate {
	@XmlElement
	long time;

	@XmlElement
	int path;
	
	@XmlElement
	RestStampPrecedenceType precedence;
	
	@XmlElement
	Set<Integer> modules;
	
	@XmlElement
	Set<RestStateType> allowedStates;
	
	public RestStampCoordinate(StampCoordinate sc) {
		time = sc.getStampPosition().getTime();
		path = sc.getStampPosition().getStampPathSequence();
		precedence = new RestStampPrecedenceType(sc.getStampPrecedence());
		modules = new HashSet<>();
		sc.getModuleSequences().stream().forEach((seq) -> modules.add(seq));
		allowedStates = new HashSet<>();
		sc.getAllowedStates().stream().forEach((state) -> allowedStates.add(new RestStateType(state)));
	}

	protected RestStampCoordinate() {
		// For JAXB
	}
}
