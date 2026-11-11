package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.domain.model.User;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = AccountPreferencesMapper.class, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AccountMapper {

    @Mapping(source = "firstname", target = "firstName")
    @Mapping(source = "lastname", target = "lastName")
    @Mapping(target = "roles", ignore = true)
    AccountDTO toDto(User user);

}
