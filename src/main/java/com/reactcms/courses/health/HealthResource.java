package com.reactcms.courses.health;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/courses/health")
public class HealthResource {

    @GET
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "courses-service-ok";
    }
}
