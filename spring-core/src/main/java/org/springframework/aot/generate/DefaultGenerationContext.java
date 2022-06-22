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
import java.util.function.Supplier;

import org.springframework.aot.generate.GeneratedClass.JavaFileGenerator;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.javapoet.ClassName;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link GenerationContext}.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public class DefaultGenerationContext implements GenerationContext {

	private final GenerationNamingStrategy generationNamingStrategy;

	private final GeneratedFiles generatedFiles;

	private final RuntimeHints runtimeHints;

	private final Map<JavaFileGenerator, GeneratedClass> classes;

	/**
	 * Create a new {@link DefaultGenerationContext} instance backed by the
	 * specified {@code generatedFiles}.
	 *
	 * @param generatedFiles the generated files
	 */
	public DefaultGenerationContext(GenerationNamingStrategy generationNamingStrategy,
			GeneratedFiles generatedFiles) {
		Assert.notNull(generationNamingStrategy, "'generationNamingStrategy' must not be null");
		Assert.notNull(generatedFiles, "'generatedFiles' must not be null");
		this.generationNamingStrategy = generationNamingStrategy;
		this.generatedFiles = generatedFiles;
		this.runtimeHints = new RuntimeHints();
		this.classes = new ConcurrentHashMap<>();
	}

	@Override
	public GenerationNamingStrategy getNamingStrategy() {
		return this.generationNamingStrategy;
	}

	@Override
	public GeneratedClass getGeneratedClass(JavaFileGenerator javaFileGenerator,
			Supplier<ClassName> className) {
		Assert.notNull(javaFileGenerator, "'javaFileGenerator' must not be null");
		Assert.notNull(className, "'className' must not be null");
		return this.classes.computeIfAbsent(javaFileGenerator,
				key -> new GeneratedClass(javaFileGenerator, className.get()));
	}

	@Override
	public GeneratedFiles getGeneratedFiles() {
		return this.generatedFiles;
	}

	@Override
	public RuntimeHints getRuntimeHints() {
		return this.runtimeHints;
	}

	@Override
	public GenerationContext withName(String name) {
		// TODO: keeping things in `GeneratedFiles` or similar makes it straightforward
		// to create clone.
		return null;
	}

	@Override
	public void close() throws IOException {
		writeGeneratedContent();
	}

	public void writeGeneratedContent() {
		List<GeneratedClass> generatedClasses = new ArrayList<>(this.classes.values());
		generatedClasses.sort(Comparator.comparing(GeneratedClass::getName));
		for (GeneratedClass generatedClass : generatedClasses) {
			this.generatedFiles.addSourceFile(generatedClass.generateJavaFile());
		}
	}

}

