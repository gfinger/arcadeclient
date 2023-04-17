package org.makkiato.arcadedb.console;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Console {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(Console.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		application.run(args);
	}

}
