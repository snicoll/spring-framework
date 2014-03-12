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

package org.springframework.jms.listener;

import org.springframework.jms.JmsException;

/**
 * TODO: not sure this makes sense
 * @author Stephane Nicoll
 */
public interface MessageListenerContainer {

	/**
	 * Start this container.
	 * @throws JmsException if starting failed
	 */
	void start() throws JmsException;

	/**
	 * Stop this container.
	 * @throws JmsException if stopping failed
	 */
	void stop() throws JmsException;
}
