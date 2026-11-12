package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.config.BaseControllerSecurityTest;
import com.marcoaga02.modularhub.core.domain.service.LanguageService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LanguageController.class)
class LanguageControllerSecurityTest extends BaseControllerSecurityTest {

    @MockitoBean
    private LanguageService languageService;

    @Nested
    class GetLanguages {

        private MockHttpServletRequestBuilder request() {
            return get("/languages");
        }

        @Test
        void getLanguages_shouldReturnOk_whenAuthenticatedWithoutRoles() throws Exception {
            performWithRoles(request()).andExpect(status().isOk());
        }

        @Test
        void getLanguages_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(languageService);
        }
    }
}
