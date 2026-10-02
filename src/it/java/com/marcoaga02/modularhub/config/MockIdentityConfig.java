package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockIdentityConfig {

    @Bean
    @Primary
    public IdentityProviderService identityProviderService() {
        return Mockito.mock(IdentityProviderService.class);
    }
}
