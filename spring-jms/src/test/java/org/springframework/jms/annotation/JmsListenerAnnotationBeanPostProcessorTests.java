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

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.JmsListenerContainerTestFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.MessageListenerTestContainer;

/**
 *
 * @author Stephane Nicoll
 */
public class JmsListenerAnnotationBeanPostProcessorTests {

	@Test
	public void simpleMessageListener() {
		final ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(Config.class);

		JmsListenerContainerTestFactory factory = context.getBean(JmsListenerContainerTestFactory.class);
		assertEquals("one container should have been registered", 1, factory.getContainers().size());
		MessageListenerTestContainer container = factory.getContainers().get(0);
		assertNotNull(container.getEndpoint().getListener());
		assertTrue("Should have been started " + container, container.isStarted());

		context.close(); // Close and stop the listeners
		assertTrue("Should have been stopped " + container, container.isStopped());

	}

	static class SimpleMessageListenerTestBean {

		@JmsListener(destination = "testQueue")
		public void handleIt(String body) {
		}

	}

	@Configuration
	static class Config {

		@Bean
		public JmsListenerAnnotationBeanPostProcessor postProcessor() {
			JmsListenerAnnotationBeanPostProcessor postProcessor = new JmsListenerAnnotationBeanPostProcessor();
			postProcessor.setEndpointRegistry(jmsListenerEndpointRegistry());
			return postProcessor;
		}

		@Bean
		public JmsListenerEndpointRegistry jmsListenerEndpointRegistry() {
			return new JmsListenerEndpointRegistry();
		}

		@Bean
		public SimpleMessageListenerTestBean target() {
			return new SimpleMessageListenerTestBean();
		}

		@Bean
		public JmsListenerContainerTestFactory testFactory() {
			return new JmsListenerContainerTestFactory("default");
		}

	}
}
