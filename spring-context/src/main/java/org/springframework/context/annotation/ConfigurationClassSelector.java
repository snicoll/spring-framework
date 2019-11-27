/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.context.annotation;

/**
 * Strategy interface to filter configuration classes upfront.
 *
 * @author Stephane Nicoll
 */
public interface ConfigurationClassSelector {

	Adapter adapter = new Adapter();

	/**
	 * Determine if the specified {@code configurationClassName} should be added to the
	 * list of configuration classes to process.
	 * @param configurationClassName the name of a configuration class
	 * @return {@code true} to process it, {@code false} otherwise
	 */
	boolean select(String configurationClassName);

	class Adapter {

		private final ThreadLocal<ConfigurationClassSelector> selector = ThreadLocal.withInitial(() -> (name) -> true);

		public void setSelector(ConfigurationClassSelector selector) {
			this.selector.set(selector);
		}

		ConfigurationClassSelector getSelector() {
			return this.selector.get();
		}
	}

}
