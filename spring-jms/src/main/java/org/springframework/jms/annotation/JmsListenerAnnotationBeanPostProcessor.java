/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.jms.annotation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.config.MethodJmsListenerEndpoint;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Bean post-processor that registers methods annotated with @{@link JmsListener}
 * to be invoked by a JMS message listener container created under the cover
 * by a {@link org.springframework.jms.config.JmsListenerContainerFactory} according
 * to the parameters of the annotation.
 *
 * <p>This post-processor is automatically registered by Spring's
 * {@code <jms:annotation-driven>} XML element, and also by the @{@link EnableJms}
 * annotation.
 *
 * <p>Auto-detects any {@link JmsListenerConfigurer} instances in the container,
 * allowing for customization of the registry to be used or for fine-grained control
 * over endpoint registration. See @{@link EnableJms} Javadoc for complete
 * usage details.
 *
 * @author Stephane Nicoll
 * @since 4.1
 * @see JmsListener
 * @see EnableJms
 * @see JmsListenerConfigurer
 * @see JmsListenerEndpointRegistrar
 * @see JmsListenerEndpointRegistry
 */
public class JmsListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered,
		ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	private final AtomicInteger counter = new AtomicInteger();

	private ApplicationContext applicationContext;

	private JmsListenerEndpointRegistry endpointRegistry;

	private final JmsListenerEndpointRegistrar registrar = new JmsListenerEndpointRegistrar();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * Set the {@link JmsListenerEndpointRegistry} that will hold the created
	 * endpoint and manage the lifecycle of the related listener container.
	 */
	public void setEndpointRegistry(JmsListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		ReflectionUtils.doWithMethods(targetClass, new ReflectionUtils.MethodCallback() {
			@Override
			public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
				JmsListener jmsListener = AnnotationUtils.getAnnotation(method, JmsListener.class);
				if (jmsListener != null) {
					processJmsListener(jmsListener, method, bean);
				}
			}
		});
		return bean;
	}

	protected void processJmsListener(JmsListener jmsListener, Method method, Object bean) {
		if (AopUtils.isJdkDynamicProxy(bean)) {
			try {
				// Found a @JmsListener method on the target class for this JDK proxy ->
				// is it also present on the proxy itself?
				method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
			}
			catch (SecurityException ex) {
				ReflectionUtils.handleReflectionException(ex);
			}
			catch (NoSuchMethodException ex) {
				throw new IllegalStateException(String.format(
						"@JmsListener method '%s' found on bean target class '%s', " +
								"but not found in any interface(s) for bean JDK proxy. Either " +
								"pull the method up to an interface or switch to subclass (CGLIB) " +
								"proxies by setting proxy-target-class/proxyTargetClass " +
								"attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()));
			}
		}

		MethodJmsListenerEndpoint endpoint = new MethodJmsListenerEndpoint();
		endpoint.setBean(bean);
		endpoint.setMethod(method);
		if (StringUtils.hasText(jmsListener.id())) {
			endpoint.setId(jmsListener.id());
		}
		else {
			endpoint.setId(generateEndpointId(jmsListener));
		}
		endpoint.setFactoryId(jmsListener.factoryId());
		endpoint.setDestination(jmsListener.destination());
		endpoint.setQueue(jmsListener.queue());
		if (StringUtils.hasText(jmsListener.selector())) {
			endpoint.setSelector(jmsListener.selector());
		}
		if (StringUtils.hasText(jmsListener.subscription())) {
			endpoint.setSubscription(jmsListener.subscription());
		}
		if (StringUtils.hasText(jmsListener.responseDestination())) {
			endpoint.setResponseDestination(jmsListener.responseDestination());
		}
		registrar.addEndpoint(endpoint);

	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (event.getApplicationContext() != this.applicationContext) {
			return;
		}

		Map<String, JmsListenerConfigurer> instances =
				this.applicationContext.getBeansOfType(JmsListenerConfigurer.class);
		for (JmsListenerConfigurer configurer : instances.values()) {
			configurer.configureJmsListeners(registrar);
		}
		if (registrar.getEndpointRegistry() == null) {
			if (endpointRegistry == null) {
				endpointRegistry = applicationContext
						.getBean(AnnotationConfigUtils.JMS_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
								JmsListenerEndpointRegistry.class);
			}
			registrar.setEndpointRegistry(endpointRegistry);
		}

		// Create all the listeners and starts them
		try {
			registrar.afterPropertiesSet();
		}
		catch (Exception e) {
			throw new BeanInitializationException(e.getMessage(), e);
		}
	}

	private String generateEndpointId(JmsListener jmsListener) {
		return "org.springframework.jms.listener-" + jmsListener.factoryId()
				+ "#" + counter.getAndIncrement();
	}

}
