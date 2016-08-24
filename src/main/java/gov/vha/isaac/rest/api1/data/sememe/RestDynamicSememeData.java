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
import javax.xml.bind.annotation.XmlSeeAlso;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.webcohesion.enunciate.metadata.json.JsonSeeAlso;
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
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeBooleanImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeByteArrayImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeDoubleImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeFloatImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeIntegerImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeLongImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNidImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeSequenceImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeStringImpl;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUIDImpl;
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
 * {@link RestDynamicSememeData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlSeeAlso ({RestDynamicSememeArray.class, RestDynamicSememeBoolean.class, RestDynamicSememeByteArray.class, RestDynamicSememeDouble.class, RestDynamicSememeFloat.class,
	RestDynamicSememeInteger.class, RestDynamicSememeLong.class, RestDynamicSememeString.class, RestDynamicSememeTypedData.class, 
	RestDynamicSememeArray[].class, RestDynamicSememeBoolean[].class, RestDynamicSememeByteArray[].class, RestDynamicSememeDouble[].class, 
	RestDynamicSememeFloat[].class, RestDynamicSememeLong[].class, RestDynamicSememeString[].class, RestDynamicSememeData[].class, RestDynamicSememeTypedData[].class})
@JsonSeeAlso ({RestDynamicSememeArray.class, RestDynamicSememeBoolean.class, RestDynamicSememeByteArray.class, RestDynamicSememeDouble.class, RestDynamicSememeFloat.class,
	RestDynamicSememeInteger.class, RestDynamicSememeLong.class, RestDynamicSememeString.class, RestDynamicSememeTypedData.class, 
	RestDynamicSememeArray[].class, RestDynamicSememeBoolean[].class, RestDynamicSememeByteArray[].class, RestDynamicSememeDouble[].class, 
	RestDynamicSememeFloat[].class, RestDynamicSememeLong[].class, RestDynamicSememeString[].class, RestDynamicSememeData[].class, RestDynamicSememeTypedData[].class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@XmlRootElement
public abstract class RestDynamicSememeData
{
	/**
	 * The 0 indexed column number for this data.  Will not be populated for nested RestDynamicSememeData objects where the 'data' field
	 * is of type RestDynamicSememeArray
	 */
	@XmlElement
	public Integer columnNumber;
	
	/**
	 * The data for a column within a RestDynamicSememeVersion instance
	 */
	@XmlElement
	public Object data;
	
	protected RestDynamicSememeData(Integer columnNumber, Object data)
	{
		this.columnNumber = columnNumber;
		this.data = data;
	}
	
	protected RestDynamicSememeData()
	{
		//for jaxb
	}
	
	public static RestDynamicSememeData translate(Integer columnNumber, DynamicSememeData data)
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
					nested.add(translate(columnNumber, nestedDataItem));
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
	
	public static DynamicSememeData[] translate(RestDynamicSememeData[] values)
	{
		if (values == null)
		{
			return null;
		}
		DynamicSememeData[] result = new DynamicSememeData[values.length];
		for (int i = 0; i < values.length; i++)
		{
			result[i] = RestDynamicSememeData.translate(values[i]);
		}
		return result;
	}
	
	public static DynamicSememeData translate(RestDynamicSememeData data)
	{
		if (data == null)
		{
			return null;
		}
		else if (data instanceof RestDynamicSememeArray)
		{
			List<DynamicSememeData> nested = new ArrayList<>();
			for (RestDynamicSememeData nestedDataItem : ((RestDynamicSememeArray)data).getDataArray())
			{
				nested.add(translate(nestedDataItem));
			}
			return new DynamicSememeArrayImpl(nested.toArray(new DynamicSememeData[nested.size()]));
		}
		else if (data instanceof RestDynamicSememeBoolean)
		{
			return new DynamicSememeBooleanImpl(((RestDynamicSememeBoolean)data).getBoolean());
		}
		else if (data instanceof RestDynamicSememeByteArray)
		{
			return new DynamicSememeByteArrayImpl(((RestDynamicSememeByteArray)data).getByteArray());
		}
		else if (data instanceof RestDynamicSememeDouble)
		{
			return new DynamicSememeDoubleImpl(((RestDynamicSememeDouble)data).getDouble());
		}
		
		else if (data instanceof RestDynamicSememeFloat)
		{
			return new DynamicSememeFloatImpl(((RestDynamicSememeFloat)data).getFloat());
		}
		else if (data instanceof RestDynamicSememeInteger)
		{
			return new DynamicSememeIntegerImpl(((RestDynamicSememeInteger)data).getInteger());
		}
		else if (data instanceof RestDynamicSememeLong)
		{
			return new DynamicSememeLongImpl(((RestDynamicSememeLong)data).getLong());
		}
		else if (data instanceof RestDynamicSememeNid)
		{
			return new DynamicSememeNidImpl(((RestDynamicSememeNid)data).getNid());
		}
		else if (data instanceof RestDynamicSememeSequence)
		{
			return new DynamicSememeSequenceImpl(((RestDynamicSememeSequence)data).getSequence());
		}
		else if (data instanceof RestDynamicSememeString)
		{
			return new DynamicSememeStringImpl(((RestDynamicSememeString)data).getString());
		}
		else if (data instanceof RestDynamicSememeUUID)
		{
			return new DynamicSememeUUIDImpl(((RestDynamicSememeUUID)data).getUUID());
		}
		else
		{
			throw new RuntimeException("Programmer error");
		}
	}
}
