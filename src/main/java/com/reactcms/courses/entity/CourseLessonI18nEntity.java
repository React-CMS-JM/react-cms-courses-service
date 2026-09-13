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
@Table(name = "course_lesson_i18n")
public class CourseLessonI18nEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(name = "course_lesson_id", nullable = false, length = 64)
    public String courseLessonId;

    @Column(name = "language_code", nullable = false, length = 5)
    public String languageCode;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String slug;

    @Column(nullable = false, columnDefinition = "longtext")
    public String content;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    public static List<CourseLessonI18nEntity> findByLessonId(String courseLessonId) {
        return list("courseLessonId", courseLessonId);
    }

    public static CourseLessonI18nEntity findByLessonAndLang(String courseLessonId, String languageCode) {
        return find("courseLessonId = ?1 and languageCode = ?2", courseLessonId, languageCode).firstResult();
    }
}
