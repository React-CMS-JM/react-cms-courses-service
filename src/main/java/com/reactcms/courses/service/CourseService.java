package com.reactcms.courses.service;

import com.reactcms.courses.dto.CreateCourseRequest;
import com.reactcms.courses.dto.CourseTranslationRequest;
import com.reactcms.courses.dto.LocalizedCourse;
import com.reactcms.courses.dto.MetadataItemRequest;
import com.reactcms.courses.dto.MetadataItemResponse;
import com.reactcms.courses.dto.UpdateCourseRequest;
import com.reactcms.courses.entity.ContentTypeEntity;
import com.reactcms.courses.entity.PostEntity;
import com.reactcms.courses.entity.PostI18nEntity;
import com.reactcms.courses.entity.PostMetadataEntity;
import com.reactcms.courses.exception.BadRequestException;
import com.reactcms.courses.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CourseService {

    public static final String COURSE_SLUG = "course";

    public Integer requireCourseContentTypeId() {
        ContentTypeEntity type = ContentTypeEntity.findBySlug(COURSE_SLUG);
        if (type == null) {
            throw new BadRequestException("Content type 'course' is not configured");
        }
        return type.id;
    }

    public PostEntity requireCoursePost(String id) {
        Integer courseTypeId = requireCourseContentTypeId();
        PostEntity post = PostEntity.findById(id);
        if (post == null || !courseTypeId.equals(post.contentTypeId)) {
            throw new NotFoundException("Course not found: " + id);
        }
        return post;
    }

    public List<LocalizedCourse> list(String status, String lang, boolean includeNonPublished) {
        String language = LocalizationHelper.normalizeLang(lang);
        Integer courseTypeId = requireCourseContentTypeId();

        List<PostEntity> posts;
        if (status != null && !status.isBlank()) {
            posts = PostEntity.list("contentTypeId = ?1 and status = ?2 order by createdAt desc",
                    courseTypeId, status.trim());
        } else if (includeNonPublished) {
            posts = PostEntity.list("contentTypeId = ?1 order by createdAt desc", courseTypeId);
        } else {
            posts = PostEntity.list("contentTypeId = ?1 and status = ?2 order by createdAt desc",
                    courseTypeId, "published");
        }

        List<LocalizedCourse> result = new ArrayList<>();
        for (PostEntity post : posts) {
            LocalizedCourse localized = LocalizationHelper.toLocalizedCourse(
                    post, PostI18nEntity.findByPostId(post.id), language);
            if (localized != null) {
                result.add(localized);
            }
        }
        return result;
    }

    public LocalizedCourse getById(String id, String lang, boolean includeNonPublished) {
        PostEntity post = requireCoursePost(id);
        if (!includeNonPublished && !"published".equals(post.status)) {
            throw new NotFoundException("Course not found: " + id);
        }
        LocalizedCourse localized = LocalizationHelper.toLocalizedCourse(
                post, PostI18nEntity.findByPostId(post.id), LocalizationHelper.normalizeLang(lang));
        if (localized == null) {
            throw new NotFoundException("Course translation not found: " + id);
        }
        return localized;
    }

    public LocalizedCourse getBySlug(String slug, String lang, boolean includeNonPublished) {
        String language = LocalizationHelper.normalizeLang(lang);
        Integer courseTypeId = requireCourseContentTypeId();

        PostI18nEntity i18n = PostI18nEntity.findBySlugAndLang(slug, language);
        if (i18n == null) {
            i18n = PostI18nEntity.findBySlug(slug);
        }
        if (i18n == null) {
            throw new NotFoundException("Course not found for slug: " + slug);
        }

        PostEntity post = PostEntity.findById(i18n.postId);
        if (post == null || !courseTypeId.equals(post.contentTypeId)) {
            throw new NotFoundException("Course not found for slug: " + slug);
        }
        if (!includeNonPublished && !"published".equals(post.status)) {
            throw new NotFoundException("Course not found for slug: " + slug);
        }

        LocalizedCourse localized = LocalizationHelper.toLocalizedCourse(
                post, PostI18nEntity.findByPostId(post.id), language);
        if (localized == null) {
            throw new NotFoundException("Course translation not found for slug: " + slug);
        }
        return localized;
    }

    @Transactional
    public LocalizedCourse create(CreateCourseRequest request, String fallbackAuthorId) {
        Integer courseTypeId = requireCourseContentTypeId();
        Instant now = Instant.now();

        String authorId = request.authorId;
        if (authorId == null || authorId.isBlank()) {
            authorId = fallbackAuthorId;
        }
        if (authorId == null || authorId.isBlank()) {
            throw new BadRequestException("authorId is required");
        }

        String status = request.status != null ? request.status : "draft";
        PostEntity post = new PostEntity();
        post.id = UUID.randomUUID().toString();
        post.authorId = authorId;
        post.contentTypeId = courseTypeId;
        post.featuredImageUrl = request.featuredImageUrl;
        post.accessLevel = request.accessLevel != null ? request.accessLevel : "public";
        post.status = status;
        post.viewCount = 0;
        post.publishedAt = "published".equals(status) ? now : null;
        post.createdAt = now;
        post.updatedAt = now;
        post.persist();

        upsertTranslation(post.id, request.translation, now, true);

        if (request.metadata != null && !request.metadata.isEmpty()) {
            replaceMetadata(post.id, request.metadata);
        }

        return LocalizationHelper.toLocalizedCourse(
                post, PostI18nEntity.findByPostId(post.id), request.translation.languageCode);
    }

    @Transactional
    public LocalizedCourse update(String id, UpdateCourseRequest request, String lang) {
        PostEntity post = requireCoursePost(id);
        Instant now = Instant.now();

        if (request.featuredImageUrl != null) {
            post.featuredImageUrl = request.featuredImageUrl;
        }
        if (request.accessLevel != null) {
            post.accessLevel = request.accessLevel;
        }
        if (request.status != null) {
            applyStatus(post, request.status, now);
        }
        post.updatedAt = now;

        String responseLang = LocalizationHelper.normalizeLang(lang);
        if (request.translation != null) {
            upsertTranslation(post.id, request.translation, now, false);
            responseLang = LocalizationHelper.normalizeLang(request.translation.languageCode);
        }

        return LocalizationHelper.toLocalizedCourse(post, PostI18nEntity.findByPostId(post.id), responseLang);
    }

    @Transactional
    public LocalizedCourse patchStatus(String id, String status, String lang) {
        PostEntity post = requireCoursePost(id);
        Instant now = Instant.now();
        applyStatus(post, status, now);
        post.updatedAt = now;
        return LocalizationHelper.toLocalizedCourse(
                post, PostI18nEntity.findByPostId(post.id), LocalizationHelper.normalizeLang(lang));
    }

    @Transactional
    public void delete(String id) {
        PostEntity post = requireCoursePost(id);
        post.delete();
    }

    public List<MetadataItemResponse> getMetadata(String id) {
        requireCoursePost(id);
        return PostMetadataEntity.findByPostId(id).stream()
                .map(m -> new MetadataItemResponse(m.id, m.postId, m.metaKey, m.metaValue))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MetadataItemResponse> replaceMetadata(String id, List<MetadataItemRequest> items) {
        requireCoursePost(id);
        PostMetadataEntity.deleteByPostId(id);
        if (items != null) {
            for (MetadataItemRequest item : items) {
                if (item.metaKey == null || item.metaKey.isBlank()) {
                    continue;
                }
                PostMetadataEntity meta = new PostMetadataEntity();
                meta.id = UUID.randomUUID().toString();
                meta.postId = id;
                meta.metaKey = item.metaKey.trim();
                meta.metaValue = item.metaValue;
                meta.persist();
            }
        }
        return getMetadata(id);
    }

    private void applyStatus(PostEntity post, String status, Instant now) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("status is required");
        }
        boolean becomingPublished = "published".equals(status) && !"published".equals(post.status);
        post.status = status;
        if (becomingPublished && post.publishedAt == null) {
            post.publishedAt = now;
        }
    }

    private void upsertTranslation(String postId, CourseTranslationRequest translation, Instant now, boolean required) {
        if (translation == null) {
            if (required) {
                throw new BadRequestException("translation is required");
            }
            return;
        }
        String languageCode = LocalizationHelper.normalizeLang(translation.languageCode);
        PostI18nEntity existing = PostI18nEntity.findByPostAndLang(postId, languageCode);
        if (existing == null) {
            PostI18nEntity i18n = new PostI18nEntity();
            i18n.postId = postId;
            i18n.languageCode = languageCode;
            i18n.title = translation.title;
            i18n.slug = translation.slug;
            i18n.content = translation.content;
            i18n.excerpt = translation.excerpt;
            i18n.metaTitle = translation.metaTitle;
            i18n.metaDescription = translation.metaDescription;
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
            if (translation.excerpt != null) {
                existing.excerpt = translation.excerpt;
            }
            if (translation.metaTitle != null) {
                existing.metaTitle = translation.metaTitle;
            }
            if (translation.metaDescription != null) {
                existing.metaDescription = translation.metaDescription;
            }
            existing.updatedAt = now;
        }
    }
}
