package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.GlobalExceptionHandler;
import com.marcoaga02.modularhub.core.api.exception.language.LanguageNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
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

    private static final String LANGUAGE_ID = "language-id";

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
                    .roles(Set.of("USER_MANAGEMENT"))
                    .preferences(new AccountPreferencesResponseDTO().language(language()));
            when(accountService.getCurrentAccount()).thenReturn(account);

            perform(get("/account"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.identityId").value("identity-id"))
                    .andExpect(jsonPath("$.email").value("mario@test.com"))
                    .andExpect(jsonPath("$.username").value("mario.rossi"))
                    .andExpect(jsonPath("$.firstName").value("Mario"))
                    .andExpect(jsonPath("$.lastName").value("Rossi"))
                    .andExpect(jsonPath("$.roles[0]").value("USER_MANAGEMENT"))
                    .andExpect(jsonPath("$.preferences.language.id").value(LANGUAGE_ID))
                    .andExpect(jsonPath("$.preferences.language.code").value("it-IT"));

            verify(accountService).getCurrentAccount();
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldReturnOkWithUpdatedPreferences_whenRequestIsValid() throws Exception {
            AccountPreferencesUpdateDTO expectedRequest = new AccountPreferencesUpdateDTO().languageId(LANGUAGE_ID);
            when(accountService.updateCurrentAccountPreferences(expectedRequest))
                    .thenReturn(new AccountPreferencesResponseDTO().language(language()));

            perform(updatePreferences("""
                            {"languageId": "%s"}
                            """.formatted(LANGUAGE_ID)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.language.id").value(LANGUAGE_ID))
                    .andExpect(jsonPath("$.language.code").value("it-IT"));

            verify(accountService).updateCurrentAccountPreferences(expectedRequest);
        }

        @Test
        void updatePreferences_shouldReturnBadRequestWithInvalidFields_whenLanguageIdIsMissing() throws Exception {
            perform(updatePreferences("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Validation.INVALID_FIELDS))
                    .andExpect(jsonPath("$.params." + GlobalExceptionHandler.FIELDS_PARAM).value("languageId"));

            verifyNoInteractions(accountService);
        }

        @Test
        void updatePreferences_shouldReturnBadRequest_whenLanguageDoesNotExist() throws Exception {
            when(accountService.updateCurrentAccountPreferences(any()))
                    .thenThrow(new LanguageNotFoundException(LANGUAGE_ID));

            perform(updatePreferences("""
                            {"languageId": "%s"}
                            """.formatted(LANGUAGE_ID)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value(ExceptionCodes.Model.Language.NOT_FOUND));
        }

        private static MockHttpServletRequestBuilder updatePreferences(String body) {
            return put("/account/preferences")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body);
        }
    }

    private static LanguageResponseDTO language() {
        return new LanguageResponseDTO().id(LANGUAGE_ID).code("it-IT").label("Italiano").isDefault(true);
    }

    private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
        return performWithRoles(request);
    }
}
