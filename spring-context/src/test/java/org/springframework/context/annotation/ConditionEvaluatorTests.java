/*
 * Copyright 2002-present the original author or authors.
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

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConditionEvaluator}.
 *
 * @author Stephane Nicoll
 */
class ConditionEvaluatorTests {

	@Test
	void evaluate() {
		AnnotationMetadata annotationMetadata = get(NeverMatchExceptAot.class);
		assertThat(createInstance(new StaticApplicationContext(), false)
				.shouldSkip(annotationMetadata)).isTrue();
	}

	@Test
	void evaluateWithAotConditions() {
		AnnotationMetadata annotationMetadata = get(NeverMatchExceptAot.class);
		assertThat(createInstance(new StaticApplicationContext(), true)
				.shouldSkip(annotationMetadata)).isFalse();
	}


	private ConditionEvaluator createInstance(BeanDefinitionRegistry registry, boolean aotConditions) {
		MockEnvironment environment = new MockEnvironment().withProperty("spring.aot.condition-evaluation", aotConditions);
		return new ConditionEvaluator(registry, environment, new DefaultResourceLoader());
	}


	private static AnnotationMetadata get(Class<?> source) {
		try {
			return MetadataReaderFactory.create(source.getClassLoader())
					.getMetadataReader(source.getName()).getAnnotationMetadata();
		}
		catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}


	@Conditional(NeverMatchExcaptAotTestCondition.class)
	private static class NeverMatchExceptAot {}

	private static class NeverMatchExcaptAotTestCondition implements Condition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return false;
		}
	}

}
