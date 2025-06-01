package com.fitconnect.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response; // Added
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.inject.Inject;
import java.util.Set; // Added

@Path("/api/protected")
@Produces(MediaType.APPLICATION_JSON)
public class ProtectedResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/user")
    @RolesAllowed({"CLIENT", "PROFESSIONAL", "ADMIN"})
    public Response getUserInfo(@Context SecurityContext ctx) {
        if (ctx.getUserPrincipal() == null || jwt == null || jwt.getName() == null) {
            // If JWT is not present or valid, SmallRye JWT filter usually returns 401 before this.
            // This is an additional check.
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated or JWT missing.").build();
        }
        String nameFromCtx = ctx.getUserPrincipal().getName();
        String upnFromJwt = jwt.getName(); // UPN (email)
        String userIdFromSub = jwt.getSubject(); // User ID from 'sub' claim
        Set<String> groups = jwt.getGroups();

        return Response.ok(String.format("Hello %s (from SecurityContext), your UPN from JWT is %s. Your UserID is %s. Your roles are: %s.", nameFromCtx, upnFromJwt, userIdFromSub, groups)).build();
    }

    @GET
    @Path("/admin")
    @RolesAllowed("ADMIN")
    public Response adminResource() {
         if (jwt == null || jwt.getName() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not authenticated or JWT missing.").build();
        }
        return Response.ok("This is an ADMIN only resource. User: " + jwt.getName()).build();
    }
}
