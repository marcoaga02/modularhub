package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPreferencesMapperTest {

    private final AccountPreferencesMapper mapper =
            new AccountPreferencesMapperImpl(Mappers.getMapper(LanguageMapper.class));

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
        AccountPreferences preferences = new AccountPreferences();
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