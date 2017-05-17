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

package gov.vha.isaac.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * {@link HashCodeUtils}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
public class HashCodeUtils {
	private HashCodeUtils() {}
	
	/**
	 * Return non null ordered list of values contained in passed array
	 * 
	 * @param array
	 * @return
	 */
	public static <T> List<T> getValues(T[] array) {
		List<T> list = new ArrayList<>();
		if (array != null) {
			for (T item : array) {
				list.add(item);
			}
		}
		
		return Collections.unmodifiableList(list);
	}
	/**
	 * Return non null ordered list of values contained in passed iterable object
	 * 
	 * @param array
	 * @return
	 */
	public static <T> List<T> getValues(Iterable<T> collection) {
		List<T> list = new ArrayList<>();
		if (collection != null) {
			for (Iterator<T> iter = collection.iterator(); iter.hasNext();) {
				list.add(iter.next());
			}
		}
		
		return Collections.unmodifiableList(list);
	}

	/**
	 * Return non null unordered set of unique values contained in passed array
	 * 
	 * @param array
	 * @return
	 */
	public static <T> Set<T> getUniqueValues(T[] array) {
		Set<T> set = new HashSet<>();
		if (array != null) {
			for (T item : array) {
				set.add(item);
			}
		}
		
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Return non null unordered set of unique values contained in passed iterable object
	 * 
	 * @param array
	 * @return
	 */
	public static <T> Set<T> getUniqueValues(Iterable<T> collection) {
		Set<T> set = new HashSet<>();
		if (collection != null) {
			for (Iterator<T> iter = collection.iterator(); iter.hasNext();) {
				set.add(iter.next());
			}
		}
		
		return Collections.unmodifiableSet(set);
	}

	/**
	 * Return non null ordered list of unique values contained in passed array
	 * 
	 * @param array
	 * @return
	 */
	public static <T> List<T> getOrderedUniqueValues(T[] array) {
		List<T> list = new ArrayList<>();
		if (array != null) {
			for (T item : array) {
				if (! list.contains(item)) {
					list.add(item);
				}
			}
		}
		
		return Collections.unmodifiableList(list);
	}
	/**
	 * Return non null ordered list of unique values contained in passed iterable object
	 * 
	 * @param array
	 * @return
	 */
	public static <T> List<T> getOrderedUniqueValues(Iterable<T> collection) {
		List<T> list = new ArrayList<>();
		if (collection != null) {
			for (Iterator<T> iter = collection.iterator(); iter.hasNext();) {
				T item = iter.next();
				if (! list.contains(item)) {
					list.add(item);
				}
			}
		}
		
		return Collections.unmodifiableList(list);
	}

	public static <T> boolean orderedUniqueEquals(Iterable<T> collection1, Iterable<T> collection2) {
		return getOrderedUniqueValues(collection1).equals(getOrderedUniqueValues(collection2));
	}
	public static <T> boolean orderedUniqueEquals(T[] array1, T[] array2) {
		return getOrderedUniqueValues(array1).equals(getOrderedUniqueValues(array2));
	}

	public static <T> boolean equals(Iterable<T> collection1, Iterable<T> collection2) {
		return getValues(collection1).equals(getValues(collection2));
	}
	public static boolean equals(Object[] array1, Object[] array2) {
		return getValues(array1).equals(getValues(array2));
	}

	/**
	 * return equivalence, taking into account null values
	 * 
	 * @param obj1
	 * @param obj2
	 * @return
	 */
	public static boolean equals(Object obj1, Object obj2) {
		if (obj1 == obj2) {
			return true;
		}
		
		if (obj1 == null || obj2 == null) {
			return false;
		}
		
		return obj1.equals(obj2);
	}
}