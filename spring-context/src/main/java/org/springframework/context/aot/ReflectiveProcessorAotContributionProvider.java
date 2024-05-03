/*
 * Copyright 2002-2024 the original author or authors.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.aot.hint.annotation.ReflectiveProcessor;
import org.springframework.aot.hint.annotation.ReflectiveRuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * Helper class to create an AOT contribution that detects the presence of
 * {@link Reflective @Reflective} on annotated elements and invoke the underlying
 * {@link ReflectiveProcessor} implementations.
 *
 * @author Stephane Nicoll
 * @since 6.2
 */
public abstract class ReflectiveProcessorAotContributionProvider {

	private static final ReflectiveRuntimeHintsRegistrar registrar = new ReflectiveRuntimeHintsRegistrar();

	/**
	 * Create an AOT contribution from the given classes by checking the ones
	 * that use {@link Reflective}. The returned contribution registers the
	 * necessary reflection hints as a result. If no class amongst the given
	 * classes use {@link Reflective}, or if the given iterable is empty,
	 * returns {@code null}.
	 * @param classes the classes to inspect
	 * @return an AOT contribution for the classes that use {@link Reflective}
	 * or {@code null} if they aren't any
	 */
	@Nullable
	public static BeanFactoryInitializationAotContribution from(Iterable<Class<?>> classes) {
		return from(StreamSupport.stream(classes.spliterator(), false).toArray(Class<?>[]::new));
	}

	/**
	 * Create an AOT contribution from the given classes by checking the ones
	 * that use {@link Reflective}. The returned contribution registers the
	 * necessary reflection hints as a result. If no class amongst the given
	 * classes use {@link Reflective}, or if the given iterable is empty,
	 * returns {@code null}.
	 * @param classes the classes to inspect
	 * @return an AOT contribution for the classes that use {@link Reflective}
	 * or {@code null} if they aren't any
	 */
	@Nullable
	public static BeanFactoryInitializationAotContribution from(Class<?>[] classes) {
		Class<?>[] types = Arrays.stream(classes).filter(registrar::isCandidate).toArray(Class<?>[]::new);
		return (types.length > 0 ? new AotContribution(types) : null);
	}

	/**
	 * Scan the given {@code packageNames} and their sub-packages for classes
	 * that uses {@link Reflective} and create an AOT contribution with the
	 * result. If no candidates were found, return {@code null}.
	 * <p>This performs a "deep scan" by loading every class in the specified
	 * packages and search for {@link Reflective} on types, constructors, methods,
	 * and fields. Enclosed classes are candidates as well. Classes that fail to
	 * load are ignored.
	 * @param classLoader the classloader to use
	 * @param packageNames the package names to scan
	 * @return an AOT contribution for the identified classes or {@code null} if
	 * they aren't any
	 */
	@Nullable
	public static BeanFactoryInitializationAotContribution scan(@Nullable ClassLoader classLoader, String... packageNames) {
		ReflectiveClassPathScanner scanner = new ReflectiveClassPathScanner(classLoader);
		Class<?>[] types = scanner.scan(packageNames);
		return (types.length > 0 ? new AotContribution(types) : null);
	}

	private static class AotContribution implements BeanFactoryInitializationAotContribution {

		private final Class<?>[] classes;

		public AotContribution(Class<?>[] classes) {
			this.classes = classes;
		}

		@Override
		public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode) {
			RuntimeHints runtimeHints = generationContext.getRuntimeHints();
			registrar.registerRuntimeHints(runtimeHints, this.classes);
		}

	}

	private static class ReflectiveClassPathScanner extends ClassPathScanningCandidateComponentProvider {

		@Nullable
		private final ClassLoader classLoader;

		ReflectiveClassPathScanner(@Nullable ClassLoader classLoader) {
			super(false);
			this.classLoader = classLoader;
			addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
		}

		Class<?>[] scan(String... packageNames) {
			if (logger.isDebugEnabled()) {
				logger.debug("Scanning all types for reflective usage from " + Arrays.toString(packageNames));
			}
			Set<BeanDefinition> candidates = new HashSet<>();
			for (String packageName : packageNames) {
				candidates.addAll(findCandidateComponents(packageName));
			}
			return candidates.stream().map(c -> (Class<?>) c.getAttribute("type")).toArray(Class<?>[]::new);
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			String className = beanDefinition.getBeanClassName();
			if (className != null) {
				try {
					Class<?> type = ClassUtils.forName(className, this.classLoader);
					beanDefinition.setAttribute("type", type);
					return registrar.isCandidate(type);
				}
				catch (Exception ex) {
					if (logger.isTraceEnabled()) {
						logger.trace("Ignoring '%s' for reflective usage: %s".formatted(className, ex.getMessage()));
					}
				}
			}
			return false;
		}
	}

}
