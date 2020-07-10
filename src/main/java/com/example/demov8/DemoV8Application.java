package com.example.demov8;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoV8Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoV8Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("Hello World!");

		Context context = Context.create();
		Value result = context.eval("js", "40+2");
		assert result.asInt() == 4;

		System.out.println(result);

	}
}
