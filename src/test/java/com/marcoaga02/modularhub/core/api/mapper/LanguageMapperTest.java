package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@Import(LanguageMapperImpl.class)
class LanguageMapperTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private LanguageMapper mapper;

    @MockitoBean
    private LanguageRepository repository;

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