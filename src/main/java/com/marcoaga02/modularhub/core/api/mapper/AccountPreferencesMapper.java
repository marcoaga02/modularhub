package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountPreferencesMapper {

    AccountPreferencesResponseDTO toDto(AccountPreferences accountPreferences);

    void updateEntity(AccountPreferencesUpdateDTO dto, @MappingTarget AccountPreferences accountPreferences);

}
