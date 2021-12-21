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

package org.springframework.core.hint;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.hint.TypeHint.Builder;

/**
 * Gather the need for reflection at runtime.
 *
 * @author Stephane Nicoll
 */
public class ReflectionHints {

	private final Map<TypeReference, TypeHint.Builder> types = new HashMap<>();


	public Stream<TypeHint> typeHints() {
		return this.types.values().stream().map(TypeHint.Builder::build);
	}

	public ReflectionHints registerType(TypeReference type, Consumer<TypeHint.Builder> typeHint) {
		Builder builder = this.types.computeIfAbsent(type, TypeHint.Builder::new);
		typeHint.accept(builder);
		return this;
	}

	public ReflectionHints registerType(TypeReference type) {
		return registerType(type, typeHint -> {
		});
	}

	public ReflectionHints registerType(Class<?> type, Consumer<TypeHint.Builder> typeHint) {
		return registerType(TypeReference.of(type), typeHint);
	}

	public ReflectionHints registerType(Class<?> type) {
		return registerType(TypeReference.of(type));
	}

	public ReflectionHints registerField(Field field, Consumer<FieldHint.Builder> fieldHint) {
		return registerType(TypeReference.of(field.getDeclaringClass()),
				typeHint -> typeHint.withField(field.getName(), fieldHint));
	}

	public ReflectionHints registerField(Field field) {
		return registerField(field, fieldHint -> fieldHint.allowWrite(true));
	}

	public ReflectionHints registerConstructor(Constructor<?> constructor, Consumer<ExecutableHint.Builder> constructorHint) {
		return registerType(TypeReference.of(constructor.getDeclaringClass()),
				typeHint -> typeHint.withConstructor(mapParameters(constructor), constructorHint));
	}

	public ReflectionHints registerConstructor(Constructor<?> constructor) {
		return registerConstructor(constructor, constructorHint -> constructorHint.withMode(ExecutableMode.EXECUTE));
	}

	public ReflectionHints registerMethod(Method method, Consumer<ExecutableHint.Builder> methodHint) {
		return registerType(TypeReference.of(method.getDeclaringClass()),
				typeHint -> typeHint.withMethod(method.getName(), mapParameters(method), methodHint));
	}

	public ReflectionHints registerMethod(Method method) {
		return registerMethod(method, methodHint -> methodHint.withMode(ExecutableMode.EXECUTE));
	}

	private List<TypeReference> mapParameters(Executable executable) {
		return Arrays.stream(executable.getParameterTypes()).map(TypeReference::of).collect(Collectors.toList());
	}

}
