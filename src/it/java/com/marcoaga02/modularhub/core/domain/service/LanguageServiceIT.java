package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.ModularhubApplication;
import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ModularhubApplication.class)
@Transactional
class LanguageServiceIT extends BaseITWithMockIdentity {

    @Autowired
    private LanguageService languageService;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getLanguages_shouldReturnEmpty_whenNoLanguagesFound() {
        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result).isEmpty();
    }

    @Test
    void getLanguages_shouldReturnLanguage_whenValidRequestSingleLanguage() {
        Language italianLang = createAndSaveLanguage("it-IT", "Italiano", true);

        List<LanguageResponseDTO> result = languageService.getLanguages();

        assertThat(result).hasSize(1);

        assertThat(result).extracting(LanguageResponseDTO::getId)
                .containsExactly(italianLang.getUuid());
    }

    @Test
    void getLanguages_shouldReturnLanguages_whenValidRequestMultipleLanguages() {
        Language italianLang = createAndSaveLanguage("it-IT", "Italiano", true);
        Language englishLang = createAndSaveLanguage("en-US", "English", false);

        List<LanguageResponseDTO> result = languageService.getLanguages().stream()
                .sorted(Comparator.comparing(LanguageResponseDTO::getCode))
                .toList();

        assertThat(result).hasSize(2);

        assertThat(result).extracting(LanguageResponseDTO::getId)
                .containsExactlyInAnyOrder(italianLang.getUuid(), englishLang.getUuid());
    }

    private Language createAndSaveLanguage(String code, String label, boolean isDefault) {
        Language language = new Language();
        language.setCode(code);
        language.setLabel(label);
        language.setIsDefault(isDefault);
        languageRepository.save(language);

        entityManager.flush();
        entityManager.clear();

        return language;
    }

}