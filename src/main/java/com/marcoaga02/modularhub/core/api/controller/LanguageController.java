package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import com.marcoaga02.modularhub.core.api.openapi.LanguageApi;
import com.marcoaga02.modularhub.core.domain.service.LanguageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LanguageController implements LanguageApi {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public ResponseEntity<List<LanguageResponseDTO>> getLanguages() {
        return ResponseEntity.ok(languageService.getLanguages());
    }
}
