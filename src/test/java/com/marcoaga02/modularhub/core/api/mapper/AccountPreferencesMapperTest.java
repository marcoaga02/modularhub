package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPreferencesMapperTest {

    private final AccountPreferencesMapper mapper = new AccountPreferencesMapperImpl();

    @Nested
    class ToDto {

        @Test
        void toDto_shouldConvertLanguage() {
            AccountPreferences preferences = new AccountPreferences();
            preferences.setLanguage(Language.IT);

            AccountPreferencesResponseDTO dto = mapper.toDto(preferences);

            assertThat(dto.getLanguage()).isEqualTo(LanguageDTO.IT);
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

    @Nested
    class UpdateEntity {

        @Test
        void updateEntity_shouldConvertLanguageIntoPreferences() {
            AccountPreferences preferences = new AccountPreferences();
            preferences.setLanguage(Language.EN);

            mapper.updateEntity(new AccountPreferencesUpdateDTO().language(LanguageDTO.IT), preferences);

            assertThat(preferences.getLanguage()).isEqualTo(Language.IT);
        }

        @Test
        void updateEntity_shouldLeavePreferencesUnchanged_whenRequestIsNull() {
            AccountPreferences preferences = new AccountPreferences();
            preferences.setLanguage(Language.EN);

            mapper.updateEntity(null, preferences);

            assertThat(preferences.getLanguage()).isEqualTo(Language.EN);
        }
    }
}
