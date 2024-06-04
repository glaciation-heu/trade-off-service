package com.glaciation.TradeOffService;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TradeOffServiceApplicationTests {

	private final Logger logger = LoggerFactory.getLogger(TradeOffServiceApplicationTests.class);

	@Test
	void contextLoads() {
		logger.info("Dependencies are set up, configuration is correct and context loaded successfully.");
	}

}
