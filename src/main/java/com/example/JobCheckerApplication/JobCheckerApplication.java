package com.example.JobCheckerApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobCheckerApplication{
	public static void main(String[] args) {
		SpringApplication.run(JobCheckerApplication.class, args);
	}
}