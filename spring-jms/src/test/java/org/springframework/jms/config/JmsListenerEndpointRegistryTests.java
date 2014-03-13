/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.config;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author Stephane Nicoll
 */
public class JmsListenerEndpointRegistryTests {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	private final JmsListenerEndpointRegistry registry = new JmsListenerEndpointRegistry();

	@Before
	public void setup() {
		List<JmsListenerContainerFactory> factories = new ArrayList<JmsListenerContainerFactory>();
		factories.add(new JmsListenerContainerTestFactory("default"));
		factories.add(new JmsListenerContainerTestFactory("simple"));
		registry.setJmsListenerContainerFactories(factories);
	}

	@Test
	public void createWithNullEndpoint() {
		thrown.expect(IllegalArgumentException.class);
		registry.createJmsListenerContainer(null);
	}

	@Test
	public void createWithNullEndpointId() {
		thrown.expect(IllegalArgumentException.class);
		registry.createJmsListenerContainer(new JmsListenerEndpoint());
	}
	@Test
	public void createWithDuplicateEndpointId() {
		registry.createJmsListenerContainer(createEndpoint("test", "default", "queue"));

		thrown.expect(IllegalStateException.class);
		registry.createJmsListenerContainer(createEndpoint("test", "default", "queue"));
	}

	private JmsListenerEndpoint createEndpoint(String id, String factoryId, String destinationName) {
		JmsListenerEndpoint endpoint = new JmsListenerEndpoint();
		endpoint.setId(id);
		endpoint.setFactoryId(factoryId);
		endpoint.setDestination(destinationName);
		return endpoint;
	}

}
