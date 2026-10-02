package com.marcoaga02.modularhub.core.api.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class KeycloakUserDataDTO {

    private String username;

    private String email;

    private String firstname;

    private String lastname;

    private Boolean enabled;

    private List<String> groupIds;

    private String password;

}
