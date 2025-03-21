package com.sandy.newston;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NewstonApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewstonApplication.class, args);
	}

}
