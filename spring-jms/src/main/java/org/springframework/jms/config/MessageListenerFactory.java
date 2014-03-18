package org.springframework.jms.config;

import javax.jms.MessageListener;

/**
 *
 * @author Stephane Nicoll
 */
public interface MessageListenerFactory {

	MessageListener createMessageListener(JmsListenerEndpoint endpoint);
}
