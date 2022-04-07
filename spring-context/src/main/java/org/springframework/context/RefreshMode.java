/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.context;

/**
 * Represent the different modes that an {@link ApplicationContext} implementation
 * can support. A particular implementation may support only a subset of this mode,
 * typically {@link #RUN}.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public enum RefreshMode {

	/**
	 * Refresh the context without any prior optimizations. This represents
	 * the default mode of operation where the context determines what it
	 * needs during the refresh phase.
	 */
	RUN,

	/**
	 * Optimize the context. The context does not actually create regular bean
	 * instances but rather determines what it would need.
	 */
	OPTIMIZE,

	/**
	 * Refresh the context with prior optimizations. In this mode of operations,
	 * certain callbacks are not invoked as a prior optimization made that
	 * obsolete.
	 */
	RUN_OPTIMIZE;

}
