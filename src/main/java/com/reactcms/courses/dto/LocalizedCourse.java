package com.reactcms.courses.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class LocalizedCourse {

    public String id;
    public String authorId;
    public Integer contentTypeId;
    public String featuredImageUrl;
    public String accessLevel;
    public String status;
    public Integer viewCount;
    public Instant publishedAt;
    public List<Integer> categoryIds = new ArrayList<>();
    public List<Integer> tagIds = new ArrayList<>();
    public Instant createdAt;
    public Instant updatedAt;

    public String languageCode;
    public String title;
    public String slug;
    public String content;
    public String excerpt;
    public String metaTitle;
    public String metaDescription;
}
