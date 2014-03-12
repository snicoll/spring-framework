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

package org.springframework.jms.annotation;

import org.springframework.jms.config.JmsListenerEndpointRegistrar;

/**
 * Optional interface to be implemented by @{@link
 * org.springframework.context.annotation.Configuration Configuration} classes annotated
 * with @{@link EnableJms}. Typically used for setting a specific
 * {@link org.springframework.jms.config.JmsListenerEndpointRegistry JmsListenerEndpointRegistry}
 * bean with specific {@link org.springframework.jms.config.JmsListenerContainerFactory
 * JmsListenerContainerFactory} instances or for registering jms endpoints in a <em>programmatic</em>
 * fashion as opposed to the <em>declarative</em> approach of using the @{@link JmsListener}
 * annotation.
 * <p>See @{@link EnableJms} for detailed usage examples.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see EnableJms
 * @see JmsListenerEndpointRegistrar
 */
public interface JmsListenerConfigurer {

	/**
	 * Callback allowing a {@link org.springframework.jms.config.JmsListenerEndpointRegistry
	 * JmsListenerEndpointRegistry} and specific {@link org.springframework.jms.config.JmsListenerEndpoint
	 * JmsListenerEndpoint} instances to be registered against the given
	 * {@link JmsListenerEndpointRegistrar}.
	 * @param registrar the registrar to be configured.
	 */
	void configureJmsListeners(JmsListenerEndpointRegistrar registrar);
}
