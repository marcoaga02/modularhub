package com.marcoaga02.modularhub.core.api.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AccountPreferencesCreateDTO {

    private String identityId;

    private String languageId;

}
