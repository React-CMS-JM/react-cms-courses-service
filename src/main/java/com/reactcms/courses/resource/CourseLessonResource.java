package com.reactcms.courses.resource;

import com.reactcms.courses.dto.CreateLessonRequest;
import com.reactcms.courses.dto.LocalizedLesson;
import com.reactcms.courses.dto.UpdateLessonRequest;
import com.reactcms.courses.service.LessonService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/api/courses/{courseId}/lessons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CourseLessonResource {

    @Inject
    LessonService lessonService;

    @GET
    @PermitAll
    public List<LocalizedLesson> list(
            @PathParam("courseId") String courseId,
            @QueryParam("lang") String lang) {
        return lessonService.listByCourse(courseId, lang);
    }

    @GET
    @Path("/{id}")
    @PermitAll
    public LocalizedLesson getById(
            @PathParam("courseId") String courseId,
            @PathParam("id") String id,
            @QueryParam("lang") String lang) {
        return lessonService.getById(courseId, id, lang);
    }

    @GET
    @Path("/by-slug/{slug}")
    @PermitAll
    public LocalizedLesson getBySlug(
            @PathParam("courseId") String courseId,
            @PathParam("slug") String slug,
            @QueryParam("lang") String lang) {
        return lessonService.getBySlug(courseId, slug, lang);
    }

    @POST
    @Authenticated
    public Response create(
            @PathParam("courseId") String courseId,
            @Valid CreateLessonRequest request) {
        LocalizedLesson created = lessonService.create(courseId, request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    public LocalizedLesson update(
            @PathParam("courseId") String courseId,
            @PathParam("id") String id,
            @Valid UpdateLessonRequest request,
            @QueryParam("lang") String lang) {
        return lessonService.update(courseId, id, request, lang);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    public Response delete(
            @PathParam("courseId") String courseId,
            @PathParam("id") String id) {
        lessonService.delete(courseId, id);
        return Response.noContent().build();
    }
}
