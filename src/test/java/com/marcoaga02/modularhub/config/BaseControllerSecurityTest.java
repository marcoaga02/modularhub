package com.marcoaga02.modularhub.config;

import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseControllerSecurityTest extends BaseControllerTest {

    protected static final String OTHER_ROLE = "SOME_OTHER_ROLE";

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
}
