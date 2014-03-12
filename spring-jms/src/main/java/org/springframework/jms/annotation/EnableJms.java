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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Enable JMS listener annotated endpoints that are created under the cover
 * by a {@link org.springframework.jms.config.JmsListenerContainerFactory
 * JmsListenerContainerFactory}. To be used on
 * @{@link org.springframework.context.annotation.Configuration Configuration} classes
 * as follows:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableJms
 * public class AppConfig {
 *     &#064;Bean
 *     public JmsListenerContainerFactory jmsListenerContainerFactory() {
 *       DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
 *       factory.setId("default");
 *       factory.setConnectionFactory(connectionFactory());
 *       factory.setDestinationResolver(destinationResolver());
 *       factory.setConcurrency("5");
 *       return factory;
 *     }
 *     // other &#064;Bean definitions
 * }</pre>
 *
 * The {@code JmsListenerContainerFactory} gathers the settings to be applied on a set
 * of endpoints. It is identified by an <em>id</em> that is used to match the proper
 * instance against the {@linkplain JmsListener#factoryId() factoryId}.
 *
 * <p>{@code @EnableJms} enables detection of @{@link JmsListener} annotations on
 * any Spring-managed bean in the container. For example, given a class {@code MyService}
 *
 * <pre class="code">
 * package com.acme.foo;
 *
 * public class MyService {
 *     &#064;JmsListener(destination="myQueue")
 *     public void process(String msg) {
 *         // process incoming message
 *     }
 * }</pre>
 *
 * the following configuration would ensure that every time a {@link javax.jms.Message}
 * is received on the {@link javax.jms.Destination} named "myQueue", {@code MyService.process()}
 * is called with the content of the message
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableJms
 * public class AppConfig {
 *     &#064;Bean
 *     public MyService myService() {
 *         return new MyService();
 *     }
 *
 *     // JMS infrastructure setup
 * }</pre>
 *
 * Alternatively, if {@code MyService} were annotated with {@code @Component}, the
 * following configuration would ensure that its {@code @JmsListener} method is
 * invoked with a matching incoming message:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableJms
 * &#064;ComponentScan(basePackages="com.acme.foo")
 * public class AppConfig {
 * }</pre>
 *
 * The {@code JmsListenerContainerFactory} is responsible to create the actual message
 * container for the annotated method. It is identified by the
 * {@link JmsListener#factoryId() factoryId}. Note that those containers are not
 * registered against the application context but can be easily located for management
 * purposes using the {@link org.springframework.jms.config.JmsListenerEndpointRegistry}.
 *
 * <p>By default, all factory instances found in the context are detected. When more
 * control is desired, a {@code @Configuration} class may implement
 * {@link JmsListenerConfigurer}. This allows access to the underlying
 * {@link org.springframework.jms.config.JmsListenerEndpointRegistrar JmsListenerEndpointRegistrar}
 * instance. The following example demonstrates how to customize the
 * {@code JmsListenerContainerFactory} instances to use
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableJms
 * public class AppConfig implements JmsListenerConfigurer {
 *     &#064;Override
 *     public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
 *         registrar.setEndpointRegistry(jmsListenerEndpointRegistry());
 *     }
 *
 *     &#064;Bean
 *     public JmsListenerEndpointRegistry jmsListenerEndpointRegistry() {
 *         //... customize the factories to use
 *     }
 * }</pre>
 *
 *
 * Implementing {@code JmsListenerConfigurer} also allows for fine-grained
 * control over endpoints registration via the {@code JmsListenerEndpointRegistrar}.
 * For example, the following configures an extra endpoint:
 *
 * <pre class="code">
 * &#064;Configuration
 * &#064;EnableJms
 * public class AppConfig implements JmsListenerConfigurer {
 *     &#064;Override
 *     public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
 *         JmsListenerEndpoint myEndpoint = new JmsListenerEndpoint();
 *         // ... configure the endpoint
 *         registrar.addEndpoint(endpoint);
 *     }
 *
 *     &#064;Bean
 *     public MyService myService() {
 *         return new MyService();
 *     }
 *
 *     // JMS infrastructure setup
 * }</pre>
 *
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see JmsListener
 * @see JmsListenerAnnotationBeanPostProcessor
 * @see org.springframework.jms.config.JmsListenerEndpointRegistrar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JmsBootstrapConfiguration.class)
public @interface EnableJms {
}
