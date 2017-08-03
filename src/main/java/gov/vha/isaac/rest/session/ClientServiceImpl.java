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

package gov.vha.isaac.rest.session;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.hk2.api.Rank;
import org.jvnet.hk2.annotations.Service;

import gov.vha.isaac.rest.services.ClientService;

/**
 * 
 * {@link ClientServiceImpl}
 *
 * @author <a href="mailto:joel.kniaz.list@gmail.com">Joel Kniaz</a>
 *
 */
@Service(name="rest-clientService")
@Rank(value = 10)
@Singleton
public class ClientServiceImpl implements ClientService {
	private Client client_;
	
	/**
	 * 
	 */
	ClientServiceImpl() {
	}

	/* (non-Javadoc)
	 * @see gov.vha.isaac.rest.session.ClientService#getClient()
	 */
	@Override
	public Client getClient() {
		return client_ != null ? client_ : ClientBuilder.newClient();
	}

	@PostConstruct
	void startup() {
		client_ = ClientBuilder.newClient();
	}

	@PreDestroy
	void shutdown() {
		if (client_ != null) {
			client_.close();
			client_ = null;
		}
	}
}
