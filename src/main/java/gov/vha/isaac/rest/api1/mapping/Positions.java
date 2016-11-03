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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.rest.api1.mapping;

import gov.vha.isaac.ochre.api.component.sememe.version.dynamicSememe.DynamicSememeUsageDescription;
import gov.vha.isaac.ochre.api.constants.DynamicSememeConstants;
import gov.vha.isaac.ochre.mapping.constants.IsaacMappingConstants;
import gov.vha.isaac.ochre.model.sememe.DynamicSememeUsageDescriptionImpl;
import gov.vha.isaac.rest.api.exceptions.RestException;

class Positions
{
	protected int targetPos;
	protected int qualfierPos;

	private Positions(int targetPos, int qualifierPos)
	{
		this.targetPos = targetPos;
		this.qualfierPos = qualifierPos;
	}
	
	
	/**
	 * @param sememeAssemblageConceptSequence
	 * @return
	 * @throws RestException 
	 */
	protected static Positions getPositions(int sememeAssemblageConceptSequence) throws RestException
	{
		int targetPos = -1;
		int qualifierPos = -1;
		
		DynamicSememeUsageDescription dsud = DynamicSememeUsageDescriptionImpl.read(sememeAssemblageConceptSequence);
		for (int i = 0; i < dsud.getColumnInfo().length; i++)
		{
			if (dsud.getColumnInfo()[i].getColumnDescriptionConcept().equals(DynamicSememeConstants.get().DYNAMIC_SEMEME_COLUMN_ASSOCIATION_TARGET_COMPONENT.getPrimordialUuid()))
			{
				targetPos = i;
			}
			else if (dsud.getColumnInfo()[i].getColumnDescriptionConcept().equals(IsaacMappingConstants.get().DYNAMIC_SEMEME_COLUMN_MAPPING_QUALIFIER.getPrimordialUuid()))
			{
				qualifierPos = i;
			}
			if (targetPos >= 0 && qualifierPos >= 0)
			{
				break;
			}
		}
		if (targetPos < 0 || qualifierPos < 0)
		{
			throw new RestException("The specified sememe doesn't appear to be configured correctly as a mapset");
		}
		return new Positions(targetPos, qualifierPos);
	}
}