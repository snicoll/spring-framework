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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerTestFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.stereotype.Component;

/**
 *
 * @author Stephane Nicoll
 */
public class EnableJmsTests {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void defaultConfiguration() {
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				EnableJmsConfig.class, SampleBean.class);

		JmsListenerContainerTestFactory defaultFactory =
				context.getBean("defaultFactory", JmsListenerContainerTestFactory.class);
		JmsListenerContainerTestFactory simpleFactory =
				context.getBean("simpleFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, defaultFactory.getContainers().size());
		assertEquals(1, simpleFactory.getContainers().size());
	}

	@Test
	public void customConfiguration() {
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				EnableJmsCustomConfig.class, CustomBean.class);

		JmsListenerContainerTestFactory defaultFactory =
				context.getBean("defaultFactory", JmsListenerContainerTestFactory.class);
		JmsListenerContainerTestFactory customFactory =
				context.getBean("customFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, defaultFactory.getContainers().size());
		assertEquals(1, customFactory.getContainers().size());

		assertEquals("Wrong listener set in custom endpoint", context.getBean("simpleMessageListener"),
				defaultFactory.getContainers().get(0).getEndpoint().getListener());
	}

	@Test
	public void fullConfiguration() {
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(
				EnableJmsConfig.class, FullBean.class);
		JmsListenerContainerTestFactory simpleFactory =
				context.getBean("simpleFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, simpleFactory.getContainers().size());
		JmsListenerEndpoint endpoint = simpleFactory.getContainers().get(0).getEndpoint();
		assertEquals("simple", endpoint.getFactoryId());
		assertEquals("queueIn", endpoint.getDestination());
		assertEquals("mySelector", endpoint.getSelector());
		assertEquals("mySubscription", endpoint.getSubscription());
	}

	@Test
	public void unknownFactory() {
		thrown.expect(BeanInitializationException.class);
		thrown.expectMessage("custom"); // Not found
		thrown.expectMessage("default");
		thrown.expectMessage("simple");
		new AnnotationConfigApplicationContext(
				EnableJmsConfig.class, CustomBean.class);

	}


	@EnableJms
	@Configuration
	static class EnableJmsConfig {

		@Bean
		public JmsListenerContainerTestFactory defaultFactory() {
			return new JmsListenerContainerTestFactory("default");
		}

		@Bean
		public JmsListenerContainerTestFactory simpleFactory() {
			return new JmsListenerContainerTestFactory("simple");
		}
	}

	@Configuration
	@Import(EnableJmsConfig.class)
	static class EnableJmsCustomConfig implements JmsListenerConfigurer {

		@Autowired
		private EnableJmsConfig jmsConfig;

		@Override
		public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
			registrar.setEndpointRegistry(customRegistry());

			// Also register a custom endpoint
			JmsListenerEndpoint endpoint = new JmsListenerEndpoint();
			endpoint.setFactoryId("default");
			endpoint.setDestination("myQueue");
			endpoint.setListener(simpleMessageListener());
			registrar.addEndpoint(endpoint);
		}

		@Bean
		public JmsListenerEndpointRegistry customRegistry() {
			JmsListenerEndpointRegistry registry = new JmsListenerEndpointRegistry();
			List<JmsListenerContainerFactory> factories = new ArrayList<JmsListenerContainerFactory>();
			factories.add(jmsConfig.defaultFactory());
			factories.add(customFactory());
			registry.setJmsListenerContainerFactories(factories);
			return registry;
		}

		@Bean
		public JmsListenerContainerTestFactory customFactory() {
			return new JmsListenerContainerTestFactory("custom");
		}

		@Bean
		public MessageListener simpleMessageListener() {
			return new MessageListener() {
				@Override
				public void onMessage(Message message) {
					// do something with the message
				}
			};
		}
	}

	@Component
	static class FullBean {

		@JmsListener(factoryId = "simple", destination = "queueIn", responseDestination = "queueOut",
				selector = "mySelector", subscription = "mySubscription")
		public String fullHandle(String msg) {
			return "reply";
		}
	}

	@Component
	static class SampleBean {

		@JmsListener(destination = "myQueue")
		public void defaultHandle(String msg) {
		}

		@JmsListener(factoryId = "simple", destination = "myQueue")
		public void simpleHandle(String msg) {
		}
	}

	@Component
	static class CustomBean {

		@JmsListener(factoryId = "custom", destination = "myQueue")
		public void customHandle(String msg) {
		}

	}

}
