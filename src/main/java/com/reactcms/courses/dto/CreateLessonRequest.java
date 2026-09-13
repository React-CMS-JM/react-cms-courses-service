package com.reactcms.courses.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CreateLessonRequest {

    public String parentLessonId;
    public Integer sortOrder = 0;
    public String accessLevel = "premium";

    @NotNull
    @Valid
    public LessonTranslationRequest translation;
}
