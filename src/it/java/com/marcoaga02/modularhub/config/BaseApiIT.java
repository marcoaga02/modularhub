package com.marcoaga02.modularhub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.roles;

@AutoConfigureMockMvc
public abstract class BaseApiIT extends BaseITWithMockIdentity {

    @Autowired
    protected MockMvc mockMvc;

    protected static RequestPostProcessor jwtOf(String subject, String... roles) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(subject))
                .authorities(roles(roles));
    }
}
