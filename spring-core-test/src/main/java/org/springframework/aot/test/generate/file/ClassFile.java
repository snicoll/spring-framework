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

import java.io.IOException;

import org.springframework.core.io.InputStreamSource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

/**
 * {@link DynamicFile} that holds Java class bytecode.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class ClassFile extends DynamicFile<byte[]> {

	private static final String CLASS_SUFFIX = ".class";

	private ClassFile(String name, byte[] content) {
		super(name, content);
	}

	/**
	 * Factory method to create a new {@link ClassFile} from the given
	 * {@code content}.
	 * @param name the fully qualified name of the class
	 * @param content the bytecode of the class
	 * @return a {@link ClassFile} instance
	 */
	public static ClassFile of(String name, byte[] content) {
		return new ClassFile(name, content);
	}

	/**
	 * Factory method to create a new {@link ClassFile} from the given
	 * {@link InputStreamSource}.
	 * @param name the fully qualified name of the class
	 * @param inputStreamSource the bytecode of the class
	 * @return a {@link ClassFile} instance
	 */
	public static ClassFile of(String name, InputStreamSource inputStreamSource) {
		return of(name, toBytes(inputStreamSource));
	}

	/**
	 * Return the name of a class based on its relative path.
	 * @param path the path of the class
	 * @return the class name
	 */
	public static String toClassName(String path) {
		Assert.hasText(path, "'path' must not be empty");
		Assert.state(path.endsWith(CLASS_SUFFIX), "'path' must end with '.class'");
		String name = path.replace('/', '.');
		return name.substring(0, name.length() - CLASS_SUFFIX.length());
	}

	private static byte[] toBytes(InputStreamSource inputStreamSource) {
		try {
			return FileCopyUtils.copyToByteArray(inputStreamSource.getInputStream());
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to read content", ex);
		}
	}

}
