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

package org.springframework.context.support;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.MergedAnnotations;

/**
 * An {@link InstantiationAwareBeanPostProcessor} that returns a proxy for beans
 * that are not safe to instantiate at build-time. By default, all beans are
 * unsafe unless they're marked explicitly:
 * <ul>
 * <li>By adding {@link AotInstantiationSafe} on the target class or the
 * {@link Bean @Bean} factory method.</li>
 * <li>By setting a, {@value AOT_INSTANTIATION_SAFE} attribute on the
 * related bean definition.</li>
 * </ul>
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public class AotInstantiationSafeBeanPostProcessor implements InstantiationAwareBeanPostProcessor {

	/**
	 * The name of an attribute that can be
	 * {@link org.springframework.core.AttributeAccessor#setAttribute set} on a
	 * {@link org.springframework.beans.factory.config.BeanDefinition} to signal
	 * that the bean can be instantiated at build-time.
	 */
	public static final String AOT_INSTANTIATION_SAFE = "aotInstantiationSafe";

	private final DefaultListableBeanFactory beanFactory;

	AotInstantiationSafeBeanPostProcessor(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		if (isAotInstantiationSafe(beanClass, beanName)) {
			return null;
		}
		try {
			return buildLazyResolutionProxy(beanClass, beanName);
		}
		catch (Exception ex) {
			throw new BeanCreationException(beanName, "Failed to create AOT instantiation proxy", ex);
		}
	}


	private boolean isAotInstantiationSafe(Class<?> beanClass, String beanName) {
		RootBeanDefinition beanDefinition = (this.beanFactory.containsBeanDefinition(beanName)
				? (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName) : null);
		if (beanDefinition != null && Boolean.TRUE.equals(beanDefinition.getAttribute(AOT_INSTANTIATION_SAFE))) {
			return true;
		}
		return (this.beanFactory.findAnnotationOnBean(beanName, AotInstantiationSafe.class) != null
				|| MergedAnnotations.from(beanClass).isPresent(AotInstantiationSafe.class));
	}

	protected Object buildLazyResolutionProxy(Class<?> beanClass, String beanName) {
		TargetSource ts = new TargetSource() {
			@Override
			public Class<?> getTargetClass() {
				return beanClass;
			}

			@Override
			public boolean isStatic() {
				return false;
			}

			@Override
			public Object getTarget() {
				throw new IllegalStateException("Instantiation of bean " + beanName
						+ " with type " + beanClass.getName()
						+ " denied during AOT execution as it was not marked safe.");
			}

			@Override
			public void releaseTarget(Object target) {
			}
		};

		ProxyFactory pf = new ProxyFactory();
		pf.setTargetSource(ts);
		if (beanClass.isInterface()) {
			pf.addInterface(beanClass);
		}
		return pf.getProxy(this.beanFactory.getBeanClassLoader());
	}

}
