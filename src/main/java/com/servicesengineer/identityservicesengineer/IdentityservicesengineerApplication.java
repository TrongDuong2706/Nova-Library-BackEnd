package com.servicesengineer.identityservicesengineer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IdentityservicesengineerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentityservicesengineerApplication.class, args);
	}

}
