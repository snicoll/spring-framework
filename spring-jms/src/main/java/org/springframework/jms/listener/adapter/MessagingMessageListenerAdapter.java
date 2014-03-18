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

package org.springframework.jms.listener.adapter;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.JmsHeaderMapper;
import org.springframework.jms.support.converter.SimpleJmsHeaderMapper;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.messaging.support.MessageBuilder;

/**
 *
 * @author Stephane Nicoll
 */
public class MessagingMessageListenerAdapter extends BaseMessageListenerAdapter {

	private Object delegate;

	private InvocableHandlerMethod handlerMethod;

	private JmsHeaderMapper headerMapper = new SimpleJmsHeaderMapper();

	public MessagingMessageListenerAdapter() {
	}

	public void setDelegate(Object delegate) {
		this.delegate = delegate;
	}

	@Override
	protected Object getDelegate() {
		return delegate;
	}

	public void setHandlerMethod(InvocableHandlerMethod handlerMethod) {
		this.handlerMethod = handlerMethod;
	}

	public void setHeaderMapper(JmsHeaderMapper headerMapper) {
		this.headerMapper = headerMapper;
	}

	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		try {
			handlerMethod.invoke(toMessagingMessage(message), message, session);
		}
		catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	protected org.springframework.messaging.Message<?> toMessagingMessage(Message jmsMessage) throws JMSException {
		Map<String, Object> mappedHeaders = this.headerMapper.toHeaders(jmsMessage);
		Object convertedObject = extractMessage(jmsMessage);
		MessageBuilder<Object> builder = (convertedObject instanceof org.springframework.messaging.Message) ?
				MessageBuilder.fromMessage((org.springframework.messaging.Message<Object>) convertedObject) :
				MessageBuilder.withPayload(convertedObject);
		return builder.copyHeadersIfAbsent(mappedHeaders).build();
	}
}
