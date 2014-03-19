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

import org.junit.Test;

import org.springframework.context.ApplicationContext;
import org.springframework.jms.config.JmsListenerContainerTestFactory;
import org.springframework.jms.config.JmsListenerEndpoint;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.stereotype.Component;

/**
 *
 * @author Stephane Nicoll
 */
public abstract class AbstractJmsAnnotationDrivenTests {

	@Test
	public abstract void sampleConfiguration();

	@Test
	public abstract void fullConfiguration();

	@Test
	public abstract void customConfiguration();

	@Test
	public abstract void defaultContainerFactoryConfiguration();

	/**
	 * Test for {@link SampleBean} discovery.
	 */
	public void testSampleConfiguration(ApplicationContext context) {
		JmsListenerContainerTestFactory defaultFactory =
				context.getBean("defaultFactory", JmsListenerContainerTestFactory.class);
		JmsListenerContainerTestFactory simpleFactory =
				context.getBean("simpleFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, defaultFactory.getContainers().size());
		assertEquals(1, simpleFactory.getContainers().size());
	}

	@Component
	static class SampleBean {

		@JmsListener(containerFactory = "defaultFactory", destination = "myQueue")
		public void defaultHandle(String msg) {
		}

		@JmsListener(containerFactory = "simpleFactory", destination = "myQueue")
		public void simpleHandle(String msg) {
		}
	}

	/**
	 * Test for {@link FullBean} discovery.
	 */
	public void testFullConfiguration(ApplicationContext context) {
		JmsListenerContainerTestFactory simpleFactory =
				context.getBean("simpleFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, simpleFactory.getContainers().size());
		JmsListenerEndpoint endpoint = simpleFactory.getContainers().get(0).getEndpoint();
		assertEquals("listener1", endpoint.getId());
		assertEquals("queueIn", endpoint.getDestination());
		assertTrue(endpoint.isQueue());
		assertEquals("mySelector", endpoint.getSelector());
		assertEquals("mySubscription", endpoint.getSubscription());
	}

	@Component
	static class FullBean {

		@JmsListener(id = "listener1", containerFactory = "simpleFactory", destination = "queueIn",
				responseDestination = "queueOut", selector = "mySelector", subscription = "mySubscription")
		public String fullHandle(String msg) {
			return "reply";
		}
	}

	/**
	 * Test for {@link CustomBean} and an manually endpoint registered
	 * with "myCustomEndpointId".
	 */
	public void testCustomConfiguration(ApplicationContext context) {
		JmsListenerContainerTestFactory defaultFactory =
				context.getBean("defaultFactory", JmsListenerContainerTestFactory.class);
		JmsListenerContainerTestFactory customFactory =
				context.getBean("customFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, defaultFactory.getContainers().size());
		assertEquals(1, customFactory.getContainers().size());
		JmsListenerEndpoint endpoint = defaultFactory.getContainers().get(0).getEndpoint();
		assertEquals("Wrong endpoint type", SimpleJmsListenerEndpoint.class, endpoint.getClass());
		assertEquals("Wrong listener set in custom endpoint", context.getBean("simpleMessageListener"),
				((SimpleJmsListenerEndpoint) endpoint).getListener());

		JmsListenerEndpointRegistry customRegistry =
				context.getBean("customRegistry", JmsListenerEndpointRegistry.class);
		assertEquals("Wrong number of containers in the registry", 2,
				customRegistry.getContainers().size());
		assertNotNull("Container with custom id on the annotation should be found",
				customRegistry.getContainer("listenerId"));
		assertNotNull("Container created with custom id should be found",
				customRegistry.getContainer("myCustomEndpointId"));
	}

	@Component
	static class CustomBean {

		@JmsListener(id = "listenerId", containerFactory = "customFactory", destination = "myQueue")
		public void customHandle(String msg) {
		}
	}

	/**
	 * Test for {@link DefaultBean} that does not define the container
	 * factory to use as a default is registered.
	 */
	public void testDefaultContainerFactoryConfiguration(ApplicationContext context) {
		JmsListenerContainerTestFactory defaultFactory =
				context.getBean("defaultFactory", JmsListenerContainerTestFactory.class);
		assertEquals(1, defaultFactory.getContainers().size());
	}

	static class DefaultBean {

		@JmsListener(destination = "myQueue")
		public void handleIt(String msg) {
		}
	}

}
