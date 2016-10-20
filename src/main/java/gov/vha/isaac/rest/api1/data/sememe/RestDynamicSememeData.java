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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
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
 * {@link RestDynamicSememeData}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
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
	 * is of type RestDynamicSememeArray.  This field MUST be provided during during a sememe create or update, and it takes priority over 
	 * the ordering of fields in an array of columns.  Also may be irrelevant in cases where setting DefaultData, or ValidatorData.  
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer columnNumber;
	
	/**
	 * The data for a column within a RestDynamicSememeVersion instance.  The data type of this field depends on the type of class that extends 
	 * this abstract class.  The mapping of types is: <ClassType> - <Java Data Type>
	 * 
	 * - RestDynamicSememeBoolean - boolean
	 * - RestDynamicSememeByteArray - byte[]
	 * - RestDynamicSememeDouble - double
	 * - RestDynamicSememeFloat - float
	 * - RestDynamicSememeInteger - int 
	 * - RestDynamicSememeLong - long
	 * - RestDynamicSememeString - string
	 * - RestDynamicSememeNid - int
	 * - RestDynamicSememeSequence - int
	 * - RestDynamicSememeUUID - UUID
	 * - RestDynamicSememeArray - An array of one of the above types
	 * 
	 * The data type as returned via the REST interface will be typed however the JSON or XML serializer handles the java types. 
	 * 
	 * When using this class in a create or update call, a special annotation must be included to create the proper type of {@link RestDynamicSememeData}
	 * because {@link RestDynamicSememeData} is an abstract type. 
	 * 
	 *  For the server to deserialize the type properly, a field must be included of the form "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.CLASSTYPE"
	 * 
	 * where CLASSTYPE is one of:
	 * - RestDynamicSememeBoolean
	 * - RestDynamicSememeByteArray
	 * - RestDynamicSememeDouble
	 * - RestDynamicSememeFloat
	 * - RestDynamicSememeInteger,
	 * - RestDynamicSememeLong,
	 * - RestDynamicSememeString,
	 * - RestDynamicSememeNid
	 * - RestDynamicSememeSequence
	 * - RestDynamicSememeUUID
	 * - RestDynamicSememeArray
	 * 
	 * Example JSON that provides two columns of differing types:
	 * 
	 * ...
	 *   "restDynamicSememeDataArrayField": [{
	 *     "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeString",
		    "data": "test"
	 *   }, {
	 *     "@class": "gov.vha.isaac.rest.api1.data.sememe.dataTypes.RestDynamicSememeLong",
	 *     "data": 5
	 *   }]
	 * }

	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
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
	
	/**
	 * This implementation sorts by the column number, and fills gaps as appropriate.
	 */
	public static DynamicSememeData[] translate(RestDynamicSememeData[] values) throws RestException
	{
		if (values == null)
		{
			return null;
		}
		
		//Sort the values by column number, identify the largest col number in case there are gaps (optional columns left blank)
		//and fill in the gaps as appropriate.
		try
		{
			Arrays.sort(values, new Comparator<RestDynamicSememeData>()
			{
				@Override
				public int compare(RestDynamicSememeData o1, RestDynamicSememeData o2)
				{
					if (o1.columnNumber == null || o2.columnNumber == null)
					{
						throw new RuntimeException("The field 'columnNumber' must be populated in the RestDynamicSememeData");
					}
					return o1.columnNumber.compareTo(o2.columnNumber);
				}
			});
		}
		catch (RuntimeException e)
		{
			throw new RestException(e.getMessage());
		}
		
		//size 1 isn't caught in the sort check above
		if (values.length == 1 && values[0].columnNumber == null)
		{
			throw new RuntimeException("The field 'columnNumber' must be populated in the RestDynamicSememeData");
		}
		
		int maxColumn = values.length == 0 ? 0 : (values[values.length - 1].columnNumber + 1);
		
		//There are some cases where the column number doesn't make sense (dynamicSememeArray type, validator data, etc) where a negative number might be passed
		if (maxColumn <= 0)
		{
			maxColumn = values.length;
		}
		
		DynamicSememeData[] result = new DynamicSememeData[maxColumn];
		int readPos = 0;
		for (int translated = 0; translated < maxColumn; translated++)
		{
			if (translated == values[readPos].columnNumber.intValue() || values[readPos].columnNumber.intValue() < 0)
			{
				result[translated] = RestDynamicSememeData.translate(values[readPos]);
				readPos++;
			}
			else
			{
				result[translated] = null;  //fill a gap
			}
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
			return new DynamicSememeArrayImpl<>(nested.toArray(new DynamicSememeData[nested.size()]));
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
