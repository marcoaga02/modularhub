package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.config.BaseControllerSecurityTest;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerSecurityTest extends BaseControllerSecurityTest {

    @MockitoBean
    private AccountService accountService;

    @Nested
    class GetCurrentAccount {

        private MockHttpServletRequestBuilder request() {
            return get("/account");
        }

        @Test
        void getCurrentAccount_shouldReturnOk_whenAuthenticatedWithoutRoles() throws Exception {
            performWithRoles(request()).andExpect(status().isOk());
        }

        @Test
        void getCurrentAccount_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(accountService);
        }
    }

    @Nested
    class UpdatePreferences {

        private MockHttpServletRequestBuilder request() {
            return put("/account/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "languageId": "lang-id"
                            }
                            """);
        }

        @Test
        void updatePreferences_shouldReturnOk_whenAuthenticatedWithoutRoles() throws Exception {
            performWithRoles(request()).andExpect(status().isOk());
        }

        @Test
        void updatePreferences_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
            assertUnauthorized(request());

            verifyNoInteractions(accountService);
        }
    }
}
