package com.reactcms.courses.dto;

import jakarta.validation.Valid;

public class UpdateCourseRequest {

    public String featuredImageUrl;
    public String accessLevel;
    public String status;

    @Valid
    public CourseTranslationRequest translation;
}
