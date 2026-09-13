package com.reactcms.courses.resource;

import com.reactcms.courses.dto.LocalizedLesson;
import com.reactcms.courses.dto.UpdateLessonRequest;
import com.reactcms.courses.service.LessonService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/lessons")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LessonAliasResource {

    @Inject
    LessonService lessonService;

    @PUT
    @Path("/{id}")
    @Authenticated
    public LocalizedLesson update(
            @PathParam("id") String id,
            @Valid UpdateLessonRequest request,
            @QueryParam("lang") String lang) {
        return lessonService.update(id, request, lang);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    public Response delete(@PathParam("id") String id) {
        lessonService.delete(id);
        return Response.noContent().build();
    }
}
