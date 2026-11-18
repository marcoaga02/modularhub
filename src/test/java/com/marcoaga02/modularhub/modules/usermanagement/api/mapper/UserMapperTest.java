package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCreateRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserUpdateRequestDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private static final OffsetDateTime CREATED_ON = OffsetDateTime.parse("2026-01-01T10:15:30+01:00");
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.parse("2026-02-01T10:15:30+01:00");

    private final UserMapper mapper = new UserMapperImpl();

    @Nested
    class ToDto {

        @Test
        void toDto_shouldExposeUuidAsId() {
            User user = new User();

            assertThat(mapper.toDto(user).getId()).isEqualTo(user.getUuid());
        }

        @Test
        void toDto_shouldCopyPersonalAndAccountFields() {
            User user = new User();
            user.setFirstname("Mario");
            user.setLastname("Rossi");
            user.setMobileNumber("0000");
            user.setTaxIdNumber("TAX");
            user.setEmail("mario@test.com");
            user.setUsername("mario");
            user.setEnabled(true);

            UserResponseDTO dto = mapper.toDto(user);

            assertThat(dto)
                    .extracting(UserResponseDTO::getFirstname, UserResponseDTO::getLastname,
                            UserResponseDTO::getMobileNumber, UserResponseDTO::getTaxIdNumber,
                            UserResponseDTO::getEmail, UserResponseDTO::getUsername, UserResponseDTO::getEnabled)
                    .containsExactly("Mario", "Rossi", "0000", "TAX", "mario@test.com", "mario", true);
        }

        @Test
        void toDto_shouldConvertGender() {
            User user = new User();
            user.setGender(Gender.M);

            assertThat(mapper.toDto(user).getGender()).isEqualTo(GenderDTO.M);
        }

        @Test
        void toDto_shouldTakeLanguageFromPreferences() {
            AccountPreferences preferences = new AccountPreferences();
            preferences.setLanguage(Language.IT);
            User user = new User();
            user.setPreferences(preferences);

            assertThat(mapper.toDto(user).getLanguage()).isEqualTo(LanguageDTO.IT);
        }

        @Test
        void toDto_shouldLeaveLanguageNull_whenUserHasNoPreferences() {
            assertThat(mapper.toDto(new User()).getLanguage()).isNull();
        }

        @Test
        void toDto_shouldLeaveGroupsAndAuditUnset_becauseTheServiceFillsThem() {
            UserResponseDTO dto = mapper.toDto(new User());

            assertThat(dto.getGroups()).isEmpty();
            assertThat(dto.getAudit()).isNull();
        }

        @Test
        void toDto_shouldReturnNull_whenUserIsNull() {
            assertThat(mapper.toDto(null)).isNull();
        }
    }

    @Nested
    class UpdateEntityFromCreateRequest {

        @Test
        void updateEntity_shouldCopyEditableFields_whenCreateRequestIsGiven() {
            User user = new User();

            mapper.updateEntity(createRequest(), user);

            assertEditableFieldsCopied(user);
        }

        @Test
        void updateEntity_shouldReturnTheSameInstance_whenCreateRequestIsGiven() {
            User user = new User();

            assertThat(mapper.updateEntity(createRequest(), user)).isSameAs(user);
        }

        @Test
        void updateEntity_shouldNotSetIdentityId_whenCreateRequestIsGiven() {
            User user = new User();

            mapper.updateEntity(createRequest(), user);

            assertThat(user.getIdentityId()).isNull();
        }

        @Test
        void updateEntity_shouldCreatePreferencesWithLanguage_whenUserHasNoPreferences() {
            User user = new User();

            mapper.updateEntity(createRequest(), user);

            assertThat(user.getPreferences().getLanguage()).isEqualTo(Language.IT);
        }

        @Test
        void updateEntity_shouldLeaveUserUnchanged_whenCreateRequestIsNull() {
            User user = new User();
            user.setFirstname("Mario");

            User result = mapper.updateEntity((UserCreateRequestDTO) null, user);

            assertThat(result).isSameAs(user);
            assertThat(user.getFirstname()).isEqualTo("Mario");
        }
    }

    @Nested
    class UpdateEntityFromUpdateRequest {

        @Test
        void updateEntity_shouldCopyEditableFields_whenUpdateRequestIsGiven() {
            User user = new User();

            mapper.updateEntity(updateRequest(), user);

            assertEditableFieldsCopied(user);
        }

        @Test
        void updateEntity_shouldReturnTheSameInstance_whenUpdateRequestIsGiven() {
            User user = new User();

            assertThat(mapper.updateEntity(updateRequest(), user)).isSameAs(user);
        }

        @Test
        void updateEntity_shouldKeepUuidIdentityIdAndPreferences_whenUpdateRequestIsGiven() {
            AccountPreferences preferences = new AccountPreferences();
            User user = new User();
            user.setIdentityId("identity-id");
            user.setPreferences(preferences);
            String uuid = user.getUuid();

            mapper.updateEntity(updateRequest(), user);

            assertThat(user.getUuid()).isEqualTo(uuid);
            assertThat(user.getIdentityId()).isEqualTo("identity-id");
            assertThat(user.getPreferences()).isSameAs(preferences);
        }

        @Test
        void updateEntity_shouldSetLanguageIntoExistingPreferences_whenUpdateRequestIsGiven() {
            AccountPreferences preferences = new AccountPreferences();
            preferences.setLanguage(Language.EN);
            User user = new User();
            user.setPreferences(preferences);

            mapper.updateEntity(updateRequest(), user);

            assertThat(preferences.getLanguage()).isEqualTo(Language.IT);
        }

        @Test
        void updateEntity_shouldKeepAuditFields_whenUpdateRequestIsGiven() {
            User user = new User();
            user.setCreatedOn(CREATED_ON);
            user.setCreatedBy("creator");
            user.setUpdatedOn(UPDATED_ON);
            user.setUpdatedBy("updater");

            mapper.updateEntity(updateRequest(), user);

            assertThat(user)
                    .extracting(User::getCreatedOn, User::getCreatedBy, User::getUpdatedOn, User::getUpdatedBy)
                    .containsExactly(CREATED_ON, "creator", UPDATED_ON, "updater");
        }

        @Test
        void updateEntity_shouldLeaveUserUnchanged_whenUpdateRequestIsNull() {
            User user = new User();
            user.setFirstname("Mario");

            User result = mapper.updateEntity((UserUpdateRequestDTO) null, user);

            assertThat(result).isSameAs(user);
            assertThat(user.getFirstname()).isEqualTo("Mario");
        }
    }

    private static void assertEditableFieldsCopied(User user) {
        assertThat(user)
                .extracting(User::getFirstname, User::getLastname, User::getGender, User::getMobileNumber,
                        User::getTaxIdNumber, User::getEmail, User::getUsername, User::getEnabled)
                .containsExactly("Mario", "Rossi", Gender.M, "0000", "TAX", "mario@test.com", "mario", true);
    }

    private static UserCreateRequestDTO createRequest() {
        return new UserCreateRequestDTO()
                .firstname("Mario")
                .lastname("Rossi")
                .gender(GenderDTO.M)
                .language(LanguageDTO.IT)
                .mobileNumber("0000")
                .taxIdNumber("TAX")
                .email("mario@test.com")
                .username("mario")
                .password("password")
                .groupIds(List.of("group-id"))
                .enabled(true);
    }

    private static UserUpdateRequestDTO updateRequest() {
        return new UserUpdateRequestDTO()
                .firstname("Mario")
                .lastname("Rossi")
                .gender(GenderDTO.M)
                .language(LanguageDTO.IT)
                .mobileNumber("0000")
                .taxIdNumber("TAX")
                .email("mario@test.com")
                .username("mario")
                .groupIds(List.of("group-id"))
                .enabled(true);
    }
}
