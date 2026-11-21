package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.GlobalExceptionHandler;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import com.marcoaga02.modularhub.config.BaseControllerTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest extends BaseControllerTest {

    @MockitoBean
    private AccountService accountService;

    @Nested
    class GetCurrentAccount {

        @Test
        void getCurrentAccount_shouldReturnOkWithAccount() throws Exception {
            AccountDTO account = new AccountDTO()
                    .identityId("identity-id")
                    .email("mario@test.com")
                    .username("mario.rossi")
                    .firstName("Mario")
                    .lastName("Rossi")
                    .permissions(Set.of("USER_MANAGEMENT"))
                    .preferences(new AccountPreferencesResponseDTO().language(LanguageDTO.IT));
            when(accountService.getCurrentAccount()).thenReturn(account);

            perform(get("/account"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.identityId").value("identity-id"))
                    .andExpect(jsonPath("$.email").value("mario@test.com"))
                    .andExpect(jsonPath("$.username").value("mario.rossi"))
                    .andExpect(jsonPath("$.firstName").value("Mario"))
                    .andExpect(jsonPath("$.lastName").value("Rossi"))
                    .andExpect(jsonPath("$.permissions[0]").value("USER_MANAGEMENT"))
                    .andExpect(jsonPath("$.preferences.language").value("it"));

            verify(accountService).getCurrentAccount();
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldReturnOkWithUpdatedPreferences_whenRequestIsValid() throws Exception {
            AccountPreferencesUpdateDTO expectedRequest = new AccountPreferencesUpdateDTO().language(LanguageDTO.IT);
            when(accountService.updateCurrentAccountPreferences(expectedRequest))
                    .thenReturn(new AccountPreferencesResponseDTO().language(LanguageDTO.IT));

            perform(updatePreferences("""
                            {"language": "it"}
                            """))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.language").value("it"));

            verify(accountService).updateCurrentAccountPreferences(expectedRequest);
        }

        @Test
        void updatePreferences_shouldReturnBadRequestWithInvalidFields_whenLanguageIsMissing() throws Exception {
            perform(updatePreferences("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS))
                    .andExpect(jsonPath("$.params." + GlobalExceptionHandler.FIELDS_PARAM).value("language"));

            verifyNoInteractions(accountService);
        }

        @Test
        void updatePreferences_shouldReturnBadRequest_whenLanguageIsNotSupported() throws Exception {
            perform(updatePreferences("""
                            {"language": "fr"}
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_REQUEST));

            verifyNoInteractions(accountService);
        }

        private static MockHttpServletRequestBuilder updatePreferences(String body) {
            return put("/account/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body);
        }
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return performWithPermissions(request);
    }
}
