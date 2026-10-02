package com.marcoaga02.modularhub.modules.usermanagement.api.mapper;

import com.marcoaga02.modularhub.core.api.dto.AccountProfileDTO;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.modules.usermanagement.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import(AccountProfileMapperImpl.class)
class AccountProfileMapperTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private AccountProfileMapper accountProfileMapper;

    @Test
    void toAccountProfileDTO_shouldMapAllFields() {
        User user = new User();
        user.setGender(Gender.M);
        user.setMobileNumber("3701234567");
        user.setTaxIdNumber("GTNMRC02H13D403T");

        AccountProfileDTO dto = accountProfileMapper.toAccountProfileDTO(user);

        assertThat(dto.getGender()).isEqualTo(Gender.M);
        assertThat(dto.getMobileNumber()).isEqualTo("3701234567");
        assertThat(dto.getTaxIdNumber()).isEqualTo("GTNMRC02H13D403T");
    }

    @Test
    void toAccountProfileDTO_shouldReturnNull_whenInputIsNull() {
        assertThat(accountProfileMapper.toAccountProfileDTO(null)).isNull();
    }

    @Test
    void toAccountProfileDTO_shouldMapNullFields_whenUserFieldsAreNull() {
        User user = new User();

        AccountProfileDTO dto = accountProfileMapper.toAccountProfileDTO(user);

        assertThat(dto.getGender()).isNull();
        assertThat(dto.getMobileNumber()).isNull();
        assertThat(dto.getTaxIdNumber()).isNull();
    }
}