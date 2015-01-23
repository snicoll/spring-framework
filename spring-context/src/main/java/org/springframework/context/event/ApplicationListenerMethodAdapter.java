/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.context.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * {@link GenericApplicationListener} adapter that delegates the processing of
 * an event to an {@link EventListener} annotated method.
 *
 * <p>Unwrap the content of a {@link PayloadApplicationEvent} if necessary
 * to allow method declaration to define any arbitrary event type.
 *
 * <p>If a condition is defined, it is evaluated prior to invoking the
 * underlying method.
 *
 * @author Stephane Nicoll
 * @since 4.2
 */
public class ApplicationListenerMethodAdapter implements GenericApplicationListener {

	protected final Log logger = LogFactory.getLog(getClass());

	private final String beanName;

	private final Method method;

	private final Class<?> targetClass;

	private final Method bridgedMethod;

	private final ResolvableType declaredEventType;

	private final AnnotatedElementKey methodKey;

	private ApplicationContext applicationContext;

	private EventExpressionEvaluator evaluator;

	public ApplicationListenerMethodAdapter(String beanName, Class<?> targetClass, Method method) {
		this.beanName = beanName;
		this.method = method;
		this.targetClass = targetClass;
		this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
		this.declaredEventType = resolveDeclaredEventType();
		this.methodKey = new AnnotatedElementKey(this.method, this.targetClass);
		this.evaluator = new EventExpressionEvaluator();
	}

	/**
	 * Initialize this instance.
	 */
	void init(ApplicationContext applicationContext, EventExpressionEvaluator evaluator) {
		this.applicationContext = applicationContext;
		this.evaluator = evaluator;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		Object[] args = resolveArguments(event);
		if (shouldHandle(event, args)) {
			Object result = doInvoke(args);
			if (result != null) {
				handleResult(result);
			}
			else {
				logger.trace("No result object given - no result to handle");
			}
		}
	}

	/**
	 * Resolve the method arguments to use for the specified {@link ApplicationEvent}.
	 * <p>These arguments will be used to invoke the method handled by this instance. Can
	 * return {@code null} to indicate that no suitable arguments could be resolved and
	 * therefore the method should not be invoked at all for the specified event.
	 */
	protected Object[] resolveArguments(ApplicationEvent event) {
		if (!ApplicationEvent.class.isAssignableFrom(this.declaredEventType.getRawClass())
				&& event instanceof PayloadApplicationEvent) {
			Object payload = ((PayloadApplicationEvent) event).getPayload();
			if (this.declaredEventType.isAssignableFrom(ResolvableType.forClass(payload.getClass()))) {
				return new Object[] {payload};
			}
		}
		else {
			return new Object[] {event};
		}
		return null;
	}

	protected void handleResult(Object result) {
		Assert.notNull(this.applicationContext, "ApplicationContext must no be null.");
		this.applicationContext.publishEvent(result);
	}


	private boolean shouldHandle(ApplicationEvent event, Object[] args) {
		if (args == null) {
			return false;
		}
		EventListener eventListener = AnnotationUtils.findAnnotation(this.method, EventListener.class);
		String condition = (eventListener != null ? eventListener.condition() : null);
		if (StringUtils.hasText(condition)) {
			Assert.notNull(this.evaluator, "Evaluator must no be null.");
			EvaluationContext evaluationContext = this.evaluator.createEvaluationContext(event,
					this.targetClass, this.method, args);
			return this.evaluator.condition(condition, this.methodKey, evaluationContext);
		}
		return true;
	}

	@Override
	public boolean supportsEventType(ResolvableType eventType) {
		if (this.declaredEventType.isAssignableFrom(eventType)) {
			return true;
		}
		else if (PayloadApplicationEvent.class.isAssignableFrom(eventType.getRawClass())) {
			ResolvableType payloadType = eventType.as(PayloadApplicationEvent.class).getGeneric();
			return eventType.hasUnresolvableGenerics() || this.declaredEventType.isAssignableFrom(payloadType);
		}
		return false;
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	@Override
	public int getOrder() {
		Order order = AnnotationUtils.findAnnotation(this.method, Order.class);
		return (order != null ? order.value() : 0);
	}

	/**
	 * Invoke the event listener method with the given argument values.
	 */
	protected Object doInvoke(Object... args) {
		Object bean = getTargetBean();
		ReflectionUtils.makeAccessible(this.bridgedMethod);
		try {
			return this.bridgedMethod.invoke(bean, args);
		}
		catch (IllegalArgumentException ex) {
			assertTargetBean(this.bridgedMethod, bean, args);
			throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
		}
		catch (IllegalAccessException ex) {
			throw new IllegalStateException(getInvocationErrorMessage(bean, ex.getMessage(), args), ex);
		}
		catch (InvocationTargetException ex) {
			// Throw underlying exception
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) targetException;
			}
			else {
				String msg = getInvocationErrorMessage(bean, "Failed to invoke event listener method", args);
				throw new UndeclaredThrowableException(targetException, msg);
			}
		}
	}

	/**
	 * Return the target bean instance to use.
	 */
	protected Object getTargetBean() {
		Assert.notNull(this.applicationContext, "ApplicationContext must no be null.");
		return this.applicationContext.getBean(this.beanName);
	}

	/**
	 * Add additional details such as the bean type and method signature to
	 * the given error message.
	 * @param message error message to append the HandlerMethod details to
	 */
	protected String getDetailedErrorMessage(Object bean, String message) {
		StringBuilder sb = new StringBuilder(message).append("\n");
		sb.append("HandlerMethod details: \n");
		sb.append("Bean [").append(bean.getClass().getName()).append("]\n");
		sb.append("Method [").append(this.bridgedMethod.toGenericString()).append("]\n");
		return sb.toString();
	}

	/**
	 * Assert that the target bean class is an instance of the class where the given
	 * method is declared. In some cases the actual bean instance at event-
	 * processing time may be a JDK dynamic proxy (lazy initialization, prototype
	 * beans, and others). Event listener beans that require proxying should prefer
	 * class-based proxy mechanisms.
	 */
	private void assertTargetBean(Method method, Object targetBean, Object[] args) {
		Class<?> methodDeclaringClass = method.getDeclaringClass();
		Class<?> targetBeanClass = targetBean.getClass();
		if (!methodDeclaringClass.isAssignableFrom(targetBeanClass)) {
			String msg = "The event listener method class '" + methodDeclaringClass.getName() +
					"' is not an instance of the actual bean instance '" +
					targetBeanClass.getName() + "'. If the bean requires proxying " +
					"(e.g. due to @Transactional), please use class-based proxying.";
			throw new IllegalStateException(getInvocationErrorMessage(targetBean, msg, args));
		}
	}

	private String getInvocationErrorMessage(Object bean, String message, Object[] resolvedArgs) {
		StringBuilder sb = new StringBuilder(getDetailedErrorMessage(bean, message));
		sb.append("Resolved arguments: \n");
		for (int i = 0; i < resolvedArgs.length; i++) {
			sb.append("[").append(i).append("] ");
			if (resolvedArgs[i] == null) {
				sb.append("[null] \n");
			}
			else {
				sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
				sb.append("[value=").append(resolvedArgs[i]).append("]\n");
			}
		}
		return sb.toString();
	}


	private ResolvableType resolveDeclaredEventType() {
		Parameter[] parameters = this.method.getParameters();
		if (parameters.length != 1) {
			throw new IllegalStateException("Only one parameter is allowed " +
					"for event listener method: " + method);
		}
		return ResolvableType.forMethodParameter(this.method, 0);
	}

	@Override
	public String toString() {
		return this.method.toGenericString();
	}

}
