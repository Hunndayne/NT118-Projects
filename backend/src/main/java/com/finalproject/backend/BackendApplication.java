package com.finalproject.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

	static {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		TimeZone.setDefault(timeZone);
		System.setProperty("user.timezone", timeZone.getID());
	}
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
