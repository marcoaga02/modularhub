package com.marcoaga02.modularhub.core.api.dto;

import com.marcoaga02.modularhub.core.domain.model.Gender;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class AccountDTO {

    private String identityId;

    private String email;

    private String username;

    private String firstName;

    private String lastName;

    private Set<String> roles;

    private AccountPreferencesResponseDTO preferences;

    private Gender gender;

    private String mobileNumber;

    private String taxIdNumber;

}
