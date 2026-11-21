package com.marcoaga02.modularhub.modules.usermanagement.api.exception.role;

import com.marcoaga02.modularhub.core.api.exception.NotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;

import java.util.Map;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException(String uuid) {
        super("Role not found: " + uuid, UserManagementExceptionCodes.Model.Role.NOT_FOUND, Map.of());
    }
}
