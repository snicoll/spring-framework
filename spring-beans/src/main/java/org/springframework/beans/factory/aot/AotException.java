/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.aot;

import org.springframework.lang.Nullable;

/**
 * Abstract superclass for all exceptions thrown by ahead-of-time processing.
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
@SuppressWarnings("serial")
public abstract class AotException extends RuntimeException {

	/**
	 * Create a new AotException with the specified message.
	 * @param msg the detail message
	 */
	protected AotException(String msg) {
		super(msg);
	}

	/**
	 * Create a new AotException with the specified message
	 * and root cause.
	 * @param msg the detail message
	 * @param cause the root cause
	 */
	protected AotException(@Nullable String msg, @Nullable Throwable cause) {
		super(msg, cause);
	}

}
