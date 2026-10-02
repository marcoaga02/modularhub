package com.marcoaga02.modularhub.core.api.dto;

import com.marcoaga02.modularhub.core.domain.model.Gender;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AccountProfileDTO {

    private Gender gender;

    private String mobileNumber;

    private String taxIdNumber;

}
