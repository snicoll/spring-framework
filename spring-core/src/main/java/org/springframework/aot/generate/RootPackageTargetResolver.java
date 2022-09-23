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

import java.util.List;
import java.util.function.UnaryOperator;

import org.springframework.javapoet.ClassName;

/**
 * A target {@link ClassName} resolver that applies a configurable root
 * package. This allows generated code to be gathered in a configurable root,
 * whilst keeping the original structure.
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
public class RootPackageTargetResolver implements UnaryOperator<ClassName> {

	private final String rootPackageName;

	public RootPackageTargetResolver(String rootPackageName) {
		this.rootPackageName = rootPackageName;
	}

	@Override
	public ClassName apply(ClassName target) {
		String packageName = this.rootPackageName + "." + target.packageName();
		List<String> simpleNames = target.simpleNames();
		int lastElementIndex = simpleNames.size() - 1;
		String name = simpleNames.get(lastElementIndex);
		String[] additionalNames = (simpleNames.size() > 1)
				? simpleNames.subList(0, lastElementIndex).toArray(String[]::new)
				: new String[0];
		return ClassName.get(packageName, name, additionalNames);
	}

}
