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

import org.springframework.beans.factory.generator.AotContributingBeanPostProcessor;
import org.springframework.beans.factory.generator.BeanInstantiationContribution;
import org.springframework.core.Ordered;

/**
 * A {@link MergedBeanDefinitionPostProcessor} that triggers the inferred destroy
 * method resolution early, so that the bean definition contains the expected
 * metadata.
 * @author Stephane Nicoll
 */
public class InferredDestroyMethodMergedBeanDefinitionPostProcessor implements AotContributingBeanPostProcessor {


	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public BeanInstantiationContribution contribute(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
		String[] destroyMethodNames = DisposableBeanAdapter.inferDestroyMethodsIfNecessary(beanType, beanDefinition);
		if (destroyMethodNames != null) {
			beanDefinition.setDestroyMethodNames(destroyMethodNames);
		}
		else {
			beanDefinition.setDestroyMethodName(null);
		}
		return null;
	}

}
