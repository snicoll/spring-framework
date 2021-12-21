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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.ObjectUtils;

/**
 * A reflection hint for a method or constructor.
 *
 * @author Stephane Nicoll
 */
public final class ExecutableHint extends MemberHint {

	private final List<TypeReference> parameterTypes;

	private final List<ExecutableMode> modes;

	private ExecutableHint(Builder builder) {
		super(builder.name);
		this.parameterTypes = new ArrayList<>(builder.parameterTypes);
		this.modes = new ArrayList<>(builder.modes);
	}

	public static Builder ofConstructor(List<TypeReference> parameterTypes) {
		return new Builder("<init>", parameterTypes);
	}

	public static Builder ofMethod(String name, List<TypeReference> parameterTypes) {
		return new Builder(name, parameterTypes);
	}

	public List<TypeReference> getParameterTypes() {
		return this.parameterTypes;
	}

	public List<ExecutableMode> getModes() {
		return this.modes;
	}

	public static final class Builder {

		private final String name;

		private final List<TypeReference> parameterTypes;

		private final Set<ExecutableMode> modes = new LinkedHashSet<>();

		private Builder(String name, List<TypeReference> parameterTypes) {
			this.name = name;
			this.parameterTypes = parameterTypes;
		}

		public Builder withMode(ExecutableMode mode) {
			this.modes.add(mode);
			return this;
		}

		public Builder setModes(ExecutableMode... modes) {
			this.modes.clear();
			if (!ObjectUtils.isEmpty(modes)) {
				this.modes.addAll(Arrays.asList(modes));
			}
			return this;
		}

		public ExecutableHint build() {
			return new ExecutableHint(this);
		}

	}

}
