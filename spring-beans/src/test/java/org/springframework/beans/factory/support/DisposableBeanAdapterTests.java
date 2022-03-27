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

package org.springframework.beans.factory.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link DisposableBeanAdapter}.
 *
 * @author Stephane Nicoll
 */
class DisposableBeanAdapterTests {

	@Test
	void inferDestroyMethodsIfNecessaryWithInferredAndClose() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
		assertThat(DisposableBeanAdapter.inferDestroyMethodsIfNecessary(CloseSample.class, beanDefinition))
				.containsExactly("close");
	}

	@Test
	void inferDestroyMethodsIfNecessaryWithInferredAndShutdown() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
		assertThat(DisposableBeanAdapter.inferDestroyMethodsIfNecessary(ShutdownSample.class, beanDefinition))
				.containsExactly("shutdown");
	}

	@Test
	void inferDestroyMethodsIfNecessaryWithAutoCloseable() {
		RootBeanDefinition beanDefinition = new RootBeanDefinition();
		beanDefinition.setDestroyMethodName(AbstractBeanDefinition.INFER_METHOD);
		assertThat(DisposableBeanAdapter.inferDestroyMethodsIfNecessary(AutoCloseableShutdownSample.class, beanDefinition))
				.containsExactly("close");
	}


	static class CloseSample {

		public void close() {

		}

	}

	static class ShutdownSample {

		public void shutdown() {

		}

	}

	static class AutoCloseableShutdownSample extends ShutdownSample implements AutoCloseable {

		@Override
		public void close() {

		}
	}

}
