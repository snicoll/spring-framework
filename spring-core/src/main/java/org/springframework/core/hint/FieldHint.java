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

import java.lang.reflect.Field;

/**
 * A reflection hint for a {@link Field}.
 *
 * @author Stephane Nicoll
 */
public final class FieldHint extends MemberHint {

	private final boolean allowWrite;

	private final boolean allowUnsafeAccess;

	private FieldHint(Builder builder) {
		super(builder.name);
		this.allowWrite = builder.allowWrite;
		this.allowUnsafeAccess = builder.allowUnsafeAccess;
	}

	public boolean isAllowWrite() {
		return this.allowWrite;
	}

	public boolean isAllowUnsafeAccess() {
		return this.allowUnsafeAccess;
	}

	public static class Builder {

		private final String name;

		private boolean allowWrite;

		private boolean allowUnsafeAccess;

		public Builder(String name) {
			this.name = name;
		}

		public Builder allowWrite(boolean allowWrite) {
			this.allowWrite = allowWrite;
			return this;
		}

		public Builder allowUnsafeAccess(boolean allowUnsafeAccess) {
			this.allowUnsafeAccess = allowUnsafeAccess;
			return this;
		}

		public FieldHint build() {
			return new FieldHint(this);
		}

	}
}
