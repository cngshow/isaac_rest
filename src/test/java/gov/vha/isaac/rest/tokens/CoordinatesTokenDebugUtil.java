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

package gov.vha.isaac.rest.tokens;

import gov.vha.isaac.ochre.api.Get;

/**
 * 
 * {@link CoordinatesTokenDebugUtil}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
class CoordinatesTokenDebugUtil {
	private CoordinatesTokenDebugUtil() {}
	
	public static String conceptsToString(int[] sequences) {
		if (sequences == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Concept Array [");
		
		for (int i = 0; i < sequences.length; ++i) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(Get.conceptDescriptionText(sequences[i]));
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	public static String toString(CoordinatesToken token) {
		return "CoordinatesToken [getStampTime()=" + token.getStampTime() + ", getStampPath()=" + Get.conceptDescriptionText(token.getStampPath())
		+ ", getStampPrecedence()=" + token.getStampPrecedence() + ", getStampModules()=" + token.getStampModules()
		+ ", getStampStates()=" + token.getStampStates() + ", getLangCoord()=" + Get.conceptDescriptionText(token.getLangCoord())
		+ ", getLangDialects()=" + conceptsToString(token.getLangDialects()) + ", getLangDescTypePrefs()=" + conceptsToString(token.getLangDescTypePrefs())
		+ ", getTaxonomyType()=" + token.getTaxonomyType() + ", getLogicStatedAssemblage()="
		+ Get.conceptDescriptionText(token.getLogicStatedAssemblage()) + ", getLogicInferredAssemblage()=" + Get.conceptDescriptionText(token.getLogicInferredAssemblage())
		+ ", getLogicDescLogicProfile()=" + token.getLogicDescLogicProfile() + ", getLogicClassifier()="
		+ Get.conceptDescriptionText(token.getLogicClassifier()) + ", getSerialized()=" + token.getSerialized() + "]";
	}
}
