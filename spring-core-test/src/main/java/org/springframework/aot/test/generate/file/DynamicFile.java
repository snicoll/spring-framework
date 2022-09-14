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

package org.springframework.aot.test.generate.file;

import java.util.Objects;

import org.springframework.util.Assert;

/**
 * Abstract base class for dynamically generated files.
 *
 * @author Phillip Webb
 * @since 6.0
 * @param <T> the content type
 * @see SourceFile
 * @see ResourceFile
 * @see ClassFile
 */
public abstract class DynamicFile<T> {

	private final String name;

	private final T content;


	protected DynamicFile(String name, T content) {
		Assert.hasText(name, "'name' must not be empty");
		Assert.notNull(content, "'content' must not be null");
		this.name = name;
		this.content = content;
	}

	/**
	 * Return the contents of the file.
	 * @return the file contents
	 */
	public T getContent() {
		return this.content;
	}

	/**
	 * Return the name of the file.
	 * @return the file name
	 */
	public String getName() {
		return this.name;
	}


	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		DynamicFile<?> other = (DynamicFile<?>) obj;
		return Objects.equals(this.name, other.name)
				&& Objects.equals(this.content, other.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.content);
	}

}
