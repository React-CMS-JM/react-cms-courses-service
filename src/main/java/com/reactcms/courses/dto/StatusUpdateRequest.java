package com.reactcms.courses.dto;

import jakarta.validation.constraints.NotBlank;

public class StatusUpdateRequest {

    @NotBlank
    public String status;
}
