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
package gov.vha.isaac.rest.api1.data.mapping;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import gov.vha.isaac.rest.api.exceptions.RestException;

/**
 * 
 * {@link RestMappingSetFieldCreate}
 * 
 * This stub class is used for callers as part of creating or editing {@link RestMappingSetExtensionValue} objects.  It only contains the fields they may be edited 
 * after creation.
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, defaultImpl=RestMappingSetFieldCreate.class)
public class RestMappingSetFieldCreate extends RestMappingSetFieldBase
{	
	public RestMappingSetFieldCreate()
	{
		//for Jaxb
		super();
	}

	public RestMappingSetFieldCreate(String nameOrConceptIdentifier) throws RestException {
		this(nameOrConceptIdentifier, (Boolean)null);
	}
	public RestMappingSetFieldCreate(String nameOrConceptIdentifier, boolean source) throws RestException {
		this(nameOrConceptIdentifier, Boolean.valueOf(source));
	}

	private RestMappingSetFieldCreate(String nameOrConceptIdentifier, Boolean source) throws RestException
	{
		//for Jaxb
		super(nameOrConceptIdentifier, source);
	}
}
