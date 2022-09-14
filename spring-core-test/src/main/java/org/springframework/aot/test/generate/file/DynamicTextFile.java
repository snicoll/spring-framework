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

import org.springframework.util.Assert;

/**
 * A {@link DynamicFile} with a textual content.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public abstract sealed class DynamicTextFile extends DynamicFile<String> permits SourceFile, ResourceFile {

	protected DynamicTextFile(String name, String content) {
		super(name, content);
		Assert.hasText(content, "'content' must not be empty");
	}

	protected static String toString(WritableContent writableContent) {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			writableContent.writeTo(stringBuilder);
			return stringBuilder.toString();
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to read content", ex);
		}
	}

}
