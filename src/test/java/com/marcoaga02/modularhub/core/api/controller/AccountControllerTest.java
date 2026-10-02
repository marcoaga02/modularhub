package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    private AccountDTO accountDTO;

    @BeforeEach
    void setUp() {
        LanguageResponseDTO language = new LanguageResponseDTO();
        language.setId("lang-123");
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        AccountPreferencesResponseDTO preferences = new AccountPreferencesResponseDTO();
        preferences.setLanguage(language);

        accountDTO = AccountDTO.builder()
                .identityId("identity-123")
                .email("mario@example.com")
                .username("mario.rossi")
                .firstName("Mario")
                .lastName("Rossi")
                .roles(Set.of("ROLE_USER"))
                .preferences(preferences)
                .build();
    }

    @Test
    void getCurrentAccount_shouldReturnOkWithAccountData() throws Exception {
        when(accountService.getCurrentAccount()).thenReturn(accountDTO);

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.identityId").value("identity-123"))
                .andExpect(jsonPath("$.email").value("mario@example.com"))
                .andExpect(jsonPath("$.username").value("mario.rossi"))
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.preferences.language.id").value("lang-123"))
                .andExpect(jsonPath("$.preferences.language.code").value("it-IT"))
                .andExpect(jsonPath("$.preferences.language.label").value("Italiano"))
                .andExpect(jsonPath("$.preferences.language.isDefault").value(true));

        verify(accountService, times(1)).getCurrentAccount();
    }

    @Test
    void updatePreferences_shouldReturnOkWithUpdatedPreferences() throws Exception {
        LanguageResponseDTO language = new LanguageResponseDTO();
        language.setId("lang-456");
        language.setCode("en-US");
        language.setLabel("English");
        language.setIsDefault(false);

        AccountPreferencesResponseDTO response = new AccountPreferencesResponseDTO();
        response.setLanguage(language);

        when(accountService.updateCurrentAccountPreferences(any())).thenReturn(response);

        mockMvc.perform(put("/account/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "languageId": "lang-456"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.language.id").value("lang-456"))
                .andExpect(jsonPath("$.language.code").value("en-US"))
                .andExpect(jsonPath("$.language.label").value("English"))
                .andExpect(jsonPath("$.language.isDefault").value(false));

        verify(accountService, times(1)).updateCurrentAccountPreferences(any());
    }

}