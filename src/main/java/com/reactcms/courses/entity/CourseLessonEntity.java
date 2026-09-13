package com.reactcms.courses.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "course_lessons")
public class CourseLessonEntity extends PanacheEntityBase {

    @Id
    @Column(length = 64)
    public String id;

    @Column(name = "course_id", nullable = false, length = 64)
    public String courseId;

    @Column(name = "parent_lesson_id", length = 64)
    public String parentLessonId;

    @Column(name = "sort_order", nullable = false)
    public Integer sortOrder = 0;

    @Column(name = "access_level", length = 50)
    public String accessLevel = "premium";

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    public static List<CourseLessonEntity> findByCourseIdOrdered(String courseId) {
        return list("courseId = ?1 order by sortOrder asc", courseId);
    }
}
