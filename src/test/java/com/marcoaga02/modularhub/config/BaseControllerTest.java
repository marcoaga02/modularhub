package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.config.CorsProperties;
import com.marcoaga02.modularhub.core.config.ErrorResponseAccessDeniedHandler;
import com.marcoaga02.modularhub.core.config.ErrorResponseAuthenticationEntryPoint;
import com.marcoaga02.modularhub.core.config.MethodSecurityConfig;
import com.marcoaga02.modularhub.core.config.PermissionAuthoritiesConverter;
import com.marcoaga02.modularhub.core.config.SecurityConfig;
import com.marcoaga02.modularhub.core.domain.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.permissions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@Import({SecurityConfig.class, MethodSecurityConfig.class, CorsProperties.class,
        ErrorResponseAuthenticationEntryPoint.class, ErrorResponseAccessDeniedHandler.class,
        PermissionAuthoritiesConverter.class})
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected PermissionService permissionService;

    protected static RequestPostProcessor withPermissions(String... permissions) {
        return jwt().authorities(permissions(permissions));
    }

    protected ResultActions performWithPermissions(MockHttpServletRequestBuilder request, String... permissions) throws Exception {
        return mockMvc.perform(request.with(withPermissions(permissions)));
    }
}
