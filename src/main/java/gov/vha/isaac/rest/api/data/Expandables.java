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
package gov.vha.isaac.rest.api.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 
 * {@link Expandables}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
public class Expandables
{
	/**
	 * The list of data types that were not expanded on this request that could be expanded directly, or can 
	 * be expanded with a second trip by calling the provided URL
	 */
	@XmlElement
	List<Expandable> items;
	
	public Expandables() {
		// For JAXB only
	}
	
	public Expandables(List<Expandable> items)
	{
		this.items = items;
	}
	
	public Expandables(Expandable ... items)
	{
		this.items = new ArrayList<>(items == null ? 0 : items.length);
		if (items != null)
		{
			this.items.addAll(Arrays.asList(items));
		}
	}

	/**
	 * @param expandable
	 */
	public void add(Expandable expandable)
	{
		if (items == null) {
			this.items = new ArrayList<>();
		}
		this.items.add(expandable);
	}
	
	/**
	 * @param expandable
	 */
	public void remove(String expandable)
	{
		for (int i = 0; items != null && i < this.items.size(); i++)
		{
			if (this.items.get(i).name.equals(expandable))
			{
				this.items.remove(i--);
			}
		}
	}

	/**
	 * @return
	 */
	public int size()
	{
		return items == null ? 0 : this.items.size();
	}
}
