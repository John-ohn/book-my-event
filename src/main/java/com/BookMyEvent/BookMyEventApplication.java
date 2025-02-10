package com.BookMyEvent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BookMyEventApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Kiev"));

		SpringApplication.run(BookMyEventApplication.class, args);
	}

}
