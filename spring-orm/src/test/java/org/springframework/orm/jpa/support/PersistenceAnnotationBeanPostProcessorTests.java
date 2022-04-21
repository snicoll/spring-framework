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

package org.springframework.orm.jpa.support;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceProperty;
import jakarta.persistence.PersistenceUnit;
import org.junit.jupiter.api.Test;

import org.springframework.aot.generator.CodeContribution;
import org.springframework.aot.generator.DefaultCodeContribution;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.generator.BeanInstantiationContribution;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.javapoet.support.CodeSnippet;
import org.springframework.lang.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PersistenceAnnotationBeanPostProcessor}.
 *
 * @author Stephane Nicoll
 */
class PersistenceAnnotationBeanPostProcessorTests {

	@Test
	void contributeForPersistenceUnitOnPublicField() {
		CodeContribution contribution = contribute(DefaultPersistenceUnitField.class);
		assertThat(contribution).isNotNull();
		assertThat(contribution.runtimeHints().reflection().typeHints()).isEmpty();
		assertThat(CodeSnippet.process(contribution.statements().toCodeBlock())).isEqualTo("""
				EntityManagerFactory entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(beanFactory, "");
				bean.emf = entityManagerFactory;
				""");
	}

	@Test
	void contributeForPersistenceUnitOnPublicSetter() {
		CodeContribution contribution = contribute(DefaultPersistenceUnitMethod.class);
		assertThat(contribution).isNotNull();
		assertThat(contribution.runtimeHints().reflection().typeHints()).isEmpty();
		assertThat(CodeSnippet.process(contribution.statements().toCodeBlock())).isEqualTo("""
				EntityManagerFactory entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(beanFactory, "");
				bean.setEmf(entityManagerFactory);
				""");
	}

	@Test
	void contributeForPersistenceUnitWithCustomUnitOnPublicSetter() {
		CodeContribution contribution = contribute(CustomUnitNamePublicPersistenceUnitMethod.class);
		assertThat(contribution).isNotNull();
		assertThat(contribution.runtimeHints().reflection().typeHints()).isEmpty();
		assertThat(CodeSnippet.process(contribution.statements().toCodeBlock())).isEqualTo("""
				EntityManagerFactory entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(beanFactory, "custom");
				bean.setEmf(entityManagerFactory);
				""");
	}

	@Test
	void contributeForPersistenceContextOnPrivateField() {
		CodeContribution contribution = contribute(DefaultPersistenceContextField.class);
		assertThat(contribution).isNotNull();
		assertThat(contribution.runtimeHints().reflection().typeHints()).singleElement().satisfies(typeHint -> {
			assertThat(typeHint.getType()).isEqualTo(TypeReference.of(DefaultPersistenceContextField.class));
			assertThat(typeHint.fields()).singleElement().satisfies(fieldHint -> {
				assertThat(fieldHint.getName()).isEqualTo("entityManager");
				assertThat(fieldHint.isAllowWrite()).isTrue();
				assertThat(fieldHint.isAllowUnsafeAccess()).isFalse();
			});
		});
		assertThat(CodeSnippet.process(contribution.statements().toCodeBlock())).isEqualTo("""
				EntityManagerFactory entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(beanFactory, "");
				EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory, null, true);
				Field entityManagerField = ReflectionUtils.findField(PersistenceAnnotationBeanPostProcessorTests.DefaultPersistenceContextField.class, "entityManager");
				ReflectionUtils.makeAccessible(entityManagerField);
				ReflectionUtils.setField(entityManagerField, bean, entityManager);
				""");
	}

	@Test
	void contributeForPersistenceContextWithCustomPropertiesOnMethod() {
		CodeContribution contribution = contribute(CustomPropertiesPersistenceContextMethod.class);
		assertThat(contribution).isNotNull();
		assertThat(contribution.runtimeHints().reflection().typeHints()).isEmpty();
		assertThat(CodeSnippet.process(contribution.statements().toCodeBlock())).isEqualTo("""
				EntityManagerFactory entityManagerFactory = EntityManagerFactoryUtils.findEntityManagerFactory(beanFactory, "");
				Properties persistenceProperties = new Properties();
				persistenceProperties.put("jpa.test", "value");
				persistenceProperties.put("jpa.test2", "value2");
				EntityManager entityManager = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory, persistenceProperties, true);
				bean.setEntityManager(entityManager);
				""");
	}


	private DefaultCodeContribution contribute(Class<?> type) {
		BeanInstantiationContribution contributor = createContribution(type);
		assertThat(contributor).isNotNull();
		DefaultCodeContribution contribution = new DefaultCodeContribution(new RuntimeHints());
		contributor.applyTo(contribution);
		return contribution;
	}

	@Nullable
	private BeanInstantiationContribution createContribution(Class<?> beanType) {
		PersistenceAnnotationBeanPostProcessor bpp = new PersistenceAnnotationBeanPostProcessor();
		RootBeanDefinition beanDefinition = new RootBeanDefinition(beanType);
		return bpp.contribute(beanDefinition, beanType, "test");
	}


	static class DefaultPersistenceUnitField {

		@PersistenceUnit
		public EntityManagerFactory emf;

	}

	static class DefaultPersistenceUnitMethod {

		@PersistenceUnit
		public void setEmf(EntityManagerFactory emf) {
		}

	}

	static class CustomUnitNamePublicPersistenceUnitMethod {

		@PersistenceUnit(unitName = "custom")
		public void setEmf(EntityManagerFactory emf) {
		}

	}

	static class DefaultPersistenceContextField {

		@PersistenceContext
		private EntityManager entityManager;

	}

	static class CustomPropertiesPersistenceContextMethod {

		@PersistenceContext(properties = {
				@PersistenceProperty(name = "jpa.test", value = "value"),
				@PersistenceProperty(name = "jpa.test2", value = "value2") })
		public void setEntityManager(EntityManager entityManager) {

		}

	}

}
