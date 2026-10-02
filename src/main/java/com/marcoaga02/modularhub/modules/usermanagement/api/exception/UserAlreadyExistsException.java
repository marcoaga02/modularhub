package com.marcoaga02.modularhub.modules.usermanagement.api.exception;

import com.marcoaga02.modularhub.core.api.exception.BadRequestException;

public class UserAlreadyExistsException extends BadRequestException {

    private UserAlreadyExistsException(String errorCode, String field, String value) {
        super(errorCode, String.format("User with %s '%s' already exists", field, value));
    }

    public static UserAlreadyExistsException taxIdNumber(String value) {
        return new UserAlreadyExistsException(
                UserManagementExceptionCodes.USER_TAX_ID_ALREADY_EXISTS, "taxIdNumber", value);
    }

    public static UserAlreadyExistsException username(String value) {
        return new UserAlreadyExistsException(
                UserManagementExceptionCodes.USER_USERNAME_ALREADY_EXISTS, "username", value);
    }

    public static UserAlreadyExistsException email(String value) {
        return new UserAlreadyExistsException(
                UserManagementExceptionCodes.USER_EMAIL_ALREADY_EXISTS, "email", value);
    }

}
