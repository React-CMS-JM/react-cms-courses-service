package com.reactcms.courses.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "post_metadata")
public class PostMetadataEntity extends PanacheEntityBase {

    @Id
    @Column(length = 64)
    public String id;

    @Column(name = "post_id", nullable = false, length = 64)
    public String postId;

    @Column(name = "meta_key", nullable = false, length = 100)
    public String metaKey;

    @Column(name = "meta_value", columnDefinition = "text")
    public String metaValue;

    public static List<PostMetadataEntity> findByPostId(String postId) {
        return list("postId", postId);
    }

    public static void deleteByPostId(String postId) {
        delete("postId", postId);
    }
}
