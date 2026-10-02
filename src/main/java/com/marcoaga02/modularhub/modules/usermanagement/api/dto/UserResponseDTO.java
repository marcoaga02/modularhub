package com.marcoaga02.modularhub.modules.usermanagement.api.dto;

import com.marcoaga02.modularhub.core.api.dto.AuditDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageResponseDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class UserResponseDTO {

    private String id;

    private String firstname;

    private String lastname;

    private String gender;

    private LanguageResponseDTO language;

    private String mobileNumber;

    private String taxIdNumber;

    private String email;

    private Boolean enabled;

    private String username;

    private Set<IdentityGroupDTO> groups = new HashSet<>();

    private AuditDTO audit;

}
