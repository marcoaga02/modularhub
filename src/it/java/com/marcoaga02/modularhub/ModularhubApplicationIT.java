package com.marcoaga02.modularhub;

import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ModularhubApplicationIT extends BaseITWithMockIdentity {

	@Test
	void contextLoads() {
	}

}
