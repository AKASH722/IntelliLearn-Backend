package com.codex.intellilearn.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    @JsonIgnore
    private Boolean isSuccess;
    @JsonIgnore
    private Boolean hasException;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CommonErrorResponse errorResponse;

    public CommonResponse(T result) {
        this.isSuccess = true;
        this.hasException = false;
        this.result = result;
        this.errorResponse = null;
    }

    public CommonResponse(CommonErrorResponse errorResponse) {
        this.isSuccess = false;
        this.hasException = false;
        this.result = null;
        errorResponse.setTimestamp(new Date());
        this.errorResponse = errorResponse;
    }

    public CommonResponse(Exception exception) {
        this.isSuccess = false;
        this.hasException = true;
        this.result = null;
        this.errorResponse = new CommonErrorResponse(
            new Date(),
            "internal_server_error",
            "Internal server error occurred",
            "Internal server error occurred"
        );
    }
}
