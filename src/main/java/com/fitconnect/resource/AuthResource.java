package com.fitconnect.resource;

import com.fitconnect.dto.LoginRequest;
import com.fitconnect.dto.LoginResponse;
import com.fitconnect.dto.ProfessionalRegisterRequest;
import com.fitconnect.dto.RegisterRequest;
import com.fitconnect.entity.User;
import com.fitconnect.service.AdminService;
import com.fitconnect.service.AuthService;
import com.fitconnect.dto.LoginResponse; // Assuming LoginResponse can be reused or adapted
import com.fitconnect.entity.User;

import jakarta.annotation.security.PermitAll; // For old endpoints if kept temporarily
import jakarta.annotation.security.RolesAllowed; // Or @Authenticated for new endpoint
import jakarta.ws.rs.Authenticated; // Recommended for new endpoint
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET; // Changed to GET for initialize-user, or POST if client sends data
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context; // For SecurityContext
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext; // For SecurityContext
import org.jboss.logging.Logger;


import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    AuthService authService;

    /*
    @POST
    @Path("/register/client")
    @PermitAll // If old registration is kept, ensure it's accessible or remove
    public Response registerClient(RegisterRequest request) {
        try {
            User client = authService.registerClient(request);
            return Response.status(Response.Status.CREATED).entity(client).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Client registration failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Registration failed: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    @PermitAll // If old login is kept, ensure it's accessible or remove
    public Response login(LoginRequest request) {
        try {
            Optional<LoginResponse> loginResponse = authService.login(request);
            if (loginResponse.isPresent()) {
                return Response.ok(loginResponse.get()).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid email or password").build();
            }
        } catch (Exception e) {
            LOG.error("Login failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Login failed: " + e.getMessage()).build();
        }
    }

    @POST
    @Path("/register/professional")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @PermitAll // If old registration is kept, ensure it's accessible or remove
    public Response registerProfessional(@BeanParam ProfessionalRegisterRequest request) {
        try {
            authService.registerProfessional(request);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.error("Professional registration failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Professional registration failed: An unexpected error occurred.").build();
        }
    }
    */

    // New endpoint for initializing user after Firebase client-side authentication
    @POST // Or GET, depending on if client needs to send any initial data beyond token
    @Path("/initialize-user")
    @Authenticated // Ensures FirebaseAuthenticationMechanism runs
    public Response initializeUser(@Context SecurityContext securityContext) {
        String uid = securityContext.getUserPrincipal().getName();
        String email = (String) securityContext.getAuthenticationScheme(); // This is not correct for email. Will get from identity.
                                                                        // Email should be an attribute in SecurityIdentity

        if (uid == null || uid.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("User ID (UID) not found in token.").build();
        }

        // Correct way to get attributes from QuarkusSecurityIdentity
        io.quarkus.security.identity.SecurityIdentity identity = (io.quarkus.security.identity.SecurityIdentity) securityContext.getUserPrincipal();
        email = identity.getAttribute("email");


        try {
            LoginResponse response = authService.initializeUser(uid, email);
            return Response.ok(response).build();
        } catch (ExecutionException | InterruptedException e) {
            LOG.errorf(e, "Error during user initialization for UID: %s", uid);
            Thread.currentThread().interrupt();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error initializing user data.").build();
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error during user initialization for UID: %s", uid);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Unexpected error initializing user.").build();
        }
    }
}
