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

package org.springframework.context.aot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.Modifier;

import org.springframework.aot.generate.GeneratedClass;
import org.springframework.aot.generate.GeneratedMethods;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.generate.MethodReference.ArgumentCodeGenerator;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.ContextAnnotationAutowireCandidateResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.lang.Nullable;

/**
 * Internal code generator to create the {@link ApplicationContextInitializer}.
 *
 * @author Phillip Webb
 * @since 6.0
 */
class ApplicationContextInitializationCodeGenerator implements BeanFactoryInitializationCode {

	private static final String INITIALIZE_METHOD = "initialize";

	private static final String APPLICATION_CONTEXT_VARIABLE = "applicationContext";

	private final List<MethodReference> initializers = new ArrayList<>();

	private final GeneratedClass generatedClass;


	ApplicationContextInitializationCodeGenerator(GenerationContext generationContext) {
		this.generatedClass = generationContext.getGeneratedClasses()
				.addForFeature("ApplicationContextInitializer", this::generateType);
		this.generatedClass.reserveMethodNames(INITIALIZE_METHOD);
	}


	private void generateType(TypeSpec.Builder type) {
		type.addJavadoc(
				"{@link $T} to restore an application context based on previous AOT processing.",
				ApplicationContextInitializer.class);
		type.addModifiers(Modifier.PUBLIC);
		type.addSuperinterface(ParameterizedTypeName.get(
				ApplicationContextInitializer.class, GenericApplicationContext.class));
		type.addMethod(generateInitializeMethod());
	}

	private MethodSpec generateInitializeMethod() {
		MethodSpec.Builder method = MethodSpec.methodBuilder(INITIALIZE_METHOD);
		method.addAnnotation(Override.class);
		method.addModifiers(Modifier.PUBLIC);
		method.addParameter(GenericApplicationContext.class, APPLICATION_CONTEXT_VARIABLE);
		method.addCode(generateInitializeCode());
		return method.build();
	}

	private CodeBlock generateInitializeCode() {
		CodeBlock.Builder code = CodeBlock.builder();
		code.addStatement("$T $L = $L.getDefaultListableBeanFactory()",
				DefaultListableBeanFactory.class, BEAN_FACTORY_VARIABLE,
				APPLICATION_CONTEXT_VARIABLE);
		code.addStatement("$L.setAutowireCandidateResolver(new $T())",
				BEAN_FACTORY_VARIABLE, ContextAnnotationAutowireCandidateResolver.class);
		code.addStatement("$L.setDependencyComparator($T.INSTANCE)",
				BEAN_FACTORY_VARIABLE, AnnotationAwareOrderComparator.class);
		ArgumentCodeGenerator argCodeGenerator = createInitializerMethodArgumentCodeGenerator();
		for (MethodReference initializer : this.initializers) {
			code.addStatement(initializer.toInvokeCodeBlock(
					this.generatedClass.getName(), argCodeGenerator));
		}
		return code.build();
	}

	private ArgumentCodeGenerator createInitializerMethodArgumentCodeGenerator() {
		return ArgumentCodeGenerator.of(ResourceLoader.class, APPLICATION_CONTEXT_VARIABLE)
				.and(TypeOrInterfaceMatcher.of(DefaultListableBeanFactory.class, BEAN_FACTORY_VARIABLE))
				.and(TypeOrInterfaceMatcher.of(ConfigurableEnvironment.class, CodeBlock.of(
						"$L.getConfigurableEnvironment()", APPLICATION_CONTEXT_VARIABLE)));
	}

	GeneratedClass getGeneratedClass() {
		return this.generatedClass;
	}

	@Override
	public GeneratedMethods getMethods() {
		return this.generatedClass.getMethods();
	}

	@Override
	public void addInitializer(MethodReference methodReference) {
		this.initializers.add(methodReference);
	}

	private static final class TypeOrInterfaceMatcher implements Function<TypeName, CodeBlock> {

		private final Class<?> candidate;

		private final CodeBlock code;

		private TypeOrInterfaceMatcher(Class<?> candidate, CodeBlock code) {
			this.candidate = candidate;
			this.code = code;
		}

		static ArgumentCodeGenerator of(Class<?> candidate, String argument) {
			return of(candidate, CodeBlock.of(argument));
		}

		static ArgumentCodeGenerator of(Class<?> candidate, CodeBlock code) {
			return ArgumentCodeGenerator.from(new TypeOrInterfaceMatcher(candidate, code));
		}

		@Override
		@Nullable
		public CodeBlock apply(TypeName typeName) {
			if (typeName instanceof ClassName className
					&& matchTypeOrAnyInterface(this.candidate, className)) {
				return this.code;
			}
			return null;
		}


		/**
		 * Specify if the {@code className} is assignable from the specified
		 * {@code candidate} using the type of the candidate and any interface
		 * that it implements.
		 * @param candidate the matching class to check
		 * @param className the requested class name
		 * @return {@code true} if the className is assignable from the candidate
		 */
		private boolean matchTypeOrAnyInterface(Class<?> candidate, ClassName className) {
			String canonicalName = className.canonicalName();
			if (canonicalName.equals(candidate.getCanonicalName())) {
				return true;
			}
			for (Class<?> interfaceCandidate : candidate.getInterfaces()) {
				if (canonicalName.equals(interfaceCandidate.getCanonicalName())) {
					return true;
				}
			}
			for (Class<?> itf : candidate.getInterfaces()) {
				if (matchTypeOrAnyInterface(itf, className)) {
					return true;
				}
			}
			return false;
		}
	}

}
