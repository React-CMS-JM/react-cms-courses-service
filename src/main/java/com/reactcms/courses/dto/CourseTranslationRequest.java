package com.reactcms.courses.dto;

import jakarta.validation.constraints.NotBlank;

public class CourseTranslationRequest {

    @NotBlank
    public String languageCode;

    @NotBlank
    public String title;

    @NotBlank
    public String slug;

    @NotBlank
    public String content;

    public String excerpt;
    public String metaTitle;
    public String metaDescription;
}
