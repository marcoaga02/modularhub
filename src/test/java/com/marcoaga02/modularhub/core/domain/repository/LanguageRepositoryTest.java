package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.Language;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findByUuid_shouldFindLanguage_whenValidUuid() {
        Language language = createAndSaveLanguage("it-IT", "Italiano", true);

        Optional<Language> result = languageRepository.findByUuid(language.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(language.getUuid());
    }

    @Test
    void findByUuid_shouldReturnEmpty_whenInvalidUuid() {
        Optional<Language> result = languageRepository.findByUuid("invalid-uuid");

        assertThat(result).isEmpty();
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