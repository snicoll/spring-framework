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

import java.util.Objects;

import org.springframework.lang.Nullable;

/**
 * Type abstraction that can be used to refer to types that are not available as
 * a {@link Class} yet.
 *
 * @author Stephane Nicoll
 */
public interface TypeReference {

	String getCanonicalName();

	String getPackageName();

	String getName();

	@Nullable
	TypeReference getEnclosingType();

	static TypeReference of(Class<?> type) {
		return new ReflectionTypeReference(type);
	}


	class ReflectionTypeReference implements TypeReference {

		private final Class<?> type;

		@Nullable
		private final TypeReference enclosing;

		private ReflectionTypeReference(Class<?> type) {
			this.type = type;
			this.enclosing = (type.getEnclosingClass() != null
					? TypeReference.of(type.getEnclosingClass()) : null);
		}

		@Override
		public String getCanonicalName() {
			return this.type.getCanonicalName();
		}

		@Override
		public String getPackageName() {
			return this.type.getPackageName();
		}

		@Override
		public String getName() {
			return this.type.getSimpleName();
		}

		@Override
		public TypeReference getEnclosingType() {
			return this.enclosing;
		}

		@Override
		public String toString() {
			return this.type.getName();
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ReflectionTypeReference that = (ReflectionTypeReference) o;
			return this.type.equals(that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type);
		}
	}

}
