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
	RestDynamicSememeInteger.class, RestDynamicSememeLong.class, RestDynamicSememeString.class, RestDynamicSememeIdentifiedData.class, 
	RestDynamicSememeArray[].class, RestDynamicSememeBoolean[].class, RestDynamicSememeByteArray[].class, RestDynamicSememeDouble[].class, 
	RestDynamicSememeFloat[].class, RestDynamicSememeLong[].class, RestDynamicSememeString[].class, RestDynamicSememeData[].class, RestDynamicSememeIdentifiedData[].class})
@JsonSeeAlso ({RestDynamicSememeArray.class, RestDynamicSememeBoolean.class, RestDynamicSememeByteArray.class, RestDynamicSememeDouble.class, RestDynamicSememeFloat.class,
	RestDynamicSememeInteger.class, RestDynamicSememeLong.class, RestDynamicSememeString.class, RestDynamicSememeIdentifiedData.class, 
	RestDynamicSememeArray[].class, RestDynamicSememeBoolean[].class, RestDynamicSememeByteArray[].class, RestDynamicSememeDouble[].class, 
	RestDynamicSememeFloat[].class, RestDynamicSememeLong[].class, RestDynamicSememeString[].class, RestDynamicSememeData[].class, RestDynamicSememeIdentifiedData[].class})
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
	 * This implementation sorts by the column number
	 * 
	 * @param values
	 * @return
	 * @throws RestException
	 */
	public static DynamicSememeData[] translate(RestDynamicSememeData[] values) throws RestException {
		return translate(values, false);
	}
	/**
	 * 
	 * This implementation sorts by the column number, and, if nullPadGaps is true, fills gaps (with nulls) as appropriate,
	 * as determined by the passed in column numbers
	 * 
	 * @param values
	 * @param nullPadGaps
	 * @return
	 * @throws RestException
	 */
	public static DynamicSememeData[] translate(RestDynamicSememeData[] values, boolean nullPadGaps) throws RestException
	{
		if (values == null)
		{
			return null;
		}
		
		sort(values);
		
		List<DynamicSememeData> result = new ArrayList<DynamicSememeData>();
		
		for (RestDynamicSememeData rdsd : values)
		{
			if (nullPadGaps) {
				while (result.size() < rdsd.columnNumber.intValue())
				{
					result.add(null);  //fill a gap
				}
				if (result.size() == rdsd.columnNumber.intValue() || rdsd.columnNumber.intValue() < 0)
				{
					result.add(RestDynamicSememeData.translate(rdsd));
				}
				else
				{
					throw new RuntimeException("Dan needs more sleep");
				}
			} else {
				result.add(RestDynamicSememeData.translate(rdsd));
			}
		}
		return result.toArray(new DynamicSememeData[result.size()]);
	}
	
	public static void sort(RestDynamicSememeData[] values) throws RestException
	{
		if (values == null)
		{
			return;
		}
		
		int nextColNum = 0;
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == null)
			{
				//Put in an arbitrary filler, so we don't null pointer below.  Assume the null was in the correct position.
				values[i] = new RestDynamicSememeString(nextColNum++, null);
			}
			else
			{
				if (values[i].columnNumber == null)
				{
					throw new RestException("The field 'columnNumber' must be populated in the RestDynamicSememeData");
				}
				nextColNum = values[i].columnNumber + 1;
			}
		}
		
		//Sort the values by column number
		Arrays.sort(values, new Comparator<RestDynamicSememeData>()
		{
			@Override
			public int compare(RestDynamicSememeData o1, RestDynamicSememeData o2)
			{
				if (o1.columnNumber == o2.columnNumber && o1.columnNumber >= 0)
				{
					throw new RuntimeException("The field 'columnNumber' contained a duplicate");
				}
				return o1.columnNumber.compareTo(o2.columnNumber);
			}
		});
	}
	
	
	/**
	 * If you are translating an array of data, you should use the {@link #translate(RestDynamicSememeData[])} method instead, as that handles 
	 * honoring column number and gaps properly.
	 * @param data
	 * @return
	 */
	public static DynamicSememeData translate(RestDynamicSememeData data)
	{
		if (data == null)
		{
			return null;
		}
		else if (data.data == null)
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnNumber == null) ? 0 : columnNumber.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestDynamicSememeData other = (RestDynamicSememeData) obj;
		if (columnNumber == null) {
			if (other.columnNumber != null)
				return false;
		} else if (!columnNumber.equals(other.columnNumber))
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestDynamicSememeData [columnNumber=" + columnNumber + ", data=" + data + "]";
	}
}
