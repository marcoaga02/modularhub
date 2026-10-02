package com.marcoaga02.modularhub.modules.usermanagement.domain.repository;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User enabledUser1, deletedUser;

    @BeforeEach
    void setUp() {
        enabledUser1 = createUser(
                "identity1",
                "firstname1",
                "lastname1",
                Gender.M,
                "0000",
                "USR_1",
                "enabled@email.com",
                "username1",
                false,
                null
        );

        deletedUser = createUser(
                "identity4",
                "firstname4",
                "lastname4",
                Gender.F,
                "3333",
                "USR_4",
                "deleted@email.com",
                "username4",
                true,
                OffsetDateTime.now()
        );

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByUuidAndDeletedOnIsNull_shouldReturnUser_whenIsPresent() {
        Optional<User> result = userRepository.findByUuidAndDeletedOnIsNull(enabledUser1.getUuid());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(enabledUser1.getUuid());
        assertThat(result.get().getDeletedOn()).isNull();
    }

    @Test
    void findByUuidAndDeletedOnIsNull_shouldReturnEmpty_whenInvalidUuid() {
        Optional<User> result = userRepository.findByUuidAndDeletedOnIsNull("invalid-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUuidAndDeletedOnIsNull_shouldReturnEmpty_whenDeletedOnIsNotNull() {
        Optional<User> result = userRepository.findByUuidAndDeletedOnIsNull(deletedUser.getUuid());

        assertThat(result).isEmpty();
    }

    @Test
    void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnUser_whenIsPresent() {
        Optional<User> result = userRepository.findByTaxIdNumberAndDeletedOnIsNull(enabledUser1.getTaxIdNumber());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(enabledUser1.getUuid());
        assertThat(result.get().getDeletedOn()).isNull();
    }

    @Test
    void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnEmpty_whenInvalidTaxIdNumber() {
        Optional<User> result = userRepository.findByTaxIdNumberAndDeletedOnIsNull("invalid-uuid");

        assertThat(result).isEmpty();
    }

    @Test
    void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnEmpty_whenDeletedOnIsNotNull() {
        Optional<User> result = userRepository.findByTaxIdNumberAndDeletedOnIsNull(deletedUser.getTaxIdNumber());

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameAndDeletedOnIsNull_shouldReturnUser_whenIsPresent() {
        Optional<User> result = userRepository.findByUsernameAndDeletedOnIsNull(enabledUser1.getUsername());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(enabledUser1.getUuid());
        assertThat(result.get().getDeletedOn()).isNull();
    }

    @Test
    void findByUsernameAndDeletedOnIsNull_shouldReturnEmpty_whenInvalidUsername() {
        Optional<User> result = userRepository.findByUsernameAndDeletedOnIsNull("invalid-username");

        assertThat(result).isEmpty();
    }

    @Test
    void findByUsernameAndDeletedOnIsNull_shouldReturnEmpty_whenDeletedOnIsNotNull() {
        Optional<User> result = userRepository.findByUsernameAndDeletedOnIsNull(deletedUser.getUsername());

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailAndDeletedOnIsNull_shouldReturnUser_whenIsPresent() {
        Optional<User> result = userRepository.findByEmailAndDeletedOnIsNull(enabledUser1.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(enabledUser1.getUuid());
        assertThat(result.get().getDeletedOn()).isNull();
    }

    @Test
    void findByEmailAndDeletedOnIsNull_shouldReturnEmpty_whenInvalidEmail() {
        Optional<User> result = userRepository.findByEmailAndDeletedOnIsNull("invalid-email");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmailAndDeletedOnIsNull_shouldReturnEmpty_whenDeletedOnIsNotNull() {
        Optional<User> result = userRepository.findByEmailAndDeletedOnIsNull(deletedUser.getEmail());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdentity_shouldReturnUser_whenIsPresent() {
        Optional<User> result = userRepository.findByIdentityId(enabledUser1.getIdentityId());

        assertThat(result).isPresent();
        assertThat(result.get().getUuid()).isEqualTo(enabledUser1.getUuid());
        assertThat(result.get().getIdentityId()).isEqualTo(enabledUser1.getIdentityId());
    }

    @Test
    void findByIdentity_shouldReturnEmpty_whenInvalidUuid() {
        Optional<User> result = userRepository.findByIdentityId("invalid-identity-id");

        assertThat(result).isEmpty();
    }

    private User createUser(
            String identityId,
            String firstname,
            String lastname,
            Gender gender,
            String mobileNumber,
            String taxIdNumber,
            String email,
            String username,
            Boolean enabled,
            OffsetDateTime deletedOn) {
        User user = new User();
        user.setIdentityId(identityId);
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setGender(gender);
        user.setMobileNumber(mobileNumber);
        user.setTaxIdNumber(taxIdNumber);
        user.setEmail(email);
        user.setUsername(username);
        user.setEnabled(enabled);
        user.setDeletedOn(deletedOn);

        userRepository.save(user);

        return user;
    }
}