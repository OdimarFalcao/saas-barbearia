package com.barbearia;

import com.barbearia.common.TestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfig.class)
class BarbeariaApplicationTests {

	@Test
	void contextLoads() {
	}

}
