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
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.MetaData;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.chronicle.LatestVersion;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import gov.vha.isaac.rest.session.RequestInfo;

/**
 * {@link RestUserInfo}
 * 
 * This class carries back various system information about this deployment.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 * 
 * 
 */
@XmlRootElement
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestUserInfo
{
	private transient static Logger log = LogManager.getLogger();
	/**
	 * The UUID(s) that identify the concept which is a placeholder for a user in the system.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestIdentifiedObject userId;
	
	/**
	 * The unique name of the user, used as the FSN on the concept that represents the user.
	 * This value was created from the information passed in via the SSO authentication.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String uniqueName;
	
	
	/**
	 * The user-preferred name of the current user.  This may be identical to the unique name, or it may have been customized by the user.
	 * 
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String preferredName;
	
	
	protected RestUserInfo()
	{
		// for jaxb
	}
	/**
	 * @param convertToConceptSequence
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public RestUserInfo(int conceptNid)
	{
		userId = new RestIdentifiedObject(Get.identifierService().getUuidsForNid(conceptNid));
		Get.sememeService().getDescriptionsForComponent(conceptNid).forEach(description -> 
		{
			DescriptionSememe ds = ((LatestVersion<DescriptionSememeImpl>)((SememeChronology)description)
					.getLatestVersion(DescriptionSememeImpl.class, RequestInfo.get().getStampCoordinate()).get()).value();
			
			if (ds.getDescriptionTypeConceptSequence() == MetaData.FULLY_SPECIFIED_NAME.getConceptSequence())
			{
				uniqueName = ds.getText();
			}
			else if (ds.getDescriptionTypeConceptSequence() == MetaData.SYNONYM.getConceptSequence())
			{
				preferredName = ((LatestVersion<DescriptionSememeImpl>)((SememeChronology)description)
						.getLatestVersion(DescriptionSememeImpl.class, RequestInfo.get().getStampCoordinate()).get()).value().getText();
			}
		});
		
		if (StringUtils.isBlank(uniqueName) || StringUtils.isBlank(preferredName))
		{
			log.warn("Error reading description(s) for user concept " + conceptNid);
		}
	}
}
