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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeColumnInfo;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeDataType;
import gov.vha.isaac.rest.api1.data.enumerations.RestDynamicSememeValidatorType;

/**
 * 
 * {@link RestDynamicSememeColumnInfo}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class RestDynamicSememeColumnInfo
{
	protected RestDynamicSememeColumnInfo()
	{
		//for jaxb
	}
	
	public RestDynamicSememeColumnInfo(DynamicSememeColumnInfo dsci)
	{
		this.columnConceptSequence = Get.identifierService().getConceptSequenceForUuids(dsci.getColumnDescriptionConcept());
		this.columnDataType = new RestDynamicSememeDataType(dsci.getColumnDataType());
		this.columnDefaultData = dsci.getDefaultColumnValue() == null ? null : RestDynamicSememeData.translate(dsci.getColumnOrder(), dsci.getDefaultColumnValue());
		this.columnDescription = dsci.getColumnDescription();
		this.columnName = dsci.getColumnName();
		this.columnOrder = dsci.getColumnOrder();
		this.columnRequired = dsci.isColumnRequired();
		this.columnValidatorData = dsci.getValidatorData() == null ? null : new RestDynamicSememeData[dsci.getValidatorData().length];
		if (this.columnValidatorData != null)
		{
			for (int i = 0; i < dsci.getValidatorData().length; i++)
			{
				this.columnValidatorData[i] = RestDynamicSememeData.translate(dsci.getColumnOrder(), dsci.getValidatorData()[i]);
			}
		}
		this.columnValidatorTypes = dsci.getValidator() == null ? null : new RestDynamicSememeValidatorType[dsci.getValidator().length];
		if (this.columnValidatorTypes != null)
		{
			for (int i = 0; i < dsci.getValidatorData().length; i++)
			{
				this.columnValidatorTypes[i] = new RestDynamicSememeValidatorType(dsci.getValidator()[i]);
			}
		}
	}

	/**
	 * The concept sequence number of the concept that represents the column within the dynamic sememe.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public int columnConceptSequence;
	
	/**
	 * The user-friendly name to display for this column.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String columnName;
	
	/**
	 * The user friendly description for this column.  Suitable for things like tooltip descriptions.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String columnDescription;
	
	/**
	 * The 0 indexed order of this column within the dynamic sememe.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public int columnOrder;
	
	/**
	 * The type of data that will be found in this column.  String, Integer, etc.  See 
	 * rest/1/enumeration/restDynamicSememeDataType for a list of all of the possible data types.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeDataType columnDataType;
	
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
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public boolean columnRequired;
	
	/**
	 * The validators types that are attached to this sememe (if any).  Interval, <, etc.  See 
	 * rest/1/enumeration/restDynamicSememeValidatorType for a list of all possible validator types.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeValidatorType[] columnValidatorTypes;
	
	/**
	 * The data required to execute the validator type specified in columnValidatorTypes.  The format and type of this field
	 * will depend on the columnValidatorTypes field.  The positions within this array will match with the columnValidatorTypes
	 * array.
	 */
	@XmlElement
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public RestDynamicSememeData[] columnValidatorData;
}
