package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.RoleSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(source = "uuid", target = "id")
    RoleSummaryDTO toSummary(Role role);

}
