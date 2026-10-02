package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesCreateDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.exception.AccountPreferencesNotFoundException;
import com.marcoaga02.modularhub.core.api.exception.InvalidArgumentException;
import com.marcoaga02.modularhub.core.api.exception.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.AccountPreferencesRepository;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountPreferencesService {

    private final AccountPreferencesRepository accountPreferencesRepository;

    private final AccountPreferencesMapper accountPreferencesMapper;

    private final LanguageRepository languageRepository;

    public AccountPreferencesService(AccountPreferencesRepository accountPreferencesRepository, AccountPreferencesMapper accountPreferencesMapper, LanguageRepository languageRepository) {
        this.accountPreferencesRepository = accountPreferencesRepository;
        this.accountPreferencesMapper = accountPreferencesMapper;
        this.languageRepository = languageRepository;
    }

    public AccountPreferencesResponseDTO getAccountPreferences(String identityId) {
        if (identityId == null) {
            throw new InvalidArgumentException("identityId cannot be null");
        }

        AccountPreferences accountPreferences = accountPreferencesRepository.findByIdentityId(identityId)
                .orElseThrow(() -> new AccountPreferencesNotFoundException(identityId));

        return accountPreferencesMapper.toDto(accountPreferences);
    }

    @Transactional
    public AccountPreferencesResponseDTO createAccountPreferences(AccountPreferencesCreateDTO dto) {
        Language language = validateLanguageOrElseThrow(dto.getLanguageId());

        AccountPreferences preferences = new AccountPreferences();
        preferences.setIdentityId(dto.getIdentityId());
        preferences.setLanguage(language);

        return accountPreferencesMapper.toDto(accountPreferencesRepository.save(preferences));
    }

    @Transactional
    public AccountPreferencesResponseDTO updateAccountPreferencesByIdentityId(String identityId, AccountPreferencesUpdateDTO dto) {
        AccountPreferences accountPreferences = accountPreferencesRepository.findByIdentityId(identityId)
                .orElseThrow(() -> new AccountPreferencesNotFoundException(identityId));

        Language language = validateLanguageOrElseThrow(dto.getLanguageId());
        accountPreferences.setLanguage(language);

        return accountPreferencesMapper.toDto(accountPreferences);
    }

    private Language validateLanguageOrElseThrow(String languageId) {
        return languageRepository.findByUuid(languageId)
                .orElseThrow(() -> new LanguageNotFoundException(languageId));
    }

}
