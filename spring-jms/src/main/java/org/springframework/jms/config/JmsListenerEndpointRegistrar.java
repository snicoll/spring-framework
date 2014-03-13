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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Helper bean for registering {@link JmsListenerEndpoint} with a {@link JmsListenerEndpointRegistry}.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see org.springframework.jms.annotation.JmsListenerConfigurer
 */
public class JmsListenerEndpointRegistrar implements InitializingBean {

	private JmsListenerEndpointRegistry endpointRegistry;

	private final List<JmsListenerEndpoint> endpoints = new ArrayList<JmsListenerEndpoint>();

	/**
	 * Set the {@link JmsListenerEndpointRegistry} instance to use.
	 */
	public void setEndpointRegistry(JmsListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	/**
	 * Return the {@link JmsListenerEndpointRegistry} instance for this
	 * registrar, may be {@code null}.
	 */
	public JmsListenerEndpointRegistry getEndpointRegistry() {
		return endpointRegistry;
	}

	/**
	 * Add a jms endpoint.
	 */
	public void addEndpoint(JmsListenerEndpoint endpoint) {
		Assert.notNull(endpoint, "endpoint must be set");
		Assert.notNull(endpoint.getId(), "endpoint id must be set.");
		this.endpoints.add(endpoint);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		startAllEndpoints();
	}

	protected void startAllEndpoints() throws Exception {
		for (JmsListenerEndpoint endpoint : endpoints) {
			endpointRegistry.createJmsListenerContainer(endpoint);
		}
	}

}
