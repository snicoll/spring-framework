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

import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.javapoet.ClassName;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * Default {@link BeanFactoryNamingConvention} implementation.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public class DefaultBeanFactoryNamingConvention implements BeanFactoryNamingConvention {

	private final ClassNameGenerator classNameGenerator;

	@Nullable
	private final Class<?> target;

	private final String name;

	public DefaultBeanFactoryNamingConvention(ClassNameGenerator classNameGenerator,
			@Nullable Class<?> target, @Nullable String name) {
		this.classNameGenerator = classNameGenerator;
		this.target = target;
		this.name = (!StringUtils.hasText(name)) ? "" : name;
	}

	@Override
	public ClassName generateClassName(String featureName) {
		return this.classNameGenerator.generateClassName(this.target, this.name + featureName);
	}

	@Override
	public String getBeanFactoryName() {
		return this.name;
	}

}
