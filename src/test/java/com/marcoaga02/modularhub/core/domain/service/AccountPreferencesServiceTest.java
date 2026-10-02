package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesCreateDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.api.exception.AccountPreferencesNotFoundException;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.InvalidArgumentException;
import com.marcoaga02.modularhub.core.api.exception.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.AccountPreferencesRepository;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AccountPreferencesServiceTest {

    @Mock
    private AccountPreferencesRepository preferencesRepository;

    @Mock
    private AccountPreferencesMapper mapper;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private AccountPreferencesService service;

    private Language lang;
    private LanguageResponseDTO langRespDto;

    private static final String LANG_1_ID = "LANG_1_ID";

    @BeforeEach
    void setUp() {
        lang = createLanguage("it-IT", "Italiano", true);
        langRespDto = createLanguageResponseDto(LANG_1_ID, lang);
    }

    @Test
    void getAccountPreferences_shouldReturnPreferences_whenValidRequest() {
        final String identityId = "identity-id";

        AccountPreferences pref = createAccountPreferences("identity1", lang);
        AccountPreferencesResponseDTO prefRespDto = createAccountPreferencesResponseDto(langRespDto);

        when(preferencesRepository.findByIdentityId(identityId))
                .thenReturn(Optional.of(pref));
        when(mapper.toDto(pref))
                .thenReturn(prefRespDto);

        AccountPreferencesResponseDTO result = service.getAccountPreferences(identityId);

        assertThat(result).isNotNull().isEqualTo(prefRespDto);

        verify(preferencesRepository).findByIdentityId(identityId);
        verify(mapper).toDto(pref);
    }

    @Test
    void getAccountPreferences_shouldThrowIllegalArgumentException_whenIdentityIdIsNull() {
        assertThatThrownBy(() -> service.getAccountPreferences(null))
                .isInstanceOf(InvalidArgumentException.class)
                .satisfies(e -> {
                    InvalidArgumentException ex = (InvalidArgumentException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("identityId cannot be null");
                });

        verify(preferencesRepository, never()).findByIdentityId(anyString());
        verify(mapper, never()).toDto(any(AccountPreferences.class));
    }

    @Test
    void getAccountPreferences_shouldThrowNotFoundException_whenIdentityIdIsNotFound() {
        final String identityId = "identity-id";

        when(preferencesRepository.findByIdentityId(identityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAccountPreferences(identityId))
                .isInstanceOf(AccountPreferencesNotFoundException.class)
                .satisfies(e -> {
                    AccountPreferencesNotFoundException ex = (AccountPreferencesNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.ACCOUNT_PREFERENCES_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("AccountPreferences for identityId 'identity-id' not found");
                });

        verify(preferencesRepository).findByIdentityId(identityId);
        verify(mapper, never()).toDto(any(AccountPreferences.class));
    }

    @Test
    void createAccountPreferences_shouldCreateAccountPreferences_whenValidRequest() {
        final String identityId = "identity-id";

        AccountPreferencesCreateDTO createDto = new AccountPreferencesCreateDTO();
        createDto.setLanguageId(lang.getUuid());
        createDto.setIdentityId(identityId);

        AccountPreferences pref = createAccountPreferences(identityId, lang);
        AccountPreferencesResponseDTO responseDto = createAccountPreferencesResponseDto(langRespDto);

        when(languageRepository.findByUuid(lang.getUuid()))
                .thenReturn(Optional.of(lang));
        when(preferencesRepository.save(any(AccountPreferences.class)))
                .thenReturn(pref);
        when(mapper.toDto(pref))
                .thenReturn(responseDto);

        AccountPreferencesResponseDTO result = service.createAccountPreferences(createDto);

        assertThat(result).isNotNull().isEqualTo(responseDto);

        verify(languageRepository).findByUuid(lang.getUuid());
        verify(preferencesRepository).save(any(AccountPreferences.class));
        verify(mapper).toDto(pref);
    }

    @Test
    void createAccountPreferences_shouldThrowNotFoundException_whenLanguageIdIsNotFound() {
        final String identityId = "identity-id";
        final String languageId = "invalid-language-id";

        AccountPreferencesCreateDTO createDto = new AccountPreferencesCreateDTO();
        createDto.setLanguageId(languageId);
        createDto.setIdentityId(identityId);

        when(languageRepository.findByUuid(languageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAccountPreferences(createDto))
                .isInstanceOf(LanguageNotFoundException.class)
                .satisfies(e -> {
                    LanguageNotFoundException ex = (LanguageNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.LANGUAGE_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("Language with uuid 'invalid-language-id' not found");
                });

        verify(languageRepository).findByUuid(languageId);
        verify(preferencesRepository, never()).save(any(AccountPreferences.class));
        verify(mapper, never()).toDto(any());
    }

    @Test
    void updateAccountPreferences_shouldUpdateAccountPreferences_whenValidRequest() {
        final String identityId = "identity-id";

        AccountPreferencesUpdateDTO updateDto = new AccountPreferencesUpdateDTO();
        updateDto.setLanguageId(lang.getUuid());

        AccountPreferences pref = createAccountPreferences(identityId, lang);
        AccountPreferencesResponseDTO responseDto = createAccountPreferencesResponseDto(langRespDto);

        when(preferencesRepository.findByIdentityId(identityId))
                .thenReturn(Optional.of(pref));
        when(languageRepository.findByUuid(lang.getUuid()))
                .thenReturn(Optional.of(lang));
        when(mapper.toDto(pref))
                .thenReturn(responseDto);

        service.updateAccountPreferencesByIdentityId(identityId, updateDto);

        verify(preferencesRepository).findByIdentityId(identityId);
        verify(languageRepository).findByUuid(lang.getUuid());
        verify(mapper).toDto(pref);
    }

    @Test
    void updateAccountPreferences_shouldThrowNotFoundException_whenIdentityIdIsNotFound() {
        final String identityId = "not-found-id";

        AccountPreferencesUpdateDTO updateDto = new AccountPreferencesUpdateDTO();
        updateDto.setLanguageId(lang.getUuid());

        when(preferencesRepository.findByIdentityId(identityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAccountPreferencesByIdentityId(identityId, updateDto))
                .isInstanceOf(AccountPreferencesNotFoundException.class)
                .satisfies(e -> {
                    AccountPreferencesNotFoundException ex = (AccountPreferencesNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.ACCOUNT_PREFERENCES_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("AccountPreferences for identityId 'not-found-id' not found");
                });

        verify(preferencesRepository).findByIdentityId(identityId);
        verify(languageRepository, never()).findByUuid(anyString());
        verify(mapper, never()).toDto(any());
    }

    @Test
    void updateAccountPreferences_shouldThrowNotFoundException_whenLanguageIdIsNotFound() {
        final String identityId = "identity-id";
        final String languageId = "invalid-language-id";

        AccountPreferencesUpdateDTO updateDto = new AccountPreferencesUpdateDTO();
        updateDto.setLanguageId(languageId);

        AccountPreferences pref = createAccountPreferences(identityId, lang);

        when(preferencesRepository.findByIdentityId(identityId))
                .thenReturn(Optional.of(pref));
        when(languageRepository.findByUuid(languageId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAccountPreferencesByIdentityId(identityId, updateDto))
                .isInstanceOf(LanguageNotFoundException.class)
                .satisfies(e -> {
                    LanguageNotFoundException ex = (LanguageNotFoundException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.LANGUAGE_NOT_FOUND);
                    assertThat(ex.getLogMessage()).isEqualTo("Language with uuid 'invalid-language-id' not found");
                });

        verify(preferencesRepository).findByIdentityId(identityId);
        verify(languageRepository).findByUuid(languageId);
        verify(mapper, never()).toDto(any());
    }

    private AccountPreferences createAccountPreferences(
            String identityId,
            Language language
    ) {
        AccountPreferences preferences = new AccountPreferences();
        preferences.setIdentityId(identityId);
        preferences.setLanguage(language);

        return preferences;
    }

    private AccountPreferencesResponseDTO createAccountPreferencesResponseDto(
            LanguageResponseDTO language
    ) {
        AccountPreferencesResponseDTO dto = new AccountPreferencesResponseDTO();
        dto.setLanguage(language);

        return dto;
    }


    private Language createLanguage(String code, String label, boolean isDefault) {
        Language language = new Language();
        language.setCode(code);
        language.setLabel(label);
        language.setIsDefault(isDefault);

        return language;
    }

    private LanguageResponseDTO createLanguageResponseDto(String id, Language language) {
        LanguageResponseDTO dto = new LanguageResponseDTO();
        dto.setId(id);
        dto.setCode(language.getCode());
        dto.setLabel(language.getLabel());
        dto.setIsDefault(language.getIsDefault());

        return dto;
    }

}