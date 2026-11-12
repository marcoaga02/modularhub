package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.mapper.LanguageMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.core.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl(Mappers.getMapper(LanguageMapper.class));

    private static final OffsetDateTime CREATED_ON = OffsetDateTime.parse("2026-01-01T10:15:30+01:00");
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.parse("2026-02-01T10:15:30+01:00");
    private static final OffsetDateTime DELETED_ON = OffsetDateTime.parse("2026-03-01T10:15:30+01:00");

    @Test
    void toDto_shouldMapAllFields_whenIgnoringAudit() {
        User user = buildUser();

        UserResponseDTO dto = userMapper.toDto(user);

        assertThat(dto.getFirstname()).isEqualTo("firstname");
        assertThat(dto.getLastname()).isEqualTo("lastname");
        assertThat(dto.getGender()).isEqualTo(GenderDTO.M);
        assertThat(dto.getLanguage()).isNotNull();
        assertThat(dto.getLanguage().getId()).isEqualTo(user.getPreferences().getLanguage().getUuid());
        assertThat(dto.getLanguage().getCode()).isEqualTo("it-IT");
        assertThat(dto.getMobileNumber()).isEqualTo("0000");
        assertThat(dto.getTaxIdNumber()).isEqualTo("USR1");
        assertThat(dto.getEmail()).isEqualTo("user@email.com");
        assertThat(dto.getUsername()).isEqualTo("username");
        assertThat(dto.getEnabled()).isTrue();
        assertThat(dto.getAudit()).isNull();
    }

    @Test
    void toDto_shouldReturnNullLanguage_whenPreferencesAreNull() {
        User user = buildUser();
        user.setPreferences(null);

        UserResponseDTO dto = userMapper.toDto(user);

        assertThat(dto.getLanguage()).isNull();
    }

    @Test
    void toDto_shouldReturnNull_whenIgnoringAuditAndInputIsNull() {
        assertThat(userMapper.toDto(null)).isNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        UserUpdateRequestDTO dto = buildUserUpdateRequestDTO();
        User existing = buildUser();
        String originalUuid = existing.getUuid();
        AccountPreferences originalPreferences = existing.getPreferences();

        User result = userMapper.updateEntity(dto, existing);
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(existing);

        assertThat(existing.getFirstname()).isEqualTo("new firstname");
        assertThat(existing.getLastname()).isEqualTo("new lastname");
        assertThat(existing.getGender()).isEqualTo(Gender.F);
        assertThat(existing.getMobileNumber()).isEqualTo("9999");
        assertThat(existing.getTaxIdNumber()).isEqualTo("NEW1");
        assertThat(existing.getEmail()).isEqualTo("new@email.com");
        assertThat(existing.getUsername()).isEqualTo("new username");
        assertThat(existing.getEnabled()).isFalse();
        assertThat(existing.getUuid()).isEqualTo(originalUuid);
        assertThat(existing.getCreatedOn()).isEqualTo(CREATED_ON);
        assertThat(existing.getUpdatedOn()).isEqualTo(UPDATED_ON);
        assertThat(existing.getDeletedOn()).isEqualTo(DELETED_ON);
        assertThat(existing.getCreatedBy()).isEqualTo("user creator");
        assertThat(existing.getUpdatedBy()).isEqualTo("user updater");
        assertThat(existing.getDeletedBy()).isEqualTo("user deleter");
        assertThat(existing.getIdentityId()).isEqualTo("identity");
        assertThat(existing.getPreferences()).isSameAs(originalPreferences);
    }

    @Test
    void updateEntity_shouldUpdateAllFields_whenDtoIsCreateRequest() {
        UserCreateRequestDTO dto = buildUserCreateRequestDTO();
        User user = new User();

        User result = userMapper.updateEntity(dto, user);
        assertThat(result).isSameAs(user);

        assertThat(user.getFirstname()).isEqualTo("Mario");
        assertThat(user.getLastname()).isEqualTo("Rossi");
        assertThat(user.getGender()).isEqualTo(Gender.M);
        assertThat(user.getMobileNumber()).isEqualTo("1234");
        assertThat(user.getTaxIdNumber()).isEqualTo("NEW2");
        assertThat(user.getEmail()).isEqualTo("mario@email.com");
        assertThat(user.getUsername()).isEqualTo("mario");
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getIdentityId()).isNull();
        assertThat(user.getPreferences()).isNull();
    }

    @Test
    void updateEntity_shouldReturnUnmodifiedUser_whenCreateRequestIsNull() {
        User existing = buildUser();

        User result = userMapper.updateEntity((UserCreateRequestDTO) null, existing);
        assertThat(result).isSameAs(existing);

        assertThat(existing.getFirstname()).isEqualTo("firstname");
        assertThat(existing.getEmail()).isEqualTo("user@email.com");
    }

    @Test
    void updateEntity_shouldReturnUnmodifiedUser_whenInputDtoIsNull() {
        User existing = buildUser();
        String originalUuid = existing.getUuid();

        User result = userMapper.updateEntity((UserUpdateRequestDTO) null, existing);
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(existing);

        assertThat(existing.getFirstname()).isEqualTo("firstname");
        assertThat(existing.getLastname()).isEqualTo("lastname");
        assertThat(existing.getGender()).isEqualTo(Gender.M);
        assertThat(existing.getMobileNumber()).isEqualTo("0000");
        assertThat(existing.getTaxIdNumber()).isEqualTo("USR1");
        assertThat(existing.getEmail()).isEqualTo("user@email.com");
        assertThat(existing.getUsername()).isEqualTo("username");
        assertThat(existing.getEnabled()).isTrue();
        assertThat(existing.getUuid()).isEqualTo(originalUuid);
        assertThat(existing.getCreatedOn()).isEqualTo(CREATED_ON);
        assertThat(existing.getUpdatedOn()).isEqualTo(UPDATED_ON);
        assertThat(existing.getDeletedOn()).isEqualTo(DELETED_ON);
        assertThat(existing.getCreatedBy()).isEqualTo("user creator");
        assertThat(existing.getUpdatedBy()).isEqualTo("user updater");
        assertThat(existing.getDeletedBy()).isEqualTo("user deleter");
    }

    private User buildUser() {
        Language language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        User user = new User();
        user.setPreferences(preferences);
        user.setFirstname("firstname");
        user.setLastname("lastname");
        user.setGender(Gender.M);
        user.setMobileNumber("0000");
        user.setTaxIdNumber("USR1");
        user.setEmail("user@email.com");
        user.setUsername("username");
        user.setIdentityId("identity");
        user.setEnabled(true);
        user.setCreatedOn(CREATED_ON);
        user.setUpdatedOn(UPDATED_ON);
        user.setDeletedOn(DELETED_ON);
        user.setCreatedBy("user creator");
        user.setUpdatedBy("user updater");
        user.setDeletedBy("user deleter");
        return user;
    }

    private UserUpdateRequestDTO buildUserUpdateRequestDTO() {
        UserUpdateRequestDTO dto = new UserUpdateRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(GenderDTO.F);
        dto.setLanguageId("lang-uuid");
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber("NEW1");
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setGroupIds(List.of("group1", "group2"));
        dto.setEnabled(false);
        return dto;
    }

    private UserCreateRequestDTO buildUserCreateRequestDTO() {
        UserCreateRequestDTO dto = new UserCreateRequestDTO();
        dto.setFirstname("Mario");
        dto.setLastname("Rossi");
        dto.setGender(GenderDTO.M);
        dto.setLanguageId("lang-uuid");
        dto.setMobileNumber("1234");
        dto.setTaxIdNumber("NEW2");
        dto.setEmail("mario@email.com");
        dto.setUsername("mario");
        dto.setPassword("password");
        dto.setGroupIds(List.of("group1"));
        dto.setEnabled(true);
        return dto;
    }
}
