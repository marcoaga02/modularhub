package com.marcoaga02.modularhub.core.api.dto;

import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AuditDTO {

    private OffsetDateTime createdOn;

    private OffsetDateTime updatedOn;

    private String createdBy;

    private String updatedBy;

}
