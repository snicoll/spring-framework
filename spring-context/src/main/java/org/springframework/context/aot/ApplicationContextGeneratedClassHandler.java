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

import java.util.function.Consumer;

import org.springframework.aot.generate.GeneratedFiles;
import org.springframework.aot.generate.GeneratedFiles.Kind;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint.Builder;
import org.springframework.aot.hint.TypeReference;
import org.springframework.cglib.core.GeneratedClassHandler;
import org.springframework.core.io.ByteArrayResource;

/**
 * A {@link GeneratedClassHandler} implementation that writes generated
 * classes and register hints automatically using a {@link GenerationContext}.
 *
 * @author Stephane Nicoll
 */
class ApplicationContextGeneratedClassHandler extends GeneratedClassHandler {

	private static final Consumer<Builder> asCglibProxy = hint ->
			hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

	private final RuntimeHints runtimeHints;

	private final GeneratedFiles generatedFiles;

	ApplicationContextGeneratedClassHandler(GenerationContext generationContext) {
		this.runtimeHints = generationContext.getRuntimeHints();
		this.generatedFiles = generationContext.getGeneratedFiles();
	}

	@Override
	public void generatedClass(String className, byte[] content) {
		this.runtimeHints.reflection().registerType(TypeReference.of(className), asCglibProxy);
		String path = className.replace(".", "/") + ".class";
		this.generatedFiles.addFile(Kind.CLASS, path, new ByteArrayResource(content));
	}

}
