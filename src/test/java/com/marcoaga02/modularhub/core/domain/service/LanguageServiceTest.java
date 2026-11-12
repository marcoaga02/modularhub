package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.api.mapper.LanguageMapper;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.UserFixtures.language;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageService languageService;

    @Test
    void getLanguages_shouldMapEveryLanguageKeepingTheRepositoryOrder() {
        Language italian = language("it-IT", true);
        Language english = language("en-US", false);
        LanguageResponseDTO italianDto = new LanguageResponseDTO().code("it-IT");
        LanguageResponseDTO englishDto = new LanguageResponseDTO().code("en-US");
        when(languageRepository.findAll()).thenReturn(List.of(italian, english));
        when(languageMapper.toDto(italian)).thenReturn(italianDto);
        when(languageMapper.toDto(english)).thenReturn(englishDto);

        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result).containsExactly(italianDto, englishDto);
        verify(languageMapper).toDto(italian);
        verify(languageMapper).toDto(english);
    }

    @Test
    void getLanguages_shouldReturnEmptyList_whenNoLanguageExists() {
        when(languageRepository.findAll()).thenReturn(List.of());

        assertThat(languageService.getLanguages()).isEmpty();

        verifyNoInteractions(languageMapper);
    }
}
