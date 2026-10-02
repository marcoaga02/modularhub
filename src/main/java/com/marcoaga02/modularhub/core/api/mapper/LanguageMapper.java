package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class LanguageMapper {

    @Mapping(source = "uuid", target = "id")
    public abstract LanguageResponseDTO toDto(Language language);

}
