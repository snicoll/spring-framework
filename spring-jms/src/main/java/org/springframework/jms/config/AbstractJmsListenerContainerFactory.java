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


import javax.jms.ConnectionFactory;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.util.ErrorHandler;

/**
 * Base {@link JmsListenerContainerFactory} for Spring's base container implementation.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see AbstractMessageListenerContainer
 */
public abstract class AbstractJmsListenerContainerFactory<C extends AbstractMessageListenerContainer>
		implements JmsListenerContainerFactory, BeanNameAware {

	private String id;

	private MessageListenerFactory messageListenerFactory;

	private ConnectionFactory connectionFactory;

	private DestinationResolver destinationResolver;

	private ErrorHandler errorHandler;

	// TODO: missing message converter

	private Boolean sessionTransacted;

	private Integer sessionAcknowledgeMode;

	private Boolean pubSubDomain;

	private Boolean subscriptionDurable;

	private String clientId;

	/**
	 * Set the id of this factory used as a reference to identify the configuration
	 * set defined by this instance.
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Set the {@link MessageListenerFactory} to use.
	 */
	public void setMessageListenerFactory(MessageListenerFactory messageListenerFactory) {
		this.messageListenerFactory = messageListenerFactory;
	}

	/**
	 * @see AbstractMessageListenerContainer#setConnectionFactory(ConnectionFactory)
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}

	/**
	 * @see AbstractMessageListenerContainer#setDestinationResolver(DestinationResolver)
	 */
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	/**
	 * @see AbstractMessageListenerContainer#setErrorHandler(ErrorHandler)
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * @see AbstractMessageListenerContainer#setSessionTransacted(boolean)
	 */
	public void setSessionTransacted(Boolean sessionTransacted) {
		this.sessionTransacted = sessionTransacted;
	}

	/**
	 * @see AbstractMessageListenerContainer#setSessionAcknowledgeMode(int)
	 */
	public void setSessionAcknowledgeMode(Integer sessionAcknowledgeMode) {
		this.sessionAcknowledgeMode = sessionAcknowledgeMode;
	}

	/**
	 * @see AbstractMessageListenerContainer#setPubSubDomain(boolean)
	 */
	public void setPubSubDomain(Boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * @see AbstractMessageListenerContainer#setSubscriptionDurable(boolean)
	 */
	public void setSubscriptionDurable(Boolean subscriptionDurable) {
		this.subscriptionDurable = subscriptionDurable;
	}

	/**
	 * @see AbstractMessageListenerContainer#setClientId(String)
	 */
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public void setBeanName(String name) {
		if (id == null) {
			this.id = name;
		}
	}

	/**
	 * Create an empty container instance.
	 */
	protected abstract C createContainerInstance();

	@Override
	public MessageListenerContainer createMessageListenerContainer(JmsListenerEndpoint endpoint) {
		C instance = createContainerInstance();

		if (connectionFactory != null) {
			instance.setConnectionFactory(connectionFactory);
		}
		if (destinationResolver != null) {
			instance.setDestinationResolver(destinationResolver);
		}
		if (errorHandler != null) {
			instance.setErrorHandler(errorHandler);
		}

		if (sessionTransacted != null) {
			instance.setSessionTransacted(sessionTransacted);
		}
		if (sessionAcknowledgeMode != null) {
			instance.setSessionAcknowledgeMode(sessionAcknowledgeMode);
		}


		if (pubSubDomain != null) {
			instance.setPubSubDomain(pubSubDomain);
		}
		if (subscriptionDurable != null) {
			instance.setSubscriptionDurable(subscriptionDurable);
		}
		if (clientId != null) {
			instance.setClientId(clientId);
		}

		initializeContainer(instance);

		instance.setMessageListener(messageListenerFactory.createMessageListener(endpoint));
		if (endpoint.getDestination() != null) {
			instance.setDestinationName(endpoint.getDestination());
		}
		if (endpoint.getSubscription() != null) {
			instance.setDurableSubscriptionName(endpoint.getSubscription());
		}
		if (endpoint.getSelector() != null) {
			instance.setMessageSelector(endpoint.getSelector());
		}

		return instance;
	}

	/**
	 * Further initialize the specified container.
	 * <p>Subclasses can inherit from this method to apply extra
	 * configuration if necessary.
	 */
	protected void initializeContainer(C instance) {

	}
}
