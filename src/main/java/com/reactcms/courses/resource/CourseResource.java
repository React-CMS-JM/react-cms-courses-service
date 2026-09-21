package com.reactcms.courses.resource;

import com.reactcms.courses.dto.CreateCourseRequest;
import com.reactcms.courses.dto.CourseStats;
import com.reactcms.courses.dto.LocalizedCourse;
import com.reactcms.courses.dto.MetadataItemRequest;
import com.reactcms.courses.dto.MetadataItemResponse;
import com.reactcms.courses.dto.PageResult;
import com.reactcms.courses.dto.StatusUpdateRequest;
import com.reactcms.courses.dto.UpdateCourseRequest;
import com.reactcms.courses.service.CourseService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/courses")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourseResource {

    @Inject
    CourseService courseService;

    @Inject
    SecurityIdentity identity;

    @GET
    @PermitAll
    public PageResult<LocalizedCourse> list(
            @QueryParam("status") String status,
            @QueryParam("lang") String lang,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return courseService.list(status, lang, isAuthenticated(), page, size);
    }

    @GET
    @Path("/stats")
    @Authenticated
    public CourseStats stats() {
        return courseService.stats();
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public LocalizedCourse getById(@PathParam("id") String id, @QueryParam("lang") String lang) {
        return courseService.getById(id, lang, isAuthenticated());
    }

    @GET
    @Path("/by-slug/{slug}")
    @PermitAll
    public LocalizedCourse getBySlug(@PathParam("slug") String slug, @QueryParam("lang") String lang) {
        return courseService.getBySlug(slug, lang, isAuthenticated());
    }

    @POST
    @Authenticated
    public Response create(@Valid CreateCourseRequest request) {
        String authorId = identity.isAnonymous() ? null : identity.getPrincipal().getName();
        LocalizedCourse created = courseService.create(request, authorId);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    public LocalizedCourse update(
            @PathParam("id") String id,
            @Valid UpdateCourseRequest request,
            @QueryParam("lang") String lang) {
        return courseService.update(id, request, lang);
    }

    @PATCH
    @Path("/{id}/status")
    @Authenticated
    public LocalizedCourse patchStatus(
            @PathParam("id") String id,
            @Valid StatusUpdateRequest request,
            @QueryParam("lang") String lang) {
        return courseService.patchStatus(id, request.status, lang);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    public Response delete(@PathParam("id") String id) {
        courseService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/metadata")
    @PermitAll
    public List<MetadataItemResponse> getMetadata(@PathParam("id") String id) {
        return courseService.getMetadata(id);
    }

    @PUT
    @Path("/{id}/metadata")
    @Authenticated
    public List<MetadataItemResponse> putMetadata(
            @PathParam("id") String id,
            @Valid List<MetadataItemRequest> items) {
        return courseService.replaceMetadata(id, items);
    }

    private boolean isAuthenticated() {
        return identity != null && !identity.isAnonymous();
    }
}
