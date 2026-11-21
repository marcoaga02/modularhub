package com.marcoaga02.modularhub.core.api.controller;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.openapi.AccountApi;
import com.marcoaga02.modularhub.core.domain.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController implements AccountApi {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public ResponseEntity<AccountDTO> getCurrentAccount() {
        return ResponseEntity.ok(accountService.getCurrentAccount());
    }

    @Override
    public ResponseEntity<AccountPreferencesResponseDTO> updatePreferences(AccountPreferencesUpdateDTO dto) {
        return ResponseEntity.ok(accountService.updateCurrentAccountPreferences(dto));
    }

}
