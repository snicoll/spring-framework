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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method to be the target of a JMS message
 * listener on the specified {@link #destination()}. The {@link #factoryId()}
 * identifies the {@link org.springframework.jms.config.JmsListenerContainerFactory}
 * to use to build the listener container.
 *
 * <p>Processing of {@code @JmsListener} annotations is performed by
 * registering a {@link JmsListenerAnnotationBeanPostProcessor}. This can be
 * done manually or, more conveniently, through the {@code <jms:annotation-driven/>}
 * element or {@link EnableJms} annotation.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see EnableJms
 * @see JmsListenerAnnotationBeanPostProcessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JmsListener {

	/**
	 * The unique identifier of the container managing this endpoint.
	 * <p>if none is specified an auto-generated one is provided.
	 * @see org.springframework.jms.config.JmsListenerEndpointRegistry#getContainer(String)
	 */
	String id() default "";

	/**
	 * The identifier of the {@link org.springframework.jms.config.JmsListenerContainerFactory}
	 * to use to create the message listener container responsible to serve this endpoint.
	 * <p>By default, the "default" factory is used.
	 */
	String factoryId() default "default";

	/**
	 * The destination name for this listener, resolved through the container-wide
	 * {@link org.springframework.jms.support.destination.DestinationResolver} strategy
	 * (if any).
	 */
	String destination();

	/**
	 * The name for the durable subscription, if any.
	 */
	String subscription() default "";

	/**
	 * The JMS message selector expression, if any
	 * <p>See the JMS specification for a detailed definition of selector expressions.
	 */
	String selector() default "";

	/**
	 * The name of the default response destination to send response messages to.
	 * <p>This will be applied in case of a request message that does not carry
	 * a "JMSReplyTo" field. The type of this destination will be determined
	 * by the listener-container's "destination-type" attribute.
	 * <p>Note: This only applies to a listener method with a return value,
	 * for which each result object will be converted into a response message.
	 */
	String responseDestination() default "";

}
