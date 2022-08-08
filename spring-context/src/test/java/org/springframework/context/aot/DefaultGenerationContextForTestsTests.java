package org.springframework.context.aot;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.aot.generate.ClassNameGenerator;
import org.springframework.aot.generate.DefaultGenerationContext;
import org.springframework.aot.generate.InMemoryGeneratedFiles;
import org.springframework.aot.test.generator.compile.TestCompiler;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;

class DefaultGenerationContextForTestsTests {

	@Test
	@SuppressWarnings("resource")
	void example() {
		ApplicationContextAotGenerator aotGenerator = new ApplicationContextAotGenerator();
		ClassNameGenerator classNameGenerator = new ClassNameGenerator(null);
		InMemoryGeneratedFiles generatedFiles = new InMemoryGeneratedFiles();
		DefaultGenerationContext generationContext = new DefaultGenerationContext(
				classNameGenerator, generatedFiles);
		getContexts().forEach((details, applicationContext) -> {
			DefaultGenerationContext generationContextToUse = generationContext
					.withDefaultTarget(details.defaultTarget()).withName(details.name());
			aotGenerator.processAheadOfTime(applicationContext, generationContextToUse);
		});
		generationContext.writeGeneratedContent();
		TestCompiler.forSystem().withFiles(generatedFiles).printFiles(System.out)
				.compile(compiled -> {});
	}

	private Map<Details, GenericApplicationContext> getContexts() {
		Map<Details, GenericApplicationContext> contexts = new LinkedHashMap<>();
		AnnotationConfigApplicationContext applicationContext1 = new AnnotationConfigApplicationContext();
		applicationContext1.register(SomeConfig1.class);
		contexts.put(new Details("context1", MyApplication.class), applicationContext1);
		AnnotationConfigApplicationContext applicationContext2 = new AnnotationConfigApplicationContext();
		applicationContext2.register(SomeConfig2.class);
		contexts.put(new Details("context2", MyApplication.class), applicationContext2);
		return Map.copyOf(contexts);
	}

	static class MyApplication {

	}

	@Configuration
	@Import(SomeBean1.class)
	static class SomeConfig1 {

	}

	static class SomeBean1 {

	}

	@Configuration
	@Import(SomeBean2.class)
	static class SomeConfig2 {

	}

	static class SomeBean2 {

	}

	record Details(String name, Class<?> defaultTarget) {
	}

}
