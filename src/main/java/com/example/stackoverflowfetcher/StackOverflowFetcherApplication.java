package com.example.stackoverflowfetcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StackOverflowFetcherApplication {

	public static void main(String[] args) {
		SpringApplication.run(StackOverflowFetcherApplication.class, args);
	}

}
