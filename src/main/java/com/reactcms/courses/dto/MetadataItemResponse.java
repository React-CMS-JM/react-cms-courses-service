package com.reactcms.courses.dto;

public class MetadataItemResponse {

    public String id;
    public String postId;
    public String metaKey;
    public String metaValue;

    public MetadataItemResponse() {
    }

    public MetadataItemResponse(String id, String postId, String metaKey, String metaValue) {
        this.id = id;
        this.postId = postId;
        this.metaKey = metaKey;
        this.metaValue = metaValue;
    }
}
