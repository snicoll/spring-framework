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

import java.util.Collections;
import java.util.List;

/**
 * A {@link PatternResourceHint} for the bytecode of a given {@link Class}.
 *
 * @author Stephane Nicoll
 */
public class TypeResourceHint extends PatternResourceHint {

	private final TypeReference type;

	public TypeResourceHint(TypeReference type) {
		super(Format.EXACT_MATCH, List.of(toIncludePattern(type)), Collections.emptyList());
		this.type = type;
	}

	static String toIncludePattern(TypeReference type) {
		StringBuilder names = new StringBuilder();
		buildName(type, names);
		String candidate = type.getPackageName() + "." + names;
		return candidate.replace(".", "/") + ".class";
	}

	static void buildName(TypeReference type, StringBuilder sb) {
		if (type == null) {
			return;
		}
		String typeName = (type.getEnclosingType() != null) ? "$" + type.getName() : type.getName();
		sb.insert(0, typeName);
		buildName(type.getEnclosingType(), sb);
	}

	public TypeReference getType() {
		return this.type;
	}

}

