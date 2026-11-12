package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPreferencesMapperTest {

    private final AccountPreferencesMapper mapper = new AccountPreferencesMapperImpl(new LanguageMapperImpl());

    @Test
    void toDto_shouldMapLanguageThroughLanguageMapper() {
        Language language = new Language();
        language.setCode("it-IT");
        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        AccountPreferencesResponseDTO dto = mapper.toDto(preferences);

        assertThat(dto.getLanguage().getId()).isEqualTo(language.getUuid());
        assertThat(dto.getLanguage().getCode()).isEqualTo("it-IT");
    }

    @Test
    void toDto_shouldLeaveLanguageNull_whenPreferencesHaveNoLanguage() {
        AccountPreferencesResponseDTO dto = mapper.toDto(new AccountPreferences());

        assertThat(dto.getLanguage()).isNull();
    }

    @Test
    void toDto_shouldReturnNull_whenPreferencesAreNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
