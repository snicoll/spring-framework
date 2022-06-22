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

import java.io.IOException;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import org.springframework.aot.generate.GeneratedClass.JavaFileGenerator;
import org.springframework.aot.generate.GeneratedFiles.Kind;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.TypeSpec;
import org.springframework.javapoet.TypeSpec.Builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests for {@link GeneratedClasses}.
 *
 * @author Phillip Webb
 */
class GeneratedClassesTests {

	private final GeneratedClasses generatedClasses = new GeneratedClasses(
			new ClassNameGenerator(TestComponent.class, ""));

	@Test
	void createWhenClassNameGeneratorIsNullThrowsException() {
		assertThatIllegalArgumentException().isThrownBy(() -> new GeneratedClasses(null))
				.withMessage("'classNameGenerator' must not be null");
	}

	@Test
	void forFeatureComponentWhenTargetIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.generatedClasses.forFeatureComponent("test", null))
				.withMessage("'component' must not be null");
	}

	@Test
	void forFeatureComponentWhenFeatureNameIsEmptyThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.generatedClasses.forFeatureComponent("", TestComponent.class))
				.withMessage("'featureName' must not be empty");
	}

	@Test
	void forFeatureWhenFeatureNameIsEmptyThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.generatedClasses.forFeature(""))
				.withMessage("'featureName' must not be empty");
	}

	@Test
	void generateWhenJavaFileGeneratorIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.generatedClasses
						.forFeatureComponent("test", TestComponent.class).generate((JavaFileGenerator) null))
				.withMessage("'javaFileGenerator' must not be null");
	}

	@Test
	void generateWhenTypeSpecCustomizerIsNullThrowsException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.generatedClasses
						.forFeatureComponent("test", TestComponent.class).generate((Consumer<TypeSpec.Builder>) null))
				.withMessage("'typeSpecCustomizer' must not be null");
	}

	@Test
	void generateReturnsDifferentInstances() {
		Consumer<Builder> typeCustomizer = mockTypeCustomizer();
		GeneratedClass generatedClass1 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).generate(typeCustomizer);
		GeneratedClass generatedClass2 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).generate(typeCustomizer);
		assertThat(generatedClass1).isNotSameAs(generatedClass2);
		assertThat(generatedClass1.getName().simpleName()).endsWith("__One");
		assertThat(generatedClass2.getName().simpleName()).endsWith("__One1");
	}

	@Test
	void getOrGenerateWhenNewReturnsGeneratedMethod() {
		Consumer<Builder> typeCustomizer = mockTypeCustomizer();
		GeneratedClass generatedClass1 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).getOrGenerate("facet", typeCustomizer);
		GeneratedClass generatedClass2 = this.generatedClasses
				.forFeatureComponent("two", TestComponent.class).getOrGenerate("facet", typeCustomizer);
		assertThat(generatedClass1).isNotNull().isNotEqualTo(generatedClass2);
		assertThat(generatedClass2).isNotNull();
	}

	@Test
	void getOrGenerateWhenRepeatReturnsSameGeneratedMethod() {
		Consumer<Builder> typeCustomizer = mockTypeCustomizer();
		GeneratedClass generatedClass1 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).getOrGenerate("facet", typeCustomizer);
		GeneratedClass generatedClass2 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).getOrGenerate("facet", typeCustomizer);
		GeneratedClass generatedClass3 = this.generatedClasses
				.forFeatureComponent("one", TestComponent.class).getOrGenerate("facet", typeCustomizer);
		assertThat(generatedClass1).isNotNull().isSameAs(generatedClass2)
				.isSameAs(generatedClass3);
		verifyNoInteractions(typeCustomizer);
		generatedClass1.generateJavaFile();
		verify(typeCustomizer).accept(any());
	}

	@Test
	void writeToInvokeJavaFileGenerator() throws IOException {
		JavaFileGenerator javaFileGenerator = mock(JavaFileGenerator.class);
		GeneratedClass generatedClass = this.generatedClasses.
				forFeatureComponent("one", TestComponent.class).generate(javaFileGenerator);
		given(javaFileGenerator.generateJavaFile(any(), any())).willReturn(
				JavaFile.builder(generatedClass.getName().packageName(), TypeSpec.classBuilder(
						generatedClass.getName()).build()).build());
		InMemoryGeneratedFiles generatedFiles = new InMemoryGeneratedFiles();
		this.generatedClasses.writeTo(generatedFiles);
		verify(javaFileGenerator).generateJavaFile(generatedClass.getName(),
				(GeneratedMethods) generatedClass.getMethodGenerator());
		assertThat(generatedFiles.getGeneratedFiles(Kind.SOURCE)).hasSize(1);
	}


	@SuppressWarnings("unchecked")
	private Consumer<TypeSpec.Builder> mockTypeCustomizer() {
		return mock(Consumer.class);
	}


	private static class TestComponent {

	}

}
