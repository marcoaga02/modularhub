package com.marcoaga02.modularhub.modules.usermanagement.api.exception;

import com.marcoaga02.modularhub.core.api.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String uuid) {
        super(UserManagementExceptionCodes.USER_NOT_FOUND, String.format("User with uuid '%s' not found", uuid));
    }

}
