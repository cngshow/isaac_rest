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
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeBoolean;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeByteArray;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeDouble;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeFloat;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeInteger;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeLong;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeSequence;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeString;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.dataTypes.DynamicSememeUUID;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeBoolean;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeByteArray;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeDouble;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeFloat;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeInteger;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeNid;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeSequence;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString;
import gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeUUID;

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
	 * The data attached to this sememe instance (if any).  The //TODO definition of the DynamicSememe
	 * can be read to determine the potential types and descriptions of these columns.
	 */
	@XmlElement
	List<RestDynamicSememeData> dataColumns;

	protected RestDynamicSememeVersion()
	{
		//for Jaxb
	}
	
	public RestDynamicSememeVersion(DynamicSememe<?> dsv, boolean includeChronology) throws RestException
	{
		super(dsv, includeChronology);
		dataColumns = translateData(dsv.getData());
	}

	public RestDynamicSememeVersion(StringSememe<?> sv, boolean includeChronology) throws RestException
	{
		super(sv, includeChronology);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeStringImpl(sv.getString())});
	}
	
	public RestDynamicSememeVersion(LongSememe<?> sv, boolean includeChronology) throws RestException
	{
		super(sv, includeChronology);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeLongImpl(sv.getLongValue())});
	}
	
	public RestDynamicSememeVersion(ComponentNidSememe<?> sv, boolean includeChronology) throws RestException
	{
		super(sv, includeChronology);
		dataColumns = translateData(new DynamicSememeData[] {new DynamicSememeNidImpl(sv.getComponentNid())});
	}
	
	public RestDynamicSememeVersion(SememeVersion<?> sv, boolean includeChronology) throws RestException
	{
		super(sv, includeChronology);
		//no data
	}

	private static List<RestDynamicSememeData> translateData(DynamicSememeData[] data)
	{
		if (data != null)
		{
			List<RestDynamicSememeData> translatedData = new ArrayList<>();
			for (int i = 0; i < data.length; i++)
			{
				translatedData.add(translate(i, data[i]));
			}
			return translatedData;
		}
		return null;
	}
	
	private static RestDynamicSememeData translate(Integer columnNumber, DynamicSememeData data)
	{
		if (data == null)
		{
			return null;
		}
		switch (data.getDynamicSememeDataType())
		{
			case ARRAY:
				List<RestDynamicSememeData> nested = new ArrayList<>();
				for (DynamicSememeData nestedDataItem : ((DynamicSememeArray<?>)data).getDataArray())
				{
					nested.add(translate(null, nestedDataItem));
				}
				return new RestDynamicSememeArray(columnNumber, nested.toArray(new RestDynamicSememeData[nested.size()]));
			case BOOLEAN:
				return new RestDynamicSememeBoolean(columnNumber, ((DynamicSememeBoolean)data).getDataBoolean());
			case BYTEARRAY:
				return new RestDynamicSememeByteArray(columnNumber, ((DynamicSememeByteArray)data).getDataByteArray());
			case DOUBLE:
				return new RestDynamicSememeDouble(columnNumber, ((DynamicSememeDouble)data).getDataDouble());
			case FLOAT:
				return new RestDynamicSememeFloat(columnNumber, ((DynamicSememeFloat)data).getDataFloat());
			case INTEGER:
				return new RestDynamicSememeInteger(columnNumber, ((DynamicSememeInteger)data).getDataInteger());
			case LONG:
				return new RestDynamicSememeLong(columnNumber, ((DynamicSememeLong)data).getDataLong());
			case NID:
				return new RestDynamicSememeNid(columnNumber, ((DynamicSememeNid)data).getDataNid());
			case SEQUENCE:
				return new RestDynamicSememeSequence(columnNumber, ((DynamicSememeSequence)data).getDataSequence());
			case STRING:
				return new RestDynamicSememeString(columnNumber, ((DynamicSememeString)data).getDataString());
			case UUID:
				return new RestDynamicSememeUUID(columnNumber, ((DynamicSememeUUID)data).getDataUUID());
			case POLYMORPHIC: case UNKNOWN:
			default :
				throw new RuntimeException("Programmer error");
		}
	}
}
