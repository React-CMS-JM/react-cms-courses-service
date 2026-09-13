package com.reactcms.courses.service;

import com.reactcms.courses.dto.LocalizedCourse;
import com.reactcms.courses.dto.LocalizedLesson;
import com.reactcms.courses.entity.CourseLessonEntity;
import com.reactcms.courses.entity.CourseLessonI18nEntity;
import com.reactcms.courses.entity.PostEntity;
import com.reactcms.courses.entity.PostI18nEntity;
import java.util.List;

public final class LocalizationHelper {

    public static final String DEFAULT_LANGUAGE = "en";

    private LocalizationHelper() {
    }

    public static <T> T pickTranslation(List<T> rows, String languageCode, java.util.function.Function<T, String> langGetter) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        T exact = rows.stream().filter(r -> languageCode.equals(langGetter.apply(r))).findFirst().orElse(null);
        if (exact != null) {
            return exact;
        }
        T fallback = rows.stream().filter(r -> DEFAULT_LANGUAGE.equals(langGetter.apply(r))).findFirst().orElse(null);
        if (fallback != null) {
            return fallback;
        }
        return rows.get(0);
    }

    public static LocalizedCourse toLocalizedCourse(PostEntity post, List<PostI18nEntity> translations, String languageCode) {
        PostI18nEntity i18n = pickTranslation(translations, languageCode, t -> t.languageCode);
        if (i18n == null) {
            return null;
        }
        LocalizedCourse dto = new LocalizedCourse();
        dto.id = post.id;
        dto.authorId = post.authorId;
        dto.contentTypeId = post.contentTypeId;
        dto.featuredImageUrl = post.featuredImageUrl;
        dto.accessLevel = post.accessLevel;
        dto.status = post.status;
        dto.viewCount = post.viewCount;
        dto.publishedAt = post.publishedAt;
        dto.createdAt = post.createdAt;
        dto.updatedAt = post.updatedAt;
        dto.languageCode = i18n.languageCode;
        dto.title = i18n.title;
        dto.slug = i18n.slug;
        dto.content = i18n.content;
        dto.excerpt = i18n.excerpt;
        dto.metaTitle = i18n.metaTitle;
        dto.metaDescription = i18n.metaDescription;
        return dto;
    }

    public static LocalizedLesson toLocalizedLesson(
            CourseLessonEntity lesson, List<CourseLessonI18nEntity> translations, String languageCode) {
        CourseLessonI18nEntity i18n = pickTranslation(translations, languageCode, t -> t.languageCode);
        if (i18n == null) {
            return null;
        }
        LocalizedLesson dto = new LocalizedLesson();
        dto.id = lesson.id;
        dto.courseId = lesson.courseId;
        dto.parentLessonId = lesson.parentLessonId;
        dto.sortOrder = lesson.sortOrder;
        dto.accessLevel = lesson.accessLevel;
        dto.createdAt = lesson.createdAt;
        dto.updatedAt = lesson.updatedAt;
        dto.languageCode = i18n.languageCode;
        dto.title = i18n.title;
        dto.slug = i18n.slug;
        dto.content = i18n.content;
        return dto;
    }

    public static String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return lang.trim().toLowerCase();
    }
}
