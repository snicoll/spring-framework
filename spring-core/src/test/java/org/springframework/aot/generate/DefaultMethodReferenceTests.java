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

package org.springframework.aot.generate;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.Test;

import org.springframework.aot.generate.MethodReference2.ArgumentCodeGenerator;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.MethodSpec.Builder;
import org.springframework.javapoet.TypeName;
import org.springframework.lang.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 *
 * @author Stephane Nicoll
 */
class DefaultMethodReferenceTests {

	private static final ClassName TEST_CLASS_NAME = ClassName.get("com.example", "Test");

	private static final ClassName INITIALIZER_CLASS_NAME = ClassName.get("com.example", "Initializer");

	@Test
	void toCodeBlock() {
		assertThat(createLocalMethodReference("methodName").toCodeBlock())
				.isEqualTo(CodeBlock.of("this::methodName"));
	}

	@Test
	void toCodeBlockWithStaticMethod() {
		assertThat(createStaticMethodReference("methodName", TEST_CLASS_NAME).toCodeBlock())
				.isEqualTo(CodeBlock.of("com.example.Test::methodName"));
	}

	@Test
	void toCodeBlockWithStaticMethodRequiresDeclaringClass() {
		MethodSpec method = createTestMethod("methodName", new TypeName[0], Modifier.STATIC);
		DefaultMethodReference methodReference = new DefaultMethodReference(method, null);
		assertThatIllegalArgumentException().isThrownBy(methodReference::toCodeBlock)
				.withMessage("static method reference must define a declaring class");
	}

	@Test
	void toInvokeCodeBlockWithNoArgsAndNullDeclaringClass() {
		MethodSpec method = createTestMethod("methodName", new TypeName[0]);
		DefaultMethodReference methodReference = new DefaultMethodReference(method, null);
		assertThat(methodReference.toInvokeCodeBlock(TEST_CLASS_NAME, ArgumentCodeGenerator.none()))
				.isEqualTo(CodeBlock.of("methodName()"));
	}

	@Test
	void toInvokeCodeBlockWithNoArgsAndMatchingDeclaringClass() {
		MethodSpec method = createTestMethod("methodName", new TypeName[0]);
		DefaultMethodReference methodReference = new DefaultMethodReference(method, TEST_CLASS_NAME);
		assertThat(methodReference.toInvokeCodeBlock(TEST_CLASS_NAME, ArgumentCodeGenerator.none()))
				.isEqualTo(CodeBlock.of("methodName()"));
	}

	@Test
	void toInvokeCodeBlockWithMatchingArg() {
		DefaultMethodReference methodReference = createLocalMethodReference("methodName", ClassName.get(String.class));
		ArgumentCodeGenerator argCodeGenerator = ArgumentCodeGenerator.of(String.class, "stringArg");
		assertThat(methodReference.toInvokeCodeBlock(TEST_CLASS_NAME, argCodeGenerator))
				.isEqualTo(CodeBlock.of("methodName(stringArg)"));
	}

	@Test
	void toInvokeCodeBlockWithMatchingArgs() {
		DefaultMethodReference methodReference = createLocalMethodReference("methodName",
				ClassName.get(Integer.class), ClassName.get(String.class));
		ArgumentCodeGenerator argCodeGenerator = ArgumentCodeGenerator.of(String.class, "stringArg")
				.and(Integer.class, "integerArg");
		assertThat(methodReference.toInvokeCodeBlock(TEST_CLASS_NAME, argCodeGenerator))
				.isEqualTo(CodeBlock.of("methodName(integerArg, stringArg)"));
	}

	@Test
	void toInvokeCodeBlockWithSeparateDeclaringClass() {
		MethodSpec method = createTestMethod("methodName", new TypeName[0]);
		DefaultMethodReference methodReference = new DefaultMethodReference(method, TEST_CLASS_NAME);
		assertThat(methodReference.toInvokeCodeBlock(INITIALIZER_CLASS_NAME, ArgumentCodeGenerator.none()))
				.isEqualTo(CodeBlock.of("new com.example.Test().methodName()"));
	}

	@Test
	void toInvokeCodeBlockWithStaticMethodAndMatchingDeclaringClass() {
		DefaultMethodReference methodReference = createStaticMethodReference("methodName", TEST_CLASS_NAME);
		assertThat(methodReference.toInvokeCodeBlock(TEST_CLASS_NAME, ArgumentCodeGenerator.none()))
				.isEqualTo(CodeBlock.of("methodName()"));
	}

	@Test
	void toInvokeCodeBlockWithStaticMethodAndSeparateDeclaringClass() {
		DefaultMethodReference methodReference = createStaticMethodReference("methodName", TEST_CLASS_NAME);
		assertThat(methodReference.toInvokeCodeBlock(INITIALIZER_CLASS_NAME, ArgumentCodeGenerator.none()))
				.isEqualTo(CodeBlock.of("com.example.Test.methodName()"));
	}


	private DefaultMethodReference createLocalMethodReference(String name, TypeName... argumentTypes) {
		return createMethodReference(name, argumentTypes, null);
	}

	private DefaultMethodReference createMethodReference(String name, TypeName[] argumentTypes, @Nullable ClassName declaringClass) {
		MethodSpec method = createTestMethod(name, argumentTypes);
		return new DefaultMethodReference(method, declaringClass);
	}

	private DefaultMethodReference createStaticMethodReference(String name, ClassName declaringClass, TypeName... argumentTypes) {
		MethodSpec method = createTestMethod(name, argumentTypes, Modifier.STATIC);
		return new DefaultMethodReference(method, declaringClass);
	}

	private MethodSpec createTestMethod(String name, TypeName[] argumentTypes, Modifier... modifiers) {
		Builder method = MethodSpec.methodBuilder(name);
		for (int i = 0; i < argumentTypes.length; i++) {
			method.addParameter(argumentTypes[i], "args" + i);
		}
		method.addModifiers(modifiers);
		return method.build();
	}

}
