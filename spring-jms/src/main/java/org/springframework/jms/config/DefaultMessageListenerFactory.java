package org.springframework.jms.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jms.MessageListener;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.jms.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.handler.annotation.support.HeaderMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.HeadersMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.MessageMethodArgumentResolver;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 *
 * @author Stephane Nicoll
 */
public class DefaultMessageListenerFactory
		implements MessageListenerFactory, ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;

	private ConversionService conversionService = new DefaultFormattingConversionService();

	private MessageConverter messageConverter;

	private Validator validator = new NoOpValidator();

	private List<HandlerMethodArgumentResolver> customArgumentResolvers
			= new ArrayList<HandlerMethodArgumentResolver>();

	private HandlerMethodArgumentResolverComposite argumentResolvers
			= new HandlerMethodArgumentResolverComposite();

	public DefaultMessageListenerFactory() {
		Collection<MessageConverter> converters = new ArrayList<MessageConverter>();
		converters.add(new StringMessageConverter());
		converters.add(new ByteArrayMessageConverter());
		this.messageConverter = new CompositeMessageConverter(converters);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public MessageConverter getMessageConverter() {
		return messageConverter;
	}

	/**
	 * The configured Validator instance
	 */
	public Validator getValidator() {
		return validator;
	}

	/**
	 * Set the Validator instance used for validating @Payload arguments
	 * @see org.springframework.validation.annotation.Validated
	 * @see PayloadArgumentResolver
	 */
	public void setValidator(Validator validator) {
		this.validator = validator;
	}

	/**
	 * Sets the list of custom {@code HandlerMethodArgumentResolver}s that will be used
	 * after resolvers for supported argument type.
	 * @param customArgumentResolvers the list of resolvers; never {@code null}.
	 */
	public void setCustomArgumentResolvers(List<HandlerMethodArgumentResolver> customArgumentResolvers) {
		Assert.notNull(customArgumentResolvers, "The 'customArgumentResolvers' cannot be null.");
		this.customArgumentResolvers = customArgumentResolvers;
	}

	public List<HandlerMethodArgumentResolver> getCustomArgumentResolvers() {
		return customArgumentResolvers;
	}

	/**
	 * Configure the complete list of supported argument types effectively overriding
	 * the ones configured by default. This is an advanced option. For most use cases
	 * it should be sufficient to use {@link #setCustomArgumentResolvers(java.util.List)}.
	 */
	public void setArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		if (argumentResolvers == null) {
			this.argumentResolvers.clear();
			return;
		}
		this.argumentResolvers.addResolvers(argumentResolvers);
	}

	public List<HandlerMethodArgumentResolver> getArgumentResolvers() {
		return this.argumentResolvers.getResolvers();
	}

	@Override
	public void afterPropertiesSet() {
		if (this.argumentResolvers.getResolvers().isEmpty()) {
			this.argumentResolvers.addResolvers(initArgumentResolvers());
		}
	}

	@Override
	public MessageListener createMessageListener(JmsListenerEndpoint endpoint) {
		if (endpoint instanceof SimpleJmsListenerEndpoint) {
			return ((SimpleJmsListenerEndpoint) endpoint).getListener();
		}
		else if (endpoint instanceof MethodJmsListenerEndpoint) {
			return createMessageListener((MethodJmsListenerEndpoint) endpoint);
		}
		else {
			throw new IllegalStateException("Unsupported endpoint type '" + endpoint.getClass().getName() + "'");
		}
	}

	protected MessageListener createMessageListener(MethodJmsListenerEndpoint endpoint) {
		InvocableHandlerMethod handlerMethod = new InvocableHandlerMethod(endpoint.getBean(), endpoint.getMethod());
		handlerMethod.setMessageMethodArgumentResolvers(argumentResolvers);
		MessagingMessageListenerAdapter adapter = new MessagingMessageListenerAdapter();
		adapter.setDelegate(endpoint.getBean());
		adapter.setHandlerMethod(handlerMethod);
		if (endpoint.getResponseDestination() != null) {
			if (endpoint.isQueue()) {
				adapter.setDefaultResponseQueueName(endpoint.getResponseDestination());
			} else {
				adapter.setDefaultResponseTopicName(endpoint.getResponseDestination());
			}
		}
		return adapter;
	}

	protected List<HandlerMethodArgumentResolver> initArgumentResolvers() {
		ConfigurableBeanFactory beanFactory =
				(ClassUtils.isAssignableValue(ConfigurableApplicationContext.class, applicationContext)) ?
						((ConfigurableApplicationContext) applicationContext).getBeanFactory() : null;

		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

		// Annotation-based argument resolution
		resolvers.add(new HeaderMethodArgumentResolver(conversionService, beanFactory));
		resolvers.add(new HeadersMethodArgumentResolver());

		// Type-based argument resolution
		resolvers.add(new MessageMethodArgumentResolver());

		resolvers.addAll(getCustomArgumentResolvers());
		resolvers.add(new PayloadArgumentResolver(getMessageConverter(), validator));

		return resolvers;
	}

	private static final class NoOpValidator implements Validator {
		@Override
		public boolean supports(Class<?> clazz) {
			return false;
		}

		@Override
		public void validate(Object target, Errors errors) {
		}
	}
}
