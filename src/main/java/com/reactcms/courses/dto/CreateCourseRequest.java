package com.reactcms.courses.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateCourseRequest {

    public String authorId;
    public String featuredImageUrl;
    public String accessLevel = "public";
    public String status = "draft";

    @NotNull
    @Valid
    public CourseTranslationRequest translation;

    @Valid
    public List<MetadataItemRequest> metadata;
}
