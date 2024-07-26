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

package org.springframework.context.annotation;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import org.springframework.context.annotation.ConfigurationClassParsingListenerTestIntegrationTests.ConfigurationWithImportSelectorAndRegisterPhaseCondition.ExampleImportSelector;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Integration tests for {@link ConfigurationClassParsingListener}.
 *
 * @author Stephane Nicoll
 */
class ConfigurationClassParsingListenerTestIntegrationTests {

	private final ConfigurationClassParsingListener parsingListener;

	private final AnnotationConfigApplicationContext ctx;

	ConfigurationClassParsingListenerTestIntegrationTests() {
		this.parsingListener = mock(ConfigurationClassParsingListener.class);
		this.ctx = new AnnotationConfigApplicationContext();
		this.ctx.getBeanDefinition(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)
				.getPropertyValues().addPropertyValue("parsingListener", this.parsingListener);
	}

	@Test
	void configurationClassThatIsSkippedByConditionCallsListener() {
		this.ctx.register(ConfigurationWithImportAndRegisterPhaseCondition.class);
		this.ctx.refresh();
		assertThat(this.ctx.getBeanDefinitionNames()).doesNotContain("mainConfig", "exampleConfig", "text");
		InOrder ordered = inOrder(this.parsingListener);
		ordered.verify(this.parsingListener).onConfigurationClassSkipped(null, ExampleConfiguration.class.getName());
		ordered.verify(this.parsingListener).onConfigurationClassSkipped("mainConfig", ConfigurationWithImportAndRegisterPhaseCondition.class.getName());
		verifyNoMoreInteractions(this.parsingListener);
	}

	@Test
	void configurationClassWhenImportedByImportSelectedAndSkippedByConditionCallsListener() {
		this.ctx.register(ConfigurationWithImportSelectorAndRegisterPhaseCondition.class);
		this.ctx.refresh();
		assertThat(this.ctx.getBeanDefinitionNames()).doesNotContain("mainConfig", "exampleConfig", "text");
		InOrder ordered = inOrder(this.parsingListener);
		ordered.verify(this.parsingListener).onConfigurationClassSkipped(null, ExampleConfiguration.class.getName());
		ordered.verify(this.parsingListener).onConfigurationClassSkipped("mainConfig", ConfigurationWithImportSelectorAndRegisterPhaseCondition.class.getName());
		verifyNoMoreInteractions(this.parsingListener);
	}


	@Configuration("mainConfig")
	@Conditional(NeverOnRegisterBeanCondition.class)
	@Import(ExampleConfiguration.class)
	static class ConfigurationWithImportAndRegisterPhaseCondition {
	}

	@Configuration("mainConfig")
	@Conditional(NeverOnRegisterBeanCondition.class)
	@Import(ExampleImportSelector.class)
	static class ConfigurationWithImportSelectorAndRegisterPhaseCondition {

		static class ExampleImportSelector implements ImportSelector {

			@Override
			public String[] selectImports(AnnotationMetadata importingClassMetadata) {
				return new String[] { ExampleConfiguration.class.getName() };
			}
		}
	}

	@Configuration("exampleConfig")
	static class ExampleConfiguration {

		@Bean
		String text() {
			return "Hello";
		}
	}

	private static final class NeverOnRegisterBeanCondition implements ConfigurationCondition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return false;
		}

		@Override
		public ConfigurationPhase getConfigurationPhase() {
			return ConfigurationPhase.REGISTER_BEAN;
		}
	}

}
