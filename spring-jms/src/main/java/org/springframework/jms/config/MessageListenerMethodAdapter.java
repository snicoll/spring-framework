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

import java.lang.reflect.Method;

import javax.jms.JMSException;
import javax.jms.Message;

import org.springframework.jms.listener.adapter.MessageListenerAdapter;

/**
 * TODO: needs rework
 * @author Stephane Nicoll
 */
public class MessageListenerMethodAdapter extends MessageListenerAdapter {

	private final Method method;

	public MessageListenerMethodAdapter(Object delegate, Method method) {
		super(delegate);
		this.method = method;
	}

	@Override
	protected String getListenerMethodName(Message originalMessage, Object extractedMessage) throws JMSException {
		return method.getName();
	}

	@Override
	protected Object[] buildListenerArguments(Object extractedMessage) {
		// TODO: check method argument types. Would be nice to get the session here to be able
		// to honor annotated parameter such as @JmsHeader("myHeader")
		return super.buildListenerArguments(extractedMessage);
	}
}
