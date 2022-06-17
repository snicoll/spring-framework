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

package org.springframework.beans.factory.aot;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.javapoet.ClassName;

/**
 * Naming strategy to use when processing a particular {@link BeanFactory}.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public interface BeanFactoryNamingConvention {

	/**
	 * Generate a new {@link ClassName} for the specified {@code featureName}.
	 * @param featureName the name of the feature that the generated class
	 * supports for the bean factory this instance handles
	 * @return a unique generated class name
	 */
	ClassName generateClassName(String featureName);

	/**
	 * Return the name of the bean factory or and empty string if no ID is available.
	 * @return the bean factory name
	 */
	default String getBeanFactoryName() {
		return "";
	}

}
