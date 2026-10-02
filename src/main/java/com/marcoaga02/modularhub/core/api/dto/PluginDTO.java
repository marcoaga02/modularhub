package com.marcoaga02.modularhub.core.api.dto;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PluginDTO {

    private String path;

    private String name;

    private String description;

    private String icon;

}