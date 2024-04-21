package com.codex.intellilearn.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CommonErrorResponse {
    private Date timestamp;
    private String code;
    private String message;
    private String details;

}
