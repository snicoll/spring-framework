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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.aot.generate.GeneratedClass.JavaFileGenerator;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.TypeSpec;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A managed collection of generated classes. This class is stateful so the
 * same instance should be used for all class generation.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 * @see GeneratedClass
 */
public class GeneratedClasses {

	private final ClassNameGenerator classNameGenerator;

	private final List<GeneratedClass> classes;

	private final Map<Owner, GeneratedClass> classesByOwner;

	GeneratedClasses(ClassNameGenerator classNameGenerator) {
		this(classNameGenerator, new ArrayList<>(), new ConcurrentHashMap<>());
	}

	private GeneratedClasses(ClassNameGenerator classNameGenerator,
			List<GeneratedClass> classes, Map<Owner, GeneratedClass> classesByOwner) {
		Assert.notNull(classNameGenerator, "'classNameGenerator' must not be null");
		this.classNameGenerator = classNameGenerator;
		this.classes = classes;
		this.classesByOwner = classesByOwner;
	}

	/**
	 * Prepare a {@link GeneratedClass} for the specified {@code featureName}
	 * targeting the specified {@code component}.
	 * @param featureName the name of the feature to associate with the generated class
	 * @param component the target component
	 * @return a {@link Builder} for further configuration
	 */
	public Builder forFeatureComponent(String featureName, Class<?> component) {
		Assert.hasLength(featureName, "'featureName' must not be empty");
		Assert.notNull(component, "'component' must not be null");
		return new Builder(featureName, component);
	}

	/**
	 * Prepare a {@link GeneratedClass} for the specified {@code featureName}
	 * and no particular component. This should be used for high-level code
	 * generation that are widely applicable and for entry points.
	 * @param featureName the name of the feature to associate with the generated class
	 * @return a {@link Builder} for further configuration
	 */
	public Builder forFeature(String featureName) {
		Assert.hasLength(featureName, "'featureName' must not be empty");
		return new Builder(featureName, null);
	}

	/**
	 * Write the {@link GeneratedClass generated classes} using the given
	 * {@link GeneratedFiles} instance.
	 * @param generatedFiles where to write the generated classes
	 * @throws IOException on IO error
	 */
	public void writeTo(GeneratedFiles generatedFiles) throws IOException {
		Assert.notNull(generatedFiles, "'generatedFiles' must not be null");
		List<GeneratedClass> generatedClasses = new ArrayList<>(this.classes);
		generatedClasses.sort(Comparator.comparing(GeneratedClass::getName));
		for (GeneratedClass generatedClass : generatedClasses) {
			generatedFiles.addSourceFile(generatedClass.generateJavaFile());
		}
	}

	GeneratedClasses withName(String name) {
		return new GeneratedClasses(this.classNameGenerator.usingFeatureNamePrefix(name),
				this.classes, this.classesByOwner);
	}

	private record Owner(String facet, String className) {

	}

	public class Builder {

		private final String featureName;

		@Nullable
		private final Class<?> target;


		Builder(String featureName, @Nullable Class<?> target) {
			this.target = target;
			this.featureName = featureName;
		}

		/**
		 * Generate a new {@link GeneratedClass} using the specified
		 * {@link JavaFileGenerator}.
		 * @param javaFileGenerator the generator to use
		 * @return a new {@link GeneratedClass}
		 */
		public GeneratedClass generate(JavaFileGenerator javaFileGenerator) {
			Assert.notNull(javaFileGenerator, "'javaFileGenerator' must not be null");
			return createGeneratedClass(javaFileGenerator);
		}

		/**
		 * Generate a new {@link GeneratedClass} using the specified type
		 * customizer.
		 * @param typeSpecCustomizer a customizer for the {@link TypeSpec.Builder}
		 * @return a new {@link GeneratedClass}
		 */
		public GeneratedClass generate(Consumer<TypeSpec.Builder> typeSpecCustomizer) {
			Assert.notNull(typeSpecCustomizer, "'typeSpecCustomizer' must not be null");
			return generate(new SimpleJavaFileGenerator(typeSpecCustomizer));
		}

		/**
		 * Get or generate a new {@link GeneratedClass} for the specified {@code facet}.
		 * @param facet a unique identifier
		 * @param javaFileGenerator the java file generator
		 * @return a {@link GeneratedClass} instance
		 */
		public GeneratedClass getOrGenerate(String facet, Supplier<JavaFileGenerator> javaFileGenerator) {
			Assert.hasLength(facet, "'facet' must not be empty");
			Assert.notNull(javaFileGenerator, "'javaFileGenerator' must not be null");
			Owner owner = new Owner(facet, GeneratedClasses.this.classNameGenerator
					.getClassName(this.target, this.featureName));
			return GeneratedClasses.this.classesByOwner.computeIfAbsent(owner,
					key -> createGeneratedClass(javaFileGenerator.get()));
		}

		/**
		 * Get or generate a new {@link GeneratedClass} for the specified {@code facet}.
		 * @param facet a unique identifier
		 * @param typeSpecCustomizer a customizer for the {@link TypeSpec.Builder}
		 * @return a {@link GeneratedClass} instance
		 */
		public GeneratedClass getOrGenerate(String facet,
				Consumer<TypeSpec.Builder> typeSpecCustomizer) {
			Assert.notNull(typeSpecCustomizer, "'typeSpecCustomizer' must not be null");
			return getOrGenerate(facet, () -> new SimpleJavaFileGenerator(typeSpecCustomizer));
		}

		private GeneratedClass createGeneratedClass(JavaFileGenerator javaFileGenerator) {
			ClassName className = GeneratedClasses.this.classNameGenerator
					.generateClassName(this.target, this.featureName);
			GeneratedClass generatedClass = new GeneratedClass(javaFileGenerator, className);
			GeneratedClasses.this.classes.add(generatedClass);
			return generatedClass;
		}

	}

	static class SimpleJavaFileGenerator implements JavaFileGenerator {

		private final Consumer<TypeSpec.Builder> typeSpecCustomizer;

		SimpleJavaFileGenerator(Consumer<TypeSpec.Builder> typeSpecCustomizer) {
			this.typeSpecCustomizer = typeSpecCustomizer;
		}

		@Override
		public JavaFile generateJavaFile(ClassName className, GeneratedMethods methods) {
			TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className);
			this.typeSpecCustomizer.accept(typeSpecBuilder);
			methods.doWithMethodSpecs(typeSpecBuilder::addMethod);
			return JavaFile.builder(className.packageName(), typeSpecBuilder.build())
					.build();
		}

	}

}
