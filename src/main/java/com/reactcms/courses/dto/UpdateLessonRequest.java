package com.reactcms.courses.dto;

import jakarta.validation.Valid;

public class UpdateLessonRequest {

    public String parentLessonId;
    public Integer sortOrder;
    public String accessLevel;

    @Valid
    public LessonTranslationRequest translation;
}
