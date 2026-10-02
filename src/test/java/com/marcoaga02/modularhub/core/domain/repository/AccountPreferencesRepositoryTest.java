package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AccountPreferencesRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private AccountPreferencesRepository repository;

    @Autowired
    private EntityManager entityManager;

    private Language lang;

    @BeforeEach
    void setUp() {
        lang = new Language();
        lang.setCode("it-IT");
        lang.setLabel("Italiano");
        lang.setIsDefault(true);

        entityManager.persist(lang);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUuid_shouldReturnAccountPreferences_whenValidRequest() {
        AccountPreferences preferences = createAndSaveAccountPreferences("identity-id", lang);

        Optional<AccountPreferences> result = repository.findByUuid(preferences.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(preferences.getUuid());
    }

    @Test
    void findByUuid_shouldReturnEmpty_whenNotFound() {
        Optional<AccountPreferences> result = repository.findByUuid("non-existent-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdentityId_shouldReturnAccountPreferences_whenValidRequest() {
        final String identityId = "identity-id";
        AccountPreferences preferences = createAndSaveAccountPreferences(identityId, lang);

        Optional<AccountPreferences> result = repository.findByIdentityId(identityId);

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(preferences.getUuid());
    }

    @Test
    void findByIdentityId_shouldReturnEmpty_whenNotFound() {
        Optional<AccountPreferences> result = repository.findByIdentityId("non-existent-identity-id");

        assertThat(result).isEmpty();
    }

    private AccountPreferences createAndSaveAccountPreferences(String identityId, Language language) {
        AccountPreferences accountPreferences = new AccountPreferences();
        accountPreferences.setIdentityId(identityId);
        accountPreferences.setLanguage(language);

        repository.save(accountPreferences);
        entityManager.flush();
        entityManager.clear();

        return accountPreferences;
    }

}