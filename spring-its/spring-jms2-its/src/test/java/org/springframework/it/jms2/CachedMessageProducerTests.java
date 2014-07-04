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

package org.springframework.it.jms2;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.jms.connection.CachingConnectionFactory;

/**
 *
 * @author Stephane Nicoll
 */
public class CachedMessageProducerTests {

	@Mock
	private ConnectionFactory targetCf;

	@Mock
	private Connection targetConnection;

	@Mock
	private Session targetSession;

	@Mock
	private Destination destination;

	@Mock
	private MessageProducer targetMessageProducer;

	@Before
	public void initialize() throws JMSException {
		MockitoAnnotations.initMocks(this);
		given(this.targetCf.createConnection()).willReturn(this.targetConnection);
		given(this.targetConnection.createSession(false, Session.AUTO_ACKNOWLEDGE)).willReturn(this.targetSession);
		given(this.targetSession.createProducer(this.destination)).willReturn(targetMessageProducer);
	}

	//void send(Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener)
	@Test
	public void sendWith5Params() throws JMSException {
		CachingConnectionFactory ccf = new CachingConnectionFactory(this.targetCf);
		Session session = ccf.createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = session.createProducer(this.destination);
		Message message = createMockMessage();
		CompletionListener completionListener = createCompletionListener();
		producer.send(message, 0, 5, 100, completionListener);

		verify(this.targetMessageProducer, times(1)).send(message, 0, 5, 100, completionListener);
	}

	//send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener)
	@Test
	public void sendWith6Params() throws JMSException {
		CachingConnectionFactory ccf = new CachingConnectionFactory(this.targetCf);
		Session session = ccf.createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer producer = session.createProducer(this.destination);
		Message message = createMockMessage();
		CompletionListener completionListener = createCompletionListener();
		producer.send(this.destination, message, 0, 5, 100, completionListener);
		verify(this.targetMessageProducer, times(1)).send(this.destination, message, 0, 5, 100, completionListener);
	}

	private CompletionListener createCompletionListener() {return mock(CompletionListener.class);}

	private Message createMockMessage() {return mock(Message.class);}

}
