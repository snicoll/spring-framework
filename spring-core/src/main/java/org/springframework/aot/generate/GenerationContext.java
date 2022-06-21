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

import java.io.Closeable;
import java.util.function.Supplier;

import org.springframework.aot.generate.GeneratedClass.JavaFileGenerator;
import org.springframework.aot.hint.ProxyHints;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.ResourceHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.SerializationHints;
import org.springframework.javapoet.ClassName;

/**
 * Central interface used for code generation.
 * <p>
 * A generation context provides:
 * <ul>
 * <li>Support for class name generation.</li>
 * <li>Central management for {@link GeneratedClass generated classes}.</li>
 * <li>Central management of all {@link #getGeneratedFiles() generated files}.</li>
 * <li>Support for the recording of {@link #getRuntimeHints() runtime hints}.</li>
 * </ul>
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 6.0
 */
public interface GenerationContext extends Closeable {

	GenerationContext usingNamingConvention(String name);

	/**
	 * Generate a unique {@link ClassName} based on the specified {@code target}
	 * class and {@code featureName}.
	 * <p>The generated class name is a suffixed version of the {@code target}.
	 * For instance, a {@code com.example.Demo} target with an
	 * {@code Initializer} feature name leads to a
	 * {@code com.example.Demo__Initializer} generated class name. If such a
	 * feature was already requested for this target, a counter is used to
	 * ensure uniqueness.
	 * @param target the class the newly generated class relates to
	 * @param featureName the name of the feature that the generated class
	 * supports
	 * @return a unique generated class name
	 */
	ClassName generateClassName(Class<?> target, String featureName);

	/**
	 * Generate a unique qualified {@link ClassName} based on the specified
	 * {@code target} class and {@code featureName}.
	 * <p>The generated class name is a suffixed version of the {@code target},
	 * using a qualifier specific to the current execution. For instance, a
	 * {@code com.example.Demo} target with an {@code Initializer} feature name
	 * leads to a {@code com.example.Demo__MyAppInitializer} generated class name,
	 * where {@code MyApp} is the current qualifier. If such a  feature was already
	 * requested for this target, a counter is used to ensure uniqueness.
	 * @param target the class the newly generated class relates to
	 * @param featureName the name of the feature that the generated class
	 * supports
	 * @return a unique generated class name
	 */
	ClassName generateQualifiedClassName(Class<?> target, String featureName);

	/**
	 * Generate a unique {@link ClassName} for the current application, based
	 * on the specified feature name.
	 * @param featureName the name of the feature that the application supports
	 * @return a unique generated class name
	 */
	ClassName generateQualifiedClassName(String featureName);

	/**
	 * Get the {@link GeneratedClass} identified by the specified generator and
	 * {@link ClassName}. If such an instance does not exist, it is created.
	 * @param generator the {@link JavaFileGenerator} to use
	 * @param className the class name
	 * @return the generated class for that key and class name
	 */
	GeneratedClass getGeneratedClass(JavaFileGenerator generator, Supplier<ClassName> className);


	/**
	 * Return the {@link RuntimeHints} being used by the context. Used to record
	 * {@link ReflectionHints reflection}, {@link ResourceHints resource},
	 * {@link SerializationHints serialization} and {@link ProxyHints proxy}
	 * hints so that the application can run in restricted environment.
	 * @return the runtime hints
	 */
	RuntimeHints getRuntimeHints();

	/**
	 * Return the {@link GeneratedFiles} being used by the context. Used to
	 * write resource, java source or class bytecode files.
	 * @return the generated files
	 */
	GeneratedFiles getGeneratedFiles();

}
