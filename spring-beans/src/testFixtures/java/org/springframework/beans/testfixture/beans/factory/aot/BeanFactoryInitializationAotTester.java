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

package org.springframework.beans.testfixture.beans.factory.aot;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.InMemoryGeneratedFiles;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.test.generator.compile.Compiled;
import org.springframework.aot.test.generator.compile.TestCompiler;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.TestBeanRegistrationsAotProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tester for {@link BeanFactoryInitializationAotProcessor}.
 *
 * @author Stephane Nicoll
 */
public class BeanFactoryInitializationAotTester {

	private final TestBeanRegistrationsAotProcessor processor = new TestBeanRegistrationsAotProcessor();

	private final Supplier<ConfigurableListableBeanFactory> freshFactory;

	public BeanFactoryInitializationAotTester() {
		this(DefaultListableBeanFactory::new);
	}

	private BeanFactoryInitializationAotTester(Supplier<ConfigurableListableBeanFactory> freshFactory) {
		this.freshFactory = freshFactory;
	}

	/**
	 * Use the specified {@link Supplier} to create a fresh bean factory instance.
	 * @param freshFactory the bean factory supplier to use
	 * @return a new instance with the updated supplier
	 */
	public BeanFactoryInitializationAotTester withFreshBeanFactorySupplier(
			Supplier<ConfigurableListableBeanFactory> freshFactory) {
		return new BeanFactoryInitializationAotTester(freshFactory);
	}

	/**
	 * Process the specified {@code beanFactory} by calling the registered
	 * {@link BeanFactoryInitializationAotProcessor processors}, compiling
	 * the generated code and re-creating a fresh bean factory that uses
	 * the generated code.
	 * @param beanFactory the bean factory to process
	 * @param result the result of the processing
	 */
	public void process(ConfigurableListableBeanFactory beanFactory, Consumer<Result> result) {
		BeanFactoryInitializationAotContribution contribution = this.processor
				.processAheadOfTime(beanFactory);
		assertThat(contribution).describedAs("AOT contribution").isNotNull();
		InMemoryGeneratedFiles generatedFiles = new InMemoryGeneratedFiles();
		DefaultGenerationContext generationContext = new DefaultGenerationContext(generatedFiles);
		MockBeanFactoryInitializationCode beanFactoryInitializationCode = new MockBeanFactoryInitializationCode();
		contribution.applyTo(generationContext, beanFactoryInitializationCode);
		generationContext.writeGeneratedContent();
		TestCompiler.forSystem().withFiles(generatedFiles).compile(compiled -> {
			MethodReference reference = beanFactoryInitializationCode
					.getInitializers().get(0);
			ConfigurableListableBeanFactory freshBeanFactory = invokeInitializer(compiled, reference);
			result.accept(new Result(compiled, generationContext.getRuntimeHints(), freshBeanFactory));
		});
	}

	private ConfigurableListableBeanFactory invokeInitializer(
			Compiled compiled, MethodReference reference) {
		Object instance = compiled.getInstance(Object.class,
				reference.getDeclaringClass().toString());
		Method method = ReflectionUtils.findMethod(instance.getClass(),
				reference.getMethodName(), DefaultListableBeanFactory.class);
		ConfigurableListableBeanFactory beanFactory = this.freshFactory.get();
		beanFactory.setBeanClassLoader(compiled.getClassLoader());
		ReflectionUtils.invokeMethod(method, instance, beanFactory);
		return beanFactory;
	}


	public static class Result {

		public final Compiled compiled;

		public final RuntimeHints runtimeHints;

		public final ConfigurableListableBeanFactory beanFactory;

		Result(Compiled compiled, RuntimeHints runtimeHints,
				ConfigurableListableBeanFactory beanFactory) {
			this.compiled = compiled;
			this.runtimeHints = runtimeHints;
			this.beanFactory = beanFactory;
		}

	}

}
