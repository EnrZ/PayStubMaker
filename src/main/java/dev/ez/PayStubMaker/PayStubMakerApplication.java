package dev.ez.PayStubMaker;

import dev.ez.PayStubMaker.controllers.HomeController;
import dev.ez.PayStubMaker.controllers.StubController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class PayStubMakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayStubMakerApplication.class, args);
	}

}
