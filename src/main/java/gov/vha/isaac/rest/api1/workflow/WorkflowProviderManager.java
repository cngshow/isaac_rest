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

package gov.vha.isaac.rest.api1.workflow;

import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowActionsPermissionsAccessor;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowHistoryAccessor;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowProcessInitializerConcluder;
import gov.vha.isaac.ochre.workflow.provider.crud.WorkflowStatusAccessor;

/**
 * 
 * {@link WorkflowProviderManager}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class WorkflowProviderManager {
	private WorkflowProviderManager() {}

	// TODO implement getWorkflowActionsPermissionsAccessor()
	public static WorkflowActionsPermissionsAccessor getWorkflowActionsPermissionsAccessor() { return null; }
	// TODO implement getWorkflowHistoryAccessor()
	public static WorkflowHistoryAccessor getWorkflowHistoryAccessor() { return null; }
	// TODO implement getWorkflowStatusAccessor()
	public static WorkflowStatusAccessor getWorkflowStatusAccessor() { return null; }
	
	// TODO implement getWorkflowProcessInitializerConcluder()
	public static WorkflowProcessInitializerConcluder getWorkflowProcessInitializerConcluder() { return null; }
}
