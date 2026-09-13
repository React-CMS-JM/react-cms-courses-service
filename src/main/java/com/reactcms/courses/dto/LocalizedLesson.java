package com.reactcms.courses.dto;

import java.time.Instant;

public class LocalizedLesson {

    public String id;
    public String courseId;
    public String parentLessonId;
    public Integer sortOrder;
    public String accessLevel;
    public Instant createdAt;
    public Instant updatedAt;

    public String languageCode;
    public String title;
    public String slug;
    public String content;
}
