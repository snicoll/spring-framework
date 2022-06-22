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

import org.springframework.javapoet.ClassName;

/**
 *
 * @author Stephane Nicoll
 */
public class GenerationNamingStrategy {

	private final ClassNameGenerator classNameGenerator;

	private final Class<?> mainTarget;

	private final String name;

	GenerationNamingStrategy(ClassNameGenerator classNameGenerator, Class<?> mainTarget, String name) {
		this.classNameGenerator = classNameGenerator;
		this.mainTarget = mainTarget;
		this.name = name;
	}

	public GenerationNamingStrategy(Class<?> mainTarget, String name) {
		this(new ClassNameGenerator(), mainTarget, name);
	}


	public Builder forComponent(Class<?> target) {
		return new Builder(target);
	}

	public Builder forMainTarget(String featureName) {
		return new Builder(this.mainTarget).withQualifiedFeatureName(featureName);
	}


	public class Builder {

		private final Class<?> target;

		private String featureName;

		Builder(Class<?> target) {
			this.target = target;
		}

		public Builder withFeatureName(String featureName) {
			this.featureName = featureName;
			return this;
		}

		public Builder withQualifiedFeatureName(String featureName) {
			return withFeatureName(GenerationNamingStrategy.this.name + featureName);
		}

		public ClassName toUniqueClassName() {
			return GenerationNamingStrategy.this.classNameGenerator
					.generateClassName(this.target, this.featureName);
		}

		public ClassName toClassName() {
			// Move ClassNameGenerator here
			return toUniqueClassName();
		}

	}
}
