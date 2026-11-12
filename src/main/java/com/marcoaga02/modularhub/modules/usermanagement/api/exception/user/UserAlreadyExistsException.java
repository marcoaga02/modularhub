package com.marcoaga02.modularhub.modules.usermanagement.api.exception.user;

import com.marcoaga02.modularhub.core.api.exception.ConflictException;
import com.marcoaga02.modularhub.modules.usermanagement.api.exception.UserManagementExceptionCodes;

import java.util.Map;

public class UserAlreadyExistsException extends ConflictException {

    private UserAlreadyExistsException(String field, String value, String errorCode) {
        super("User with the same " + field + " already exists", errorCode, Map.of("value", value));
    }

    public static UserAlreadyExistsException taxIdNumber(String value) {
        return new UserAlreadyExistsException("taxIdNumber", value, UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_TAX_ID);
    }

    public static UserAlreadyExistsException username(String value) {
        return new UserAlreadyExistsException("username", value, UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_USERNAME);
    }

    public static UserAlreadyExistsException email(String value) {
        return new UserAlreadyExistsException("email", value, UserManagementExceptionCodes.Model.User.ALREADY_EXISTS_EMAIL);
    }
}
