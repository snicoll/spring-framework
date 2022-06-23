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

import java.util.Collection;
import java.util.Collections;

import org.springframework.cglib.core.ClassGenerator;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.JavaFile;
import org.springframework.util.Assert;

/**
 * A generated class.
 *
 * @author Phillip Webb
 * @since 6.0
 * @see GeneratedClasses
 * @see ClassGenerator
 */
public final class GeneratedClass {

	private final JavaFileGenerator JavaFileGenerator;

	private final ClassName name;

	private final GeneratedMethods methods;


	/**
	 * Create a new {@link GeneratedClass} instance with the given name. This
	 * constructor is package-private since names should only be generated via a
	 * {@link GeneratedClasses}.
	 * @param name the generated name
	 */
	GeneratedClass(JavaFileGenerator javaFileGenerator, ClassName name) {
		MethodNameGenerator methodNameGenerator = new MethodNameGenerator(
				javaFileGenerator.getReservedMethodNames());
		this.JavaFileGenerator = javaFileGenerator;
		this.name = name;
		this.methods = new GeneratedMethods(methodNameGenerator);
	}


	/**
	 * Return the name of the generated class.
	 * @return the name of the generated class
	 */
	public ClassName getName() {
		return this.name;
	}

	/**
	 * Return the method generator that can be used for this generated class.
	 * @return the method generator
	 */
	public MethodGenerator getMethodGenerator() {
		return this.methods;
	}

	JavaFile generateJavaFile() {
		JavaFile javaFile = this.JavaFileGenerator.generateJavaFile(this.name,
				this.methods);
		Assert.state(this.name.packageName().equals(javaFile.packageName),
				() -> "Generated JavaFile should be in package '"
						+ this.name.packageName() + "'");
		Assert.state(this.name.simpleName().equals(javaFile.typeSpec.name),
				() -> "Generated JavaFile should be named '" + this.name.simpleName()
						+ "'");
		return javaFile;
	}

	/**
	 * Strategy used to generate the java file for the generated class.
	 * Implementations of this interface are included as part of the key used to
	 * identify classes that have already been created and as such should be
	 * static final instances or implement a valid
	 * {@code equals}/{@code hashCode}.
	 */
	@FunctionalInterface
	public interface JavaFileGenerator {

		/**
		 * Generate the file {@link JavaFile} to be written.
		 * @param className the class name of the file
		 * @param methods the generated methods that must be included
		 * @return the generated files
		 */
		JavaFile generateJavaFile(ClassName className, GeneratedMethods methods);

		/**
		 * Return method names that must not be generated.
		 * @return the reserved method names
		 */
		default Collection<String> getReservedMethodNames() {
			return Collections.emptySet();
		}

	}

}
