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

package org.springframework.cglib.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * Hook point to handle generated classes.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public abstract class GeneratedClassHandler {

	private static final Map<Predicate<ClassLoader>, GeneratedClassHandler> CACHE = new WeakHashMap<>();

	/**
	 * Return the {@link GeneratedClassHandler} to use for the given {@link ClassLoader}.
	 * @param classLoader the classloader, or {@code null}
	 * @return the handler to use, never {@code null}
	 */
	public static GeneratedClassHandler get(ClassLoader classLoader) {
		List<GeneratedClassHandler> handlers = CACHE.entrySet().stream()
				.filter(entry -> entry.getKey().test(classLoader))
				.map(Entry::getValue).toList();
		return new CompositeGeneratedClassHandler(handlers);
	}

	/**
	 * Register that the specified {@link GeneratedClassHandler} should be invoked
	 * when the predicate for the specified {@link ClassLoader} matches.
	 * @param predicate the predicate, should tolerate a {@code null} class loader
	 * @param generatedClassHandler the handler to use if the predicate metches
	 */
	public static void register(Predicate<ClassLoader> predicate, GeneratedClassHandler generatedClassHandler) {
		CACHE.put(predicate, generatedClassHandler);
	}

	/**
	 * Remove the specified predicate and its associated {@link GeneratedClassHandler}.
	 * @param predicate the predicate to remove
	 * @return whether an entry was removed
	 */
	public static boolean clear(Predicate<ClassLoader> predicate) {
		return (CACHE.remove(predicate) != null);
	}

	static void clearAll() {
		CACHE.clear();
	}

	/**
	 * Handle a generated class.
	 * @param className the fully qualified name associated with the generated class
	 * @param content the content of the generation class
	 * @throws IOException if an IO operation occurs while processing the generated class
	 */
	public abstract void generatedClass(String className, byte[] content) throws IOException;


	private static class CompositeGeneratedClassHandler extends GeneratedClassHandler {

		private final Iterable<? extends GeneratedClassHandler> handlers;

		CompositeGeneratedClassHandler(Iterable<? extends GeneratedClassHandler> handlers) {
			this.handlers = handlers;
		}

		@Override
		public void generatedClass(String className, byte[] content) throws IOException {
			for (GeneratedClassHandler handler : this.handlers) {
				handler.generatedClass(className, content);
			}
		}

	}

}


