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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.MessageListenerContainer;

/**
 * Create the necessary {@link MessageListenerContainer} instances
 * for the registered {@linkplain JmsListenerEndpoint endpoints}. Also
 * manage the lifecycle of the containers, in particular with the
 * lifecycle of the application context.
 * <p>Contrary to {@link MessageListenerContainer} created manually,
 * containers managed by this instances are not registered in the
 * application context and cannot be located for autowiring. Use
 * {@link #getContainers()} if you need to access the containers
 * of this instance for management purposes.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see JmsListenerEndpoint
 * @see MessageListenerContainer
 * @see JmsListenerContainerFactory
 */
public class JmsListenerEndpointRegistry implements DisposableBean {

	private Map<String, JmsListenerContainerFactory> factories
			= new HashMap<String, JmsListenerContainerFactory>();

	private final Set<MessageListenerContainer> containers =
			new HashSet<MessageListenerContainer>();

	/**
	 * Set the {@link JmsListenerContainerFactory} instances to use by
	 * this registry. Each {@link JmsListenerEndpoint} defines the
	 * factory to use through the {@linkplain JmsListenerEndpoint#factoryId
	 * factoryId} element.
	 * <p>By default, all instances found in the context are used.
	 */
	@Autowired(required = false)
	public void setJmsListenerContainerFactories(
			Collection<JmsListenerContainerFactory> jmsListenerContainerFactories) {
		this.factories.clear();
		for (JmsListenerContainerFactory factory : jmsListenerContainerFactories) {
			factories.put(factory.getId(), factory);
		}
	}

	/**
	 * Return the registered {@link JmsListenerContainerFactory} instances.
	 */
	protected Map<String, JmsListenerContainerFactory> getFactories() {
		return factories;
	}

	/**
	 * Return the managed {@link MessageListenerContainer} instance(s).
	 */
	public Set<MessageListenerContainer> getContainers() {
		return Collections.unmodifiableSet(containers);
	}

	/**
	 * Add a {@link JmsListenerEndpoint}.
	 * <p>This create the necessary infrastructure to honor that endpoint
	 * with regards to its configuration.
	 * @param endpoint the endpoint to add
	 * @see #getContainers()
	 */
	public void addJmsListenerEndpoint(JmsListenerEndpoint endpoint) {
		String factoryId = endpoint.getFactoryId();
		JmsListenerContainerFactory factory = getFactories().get(factoryId);
		if (factory == null) {
			throw new IllegalStateException("No JMS listener container factory found with id '"
					+ factoryId + "' among " + getFactories().keySet() + ". Make sure that a '"
					+ JmsListenerContainerFactory.class.getName() + "' implementation is "
					+ "registered with that id.");
		}
		MessageListenerContainer container = createContainer(factory, endpoint);
		containers.add(container);
	}

	/**
	 * Create and start a new container using the specified factory.
	 */
	protected MessageListenerContainer createContainer(JmsListenerContainerFactory factory,
			JmsListenerEndpoint endpoint) {
		MessageListenerContainer container = factory.createMessageListenerContainer(endpoint);
		initializeContainer(container);
		return container;
	}

	@Override
	public void destroy() throws Exception {
		for (MessageListenerContainer container : getContainers()) {
			stopContainer(container);
		}
	}

	protected void initializeContainer(MessageListenerContainer container) {
		container.start();
		if (container instanceof InitializingBean) {
			try {
				((InitializingBean) container).afterPropertiesSet();
			}
			catch (Exception e) {
				throw new BeanInitializationException("Could not start message listener container", e);
			}
		}
	}

	protected void stopContainer(MessageListenerContainer container) throws Exception {
		container.stop();
		if (container instanceof DisposableBean) {
			((DisposableBean) container).destroy();
		}
	}

}
