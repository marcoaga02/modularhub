package com.marcoaga02.modularhub.core.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdentityUserResponseDTO {

    private String id;

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private Boolean enabled;

    private List<IdentityGroupDTO> groups;

}
