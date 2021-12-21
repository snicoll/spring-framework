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

/**
 * Represent groups of members.
 *
 * @author Andy Clement
 * @author Sebastien Deleuze
 * @author Stephane Nicoll
 */
public enum MemberCategory {

	/**
	 * Public fields access.
	 * @see Class#getFields()
	 */
	PUBLIC_FIELDS("allPublicFields"),

	/**
	 * Declared fields access: public, protected, default (package) access, and private, but excluding inherited ones.
	 * @see Class#getDeclaredFields()
	 */
	DECLARED_FIELDS("allDeclaredFields"),

	/**
	 * Declared constructors access: public, protected, default (package) access, and private ones.
	 * @see Class#getDeclaredConstructors()
	 */
	DECLARED_CONSTRUCTORS("allDeclaredConstructors"),

	/**
	 * Public constructors.
	 * @see Class#getConstructors()
	 */
	PUBLIC_CONSTRUCTORS("allPublicConstructors"),

	/**
	 * Declared methods access: public, protected, default (package) access, and private, but excluding inherited ones.
	 * Consider whether you need this or @link {@link #PUBLIC_METHODS}.
	 * @see Class#getDeclaredMethods()
	 */
	DECLARED_METHODS("allDeclaredMethods"),

	/**
	 * Public methods access: public methods of the class including inherited ones.
	 * Consider whether you need this or @link {@link #DECLARED_METHODS}.
	 * @see Class#getMethods()
	 */
	PUBLIC_METHODS("allPublicMethods"),

	/**
	 * Do not automatically register the inner classes for reflective access but make them available via {@link Class#getDeclaredClasses}.
	 */
	DECLARED_CLASSES("allDeclaredClasses"),

	/**
	 * Do not automatically register the inner classes for reflective access but make them available via {@link Class#getClasses}.
	 */
	PUBLIC_CLASSES("allPublicClasses"),

	/**
	 * Declared method's metadata query: public, protected, default (package) access, and private, but excluding inherited ones.
	 * Consider whether you need this or @link {@link #QUERY_PUBLIC_METHODS}.
	 * @see Class#getDeclaredMethods()
	 */
	QUERY_DECLARED_METHODS("queryAllDeclaredMethods"),

	/**
	 * Public method's metadata query access: public methods of the class including inherited ones.
	 * Consider whether you need this or @link {@link #QUERY_DECLARED_METHODS}.
	 * @see Class#getMethods()
	 */
	QUERY_PUBLIC_METHODS("queryAllPublicMethods"),

	/**
	 * Declared constructor's metadata query: public, protected, default (package) access, and private ones.
	 * @see Class#getDeclaredConstructors()
	 */
	QUERY_DECLARED_CONSTRUCTORS("queryAllDeclaredConstructors"),

	/**
	 * Queried public constructors.
	 * @see Class#getConstructors()
	 */
	QUERY_PUBLIC_CONSTRUCTORS("queryAllPublicConstructors");

	private final String value;

	MemberCategory(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}
