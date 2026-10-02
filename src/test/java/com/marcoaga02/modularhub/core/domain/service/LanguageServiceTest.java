package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.api.mapper.LanguageMapper;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private LanguageMapper languageMapper;

    @InjectMocks
    private LanguageService languageService;

    private Language language1, language2;

    private LanguageResponseDTO languageResponseDTO1, languageResponseDTO2;

    @BeforeEach
    void setUp() {
        language1 = new Language();
        language1.setCode("it-IT");
        language1.setLabel("Italiano");
        language1.setIsDefault(true);

        languageResponseDTO1 = new LanguageResponseDTO();
        languageResponseDTO1.setId(language1.getUuid());
        languageResponseDTO1.setCode("it-IT");
        languageResponseDTO1.setLabel("Italiano");
        languageResponseDTO1.setIsDefault(true);

        language2 = new Language();
        language2.setCode("en-EN");
        language2.setLabel("English");
        language2.setIsDefault(false);

        languageResponseDTO2 = new LanguageResponseDTO();
        languageResponseDTO2.setId(language2.getUuid());
        languageResponseDTO2.setCode("en-EN");
        languageResponseDTO2.setLabel("English");
        languageResponseDTO2.setIsDefault(false);
    }

    @Test
    void getLanguages_shouldReturnListOfLanguages_whenSingleLanguageExist() {
        when(languageRepository.findAll()).thenReturn(List.of(language1));
        when(languageMapper.toDto(language1)).thenReturn(languageResponseDTO1);

        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result)
                .hasSize(1)
                .containsExactly(languageResponseDTO1);

        verify(languageRepository).findAll();
        verify(languageMapper).toDto(language1);
    }

    @Test
    void getLanguages_shouldReturnAllLanguages_whenMultipleLanguagesExist() {
        when(languageRepository.findAll()).thenReturn(List.of(language1, language2));
        when(languageMapper.toDto(language1)).thenReturn(languageResponseDTO1);
        when(languageMapper.toDto(language2)).thenReturn(languageResponseDTO2);

        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(languageResponseDTO1, languageResponseDTO2);

        verify(languageRepository).findAll();
        verify(languageMapper).toDto(language1);
        verify(languageMapper).toDto(language2);
    }

    @Test
    void getLanguages_shouldReturnEmptyList_whenNoLanguagesExist() {
        when(languageRepository.findAll()).thenReturn(List.of());

        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result).isEmpty();

        verify(languageRepository).findAll();
    }
}
