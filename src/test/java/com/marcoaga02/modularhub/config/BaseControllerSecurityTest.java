package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.config.CorsProperties;
import com.marcoaga02.modularhub.core.config.ErrorResponseAccessDeniedHandler;
import com.marcoaga02.modularhub.core.config.ErrorResponseAuthenticationEntryPoint;
import com.marcoaga02.modularhub.core.config.MethodSecurityConfig;
import com.marcoaga02.modularhub.core.config.SecurityConfig;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfig.class, MethodSecurityConfig.class, CorsProperties.class,
        ErrorResponseAuthenticationEntryPoint.class, ErrorResponseAccessDeniedHandler.class})
public abstract class BaseControllerSecurityTest {

    protected static final String OTHER_ROLE = "SOME_OTHER_ROLE";

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    protected static RequestPostProcessor withRoles(String... roles) {
        return jwt().authorities(Arrays.stream(roles)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + role))
                .toList());
    }

    protected void assertUnauthorized(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Access.UNAUTHORIZED))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    protected void assertForbidden(MockHttpServletRequestBuilder request, String... roles) throws Exception {
        mockMvc.perform(request.with(withRoles(roles)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Access.DENIED));
    }

    protected ResultActions performWithRoles(MockHttpServletRequestBuilder request, String... roles) throws Exception {
        return mockMvc.perform(request.with(withRoles(roles)));
    }
}
