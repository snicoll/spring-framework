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
import java.util.Map;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.util.Assert;

/**
 * Create the necessary {@link MessageListenerContainer} instances
 * for the registered {@linkplain JmsListenerEndpoint endpoints}. Also
 * manage the lifecycle of the containers, in particular with the
 * lifecycle of the application context.
 * <p>Contrary to {@link MessageListenerContainer} created manually,
 * containers managed by this instances are not registered in the
 * application context and are not candidates for autowiring. Use
 * {@link #getContainers()} if you need to access the containers
 * of this instance for management purposes. If you need to access
 * a particular container, use {@link #getContainer(String)} with the
 * id of the endpoint.
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

	private final Map<String, MessageListenerContainer> containers =
			new HashMap<String, MessageListenerContainer>();

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
	 * Return the {@link MessageListenerContainer} with the specified id or
	 * {@code null} if no such container exists.
	 *
	 * @param id the id of the container
	 * @return the container or {@code null} if no container with that id exists
	 * @see org.springframework.jms.config.JmsListenerEndpoint#getId()
	 */
	public MessageListenerContainer getContainer(String id) {
		Assert.notNull(id, "the container identifier must be set.");
		return containers.get(id);
	}

	/**
	 * Return the managed {@link MessageListenerContainer} instance(s).
	 */
	public Collection<MessageListenerContainer> getContainers() {
		return Collections.unmodifiableCollection(containers.values());
	}

	/**
	 * Create a message listener container for the given {@link JmsListenerEndpoint}.
	 * <p>This create the necessary infrastructure to honor that endpoint
	 * with regards to its configuration.
	 * @param endpoint the endpoint to add
	 * @see #getContainers()
	 * @see #getContainer(String)
	 */
	public void createJmsListenerContainer(JmsListenerEndpoint endpoint) {
		Assert.notNull(endpoint, "endpoint must be set");

		String id = endpoint.getId();
		Assert.notNull(id, "endpoint id must be set.");
		Assert.state(!containers.containsKey(id), "another endpoint is already " +
				"registered with id '" + id + "'");

		String factoryId = endpoint.getFactoryId();
		JmsListenerContainerFactory factory = getFactories().get(factoryId);
		if (factory == null) {
			throw new IllegalStateException("No JMS listener container factory found with id '"
					+ factoryId + "' among " + getFactories().keySet() + ". Make sure that a '"
					+ JmsListenerContainerFactory.class.getName() + "' implementation is "
					+ "registered with that id.");
		}
		MessageListenerContainer container = doCreateJmsListenerContainer(factory, endpoint);
		containers.put(id, container);
	}

	/**
	 * Create and start a new container using the specified factory.
	 */
	protected MessageListenerContainer doCreateJmsListenerContainer(JmsListenerContainerFactory factory,
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
