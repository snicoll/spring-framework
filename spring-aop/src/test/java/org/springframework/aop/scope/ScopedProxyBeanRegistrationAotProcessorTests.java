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

package org.springframework.aop.scope;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.aot.AotFactoriesLoader;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.testfixture.beans.factory.aot.BeanFactoryInitializationAotTester;
import org.springframework.beans.testfixture.beans.factory.generator.factory.NumberHolder;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Tests for {@link ScopedProxyBeanRegistrationAotProcessor}.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 6.0
 */
class ScopedProxyBeanRegistrationAotProcessorTests {

	private final BeanFactoryInitializationAotTester tester = new BeanFactoryInitializationAotTester();

	private final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

	@Test
	void scopedProxyBeanRegistrationAotProcessorIsRegistered() {
		assertThat(new AotFactoriesLoader(this.beanFactory).load(BeanRegistrationAotProcessor.class))
				.anyMatch(ScopedProxyBeanRegistrationAotProcessor.class::isInstance);
	}

	@Test
	void getBeanRegistrationCodeGeneratorWhenNotScopedProxy() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(PropertiesFactoryBean.class).getBeanDefinition();
		this.beanFactory.registerBeanDefinition("test", beanDefinition);
		this.tester.process(this.beanFactory, result -> {
			Object bean = result.beanFactory.getBean("test");
			assertThat(bean).isInstanceOf(Properties.class);
		});
	}

	@Test
	void getBeanRegistrationCodeGeneratorWhenScopedProxyWithoutTargetBeanName() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(ScopedProxyFactoryBean.class).getBeanDefinition();
		this.beanFactory.registerBeanDefinition("test", beanDefinition);
		this.tester.process(this.beanFactory, result ->
				assertThatExceptionOfType(BeanCreationException.class)
						.isThrownBy(() -> result.beanFactory.getBean("test"))
						.withMessageContaining("'targetBeanName' is required"));
	}

	@Test
	void getBeanRegistrationCodeGeneratorWhenScopedProxyWithInvalidTargetBeanName() {
		BeanDefinition beanDefinition = BeanDefinitionBuilder
				.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "testDoesNotExist")
				.getBeanDefinition();
		this.beanFactory.registerBeanDefinition("test", beanDefinition);
		this.tester.process(this.beanFactory, result ->
				assertThatExceptionOfType(BeanCreationException.class)
						.isThrownBy(() -> result.beanFactory.getBean("test"))
						.withMessageContaining("No bean named 'testDoesNotExist'"));
	}

	@Test
	void getBeanRegistrationCodeGeneratorWhenScopedProxyWithTargetBeanName() {
		RootBeanDefinition targetBean = new RootBeanDefinition();
		targetBean.setTargetType(
				ResolvableType.forClassWithGenerics(NumberHolder.class, Integer.class));
		targetBean.setScope("custom");
		this.beanFactory.registerBeanDefinition("numberHolder", targetBean);
		BeanDefinition scopedBean = BeanDefinitionBuilder
				.rootBeanDefinition(ScopedProxyFactoryBean.class)
				.addPropertyValue("targetBeanName", "numberHolder").getBeanDefinition();
		this.beanFactory.registerBeanDefinition("test", scopedBean);
		this.tester.process(this.beanFactory, result -> {
			Object bean = result.beanFactory.getBean("test");
			assertThat(bean).isNotNull().isInstanceOf(NumberHolder.class)
					.isInstanceOf(AopInfrastructureBean.class);
		});
	}

}
