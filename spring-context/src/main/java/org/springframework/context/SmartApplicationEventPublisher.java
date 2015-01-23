/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.context;

import org.springframework.core.ResolvableType;

/**
 * Extended variant of the standard {@link ApplicationEventPublisher} interface,
 * exposing a {@link #publishEvent(Object, ResolvableType)} method that provides
 * a better support for generic-based events.
 *
 * @author Stephane Nicoll
 * @since 4.2.0
 */
public interface SmartApplicationEventPublisher extends ApplicationEventPublisher {

	/**
	 * Notify all  <strong>matching</strong> listeners registered with this
	 * application of an application event. Events may be framework events
	 * (such as RequestHandledEvent) or application-specific events.
	 * <p>If the specified {@code event} is not an {@link ApplicationEvent}, it
	 * is wrapped in a {@code GenericApplicationEvent}.
	 * @param event the event to publish
	 * @param eventType the event type
	 * @see #publishEvent(ApplicationEvent)
	 */
	void publishEvent(Object event, ResolvableType eventType);

}
