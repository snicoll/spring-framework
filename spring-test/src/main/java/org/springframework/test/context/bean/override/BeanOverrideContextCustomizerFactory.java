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

package org.springframework.test.context.bean.override;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.TestContextAnnotationUtils;

/**
 * {@link ContextCustomizerFactory} which provides support for Bean Overriding
 * in tests. This is automatically registered via spring.factories.
 *
 * @author Simon Baslé
 * @since 6.2
 */
public final class BeanOverrideContextCustomizerFactory implements ContextCustomizerFactory {

	@Override
	@Nullable
	public ContextCustomizer createContextCustomizer(Class<?> testClass,
			List<ContextConfigurationAttributes> configAttributes) {

		BeanOverrideParser parser = new BeanOverrideParser();
		findClassesWithBeanOverride(testClass, parser);
		if (parser.getDetectedClasses().isEmpty()) {
			return null;
		}

		return new BeanOverrideContextCustomizer(parser.getDetectedClasses());
	}

	private void findClassesWithBeanOverride(Class<?> testClass, BeanOverrideParser parser) {
		parser.hasBeanOverride(testClass);
		if (TestContextAnnotationUtils.searchEnclosingClass(testClass)) {
			findClassesWithBeanOverride(testClass.getEnclosingClass(), parser);
		}
	}

	/**
	 * {@link ContextCustomizer} for Bean Overriding in tests.
	 */
	private static final class BeanOverrideContextCustomizer implements ContextCustomizer {

		private final Set<Class<?>> detectedClasses;

		/**
		 * Construct a context customizer given a set of classes that have been
		 * determined to contain bean overriding annotations, typically by a
		 * {@link BeanOverrideParser}.
		 * @param detectedClasses the set of test classes with bean overriding
		 */
		BeanOverrideContextCustomizer(Set<Class<?>> detectedClasses) {
			this.detectedClasses = detectedClasses;
		}

		@Override
		public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
			if (context instanceof BeanDefinitionRegistry registry) {
				BeanOverrideBeanPostProcessor.register(registry, this.detectedClasses);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != getClass()) {
				return false;
			}
			BeanOverrideContextCustomizer other = (BeanOverrideContextCustomizer) obj;
			return this.detectedClasses.equals(other.detectedClasses);
		}

		@Override
		public int hashCode() {
			return this.detectedClasses.hashCode();
		}
	}

}
