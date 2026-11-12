package com.marcoaga02.modularhub.core.api.exception.language;

import com.marcoaga02.modularhub.core.api.exception.BadRequestException;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;

import java.util.Map;

public class LanguageNotFoundException extends BadRequestException {

    public LanguageNotFoundException(String uuid) {
        super("Language not found: " + uuid, ExceptionCodes.Model.Language.NOT_FOUND, Map.of());
    }
}
