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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.BeanThatBroadcasts;
import org.springframework.context.BeanThatListens;
import org.springframework.context.event.test.TestEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.scheduling.support.TaskUtils;
import org.springframework.tests.sample.beans.TestBean;
import org.springframework.util.ErrorHandler;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit and integration tests for the ApplicationContext event support.
 *
 * @author Alef Arendsen
 * @author Rick Evans
 * @author Stephane Nicoll
 */
public class ApplicationContextEventTests extends AbstractApplicationEventListenerTests {

	@Test
	public void multicastSimpleEvent() {
		multicastEvent(true, ApplicationListener.class,
				new ContextClosedEvent(new StaticApplicationContext()), null);
	}

	@Test
	public void multicastGenericEvent() {
		multicastEvent(true, StringEventListener.class, createGenericTestEvent("test"),
				getGenericApplicationEventType("stringEvent"));
	}

	@Test
	public void multicastGenericEventWrongType() {
		multicastEvent(false, StringEventListener.class, createGenericTestEvent(123L),
				getGenericApplicationEventType("longEvent"));
	}

	@Test // Unfortunate - this should work as well
	public void multicastGenericEventWildcardSubType() {
		multicastEvent(false, StringEventListener.class, createGenericTestEvent("test"),
				getGenericApplicationEventType("wildcardEvent"));
	}

	@Test
	public void multicastConcreteTypeGenericListener() {
		multicastEvent(true, StringEventListener.class, new StringEvent(this, "test"), null);
	}

	@Test
	public void multicastConcreteWrongTypeGenericListener() {
		multicastEvent(false, StringEventListener.class, new LongEvent(this, 123L), null);
	}

	private void multicastEvent(boolean match, Class<?> listenerType,
			ApplicationEvent event, ResolvableType eventType) {
		@SuppressWarnings("unchecked")
		ApplicationListener<ApplicationEvent> listener =
				(ApplicationListener<ApplicationEvent>) mock(listenerType);
		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.addApplicationListener(listener);

		if (eventType != null) {
			smc.multicastEvent(event, eventType);
		} else {
			smc.multicastEvent(event);
		}
		int invocation = match ? 1 : 0;
		verify(listener, times(invocation)).onApplicationEvent(event);
	}

	@Test
	public void simpleApplicationEventMulticasterWithTaskExecutor() {
		@SuppressWarnings("unchecked")
		ApplicationListener<ApplicationEvent> listener = mock(ApplicationListener.class);
		ApplicationEvent evt = new ContextClosedEvent(new StaticApplicationContext());

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.setTaskExecutor(new Executor() {
			@Override
			public void execute(Runnable command) {
				command.run();
				command.run();
			}
		});
		smc.addApplicationListener(listener);

		smc.multicastEvent(evt);
		verify(listener, times(2)).onApplicationEvent(evt);
	}

	@Test
	public void simpleApplicationEventMulticasterWithException() {
		@SuppressWarnings("unchecked")
		ApplicationListener<ApplicationEvent> listener = mock(ApplicationListener.class);
		ApplicationEvent evt = new ContextClosedEvent(new StaticApplicationContext());

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.addApplicationListener(listener);

		RuntimeException thrown = new RuntimeException();
		willThrow(thrown).given(listener).onApplicationEvent(evt);
		try {
			smc.multicastEvent(evt);
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			assertSame(thrown, ex);
		}
	}

	@Test
	public void simpleApplicationEventMulticasterWithErrorHandler() {
		@SuppressWarnings("unchecked")
		ApplicationListener<ApplicationEvent> listener = mock(ApplicationListener.class);
		ApplicationEvent evt = new ContextClosedEvent(new StaticApplicationContext());

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.setErrorHandler(TaskUtils.LOG_AND_SUPPRESS_ERROR_HANDLER);
		smc.addApplicationListener(listener);

		willThrow(new RuntimeException()).given(listener).onApplicationEvent(evt);
		smc.multicastEvent(evt);
	}

	@Test
	public void errorHandlerCalledWithActualException() {
		@SuppressWarnings("unchecked")
		ApplicationListener<ApplicationEvent> listener = mock(ApplicationListener.class);
		ApplicationEvent evt = new TestEvent();

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		ErrorHandler errorHandler = mock(ErrorHandler.class);
		smc.setErrorHandler(errorHandler);
		smc.addApplicationListener(listener);

		Exception actualException = new InterruptedException();
		willThrow(new EventListenerExecutionException("test", actualException))
				.given(listener).onApplicationEvent(evt);
		smc.multicastEvent(evt);
		verify(errorHandler, times(1)).handleError(actualException);
	}

