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

package org.springframework.orm.jpa;

import java.lang.reflect.Executable;
import java.net.URL;
import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.aot.generate.GeneratedMethod;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.aot.BeanRegistrationCodeFragments;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.javapoet.CodeBlock;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.lang.Nullable;

/**
 * {@link BeanRegistrationAotProcessor} implementations for JPA support.
 *
 * <p>Allows a {@link JpaManagedTypes} to be instantiated at build-time
 * and replace the runtime behavior with an hard-coded list of managed
 * types
 *
 * @author Stephane Nicoll
 * @since 6.0
 */
class JpaManagedTypesBeanRegistrationAotProcessor implements BeanRegistrationAotProcessor {

	@Nullable
	@Override
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {
		if (JpaManagedTypes.class.isAssignableFrom(registeredBean.getBeanClass())) {
			return BeanRegistrationAotContribution.ofBeanRegistrationCodeFragmentsCustomizer(codeFragments ->
					new JpaManagedTypesBeanRegistrationCodeFragments(codeFragments, registeredBean));
		}
		return null;
	}

	private static class JpaManagedTypesBeanRegistrationCodeFragments extends BeanRegistrationCodeFragments {

		private static final String INSTANCE_NAME = "jpaManagedTypes";

		private final RegisteredBean registeredBean;

		private final JpaManagedTypes jpaManagedTypes;

		public JpaManagedTypesBeanRegistrationCodeFragments(BeanRegistrationCodeFragments codeFragments,
				RegisteredBean registeredBean) {
			super(codeFragments);
			this.registeredBean = registeredBean;
			this.jpaManagedTypes = registeredBean.getBeanFactory().getBean(registeredBean.getBeanName(), JpaManagedTypes.class);
		}

		@Override
		public CodeBlock generateInstanceSupplierCode(GenerationContext generationContext,
				BeanRegistrationCode beanRegistrationCode,
				Executable constructorOrFactoryMethod,
				boolean allowDirectSupplierShortcut) {

			GeneratedMethod generatedMethod = beanRegistrationCode.getMethods()
					.add("Instance", method -> {
						Class<?> beanType = SimpleJpaManagedTypes.class;
						ParameterizedTypeName listOfStrings = ParameterizedTypeName.get(List.class, String.class);
						method.addJavadoc(
								"Create the JPA managed types bean instance for '$L'.",
								this.registeredBean.getBeanName());
						method.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
						method.returns(beanType);
						method.addStatement("$T managedClassNames = $T.of($L)", listOfStrings,
								List.class, toCodeBlock(this.jpaManagedTypes.getManagedClassNames()));
						method.addStatement("$T managedPackages = $T.of($L)", listOfStrings,
								List.class, toCodeBlock(this.jpaManagedTypes.getManagedPackages()));
						URL persistenceUnitRootUrl = this.jpaManagedTypes.getPersistenceUnitRootUrl();
						if (persistenceUnitRootUrl != null) {
							method.addStatement("return new $T($L,$L,$L)", "managedClassNames", "managedPackages",
									persistenceUnitRootUrl);
						}
						method.addStatement("return new $T($L, $L)", beanType, "managedClassNames", "managedPackages");
					});
			return CodeBlock.of("() -> $T.$L()", beanRegistrationCode.getClassName(), generatedMethod.getName());
		}

		private CodeBlock toCodeBlock(List<String> values) {
			return CodeBlock.join(values.stream().map(value -> CodeBlock.of("$S", value)).toList(), ", ");
		}

	}
}
