package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "language", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(source = "user.uuid", target = "id")
    UserResponseDTO toDto(User user);

    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "updatedOn", ignore = true)
    @Mapping(target = "deletedOn", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "identityId", ignore = true)
    User updateEntity(UserRequestDTO dto, @MappingTarget User user);

}
