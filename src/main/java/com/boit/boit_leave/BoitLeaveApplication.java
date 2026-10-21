package com.boit.boit_leave;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class BoitLeaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoitLeaveApplication.class, args);
	}
	
//	@Bean
//	CommandLineRunner test(
//	        PasswordEncoder encoder) {
//
//	    return args -> {
//
//	        System.out.println(
//	            encoder.encode("Admin123")
//	        );
//	    };
//	}

}
