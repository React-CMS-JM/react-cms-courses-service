package com.reactcms.courses.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "post_i18n")
public class PostI18nEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "post_id", nullable = false, length = 64)
    public String postId;

    @Column(name = "language_code", nullable = false, length = 5)
    public String languageCode;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String slug;

    @Column(nullable = false, columnDefinition = "longtext")
    public String content;

    @Column(columnDefinition = "text")
    public String excerpt;

    @Column(name = "meta_title")
    public String metaTitle;

    @Column(name = "meta_description", columnDefinition = "text")
    public String metaDescription;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    public static List<PostI18nEntity> findByPostId(String postId) {
        return list("postId", postId);
    }

    public static PostI18nEntity findByPostAndLang(String postId, String languageCode) {
        return find("postId = ?1 and languageCode = ?2", postId, languageCode).firstResult();
    }

    public static PostI18nEntity findBySlugAndLang(String slug, String languageCode) {
        return find("slug = ?1 and languageCode = ?2", slug, languageCode).firstResult();
    }

    public static PostI18nEntity findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }
}
