package com.marcoaga02.modularhub.modules.usermanagement.domain.repository.specification;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserCriteriaDTO;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import com.marcoaga02.modularhub.modules.usermanagement.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserSpecificationTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void byCriteria_shouldReturnEmpty_whenCriteriaIsNullAndOnlyDeletedUsers() {
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(null)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenCriteriaIsNull() {
        User user = createAndSaveUser("Mario", "Rossi", "mario@rossi.com", null);
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(null)
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedMultipleUsers_whenCriteriaIsNull() {
        User firstUser = createAndSaveUser("Mario", "Rossi", "mario@rossi.com", null);
        User secondUser = createAndSaveUser("Maria", "Bianchi", "ettore@bianchi.com", null);
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(null)
        )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getUuid()).isEqualTo(secondUser.getUuid());
        assertThat(result.getLast().getUuid()).isEqualTo(firstUser.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenCriteriaHasNullText() {
        User user = createAndSaveUser("Mario", "Rossi", "mario@rossi.com", null);
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(new UserCriteriaDTO(null))
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenCriteriaHasEmptyText() {
        User user = createAndSaveUser("Mario", "Rossi", "mario@rossi.com", null);
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(new UserCriteriaDTO(""))
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenFilteringOnFirstName() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@gmail.com", null);
        createAndSaveUser("Pippo", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Mariolino", "Verdi", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("ari"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUserWithTrim_whenFilteringOnFirstName() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@gmail.com", null);
        createAndSaveUser("Pippo", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Mariolino", "Verdi", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO(" rio\t"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedMultipleUsers_whenFilteringOnFirstNameIgnoreCase() {
        User firstUser = createAndSaveUser("MARIO", "Rossi", "email1@gmail.com", null);
        User secondUser = createAndSaveUser("Maria", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("MarIolino", "Verdi", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("aRi"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getUuid()).isEqualTo(secondUser.getUuid());
        assertThat(result.getLast().getUuid()).isEqualTo(firstUser.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenFilteringOnLastName() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@gmail.com", null);
        createAndSaveUser("Maria", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Luigi", "Fossati", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("oss"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUserWithTrim_whenFilteringOnLastName() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@gmail.com", null);
        createAndSaveUser("Maria", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Luigi", "Fossati", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO(" ssi\t "))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedMultipleUsers_whenFilteringOnLastNameIgnoreCase() {
        User firstUser = createAndSaveUser("Mario", "ROSSI", "email1@gmail.com", null);
        User secondUser = createAndSaveUser("Maria", "PRoSsima", "email2@gmail.com", null);
        createAndSaveUser("Luigi", "Fossati", "email3@gmail.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("oSs"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getUuid()).isEqualTo(secondUser.getUuid());
        assertThat(result.getLast().getUuid()).isEqualTo(firstUser.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUser_whenFilteringOnEmail() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@tiscali.com", null);
        createAndSaveUser("Maria", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Luigi", "Verdi", "email3@tiscali.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("isc"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedUserWithTrim_whenFilteringOnEmail() {
        User user = createAndSaveUser("Mario", "Rossi", "email1@tiscali.com", null);
        createAndSaveUser("Maria", "Bianchi", "email2@gmail.com", null);
        createAndSaveUser("Luigi", "Verdi", "email3@tiscali.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO(" ali.com\t "))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUuid()).isEqualTo(user.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedMultipleUsers_whenFilteringOnEmailIgnoreCase() {
        User firstUser = createAndSaveUser("Mario", "Rossi", "email1@TISCALI.com", null);
        User secondUser = createAndSaveUser("Maria", "Bianchi", "email2@tIsCali.com", null);
        createAndSaveUser("Luigi", "Verdi", "email3@tiscali.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("iSc"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getUuid()).isEqualTo(secondUser.getUuid());
        assertThat(result.getLast().getUuid()).isEqualTo(firstUser.getUuid());
    }

    @Test
    void byCriteria_shouldReturnNotDeletedMultipleUsers_whenFilteringOnAllField() {
        User firstNameUser = createAndSaveUser("MARIO", "Rossi", "email1@tiscali.com", null);
        User lastNameUser = createAndSaveUser("Nicola", "MArIttimo", "email2@tiscali.com", null);
        User emailUser = createAndSaveUser("Pippo", "Bianchi", "pippo@sanitaria.com", null);
        createAndSaveUser("Luigi", "Verdi", "email3@tiscali.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                        UserSpecification.byCriteria(new UserCriteriaDTO("aRi"))
                )
                .stream()
                .sorted(Comparator.comparing(User::getFirstname, String.CASE_INSENSITIVE_ORDER))
                .toList();

        assertThat(result).hasSize(3);
        assertThat(result.getFirst().getUuid()).isEqualTo(firstNameUser.getUuid());
        assertThat(result.get(1).getUuid()).isEqualTo(lastNameUser.getUuid());
        assertThat(result.getLast().getUuid()).isEqualTo(emailUser.getUuid());
    }

    @Test
    void byCriteria_shouldReturnEmpty_whenCriteriaHasNotMatching() {
        createAndSaveUser("Mario", "Rossi", "mario@rossi.com", null);
        createAndSaveUser("Luigi", "Verdi", "luigi@verdi.com", OffsetDateTime.now());

        List<User> result = userRepository.findAll(
                UserSpecification.byCriteria(new UserCriteriaDTO("viola"))
        );

        assertThat(result).isEmpty();
    }

    private User createAndSaveUser(
            String firstname,
            String lastname,
            String email,
            OffsetDateTime deletedOn
    ) {
        User user = new User();
        user.setIdentityId(UUID.randomUUID().toString());
        user.setFirstname(firstname);
        user.setLastname(lastname);
        user.setGender(Gender.M);
        user.setMobileNumber("0000");
        user.setTaxIdNumber(firstname + lastname);
        user.setEmail(email);
        user.setUsername(firstname);
        user.setEnabled(true);
        user.setDeletedOn(deletedOn);

        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        return user;
    }

}