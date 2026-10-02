package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountProfileDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountProfileMapper {

    AccountProfileDTO toAccountProfileDTO(User user);

}
