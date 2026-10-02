package com.marcoaga02.modularhub.core.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class IdentityUserUpdateRequestDTO {

    private String username;

    private String email;

    private String firstName;

    private String lastName;

    private Boolean enabled;

    private List<String> groupIds;

}
