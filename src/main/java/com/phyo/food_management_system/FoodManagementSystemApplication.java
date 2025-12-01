package com.phyo.food_management_system;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
@OpenAPIDefinition(
		info = @Info(
				title = "Food Management System API",
				description = "API documentation for Food Management System Project",
				version = "1.0.0",
				contact = @Contact(
						name = "Phyo Zaw Oo",
						email = "zawoo389p@prestigein.com"
				)
		)


)
public class FoodManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodManagementSystemApplication.class, args);
	}

}
