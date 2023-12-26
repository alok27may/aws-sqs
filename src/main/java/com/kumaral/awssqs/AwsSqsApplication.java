package com.kumaral.awssqs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AwsSqsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsSqsApplication.class, args);
	}

}