	@Test
	public void orderedListeners() {
		MyOrderedListener1 listener1 = new MyOrderedListener1();
		MyOrderedListener2 listener2 = new MyOrderedListener2(listener1);

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.addApplicationListener(listener2);
		smc.addApplicationListener(listener1);

		smc.multicastEvent(new MyEvent(this));
		smc.multicastEvent(new MyOtherEvent(this));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void proxiedListeners() {
		MyOrderedListener1 listener1 = new MyOrderedListener1();
		MyOrderedListener2 listener2 = new MyOrderedListener2(listener1);
		ApplicationListener<ApplicationEvent> proxy1 = (ApplicationListener<ApplicationEvent>) new ProxyFactory(listener1).getProxy();
		ApplicationListener<ApplicationEvent> proxy2 = (ApplicationListener<ApplicationEvent>) new ProxyFactory(listener2).getProxy();

		SimpleApplicationEventMulticaster smc = new SimpleApplicationEventMulticaster();
		smc.addApplicationListener(proxy1);
		smc.addApplicationListener(proxy2);

		smc.multicastEvent(new MyEvent(this));
		smc.multicastEvent(new MyOtherEvent(this));
	}

	@Test
	public void testEventPublicationInterceptor() throws Throwable {
		MethodInvocation invocation = mock(MethodInvocation.class);
		ApplicationContext ctx = mock(ApplicationContext.class);

		EventPublicationInterceptor interceptor = new EventPublicationInterceptor();
		interceptor.setApplicationEventClass(MyEvent.class);
		interceptor.setApplicationEventPublisher(ctx);
		interceptor.afterPropertiesSet();

		given(invocation.proceed()).willReturn(new Object());
		given(invocation.getThis()).willReturn(new Object());
		interceptor.invoke(invocation);
		verify(ctx).publishEvent(isA(MyEvent.class));
	}

	@Test
	public void listenersInApplicationContext() {
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerBeanDefinition("listener1", new RootBeanDefinition(MyOrderedListener1.class));
		RootBeanDefinition listener2 = new RootBeanDefinition(MyOrderedListener2.class);
		listener2.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("listener1"));
		listener2.setLazyInit(true);
		context.registerBeanDefinition("listener2", listener2);
		context.refresh();
		assertFalse(context.getDefaultListableBeanFactory().containsSingleton("listener2"));

		MyOrderedListener1 listener1 = context.getBean("listener1", MyOrderedListener1.class);
		MyOtherEvent event1 = new MyOtherEvent(context);
		context.publishEvent(event1);
		assertFalse(context.getDefaultListableBeanFactory().containsSingleton("listener2"));
		MyEvent event2 = new MyEvent(context);
		context.publishEvent(event2);
		assertTrue(context.getDefaultListableBeanFactory().containsSingleton("listener2"));
		MyEvent event3 = new MyEvent(context);
		context.publishEvent(event3);
		MyOtherEvent event4 = new MyOtherEvent(context);
		context.publishEvent(event4);
		assertTrue(listener1.seenEvents.contains(event1));
		assertTrue(listener1.seenEvents.contains(event2));
		assertTrue(listener1.seenEvents.contains(event3));
		assertTrue(listener1.seenEvents.contains(event4));

		listener1.seenEvents.clear();
		context.publishEvent(event1);
		context.publishEvent(event2);
		context.publishEvent(event3);
		context.publishEvent(event4);
		assertTrue(listener1.seenEvents.contains(event1));
		assertTrue(listener1.seenEvents.contains(event2));
		assertTrue(listener1.seenEvents.contains(event3));
		assertTrue(listener1.seenEvents.contains(event4));

		context.close();
	}

	@Test
	public void listenersInApplicationContextWithNestedChild() {
		StaticApplicationContext context = new StaticApplicationContext();
		RootBeanDefinition nestedChild = new RootBeanDefinition(StaticApplicationContext.class);
		nestedChild.getPropertyValues().add("parent", context);
		nestedChild.setInitMethodName("refresh");
		context.registerBeanDefinition("nestedChild", nestedChild);
		RootBeanDefinition listener1Def = new RootBeanDefinition(MyOrderedListener1.class);
		listener1Def.setDependsOn(new String[] {"nestedChild"});
		context.registerBeanDefinition("listener1", listener1Def);
		context.refresh();

		MyOrderedListener1 listener1 = context.getBean("listener1", MyOrderedListener1.class);
		MyEvent event1 = new MyEvent(context);
		context.publishEvent(event1);
		assertTrue(listener1.seenEvents.contains(event1));

		SimpleApplicationEventMulticaster multicaster = context.getBean(
				AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
				SimpleApplicationEventMulticaster.class);
		assertFalse(multicaster.getApplicationListeners().isEmpty());

		context.close();
		assertTrue(multicaster.getApplicationListeners().isEmpty());
	}

