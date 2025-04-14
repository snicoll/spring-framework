/*
 * Copyright 2002-2025 the original author or authors.
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

package org.springframework.web.service.registry;

import java.util.LinkedHashSet;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.generate.ValueCodeGenerator;
import org.springframework.javapoet.CodeBlock;
import org.springframework.web.service.registry.GroupsMetadata.DefaultRegistration;

final class GroupsMetadataValueDelegate implements ValueCodeGenerator.Delegate {

	@Override
	public @Nullable CodeBlock generateCode(ValueCodeGenerator valueCodeGenerator, Object value) {
		if (value instanceof DefaultRegistration registration) {
			return generateRegistrationCode(valueCodeGenerator, registration);
		}
		if (value instanceof GroupsMetadata groupsMetadata) {
			return generateGroupsMetadataCode(valueCodeGenerator, groupsMetadata);
		}
		return null;
	}

	public CodeBlock generateRegistrationCode(ValueCodeGenerator
			valueCodeGenerator, DefaultRegistration value) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("new $T($S, $L, $L)", DefaultRegistration.class, value.name(),
				valueCodeGenerator.generateCode(value.clientType()),
				!value.httpServiceTypeNames().isEmpty() ?
						valueCodeGenerator.generateCode(value.httpServiceTypeNames()) :
						CodeBlock.of("new $T()", LinkedHashSet.class));
		return code.build();
	}

	private CodeBlock generateGroupsMetadataCode(ValueCodeGenerator valueCodeGenerator, GroupsMetadata groupsMetadata) {
		CodeBlock.Builder code = CodeBlock.builder();
		code.add("new $T($L)", GroupsMetadata.class, valueCodeGenerator.generateCode(groupsMetadata.getMetadata()));
		return code.build();
	}

}
