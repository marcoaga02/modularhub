package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.config.BaseKeycloakIT;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceRollbackIT extends BaseKeycloakIT {

    private static final String TOO_LONG_TAX_ID = "X".repeat(300);

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldKeepIdentityUser_whenTransactionCommits() {
        UserResponseDTO result = userService.createUser(createRequest("committed.user", "TAX"));

        assertThat(userRepository.findByUuidAndDeletedOnIsNull(result.getId())).isPresent();
        assertThat(realm().users().search("committed.user", true)).hasSize(1);
    }

    @Test
    void createUser_shouldDeleteIdentityUser_whenDatabaseSaveFails() {
        UserCreateRequestDTO request = createRequest("rolledback.user", TOO_LONG_TAX_ID);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userRepository.findAll()).isEmpty();
        assertThat(realm().users().search("rolledback.user", true)).isEmpty();
    }

    private UserCreateRequestDTO createRequest(String username, String taxIdNumber) {
        return new UserCreateRequestDTO()
                .firstname("Mario")
                .lastname("Rossi")
                .gender(GenderDTO.M)
                .language(LanguageDTO.IT)
                .taxIdNumber(taxIdNumber)
                .email(username + "@test.com")
                .username(username)
                .password("pa55w0rd")
                .enabled(true);
    }
}
