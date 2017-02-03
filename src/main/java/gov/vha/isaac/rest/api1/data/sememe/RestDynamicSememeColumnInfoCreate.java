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

import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;

/**
 * 
 * {@link RestDynamicSememeColumnInfoCreate}
 * 
 * This class is used to pass the information needed to specify a new dynamic sememe column
 * during sememe definition.
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestDynamicSememeColumnInfoCreate.class)
public class RestDynamicSememeColumnInfoCreate
{
	/**
	 * The concept identifier (uuid, nid or sequence) of the concept that represents the column within the dynamic sememe.
	 */
	@XmlElement
	@JsonInclude
	public String columnLabelConcept;
	
	/**
	 * The data type of the value to be stored in this column.  
	 * 
	 * The value passed here can be the value provided by {@link RestDynamicSememeDataType#name} or {@link RestDynamicSememeDataType#enumId}.  
	 * To retrieve the valid RestDynamicSememeDataType types, call 1/system/enumeration/restDynamicSememeDataType/
	 * 
	 * The typical values for this parameter would be "STRING" or "LONG"
	 */
	@XmlElement
	@JsonInclude
	public String columnDataType;
	
	/**
	 * The default value to use for this column when creating a new sememe (if no user value is specified).
	 * This field is optional and may be null.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeData columnDefaultData;
	
	/**
	 * Does the user have to provide a value for this column in order to create an instance of this sememe.
	 */
	@XmlElement
	@JsonInclude
	public boolean columnRequired;
	
	/**
	 * The validators types that are attached to this sememe (if any).  Interval, <, etc. 
	 * 
	 * The value passed here can be the value provided by {@link RestDynamicSememeValidatorType#name} or {@link RestDynamicSememeValidatorType#enumId}.  
	 * To retrieve the valid RestDynamicSememeValidatorType types, call 1/system/enumeration/restDynamicSememeValidatorType/
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String[] columnValidatorTypes;
	
	/**
	 * The data required to execute the validator type specified in columnValidatorTypes.  The format and type of this field
	 * will depend on the columnValidatorTypes field.  The positions within this array will match with the columnValidatorTypes
	 * array.  This optional field should only be populated if the columnValidatorTypes is populated.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeData[] columnValidatorData;

	
	public RestDynamicSememeColumnInfoCreate()
	{
		//for jaxb
	}
	
	/**
	 * @param columnConceptLabelConcept The concept sequence number of the concept that represents the column within the dynamic sememe.  This returns a sequence, 
	 * but for creation purposes, can accept a sequence or a nid.
	 * @param columnDataType The type of data that will be found in this column.  String, Integer, etc.  See 
	 * rest/1/enumeration/restDynamicSememeDataType for a list of all of the possible data types.
	 * @param columnDefaultData The default value to use for this column when creating a new sememe (if no user value is specified).
	 * This field is optional and may be null.
	 * @param columnRequired Does the user have to provide a value for this column in order to create an instance of this sememe.
	 * @param columnValidatorTypes The validators types that are attached to this sememe (if any).  Interval, <, etc.  See 
	 * rest/1/enumeration/restDynamicSememeValidatorType for a list of all possible validator types.
	 * @param columnValidatorData The data required to execute the validator type specified in columnValidatorTypes.  The format and type of this field
	 * will depend on the columnValidatorTypes field.  The positions within this array will match with the columnValidatorTypes
	 * array.  This optional field should only be populated if the columnValidatorTypes is populated.
	 */
	public RestDynamicSememeColumnInfoCreate(String columnConceptLabelConcept, RestDynamicSememeDataType columnDataType, RestDynamicSememeData columnDefaultData, 
			boolean columnRequired, RestDynamicSememeValidatorType[] columnValidatorTypes, RestDynamicSememeData[] columnValidatorData)
	{
		this.columnLabelConcept = columnConceptLabelConcept;
		this.columnDataType = columnDataType.enumName;
		this.columnDefaultData = columnDefaultData;
		this.columnRequired = columnRequired;
		if (columnValidatorTypes != null)
		{
			this.columnValidatorTypes = new String[columnValidatorTypes.length];
			for (int i = 0; i < columnValidatorTypes.length; i++)
			{
				this.columnValidatorTypes[i] = columnValidatorTypes[i].enumName;
			}
		}
		else
		{
			this.columnValidatorTypes = null;
		}
		this.columnValidatorData = columnValidatorData;
	}
	
	/**
	 * equivalent to:
	 * {@link #RestDynamicSememeColumnInfoCreate(int, RestDynamicSememeDataType, RestDynamicSememeData, boolean, RestDynamicSememeValidatorType[], RestDynamicSememeData[])}
	 * with RestDynamicSememeColumnInfoCreate(int, RestDynamicSememeDataType, RestDynamicSememeData, boolean, null, null)}
	 */
	public RestDynamicSememeColumnInfoCreate(String columnConceptLabelConcept, RestDynamicSememeDataType columnDataType, RestDynamicSememeData columnDefaultData, 
			boolean columnRequired)
	{
		this(columnConceptLabelConcept, columnDataType, columnDefaultData, columnRequired, null, null);
	}
	
	/**
	 * equivalent to:
	 * {@link #RestDynamicSememeColumnInfoCreate(int, RestDynamicSememeDataType, RestDynamicSememeData, boolean, RestDynamicSememeValidatorType[], RestDynamicSememeData[])}
	 * with RestDynamicSememeColumnInfoCreate(int, RestDynamicSememeDataType, null, boolean, null, null)}
	 */
	public RestDynamicSememeColumnInfoCreate(String columnConceptLabelConcept, RestDynamicSememeDataType columnDataType, boolean columnRequired)
	{
		this(columnConceptLabelConcept, columnDataType, null, columnRequired, null, null);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RestDynamicSememeColumnInfoCreate [columnLabelConcept=" + columnLabelConcept + ", columnDataType="
				+ columnDataType + ", columnDefaultData=" + columnDefaultData + ", columnRequired=" + columnRequired
				+ ", columnValidatorTypes=" + Arrays.toString(columnValidatorTypes) + ", columnValidatorData="
				+ Arrays.toString(columnValidatorData) + "]";
	}
}
