package com.reactcms.courses.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_types")
public class ContentTypeEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @Column(nullable = false, length = 50, unique = true)
    public String name;

    @Column(nullable = false, length = 50, unique = true)
    public String slug;

    @Column(columnDefinition = "text")
    public String description;

    public static ContentTypeEntity findBySlug(String slug) {
        return find("slug", slug).firstResult();
    }
}
