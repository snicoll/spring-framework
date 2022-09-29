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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.lang.model.element.Modifier;

import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.beans.testfixture.beans.TestBean;
import org.springframework.beans.testfixture.beans.TestBeanWithPrivateMethod;
import org.springframework.beans.testfixture.beans.TestBeanWithPublicField;
import org.springframework.core.test.tools.Compiled;
import org.springframework.core.test.tools.TestCompiler;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link InjectionCodeGenerator}.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 */
class InjectionCodeGeneratorTests {

	private static final String INSTANCE_VARIABLE = "instance";

	private static final ClassName TEST_TARGET = ClassName.get("com.example", "Test");

	private final RuntimeHints hints = new RuntimeHints();

	@Test
	void generateCodeWhenPublicFieldInjectsValue() {
		TestBeanWithPublicField bean = new TestBeanWithPublicField();
		Field field = field(bean.getClass(), "age");
		CodeBlock generatedCode = createGenerator(TEST_TARGET).generateInjectionCode(
				field, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		testCompiledResult(TEST_TARGET, generatedCode, TestBeanWithPublicField.class, (actual, compiled) -> {
			TestBeanWithPublicField instance = new TestBeanWithPublicField();
			actual.accept(instance);
			assertThat(instance).extracting("age").isEqualTo(123);
			assertThat(compiled.getSourceFile()).contains("instance.age = 123");
		});
	}

	@Test
	void generateCodeWhenPrivateFieldInjectsValueUsingReflectionAndType() {
		TestBean bean = new TestBean();
		Field field = field(bean.getClass(), "age");
		CodeBlock generatedCode = createGenerator(ClassName.get(TestBean.class)).generateInjectionCode(
				field, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		testCompiledResult(TEST_TARGET, generatedCode, TestBean.class, (actual, compiled) -> {
			TestBean instance = new TestBean();
			actual.accept(instance);
			assertThat(instance).extracting("age").isEqualTo(123);
			assertThat(compiled.getSourceFile())
					.doesNotContain("ClassUtils.resolveClassName")
					.contains("setField(");
		});
	}

	@Test
	void generateCodeWhenPublicFieldOnProtectedTypeInjectsValueUsingReflectionAndTypeReference() {
		ProtectedBean bean = new ProtectedBean();
		Field field = field(bean.getClass(), "name");
		CodeBlock generatedCode = createGenerator(TEST_TARGET).generateInjectionCode(
				field, INSTANCE_VARIABLE, CodeBlock.of("$S", "test"));
		testCompiledResult(TEST_TARGET, generatedCode, Object.class, (actual, compiled) -> {
			ProtectedBean instance = new ProtectedBean();
			actual.accept(instance);
			assertThat(instance.name).isEqualTo("test");
			assertThat(compiled.getSourceFile())
					.contains("ClassUtils.resolveClassName")
					.contains("\"" + ProtectedBean.class.getName() + "\"")
					.contains("setField(");
		});
	}

	@Test
	void generateCodeWhenPrivateFieldAddsHint() {
		TestBean bean = new TestBean();
		Field field = field(bean.getClass(), "age");
		createGenerator(TEST_TARGET).generateInjectionCode(
				field, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		assertThat(RuntimeHintsPredicates.reflection().onField(TestBean.class, "age"))
				.accepts(this.hints);
	}

	@Test
	void generateCodeWhenPublicMethodInjectsValue() {
		TestBean bean = new TestBean();
		Method method = method(bean.getClass(), "setAge", int.class);
		CodeBlock generatedCode = createGenerator(TEST_TARGET).generateInjectionCode(
				method, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		testCompiledResult(TEST_TARGET, generatedCode, TestBean.class, (actual, compiled) -> {
			TestBean instance = new TestBean();
			actual.accept(instance);
			assertThat(instance).extracting("age").isEqualTo(123);
			assertThat(compiled.getSourceFile()).contains("instance.setAge(");
		});
	}

	@Test
	void generateCodeWhenPrivateMethodInjectsValueUsingReflection() {
		TestBeanWithPrivateMethod bean = new TestBeanWithPrivateMethod();
		Method method = method(bean.getClass(), "setAge", int.class);
		CodeBlock generatedCode = createGenerator(ClassName.get(TestBeanWithPrivateMethod.class))
				.generateInjectionCode(method, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		testCompiledResult(TEST_TARGET, generatedCode, TestBeanWithPrivateMethod.class, (actual, compiled) -> {
			TestBeanWithPrivateMethod instance = new TestBeanWithPrivateMethod();
			actual.accept(instance);
			assertThat(instance).extracting("age").isEqualTo(123);
			assertThat(compiled.getSourceFile()).contains("invokeMethod(");
		});
	}

	@Test
	void generateCodeWhenPublicMethodOnProtectedTypeInjectsValueUsingReflectionAndTypeReference() {
		ProtectedBean bean = new ProtectedBean();
		Method method = method(bean.getClass(), "setName", String.class);
		CodeBlock generatedCode = createGenerator(TEST_TARGET)
				.generateInjectionCode(method, INSTANCE_VARIABLE, CodeBlock.of("$S", "test"));
		testCompiledResult(TEST_TARGET, generatedCode, Object.class, (actual, compiled) -> {
			ProtectedBean instance = new ProtectedBean();
			actual.accept(instance);
			assertThat(instance.name).isEqualTo("test");
			assertThat(compiled.getSourceFile())
					.contains("ClassUtils.resolveClassName")
					.contains("\"" + ProtectedBean.class.getName() + "\"")
					.contains("invokeMethod(");
		});
	}

	@Test
	void generateCodeWhenPrivateMethodAddsHint() {
		TestBeanWithPrivateMethod bean = new TestBeanWithPrivateMethod();
		Method method = method(bean.getClass(), "setAge", int.class);
		createGenerator(TEST_TARGET).generateInjectionCode(
				method, INSTANCE_VARIABLE, CodeBlock.of("$L", 123));
		assertThat(RuntimeHintsPredicates.reflection()
				.onMethod(TestBeanWithPrivateMethod.class, "setAge").invoke()).accepts(this.hints);
	}

	private InjectionCodeGenerator createGenerator(ClassName target) {
		return new InjectionCodeGenerator(this.hints, target);
	}

	private static Field field(Class<?> target, String name) {
		Field field = ReflectionUtils.findField(target, name);
		assertThat(field).isNotNull();
		return field;
	}

	private static Method method(Class<?> target, String name, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(target, name, parameterTypes);
		assertThat(method).isNotNull();
		return method;
	}

	@SuppressWarnings("unchecked")
	private <T> void testCompiledResult(ClassName target, CodeBlock generatedCode, Class<T> instanceType,
			BiConsumer<Consumer<T>, Compiled> result) {
		JavaFile javaFile = createJavaFile(target, generatedCode, instanceType);
		TestCompiler.forSystem().compile(javaFile::writeTo,
				compiled -> result.accept(compiled.getInstance(Consumer.class), compiled));
	}

	private JavaFile createJavaFile(ClassName target, CodeBlock generatedCode, Class<?> instanceType) {
		TypeSpec.Builder builder = TypeSpec.classBuilder("Injector");
		builder.addModifiers(Modifier.PUBLIC);
		builder.addSuperinterface(ParameterizedTypeName.get(Consumer.class, instanceType));
		builder.addMethod(MethodSpec.methodBuilder("accept").addModifiers(Modifier.PUBLIC)
				.addParameter(instanceType, INSTANCE_VARIABLE).addCode(generatedCode).build());
		return JavaFile.builder(target.packageName(), builder.build()).build();
	}

	static class ProtectedBean {

		public String name;

		public void setName(String name) {
			this.name = name;
		}

	}

}
