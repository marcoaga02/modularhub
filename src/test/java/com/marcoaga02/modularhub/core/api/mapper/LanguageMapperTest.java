package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LanguageMapperTest {

    private final LanguageMapper mapper = Mappers.getMapper(LanguageMapper.class);

    @Test
    void toDto_shouldMapAllFields() {
        Language language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italian");
        language.setIsDefault(true);

        LanguageResponseDTO dto = mapper.toDto(language);

        assertThat(dto.getId()).isEqualTo(language.getUuid());
        assertThat(dto.getCode()).isEqualTo("it-IT");
        assertThat(dto.getLabel()).isEqualTo("Italian");
        assertThat(dto.getIsDefault()).isTrue();
    }

    @Test
    void toDto_shouldReturnNull_whenInputIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}