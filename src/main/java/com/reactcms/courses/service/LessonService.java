package com.reactcms.courses.service;

import com.reactcms.courses.dto.CreateLessonRequest;
import com.reactcms.courses.dto.LessonTranslationRequest;
import com.reactcms.courses.dto.LocalizedLesson;
import com.reactcms.courses.dto.UpdateLessonRequest;
import com.reactcms.courses.entity.CourseLessonEntity;
import com.reactcms.courses.entity.CourseLessonI18nEntity;
import com.reactcms.courses.exception.BadRequestException;
import com.reactcms.courses.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class LessonService {

    @Inject
    CourseService courseService;

    public List<LocalizedLesson> listByCourse(String courseId, String lang) {
        courseService.requireCoursePost(courseId);
        String language = LocalizationHelper.normalizeLang(lang);
        List<LocalizedLesson> result = new ArrayList<>();
        for (CourseLessonEntity lesson : CourseLessonEntity.findByCourseIdOrdered(courseId)) {
            LocalizedLesson localized = LocalizationHelper.toLocalizedLesson(
                    lesson, CourseLessonI18nEntity.findByLessonId(lesson.id), language);
            if (localized != null) {
                result.add(localized);
            }
        }
        return result;
    }

    public LocalizedLesson getById(String courseId, String lessonId, String lang) {
        CourseLessonEntity lesson = requireLessonInCourse(courseId, lessonId);
        LocalizedLesson localized = LocalizationHelper.toLocalizedLesson(
                lesson, CourseLessonI18nEntity.findByLessonId(lesson.id),
                LocalizationHelper.normalizeLang(lang));
        if (localized == null) {
            throw new NotFoundException("Lesson translation not found: " + lessonId);
        }
        return localized;
    }

    public LocalizedLesson getById(String lessonId, String lang) {
        CourseLessonEntity lesson = CourseLessonEntity.findById(lessonId);
        if (lesson == null) {
            throw new NotFoundException("Lesson not found: " + lessonId);
        }
        courseService.requireCoursePost(lesson.courseId);
        LocalizedLesson localized = LocalizationHelper.toLocalizedLesson(
                lesson, CourseLessonI18nEntity.findByLessonId(lesson.id),
                LocalizationHelper.normalizeLang(lang));
        if (localized == null) {
            throw new NotFoundException("Lesson translation not found: " + lessonId);
        }
        return localized;
    }

    public LocalizedLesson getBySlug(String courseId, String slug, String lang) {
        courseService.requireCoursePost(courseId);
        String language = LocalizationHelper.normalizeLang(lang);
        List<CourseLessonEntity> lessons = CourseLessonEntity.findByCourseIdOrdered(courseId);

        for (CourseLessonEntity lesson : lessons) {
            LocalizedLesson localized = LocalizationHelper.toLocalizedLesson(
                    lesson, CourseLessonI18nEntity.findByLessonId(lesson.id), language);
            if (localized != null && slug.equals(localized.slug)) {
                return localized;
            }
        }

        for (CourseLessonEntity lesson : lessons) {
            List<CourseLessonI18nEntity> translations = CourseLessonI18nEntity.findByLessonId(lesson.id);
            boolean slugMatch = translations.stream().anyMatch(t -> slug.equals(t.slug));
            if (slugMatch) {
                LocalizedLesson localized = LocalizationHelper.toLocalizedLesson(lesson, translations, language);
                if (localized != null) {
                    return localized;
                }
            }
        }

        throw new NotFoundException("Lesson not found for slug: " + slug);
    }

    @Transactional
    public LocalizedLesson create(String courseId, CreateLessonRequest request) {
        courseService.requireCoursePost(courseId);
        Instant now = Instant.now();

        if (request.parentLessonId != null && !request.parentLessonId.isBlank()) {
            requireLessonInCourse(courseId, request.parentLessonId);
        }

        CourseLessonEntity lesson = new CourseLessonEntity();
        lesson.id = UUID.randomUUID().toString();
        lesson.courseId = courseId;
        lesson.parentLessonId = blankToNull(request.parentLessonId);
        lesson.sortOrder = request.sortOrder != null ? request.sortOrder : 0;
        lesson.accessLevel = request.accessLevel != null ? request.accessLevel : "premium";
        lesson.createdAt = now;
        lesson.updatedAt = now;
        lesson.persist();

        upsertTranslation(lesson.id, request.translation, now, true);

        return LocalizationHelper.toLocalizedLesson(
                lesson, CourseLessonI18nEntity.findByLessonId(lesson.id), request.translation.languageCode);
    }

    @Transactional
    public LocalizedLesson update(String courseId, String lessonId, UpdateLessonRequest request, String lang) {
        CourseLessonEntity lesson = requireLessonInCourse(courseId, lessonId);
        return applyUpdate(lesson, request, lang);
    }

    @Transactional
    public LocalizedLesson update(String lessonId, UpdateLessonRequest request, String lang) {
        CourseLessonEntity lesson = CourseLessonEntity.findById(lessonId);
        if (lesson == null) {
            throw new NotFoundException("Lesson not found: " + lessonId);
        }
        courseService.requireCoursePost(lesson.courseId);
        return applyUpdate(lesson, request, lang);
    }

    @Transactional
    public void delete(String courseId, String lessonId) {
        CourseLessonEntity lesson = requireLessonInCourse(courseId, lessonId);
        deleteCascade(lesson);
    }

    @Transactional
    public void delete(String lessonId) {
        CourseLessonEntity lesson = CourseLessonEntity.findById(lessonId);
        if (lesson == null) {
            throw new NotFoundException("Lesson not found: " + lessonId);
        }
        courseService.requireCoursePost(lesson.courseId);
        deleteCascade(lesson);
    }

    private LocalizedLesson applyUpdate(CourseLessonEntity lesson, UpdateLessonRequest request, String lang) {
        Instant now = Instant.now();

        if (request.parentLessonId != null) {
            String parentId = blankToNull(request.parentLessonId);
            if (parentId != null) {
                if (parentId.equals(lesson.id)) {
                    throw new BadRequestException("Lesson cannot be its own parent");
                }
                requireLessonInCourse(lesson.courseId, parentId);
            }
            lesson.parentLessonId = parentId;
        }
        if (request.sortOrder != null) {
            lesson.sortOrder = request.sortOrder;
        }
        if (request.accessLevel != null) {
            lesson.accessLevel = request.accessLevel;
        }
        lesson.updatedAt = now;

        String responseLang = LocalizationHelper.normalizeLang(lang);
        if (request.translation != null) {
            upsertTranslation(lesson.id, request.translation, now, false);
            responseLang = LocalizationHelper.normalizeLang(request.translation.languageCode);
        }

        return LocalizationHelper.toLocalizedLesson(
                lesson, CourseLessonI18nEntity.findByLessonId(lesson.id), responseLang);
    }

    private void deleteCascade(CourseLessonEntity lesson) {
        Set<String> removeIds = new HashSet<>();
        collectDescendants(lesson.courseId, lesson.id, removeIds);
        removeIds.add(lesson.id);

        for (String id : removeIds) {
            CourseLessonI18nEntity.delete("courseLessonId", id);
            CourseLessonEntity entity = CourseLessonEntity.findById(id);
            if (entity != null) {
                entity.delete();
            }
        }
    }

    private void collectDescendants(String courseId, String parentId, Set<String> acc) {
        List<CourseLessonEntity> children = CourseLessonEntity.list(
                "courseId = ?1 and parentLessonId = ?2", courseId, parentId);
        for (CourseLessonEntity child : children) {
            if (acc.add(child.id)) {
                collectDescendants(courseId, child.id, acc);
            }
        }
    }

    private CourseLessonEntity requireLessonInCourse(String courseId, String lessonId) {
        courseService.requireCoursePost(courseId);
        CourseLessonEntity lesson = CourseLessonEntity.findById(lessonId);
        if (lesson == null || !courseId.equals(lesson.courseId)) {
            throw new NotFoundException("Lesson not found: " + lessonId);
        }
        return lesson;
    }

    private void upsertTranslation(
            String lessonId, LessonTranslationRequest translation, Instant now, boolean required) {
        if (translation == null) {
            if (required) {
                throw new BadRequestException("translation is required");
            }
            return;
        }
        String languageCode = LocalizationHelper.normalizeLang(translation.languageCode);
        CourseLessonI18nEntity existing = CourseLessonI18nEntity.findByLessonAndLang(lessonId, languageCode);
        if (existing == null) {
            CourseLessonI18nEntity i18n = new CourseLessonI18nEntity();
            i18n.courseLessonId = lessonId;
            i18n.languageCode = languageCode;
            i18n.title = translation.title;
            i18n.slug = translation.slug;
            i18n.content = translation.content;
            i18n.createdAt = now;
            i18n.updatedAt = now;
            i18n.persist();
        } else {
            if (translation.title != null) {
                existing.title = translation.title;
            }
            if (translation.slug != null) {
                existing.slug = translation.slug;
            }
            if (translation.content != null) {
                existing.content = translation.content;
            }
            existing.updatedAt = now;
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
