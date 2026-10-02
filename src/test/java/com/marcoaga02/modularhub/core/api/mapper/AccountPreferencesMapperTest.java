package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.AccountPreferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({AccountPreferencesMapperImpl.class, LanguageMapperImpl.class})
class AccountPreferencesMapperTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private AccountPreferencesMapper mapper;

    @MockitoBean
    private AccountPreferencesRepository repository;

    private Language lang;

    @BeforeEach
    void setUp() {
        lang = new Language();
        lang.setCode("it-IT");
        lang.setLabel("Italiano");
        lang.setIsDefault(true);
    }

    @Test
    void toDto_shouldMapAllFields() {
        final String identityId = "identity-id";

        AccountPreferences preferences = new AccountPreferences();
        preferences.setIdentityId(identityId);
        preferences.setLanguage(lang);

        AccountPreferencesResponseDTO dto =  mapper.toDto(preferences);

        assertThat(dto).isNotNull();
        assertThat(dto.getLanguage()).isNotNull();
        assertThat(dto.getLanguage().getId()).isEqualTo(lang.getUuid());
        assertThat(dto.getLanguage().getCode()).isEqualTo("it-IT");
        assertThat(dto.getLanguage().getLabel()).isEqualTo("Italiano");
        assertThat(dto.getLanguage().getIsDefault()).isTrue();
    }

    @Test
    void toDto_shouldReturnNull_whenInputIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}