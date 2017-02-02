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

package gov.vha.isaac.rest.api1.data.enumerations;

/**
 * 
 * {@link MapSetItemComponent}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public enum MapSetItemComponent {
	SOURCE, TARGET, EQUIVALENCE_TYPE, // Attached sememes: i.e. VUID, CODE, SCT_ID, LOINC_NUM, RXCUI, FSN, PT
	ITEM_EXTENDED // mapItemExtendedFields column numbers: i.e. 0, 1, 2, 3
}
