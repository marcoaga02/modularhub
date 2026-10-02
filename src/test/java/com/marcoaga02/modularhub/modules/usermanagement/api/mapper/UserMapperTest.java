package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@Import(UserMapperImpl.class)
class UserMapperTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UserMapper userMapper;

    private static final OffsetDateTime CREATED_ON = OffsetDateTime.parse("2026-01-01T10:15:30+01:00");
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.parse("2026-02-01T10:15:30+01:00");
    private static final OffsetDateTime DELETED_ON = OffsetDateTime.parse("2026-03-01T10:15:30+01:00");

    @Test
    void toDto_shouldMapAllFields_whenIgnoringAudit() {
        User user = buildUser();

        UserResponseDTO dto = userMapper.toDto(user);

        assertThat(dto.getFirstname()).isEqualTo("firstname");
        assertThat(dto.getLastname()).isEqualTo("lastname");
        assertThat(dto.getGender()).isEqualTo(Gender.M.name());
        assertThat(dto.getLanguage()).isNull();
        assertThat(dto.getMobileNumber()).isEqualTo("0000");
        assertThat(dto.getTaxIdNumber()).isEqualTo("USR1");
        assertThat(dto.getEmail()).isEqualTo("user@email.com");
        assertThat(dto.getUsername()).isEqualTo("username");
        assertThat(dto.getEnabled()).isTrue();
        assertThat(dto.getAudit()).isNull();
    }

    @Test
    void toDto_shouldReturnNull_whenIgnoringAuditAndInputIsNull() {
        assertThat(userMapper.toDto(null)).isNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        UserRequestDTO dto = buildUserRequestDTO();
        User existing = buildUser();
        String originalUuid = existing.getUuid();

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
    }

    @Test
    void updateEntity_shouldReturnUnmodifiedUser_whenInputDtoIsNull() {
        User existing = buildUser();
        String originalUuid = existing.getUuid();

        User result = userMapper.updateEntity(null, existing);
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
        User user = new User();
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

    private UserRequestDTO buildUserRequestDTO() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstname("new firstname");
        dto.setLastname("new lastname");
        dto.setGender(Gender.F);
        dto.setLanguageId("lang-uuid");
        dto.setMobileNumber("9999");
        dto.setTaxIdNumber("NEW1");
        dto.setEmail("new@email.com");
        dto.setUsername("new username");
        dto.setPassword("new password");
        dto.setGroupIds(List.of("group1", "group2"));
        dto.setEnabled(false);
        return dto;
    }

}