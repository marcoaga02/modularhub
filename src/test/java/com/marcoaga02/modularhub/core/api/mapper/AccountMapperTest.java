package com.marcoaga02.modularhub.core.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.GenderDTO;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapperImpl(
            new AccountPreferencesMapperImpl(Mappers.getMapper(LanguageMapper.class))
    );

    @Test
    void toDto_shouldMapAllFields() {
        Language language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);

        AccountPreferences preferences = new AccountPreferences();
        preferences.setLanguage(language);

        User user = new User();
        user.setIdentityId("identity-id");
        user.setUsername("mario.rossi");
        user.setEmail("mario@test.com");
        user.setFirstname("Mario");
        user.setLastname("Rossi");
        user.setGender(Gender.M);
        user.setMobileNumber("0000");
        user.setTaxIdNumber("TAX");
        user.setPreferences(preferences);

        AccountDTO dto = mapper.toDto(user);

        assertThat(dto.getIdentityId()).isEqualTo("identity-id");
        assertThat(dto.getUsername()).isEqualTo("mario.rossi");
        assertThat(dto.getEmail()).isEqualTo("mario@test.com");
        assertThat(dto.getFirstName()).isEqualTo("Mario");
        assertThat(dto.getLastName()).isEqualTo("Rossi");
        assertThat(dto.getGender()).isEqualTo(GenderDTO.M);
        assertThat(dto.getMobileNumber()).isEqualTo("0000");
        assertThat(dto.getTaxIdNumber()).isEqualTo("TAX");
        assertThat(dto.getRoles()).isEmpty();
        assertThat(dto.getPreferences().getLanguage().getId()).isEqualTo(language.getUuid());
        assertThat(dto.getPreferences().getLanguage().getCode()).isEqualTo("it-IT");
    }

    @Test
    void toDto_shouldReturnNull_whenInputIsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
