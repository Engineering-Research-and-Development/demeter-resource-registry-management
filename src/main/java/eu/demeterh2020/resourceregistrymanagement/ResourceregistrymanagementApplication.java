package eu.demeterh2020.resourceregistrymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class ResourceregistrymanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceregistrymanagementApplication.class, args);
	}

}
