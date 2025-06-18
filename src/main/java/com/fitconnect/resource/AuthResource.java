package com.fitconnect.resource;

import com.fitconnect.dto.LoginRequest;
import com.fitconnect.dto.LoginResponse;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.dto.RegisterRequest;
import com.fitconnect.entity.User;
import com.fitconnect.service.AdminService;
import com.fitconnect.service.AuthService;

import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Optional;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @POST
    @Path("/register/client")
    public Response registerClient(RegisterRequest request) {
        try {
            User client = authService.registerClient(request);
            // Consider returning user info or just a success message
            return Response.status(Response.Status.CREATED).entity(client).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        Optional<LoginResponse> loginResponse = authService.login(request);
        if (loginResponse.isPresent()) {
            return Response.ok(loginResponse.get()).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email or password").build();
        }
    }

    @POST
    @Path("/register/professional")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerProfessional(@BeanParam ProfessionalRegisterRequest request) {
        try {
            authService.registerProfessional(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            // Log the exception for debugging purposes
            // Logger.getLogger(AuthResource.class).error("Professional registration failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Professional registration failed: An unexpected error occurred.").build();
        }
    }
}
