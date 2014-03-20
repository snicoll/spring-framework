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

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import org.springframework.context.support.StaticApplicationContext;
import org.springframework.jms.StubTextMessage;
import org.springframework.jms.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.jms.support.JmsMessageHeaderAccessor;
import org.springframework.jms.support.converter.JmsHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.ReflectionUtils;

/**
 *
 * @author Stephane Nicoll
 */
public class DefaultMessageListenerFactoryTests {

	@Rule
	public final TestName name = new TestName();

	private final DefaultMessageListenerFactory factory = new DefaultMessageListenerFactory();

	private final JmsEndpointSampleBean sample = new JmsEndpointSampleBean();

	@Before
	public void setup() {
		factory.setApplicationContext(new StaticApplicationContext());
		factory.afterPropertiesSet();
	}

	@Test
	public void resolveMessageAndSession() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(javax.jms.Message.class, Session.class);

		Session session = mock(Session.class);
		listener.onMessage(createSimpleJmsTextMessage("test"), session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveGenericMessage() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(Message.class);

		Session session = mock(Session.class);
		listener.onMessage(createSimpleJmsTextMessage("test"), session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveHeaderAndPayload() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(String.class, int.class);

		Session session = mock(Session.class);
		StubTextMessage message = createSimpleJmsTextMessage("my payload");
		message.setIntProperty("myCounter", 55);
		listener.onMessage(message, session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveHeaders() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(String.class, Map.class);

		Session session = mock(Session.class);
		StubTextMessage message = createSimpleJmsTextMessage("my payload");
		message.setIntProperty("customInt", 1234);
		message.setJMSMessageID("abcd-1234");
		listener.onMessage(message, session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveMessageHeaders() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(MessageHeaders.class);

		Session session = mock(Session.class);
		StubTextMessage message = createSimpleJmsTextMessage("my payload");
		message.setLongProperty("customLong", 4567L);
		message.setJMSType("myMessageType");
		listener.onMessage(message, session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveJmsMessageHeaderAccessor() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(JmsMessageHeaderAccessor.class);

		Session session = mock(Session.class);
		StubTextMessage message = createSimpleJmsTextMessage("my payload");
		message.setBooleanProperty("customBoolean", true);
		message.setJMSPriority(9);
		listener.onMessage(message, session);
		assertDefaultListenerMethodInvocation();
	}

	@Test
	public void resolveObjectPayload() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(MyBean.class);
		MyBean myBean = new MyBean();
		myBean.name = "myBean name";

		Session session = mock(Session.class);
		ObjectMessage message = mock(ObjectMessage.class);
		given(message.getObject()).willReturn(myBean);

		listener.onMessage(message, session);
		assertDefaultListenerMethodInvocation();
	}

	// failure scenario

	@Test
	@Ignore
	public void unresolvableParameter() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(MyBean.class);

		Session session = mock(Session.class);
		listener.onMessage(createSimpleJmsTextMessage("test"), session);
	}

	@Test
	@Ignore
	public void invalidPayloadType() throws JMSException {
		MessagingMessageListenerAdapter listener = createDefaultInstance(Message.class);

		Session session = mock(Session.class);
		listener.onMessage(createSimpleJmsTextMessage("test"), session);
	}


	private MessagingMessageListenerAdapter createDefaultInstance(Class<?>... parameterTypes) {
		MethodJmsListenerEndpoint endpoint = new MethodJmsListenerEndpoint();
		endpoint.setBean(sample);
		endpoint.setMethod(getDefaultListenerMethod(parameterTypes));
		return (MessagingMessageListenerAdapter) factory.createMessageListener(endpoint);
	}

	private StubTextMessage createSimpleJmsTextMessage(String body) {
		return new StubTextMessage(body);
	}

	private Method getDefaultListenerMethod(Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(JmsEndpointSampleBean.class,
				name.getMethodName(), parameterTypes);
		assertNotNull("no method found with name " + name.getMethodName()
				+ " and parameters " + Arrays.toString(parameterTypes));
		return method;
	}

	private void assertDefaultListenerMethodInvocation() {
		assertListenerMethodInvocation(sample, name.getMethodName());
	}

	private void assertListenerMethodInvocation(JmsEndpointSampleBean bean, String methodName) {
		assertTrue("Method " + methodName + " should have been invoked", bean.invocations.get(methodName));
	}

	static class JmsEndpointSampleBean {

		private final Map<String, Boolean> invocations = new HashMap<String, Boolean>();

		public void resolveMessageAndSession(javax.jms.Message message, Session session) {
			invocations.put("resolveMessageAndSession", true);
			assertNotNull("Message not injected", message);
			assertNotNull("Session not injected", session);
		}

		public void resolveGenericMessage(Message<String> message) {
			invocations.put("resolveGenericMessage", true);
			assertNotNull("Generic message not injected", message);
			assertEquals("Wrong message payload", "test", message.getPayload());
		}

		public void resolveHeaderAndPayload(@Payload String content, @Header("myCounter") int counter) {
			invocations.put("resolveHeaderAndPayload", true);
			assertEquals("Wrong @Payload resolution", "my payload", content);
			assertEquals("Wrong @Header resolution", 55, counter);
		}

		public void resolveHeaders(String content, @Headers Map<String, Object> headers) {
			invocations.put("resolveHeaders", true);
			assertEquals("Wrong payload resolution", "my payload", content);
			assertNotNull("headers not injected", headers);
			assertEquals("Missing JMS message id header", "abcd-1234", headers.get(JmsHeaders.MESSAGE_ID));
			assertEquals("Missing custom header", 1234, headers.get("customInt"));
		}

		public void resolveMessageHeaders(MessageHeaders headers) {
			invocations.put("resolveMessageHeaders", true);
			assertNotNull("MessageHeaders not injected", headers);
			assertEquals("Missing JMS message type header", "myMessageType", headers.get(JmsHeaders.TYPE));
			assertEquals("Missing custom header", 4567L, (long) headers.get("customLong"), 0.0);
		}

		public void resolveJmsMessageHeaderAccessor(JmsMessageHeaderAccessor headers) {
			invocations.put("resolveJmsMessageHeaderAccessor", true);
			assertNotNull("MessageHeaders not injected", headers);
			assertEquals("Missing JMS message priority header", Integer.valueOf(9), headers.getPriority());
			assertEquals("Missing custom header", true, headers.getHeader("customBoolean"));
		}

		public void resolveObjectPayload(MyBean bean) {
			invocations.put("resolveObjectPayload", true);
			assertNotNull("Object payload not injected", bean);
			assertEquals("Wrong content for payload", "myBean name", bean.name);
		}

		public void unresolvableParameter(MyBean myBean) {
			throw new IllegalStateException("Should never be called.");
		}

		public void invalidPayloadType(Message<Integer> message) {
			throw new IllegalStateException("Should never be called.");
		}

	}

	@SuppressWarnings("serial")
	static class MyBean implements Serializable {
		private String name;

	}
}
