package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = LanguageMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccountPreferencesMapper {

    AccountPreferencesResponseDTO toDto(AccountPreferences accountPreferences);

}
