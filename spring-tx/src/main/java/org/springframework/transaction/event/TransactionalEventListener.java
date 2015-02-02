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

package org.springframework.transaction.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.event.EventListener;

/**
 * An {@link EventListener} that is invoked according to a transactional {@link Phase}.
 * <p>
 * If the event is not published in the boundaries of a managed transaction, the event
 * is processed immediately as any regular event would. Otherwise, the event is processed
 * according to its {@link Phase}.
 * <p>
 * Adding {@link org.springframework.core.annotation.Order @Order} on your annotated method
 * allows you to prioritize that listener amongst other listeners running on the same phase.
 *
 * @author Stephane Nicoll
 * @since 4.2.0
 */
@EventListener
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TransactionalEventListener {

	/**
	 * Phase to bind the handling of an event. If no transaction is in progress, no
	 * transactional phase could not be honored and the event is therefore processed
	 * immediately.
	 */
	Phase phase() default Phase.AFTER_COMMIT;

	/**
	 * Spring Expression Language (SpEL) attribute used for conditioning the event handling.
	 * <p>Default is "", meaning the event is always handled.
	 * @see EventListener#condition()
	 */
	String condition() default "";

}
