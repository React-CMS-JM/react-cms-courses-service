package com.reactcms.courses.dto;

import jakarta.validation.constraints.NotBlank;

public class LessonTranslationRequest {

    @NotBlank
    public String languageCode;

    @NotBlank
    public String title;

    @NotBlank
    public String slug;

    @NotBlank
    public String content;
}
