package com.marcoaga02.modularhub.modules.usermanagement.api.exception.user;

import com.marcoaga02.modularhub.core.api.exception.NotFoundException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;

import java.util.Map;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String uuid) {
        super("User not found: " + uuid, UserManagementExceptionCodes.Model.User.NOT_FOUND, Map.of());
    }
}
