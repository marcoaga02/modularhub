package com.marcoaga02.modularhub.core.api.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class LanguageResponseDTO {

    private String id;

    private String code;

    private String label;

    private Boolean isDefault;

}
