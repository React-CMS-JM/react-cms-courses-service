package com.reactcms.courses.dto;

import jakarta.validation.constraints.NotBlank;

public class MetadataItemRequest {

    @NotBlank
    public String metaKey;

    public String metaValue;
}
