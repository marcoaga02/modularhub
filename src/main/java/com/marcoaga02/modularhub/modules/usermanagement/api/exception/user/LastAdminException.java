package com.marcoaga02.modularhub.modules.usermanagement.api.exception.user;

import com.marcoaga02.modularhub.core.api.exception.ForbiddenException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;

import java.util.Map;

public class LastAdminException extends ForbiddenException {

    public LastAdminException(String uuid) {
        super("Operation would leave no active administrator: " + uuid, UserManagementExceptionCodes.Model.User.LAST_ADMIN, Map.of());
    }
}
