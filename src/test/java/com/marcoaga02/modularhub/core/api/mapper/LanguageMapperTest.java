package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageMapperTest {

    private final LanguageMapper mapper = new LanguageMapperImpl();

    @Test
    void toDto_shouldExposeUuidAsId() {
        Language language = new Language();

        LanguageResponseDTO dto = mapper.toDto(language);

        assertThat(dto.getId()).isEqualTo(language.getUuid());
    }

    @Test
    void toDto_shouldCopyCodeLabelAndDefaultFlag() {
        Language language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        LanguageResponseDTO dto = mapper.toDto(language);

        assertThat(dto)
                .extracting(LanguageResponseDTO::getCode, LanguageResponseDTO::getLabel, LanguageResponseDTO::getIsDefault)
                .containsExactly("it-IT", "Italiano", true);
    }

    @Test
    void toDto_shouldReturnNull_whenLanguageIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
