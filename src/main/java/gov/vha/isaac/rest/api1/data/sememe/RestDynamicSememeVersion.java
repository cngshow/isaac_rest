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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import gov.vha.isaac.ochre.api.component.sememe.version.ComponentNidSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.DynamicSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LongSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.component.sememe.version.StringSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeData;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RestDynamicSememeVersion}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
public class RestDynamicSememeVersion extends RestSememeVersion
{
	/**
	 * The data attached to this sememe instance (if any).  The 'sememe/sememeDefinition/{id}'
	 * can be read to determine the potential types and descriptions of these columns.
	 */
	@XmlElement
	List<RestDynamicSememeData> dataColumns;

	protected RestDynamicSememeVersion()
	{
		//for Jaxb
	}
	
	public RestDynamicSememeVersion(DynamicSememe<?> dsv, boolean includeChronology, boolean expandNested) throws RestException
	{
		super(dsv, includeChronology, expandNested, null);
		dataColumns = translateData(dsv.getData());
	}

	public RestDynamicSememeVersion(StringSememe<?> sv, boolean includeChronology, boolean expandNested) throws RestException
	{
		super(sv, includeChronology, expandNested, null);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeStringImpl(sv.getString())});
	}
	
	public RestDynamicSememeVersion(LongSememe<?> sv, boolean includeChronology, boolean expandNested) throws RestException
	{
		super(sv, includeChronology, expandNested, null);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeLongImpl(sv.getLongValue())});
	}
	
	public RestDynamicSememeVersion(ComponentNidSememe<?> sv, boolean includeChronology, boolean expandNested) throws RestException
	{
		super(sv, includeChronology, expandNested, null);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeNidImpl(sv.getComponentNid())});
	}
	
	public RestDynamicSememeVersion(SememeVersion<?> sv, boolean includeChronology, boolean expandNested) throws RestException
	{
		super(sv, includeChronology, expandNested, null);
		//no data
	}

	private static List<RestDynamicSememeData> translateData(DynamicSememeData[] data)
	{
		if (data != null)
		{
			List<RestDynamicSememeData> translatedData = new ArrayList<>();
			for (int i = 0; i < data.length; i++)
			{
				translatedData.add(RestDynamicSememeData.translate(i, data[i]));
			}
			return translatedData;
		}
		return null;
	}
}
