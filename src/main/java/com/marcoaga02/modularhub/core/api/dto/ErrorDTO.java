package com.marcoaga02.modularhub.core.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorDTO {

    private LocalDateTime timestamp;

    private int status;

    private String errorCode;

    private List<String> fields;

    public ErrorDTO(LocalDateTime timestamp, int status, String errorCode) {
        this.timestamp = timestamp;
        this.status = status;
        this.errorCode = errorCode;
        this.fields = List.of();
    }

}