	@Test
	public void nonSingletonListenerInApplicationContext() {
		StaticApplicationContext context = new StaticApplicationContext();
		RootBeanDefinition listener = new RootBeanDefinition(MyNonSingletonListener.class);
		listener.setScope(RootBeanDefinition.SCOPE_PROTOTYPE);
		context.registerBeanDefinition("listener", listener);
		context.refresh();

		MyEvent event1 = new MyEvent(context);
		context.publishEvent(event1);
		MyOtherEvent event2 = new MyOtherEvent(context);
		context.publishEvent(event2);
		MyEvent event3 = new MyEvent(context);
		context.publishEvent(event3);
		MyOtherEvent event4 = new MyOtherEvent(context);
		context.publishEvent(event4);
		assertTrue(MyNonSingletonListener.seenEvents.contains(event1));
		assertTrue(MyNonSingletonListener.seenEvents.contains(event2));
		assertTrue(MyNonSingletonListener.seenEvents.contains(event3));
		assertTrue(MyNonSingletonListener.seenEvents.contains(event4));
		MyNonSingletonListener.seenEvents.clear();

		context.close();
	}

	@Test
	public void listenerAndBroadcasterWithCircularReference() {
		StaticApplicationContext context = new StaticApplicationContext();
		context.registerBeanDefinition("broadcaster", new RootBeanDefinition(BeanThatBroadcasts.class));
		RootBeanDefinition listenerDef = new RootBeanDefinition(BeanThatListens.class);
		listenerDef.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference("broadcaster"));
		context.registerBeanDefinition("listener", listenerDef);
		context.refresh();

		BeanThatBroadcasts broadcaster = context.getBean("broadcaster", BeanThatBroadcasts.class);
		context.publishEvent(new MyEvent(context));
		assertEquals("The event was not received by the listener", 2, broadcaster.receivedCount);

		context.close();
	}

	@Test
	public void innerBeanAsListener() {
		StaticApplicationContext context = new StaticApplicationContext();
		RootBeanDefinition listenerDef = new RootBeanDefinition(TestBean.class);
		listenerDef.getPropertyValues().add("friends", new RootBeanDefinition(BeanThatListens.class));
		context.registerBeanDefinition("listener", listenerDef);
		context.refresh();

		context.publishEvent(new MyEvent(this));
		context.publishEvent(new MyEvent(this));
		TestBean listener = context.getBean(TestBean.class);
		assertEquals(3, ((BeanThatListens) listener.getFriends().iterator().next()).getEventCount());

		context.close();
	}


	@SuppressWarnings("serial")
	public static class MyEvent extends ApplicationEvent {

		public MyEvent(Object source) {
			super(source);
		}
	}


	@SuppressWarnings("serial")
	public static class MyOtherEvent extends ApplicationEvent {

		public MyOtherEvent(Object source) {
			super(source);
		}
	}


	public static class MyOrderedListener1 implements ApplicationListener<ApplicationEvent>, Ordered {

		public final Set<ApplicationEvent> seenEvents = new HashSet<ApplicationEvent>();

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			this.seenEvents.add(event);
		}

		@Override
		public int getOrder() {
			return 0;
		}
	}


	public interface MyOrderedListenerIfc<E extends ApplicationEvent> extends ApplicationListener<E>, Ordered {
	}


	public static abstract class MyOrderedListenerBase implements MyOrderedListenerIfc<MyEvent> {

		@Override
		public int getOrder() {
			return 1;
		}
	}


	public static class MyOrderedListener2 extends MyOrderedListenerBase {

		private final MyOrderedListener1 otherListener;

		public MyOrderedListener2(MyOrderedListener1 otherListener) {
			this.otherListener = otherListener;
		}

		@Override
		public void onApplicationEvent(MyEvent event) {
			assertTrue(otherListener.seenEvents.contains(event));
		}
	}


	public static class MyNonSingletonListener implements ApplicationListener<ApplicationEvent> {

		public static final Set<ApplicationEvent> seenEvents = new HashSet<ApplicationEvent>();

		@Override
		public void onApplicationEvent(ApplicationEvent event) {
			seenEvents.add(event);
		}
	}

}
