package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.config.JpaAuditingConfig;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({PostgresContainerConfig.class, JpaAuditingConfig.class})
public abstract class BaseRepositoryTest {
}
