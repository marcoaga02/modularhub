package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper =
            new AccountMapperImpl(new AccountPreferencesMapperImpl());

    @Test
    void toDto_shouldRenameFirstnameAndLastname() {
        User user = new User();
        user.setFirstname("Mario");
        user.setLastname("Rossi");

        AccountDTO dto = mapper.toDto(user);

        assertThat(dto)
                .extracting(AccountDTO::getFirstName, AccountDTO::getLastName)
                .containsExactly("Mario", "Rossi");
    }

    @Test
    void toDto_shouldCopyIdentityAndContactFields() {
        User user = new User();
        user.setIdentityId("identity-id");
        user.setUsername("mario.rossi");
        user.setEmail("mario@test.com");
        user.setMobileNumber("0000");
        user.setTaxIdNumber("TAX");

        AccountDTO dto = mapper.toDto(user);

        assertThat(dto)
                .extracting(AccountDTO::getIdentityId, AccountDTO::getUsername, AccountDTO::getEmail,
                        AccountDTO::getMobileNumber, AccountDTO::getTaxIdNumber)
                .containsExactly("identity-id", "mario.rossi", "mario@test.com", "0000", "TAX");
    }

    @Test
    void toDto_shouldConvertGender() {
        User user = new User();
        user.setGender(Gender.F);

        assertThat(mapper.toDto(user).getGender()).isEqualTo(GenderDTO.F);
    }

    @Test
    void toDto_shouldMapPreferencesLanguage() {
        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(Language.EN);
        User user = new User();
        user.setPreferences(preferences);

        AccountDTO dto = mapper.toDto(user);

        assertThat(dto.getPreferences().getLanguage()).isEqualTo(LanguageDTO.EN);
    }

    @Test
    void toDto_shouldLeaveRolesEmpty_becauseTheyComeFromTheToken() {
        assertThat(mapper.toDto(new User()).getRoles()).isEmpty();
    }

    @Test
    void toDto_shouldReturnNull_whenUserIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
