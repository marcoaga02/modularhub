package com.marcoaga02.modularhub.modules.usermanagement.api.dto;

import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.validation.OnCreate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class UserRequestDTO {

    @NotBlank
    private String firstname;

    @NotBlank
    private String lastname;

    @NotNull
    private Gender gender;

    @NotBlank
    private String languageId;

    private String mobileNumber;

    @NotBlank
    private String taxIdNumber;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String username;

    @NotBlank(groups = OnCreate.class)
    private String password;

    private List<String> groupIds;

    @NotNull
    private Boolean enabled;

}
