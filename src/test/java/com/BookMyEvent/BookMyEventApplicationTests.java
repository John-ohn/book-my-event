package com.BookMyEvent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(
		locations = "classpath:integrationtest.properties")
class BookMyEventApplicationTests {

	@Test
	void contextLoads() {
	}

}
